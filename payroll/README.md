# Payroll Agent Module (A2A-connected to Shrija)

## What's in here

```
payroll/
├── pom.xml
├── src/main/java/com/shrija/payroll/
│   ├── model/Employee.java            # employee data shape
│   ├── model/Payslip.java             # computed payslip shape
│   ├── data/MockEmployeeRepository.java  # in-memory data (swap later for real HRM DB)
│   ├── tools/PayrollTools.java        # calculateSalary(), generatePayslip()
│   ├── agent/PayrollAgent.java        # the LlmAgent that owns those tools
│   └── a2a/PayrollAgentExecutorProducer.java  # exposes the agent over A2A (Quarkus)
├── src/main/resources/application.properties # port config (9091)
└── shrija-side-snippet/ShrijaPayrollIntegration.java  # goes in adk-shrija, NOT here
```

## Overall flow

1. **User talks to Shrija.** Shrija is the root `LlmAgent` in `adk-shrija`.
2. **Shrija's LLM decides** whether a request is payroll-related (per its instruction).
3. If yes, Shrija delegates to its `payroll_agent` sub-agent — which isn't running
   in-process, it's a **remote agent reached over A2A** (JSON-RPC over HTTP).
4. That request lands on the **Payroll A2A server** (this module), which:
   - Resolves it through `A2aAgentExecutor` → the actual `PayrollAgent` (`LlmAgent`)
   - The Payroll agent's own LLM decides which tool to call: `calculateSalary` or `generatePayslip`
   - The tool reads from `MockEmployeeRepository` (in-memory for now) and returns a result
5. The result flows back over A2A to Shrija, which relays it to the user.

```
User → Shrija (adk-shrija, root agent)
           │  decides: "this is payroll" → delegate
           ▼  A2A / JSON-RPC over HTTP (localhost:9091)
    Payroll A2A Server (this module, Quarkus)
           │
           ▼
    PayrollAgent (LlmAgent)
           │  picks a tool
           ▼
    PayrollTools.calculateSalary / generatePayslip
           │
           ▼
    EmployeeRepository (interface)
           │
           ▼
    MySqlEmployeeRepository ──▶ MySQL: employees JOIN salary_structure
    (or MockEmployeeRepository, if PAYROLL_DATA_SOURCE=mock)
```

## Why A2A instead of a plain tool call

Since you chose a2a, Payroll runs as its **own independent process/service** with
its own agent card (`/.well-known/agent-card.json`), rather than being compiled
directly into Shrija. That means:
- You can build, test, and redeploy payroll independently of Shrija.
- Later, payroll could even run on a different machine/team's infra.
- Payroll can have its own multi-step reasoning (e.g. explaining a deduction),
  not just a single function call.

The tradeoff: it's a network hop, and both services need to be running for the
integration to work end-to-end.

## Where the data comes from (your MySQL DB)

Your `employee_db.employees` table holds identity/login/role data — no salary
figures. Payroll data lives in a **new table, `salary_structure`**, joined
back to `employees` on `employee_id`:

```sql
SELECT e.employee_id, e.employee_name, e.designation,
       s.base_salary, s.hra, s.special_allowance
FROM employees e
JOIN salary_structure s ON e.employee_id = s.employee_id
WHERE e.active = 1;
```

Run `sql/01_create_salary_structure.sql` in MySQL Workbench (against
`employee_db`) — it creates the table, seeds it with rows for your 4 existing
employees, and ends with that same join as a sanity check so you can see it
work before Java touches it.

Statutory deductions (PF 12%, professional tax flat ₹200) are **not** stored
per-employee in the DB — they're the same rule for everyone, so they live as
constants in `MySqlEmployeeRepository`. Only move them into a table if they
ever need to vary (e.g. PF caps, state-specific PT slabs).

`EmployeeRepository` is the interface `PayrollTools` actually depends on.
`MockEmployeeRepository` and `MySqlEmployeeRepository` both implement it —
switching between them is one env var, `PAYROLL_DATA_SOURCE`, not a code change.

## Setup & run steps

1. **Run the SQL script** in MySQL Workbench against `employee_db`:
   `sql/01_create_salary_structure.sql`. Check the final `SELECT` at the
   bottom returns joined rows before moving on.

2. **Drop this `payroll/` folder** in as a sibling module to `core`, `contrib`,
   `a2a`, `adk-shrija` (you've already created the empty folder — just fill it
   with these files).

3. **Register it as a module** in the parent `java-adk001/pom.xml` reactor:
   ```xml
   <modules>
     ...
     <module>payroll</module>
   </modules>
   ```

4. **Check versions**: `adk.version` in `payroll/pom.xml` must match whatever
   version `core`/`contrib` are pinned to in the parent POM.

5. **Set environment variables** (so your DB password never gets hardcoded
   into a file):
   ```bash
   # Windows PowerShell
   $env:PAYROLL_DATA_SOURCE = "mysql"
   $env:PAYROLL_DB_URL      = "jdbc:mysql://localhost:3306/employee_db"
   $env:PAYROLL_DB_USER     = "root"
   $env:PAYROLL_DB_PASSWORD = "your-mysql-password"
   ```
   Leave `PAYROLL_DATA_SOURCE` unset (or "mock") to keep using in-memory data
   while you're still testing the agent/tool logic itself.

6. **Cross-check the A2A exposure boilerplate**: `PayrollAgentExecutorProducer.java`
   mirrors the shape of `contrib/samples/a2a_server` in your adk-java checkout —
   open that sample side by side, since exact producer signatures shift between
   adk-java releases (this is Pre-GA software).

7. **Build it** first, to catch any dependency/version mismatches early:
   ```bash
   cd java-adk001
   ./mvnw -f payroll/pom.xml clean compile
   ```

8. **Run the payroll server** (same terminal, env vars from step 5 still set):
   ```bash
   ./mvnw -f payroll/pom.xml quarkus:dev
   ```
   (or however `a2a_server` is normally launched in your setup — check its README
   for the exact command, since this depends on your adk-java version)

9. **Test it standalone first**, before touching Shrija at all — this is the
   step that answers "where is the data coming from" for real:
   - Open `http://localhost:9091/.well-known/agent-card.json` in a browser —
     confirms the server is up and the agent card is being served.
   - Use the `adk web` dev UI or the `a2a_basic` client sample (in your
     adk-java `contrib` checkout) to send it a message like
     *"calculate salary for EMP001 for 2026-07"* and confirm it returns real
     numbers from your DB, not the mock ones.

10. **Only once step 9 works**, wire Shrija to it: drop
    `shrija-side-snippet/ShrijaPayrollIntegration.java` into `adk-shrija`, fix
    the package name, and call `buildShrijaRootAgentWithPayroll()` wherever
    Shrija's root agent is currently built. Then run Shrija and ask it a
    payroll question directly.

## What's not built yet (next iterations)

- Attendance/leave-based deductions
- Real HRM data source integration
- Auth between Shrija and Payroll (A2A supports authenticated agent cards —
  worth adding before this touches real salary data)
