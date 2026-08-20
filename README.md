# Shrija AI - Login + LLM Orchestration Implementation

## Services

1. auth-service: http://localhost:8081
2. adk-shrija-v2: http://localhost:8080
3. hrms-mcp-server: http://localhost:8082/mcp

## Shared secret

Set the same JWT_SECRET in auth-service and adk-shrija-v2.

## Flow

React UI -> POST /api/auth/login -> JWT(userId, role, employeeCode) ->
POST /api/v1/chat with Authorization Bearer JWT -> JWT validation ->
Manager/Orchestration Agent -> Gemini semantic routing ->
Employee/Attendance/Payroll/HR Agent -> filtered MCP toolset.

## Demo users

prachi@gmail.com / password123 -> EMPLOYEE / EMP1001
rahul@gmail.com / password123 -> MANAGER / EMP1002
hradmin@gmail.com / password123 -> HR / EMP1003
admin@gmail.com / password123 -> ADMIN / EMP1004

## Important

The current implementation uses the hrms-mcp-server tool names from the uploaded project.
The MCP endpoint is /mcp.

Payroll and attendance are now real sub-agents in the orchestrator. The old stub PayrollAgentFactory is replaced.
HR is protected by a deterministic role guard (HR and ADMIN only).

For production, replace the development JWT secret and H2 database with environment-backed secrets and a persistent DB.
