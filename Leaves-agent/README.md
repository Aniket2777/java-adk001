# Enterprise ADK demo (Java)

A small multi-agent system built with Google's Agent Development Kit (ADK),
Java edition. One root agent authenticates a user and routes their request
to the correct specialist sub-agent.

## Architecture

```
ManagerAgent (root)
 ├─ authenticate() tool           -> EmployeeDao -> MySQL
 ├─ employee_agent (sub-agent)
 │    └─ getEmployee() tool        -> EmployeeDao -> MySQL
 ├─ leave_agent (sub-agent)
 │    └─ getLeaveBalance() tool    -> LeaveDao -> MySQL
 └─ payroll_agent (sub-agent)
      └─ getPayslip() tool         -> PayrollDao -> MySQL  (read-only)
```

`ManagerAgent` is wired to its sub-agents with `.subAgents(...)` in
`ManagerAgent.java`. ADK's `LlmAgent` reads each sub-agent's `description`
field at runtime and decides, per user turn, whether to answer itself or
hand off ("transfer") to one of them. This is the actual mechanism the
`instruction` prompt text is describing — the prompt alone does nothing
without the code-level wiring.

## Package layout

| Package    | Responsibility                                          |
|------------|----------------------------------------------------------|
| `entity`   | Plain data classes (no ORM)                              |
| `config`   | Environment-based DB configuration                        |
| `dao`      | JDBC queries, one class per table/domain                  |
| `tools`    | Functions exposed to the LLM via `@Schema` annotations     |
| `agent`    | `LlmAgent` definitions — instructions, model, tools, sub-agents |
| `runner`   | Local CLI entry point                                     |

## Setup

1. **Database**: create a MySQL instance and run `schema.sql` against it —
   it seeds a test employee (`E1001` / `password123`) with plaintext password
   storage (see security note below).
2. **Environment**: copy `.env.example` to `.env` and fill in `DB_URL`,
   `DB_USER`, `DB_PASSWORD`, and a free `GOOGLE_API_KEY` from
   [Google AI Studio](https://aistudio.google.com/app/apikey).
3. **Run**:
   ```
   mvn compile exec:java
   ```
   or, for the browser dev UI (shows every tool call the model makes):
   ```
   mvn compile exec:java -Dexec.mainClass="com.google.adk.web.AdkWebServer" -Dexec.args="--adk.agents.source-dir=target --server.port=8000"
   ```

## ⚠️ Security note: plaintext passwords

At the user's explicit request, this project stores and compares passwords
as plaintext (`employees.password`), not hashed. This is fine for local
learning/testing only. Do not carry this pattern into anything that holds
real user data — anyone with read access to the database (a backup, a
leaked dump, a query log) gets every password directly, and people reuse
passwords across systems. If you want to restore hashing later, reintroduce
a `PasswordUtil` wrapping BCrypt (e.g. `org.mindrot:jbcrypt`) and compare
with `BCrypt.checkpw(...)` instead of `String.equals(...)`.

## What's different from a typical first-draft ADK project

These are the specific gaps this project fixes, based on a common pattern
seen in early tutorial-style ADK projects:

- **Sub-agents are actually wired** via `.subAgents(...)`, not just described
  in an instruction prompt with no code-level connection.
- **DB credentials come from the environment**, not hardcoded in source.
- **No duplicated authentication logic** — auth happens once, at the
  `ManagerAgent` level; sub-agents assume the user is already authenticated.
- **Exceptions are logged**, not silently swallowed into a generic `null`
  return that looks identical to "not found."

## Extending this

`payroll_agent` is a working example of the pattern below — follow the same
four steps to add a fourth domain:
1. Add an entity + DAO for the new table (see `Payslip` / `PayrollDao`).
2. Add a `Tool` class exposing the operations you want the LLM to call
   (see `PayrollTool` — notice it only exposes a getter; there's no way
   for the LLM to reach a write operation because none exists).
3. Add a new `LlmAgent` (see `PayrollAgent`) with a clear `description`
   and an `instruction` that scopes what it will and won't do.
4. Add it to `ManagerAgent`'s `.subAgents(...)` list and mention it in the
   routing instructions.
