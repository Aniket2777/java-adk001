# HRMS Multi-Agent System — Java ADK + MCP

One **Orchestrator (Manager) Agent** fronting five domain sub-agents
(Employee, HR, Leave, Payroll, Attendance), all backed by **one shared MCP
server** and **one MySQL database** — no per-agent MCP servers.

```
User
  |
  v
Orchestrator Agent (Java ADK)
  |
  +---------+---------+---------+---------+
  v         v         v         v         v
Employee   HR      Leave    Payroll   Attendance
  |         |         |         |         |
  +---------+---------+---------+---------+
              |
        MCP Client (Java ADK, shared McpToolset)
              |
      hrms-mcp-server (Spring Boot, ONE SSE endpoint)
              |
          MySQL (hrms_db)
```

## Modules

| Module | What it is |
|---|---|
| `mcp-server` | Spring Boot + Spring AI MCP server. Exposes tools for all 5 domains over one SSE endpoint (`/mcp/message` on port `8081`), backed by MySQL via JPA. |
| `orchestrator-agent` | Java ADK app. One root `LlmAgent` (the manager) with 5 sub-agents, all sharing a single `McpToolset` pointed at `mcp-server`. |

## Responsibility table (from the project brief)

| Agent | Owns | Calls other agents |
|---|---|---|
| Employee | Employee profile, employee queries | Leave, Attendance, Payroll |
| Leave | Leave requests and balances | Manager, HR, Payroll |
| Attendance | Attendance and working hours | Payroll |
| Payroll | Salary generation | Attendance, Leave |
| HR | Employee lifecycle | Payroll, Budget |
| Manager | Team approvals | Leave, Attendance |
| Budget | Department budgets | Payroll, HR |
| CTO | Executive dashboards | All agents |

This build covers **Employee, Leave, Attendance, Payroll, HR** under the
Orchestrator, matching the architecture diagram. Manager/Budget/CTO are
noted for context but are out of scope here (Leave's manager-approval tool
and HR's exit-triggers-payroll flow are wired in as the concrete
cross-agent calls this scope requires).

"Calls other agents" in practice, since everything shares one MCP server:
cross-domain calls (e.g. Payroll pulling Attendance/Leave data, HR
triggering Payroll on exit) are made **in-process** between the relevant
`@Component` tool classes inside `mcp-server`, rather than as separate
MCP round-trips. The sub-agents in `orchestrator-agent` still map 1:1 to
the domains in the diagram.

## Domain models (`mcp-server/.../domain`)

- `Employee` — profile, department, designation, status
- `LeaveRequest` / `LeaveBalance` — leave applications, approvals, balances
- `Attendance` — check-in/out, hours worked, daily status
- `SalarySlip` — monthly payroll, unpaid-leave deductions
- `EmployeeLifecycleEvent` — onboarding, promotion, transfer, exit

## MCP tools by domain (`mcp-server/.../tools`)

- **EmployeeTools**: `getEmployeeProfile`, `findEmployeeByEmail`, `createEmployee`, `updateEmployeeDetails`, `listEmployees`
- **LeaveTools**: `applyForLeave`, `decideOnLeaveRequest`, `getLeaveRequests`, `getLeaveBalances`, `getUnpaidLeaveDaysForMonth`
- **AttendanceTools**: `checkIn`, `checkOut`, `markStatus`, `getAttendanceForRange`, `getMonthlyAttendanceSummary`
- **PayrollTools**: `generateSalarySlip` (pulls attendance + unpaid leave automatically), `markSalarySlipAsPaid`, `getSalarySlip`
- **HrTools**: `recordOnboarding`, `recordPromotion`, `recordTransfer`, `recordExit` (triggers final payroll settlement), `getLifecycleHistory`

All five are registered as one `ToolCallbackProvider` in `McpToolConfig`, so
they're exposed as a single tool set on the one MCP server.

## Running it

1. **Start MySQL** and create credentials matching `mcp-server/src/main/resources/application.yml`
   (or override `DB_USERNAME` / `DB_PASSWORD` env vars). `reference/schema.sql`
   is there for manual review — Hibernate (`ddl-auto: update`) will create
   the tables automatically on first run.

2. **Run the MCP server**
   ```bash
   cd mcp-server
   mvn spring-boot:run
   ```
   It comes up on `http://localhost:8081`, SSE endpoint at `/mcp/message`.

3. **Run the orchestrator**
   ```bash
   cd orchestrator-agent
   mvn compile exec:java -Dexec.mainClass=com.hrms.orchestrator.OrchestratorApplication
   ```
   (or build the shaded jar with `mvn package` and `java -jar target/hrms-orchestrator-agent.jar`)

   It connects to the MCP server as a client and drops you into an
   interactive prompt — type requests like:
   - "Onboard a new employee, John Doe, john@company.com, Engineering, joining 2026-08-15"
   - "Apply for 3 days of casual leave for employee 1 starting 2026-09-01"
   - "Generate August payroll for employee 1"

## Notes / next steps for the team

- `google-adk` / `google-adk-dev` version and Spring AI MCP artifact
  coordinates in the POMs are placeholders — confirm exact versions against
  your team's `java-adk001` fork before building.
- `LlmAgent`/`McpToolset`/`InMemoryRunner` API calls in
  `OrchestratorApplication` follow the standard ADK Java patterns, but
  double check builder method names against the SDK version you land on.
- Swap `gemini-2.0-flash` for whatever model your team is standardizing on.
- Budget/Manager/CTO agents aren't built here — if your team picks those up,
  they'd plug into the same shared MCP server the same way.
