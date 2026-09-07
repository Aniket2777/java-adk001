# Attendance Agent

Attendance Agent is a Spring Boot 4 + Java 21 microservice using Google ADK for reasoning, MCP for attendance data access, and A2A for communication with Employee, Payroll, and Manager agents.

## Ports

- Attendance Agent: `8084`
- Attendance MCP Server: `8083`
- MCP endpoint: `http://localhost:8083/mcp`

## Data boundary

The Attendance Agent contains no JPA entity, repository, SQL, datasource, or MySQL dependency. All attendance persistence and attendance calculations are owned by `attendance-mcp-server`.

## Required configuration

```yaml
spring:
  application:
    name: attendance-agent

server:
  port: 8084

shrija:
  ai:
    gemini-api-key: ${GOOGLE_API_KEY}
    gemini-model: gemini-3.1-flash-lite
    mcp-server-url: http://localhost:8083/mcp
    employee-agent-url: http://<employee-a2a-host>:<port>
    payroll-agent-url: http://<payroll-a2a-host>:<port>
    manager-agent-url: http://<manager-a2a-host>:<port>
```

MCP database configuration belongs only to `attendance-mcp-server`:

```text
ATTENDANCE_DB_URL=jdbc:mysql://localhost:3306/attendance_hrm_db
ATTENDANCE_DB_USERNAME=root
ATTENDANCE_DB_PASSWORD=root
```

Attendance policy defaults:

- standard start: `09:30`
- standard end: `18:00`
- full day: `480` minutes

Override them with `ATTENDANCE_STANDARD_START`, `ATTENDANCE_STANDARD_END`, and `ATTENDANCE_FULL_DAY_MINUTES`.

## Run order

1. Create the MySQL database `attendance_hrm_db`.
2. Start `attendance-mcp-server` on port `8083`.
3. Start the Employee, Payroll, and Manager agents with A2A endpoints and agent cards.
4. Start `attendance-agent` on port `8084`.
5. Call `POST http://localhost:8084/api/v1/attendance/chat`.

The MCP server creates/updates the `attendance_record` table using JPA `ddl-auto=update`.

## Example chat request

```http
POST http://localhost:8084/api/v1/attendance/chat
Content-Type: application/json

{
  "userId": "E001",
  "message": "I am employee E001 with EMPLOYEE role. Check me in at 2026-08-10T09:20:00."
}
```

Other examples:

```json
{
  "userId": "E001",
  "message": "I am E001 with EMPLOYEE role. Show my attendance for August 2026."
}
```

```json
{
  "userId": "M001",
  "message": "I am M001 with MANAGER role. Show today's team attendance."
}
```

```json
{
  "userId": "E001",
  "message": "I am E001 with EMPLOYEE role. Send my August 2026 attendance summary to Payroll."
}
```

## A2A behavior

- Employee Agent is called before employee-specific attendance operations.
- Payroll Agent receives a confirmed attendance summary when requested.
- Manager Agent receives confirmed team attendance when requested.
- A2A failures return an unavailable-service error; the Attendance Agent does not fabricate a response.

## Verification

The implementation was statically checked for Maven XML validity and for TODO/placeholder implementations in the new modules. Maven compilation could not be executed in the supplied execution environment because Maven is not installed and the repository wrapper attempted to download Maven from the internet.
