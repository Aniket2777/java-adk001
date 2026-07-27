# Shrija AI — `adk-shrija` module

Multi-agent enterprise assistant built on Google ADK (Java) + Spring Boot, as a module of the `google-adk-parent` reactor.

## ⚠️ Architecture change: all DB access now goes through MCP

This module no longer touches a database directly. The domain layer (HR/leave/document/lifecycle
models, repositories, services) moved to a new sibling module, **`adk-shrija-domain`**, and is now
exposed exclusively through a new **`mcp-shrija-server`** module — a real MCP server (Spring AI's
`spring-ai-starter-mcp-server-webmvc`, `@Tool`-annotated methods, `MethodToolCallbackProvider`).

`adk-shrija` reaches it through two **filtered** `McpToolset` client beans (`McpToolsetConfig`):
- `hrMcpToolset` — employee directory + leave approval + document fulfillment tool names only
- `employeeMcpToolset` — leave application/balance + document request + lifecycle status tool names only

**Why filtered, not one shared toolset:** the MCP server exposes all 16 tools from one endpoint.
Without a client-side allowlist, the Employee Agent would gain `approveLeaveRequest` and every
other HR-only action just by being handed the same toolset object — silently undoing the actor
separation `EmployeeSelfServiceService`/`LeaveApprovalService` were built to enforce. The
allowlists live in `McpToolsetConfig` now instead of "which Java class has a reference to which
service."

Old direct-call tool classes (`HrTools`, `EmployeeTools`, `HrLeaveApprovalTools`,
`HrDocumentFulfillmentTools`) are deleted, not deprecated — they're fully superseded by the MCP
toolsets. JPA/Postgres dependencies removed from this module's `pom.xml` entirely.

**Run order now matters:** `mcp-shrija-server` (which needs Postgres) must be up before
`adk-shrija` starts, since `HrAgentFactory`/`EmployeeAgentFactory` build their `McpToolset`
connections eagerly.

## Verified against your actual ADK source (previous draft was mostly right, one real bug fixed)

You uploaded the real `core`/`dev`/`a2a` source and the root `pom.xml`. Here's what changed:

| Item | Verdict |
|---|---|
| `com.google.adk:google-adk` as the core artifact coordinate | **Correct as originally guessed** — confirmed via `contrib/samples/helloworld/pom.xml` |
| `new Gemini(modelName, apiKey)` | **Correct as originally guessed** — real constructor in `models/Gemini.java` |
| `LlmAgent.builder().name().instruction().model().subAgents()` | **Correct as originally guessed** — all real methods on `LlmAgent.Builder` / `BaseAgent.Builder` |
| `new InMemoryRunner(agent)` | **Correct as originally guessed** |
| Parent POM | **Fixed** — now inherits `com.google.adk:google-adk-parent:1.6.1-SNAPSHOT` (you'd already added `adk-shrija` to its `<modules>`), instead of standing alone on `spring-boot-starter-parent`. Spring Boot version now correctly follows the parent's pinned `4.0.2`, not an assumed `4.1.0`. |
| `ConversationService` — running a turn | **Real bug fixed.** `Runner.run(...)` returning a plain `String` doesn't exist; the actual API is `runner.runAsync(userId, sessionId, Content, RunConfig)` returning `Flowable<Event>` (RxJava3). Text comes from `event.stringifyContent()` on each collected event. |
| `ConversationService` — session lifecycle | **Real bug fixed.** The previous draft built a new `InMemoryRunner` (and therefore a brand-new, empty `InMemorySessionService`) on every single request — the `sessionId` returned to a caller would never resolve on the next call, silently breaking multi-turn conversations. The Runner is now built once, in the constructor, and reused. Session creation is now explicit (`sessionService().getSession(...)` then `createSession(...)` if absent), mirroring ADK's own `HelloWorldRun` sample, rather than relying on `RunConfig.autoCreateSession()` — which defaults to `false` and would have thrown `IllegalArgumentException: Session not found` on every first message. |

## Structure

```
com.shrija.ai
├── ShrijaAiApplication.java
├── config/              ShrijaAiProperties, GeminiModelConfig
├── model/                HrEmployee, LeaveType, DocumentType, EmployeeLeaveBalance,
│                         LeaveRequest, EmployeeLifecycleTask, DocumentRequest
├── repository/            HrEmployeeRepository, EmployeeLeaveBalanceRepository,
│                         LeaveRequestRepository, EmployeeLifecycleTaskRepository,
│                         DocumentRequestRepository
├── mapper/                HrEmployeeMapper, EmployeeSelfServiceMapper
├── tools/                 HrTools, EmployeeTools — real ADK FunctionTool-wrapped functions
├── prompts/               HrAgentPrompts, EmployeeAgentPrompts
├── util/                  empty — populate when an agent needs a shared helper
├── agent/
│   ├── AgentFactory.java
│   ├── manager/ManagerAgentFactory.java     Real orchestration logic
│   ├── hr/HrAgentFactory.java                REAL
│   ├── employee/EmployeeAgentFactory.java    REAL
│   ├── payroll/ budget/ settlement/ report/ notification/
│                                              Still stubs
├── service/              ConversationService, HrEmployeeService, EmployeeSelfServiceService
├── controller/ChatController.java            POST /api/v1/chat
├── dto/                   ChatRequest, ChatResponse, HrEmployeeDto, LeaveBalanceDto,
│                         LeaveRequestDto, LifecycleTaskDto, DocumentRequestDto
└── exception/             ShrijaAiException, ResourceNotFoundException,
                          BusinessRuleViolationException, HrEmployeeNotFoundException,
                          InvalidLeaveRequestException, AgentExecutionException,
                          GlobalExceptionHandler, ErrorResponse
```

## Employee Agent — self-service, distinct from HR's record lookup

Scope decision: HR Agent owns the employee *directory* (record lookup); Employee Agent owns
employee *self-service* — leave, onboarding/offboarding status, and document requests. Same
Manager Agent auto-discovery as HR: no changes needed to `ManagerAgentFactory`.

Five tools:
- `checkLeaveBalance(employeeCode, leaveType)`
- `applyForLeave(employeeCode, leaveType, startDate, endDate)` — validates date range, rejects
  past start dates, checks remaining balance, creates a `PENDING` `LeaveRequest`, decrements
  the balance. Approval itself is out of scope (no approver workflow yet).
- `checkOnboardingOffboardingStatus(employeeCode)` — reads `EmployeeLifecycleTask` rows;
  nothing creates these tasks yet (that's a separate onboarding/offboarding *trigger* workflow,
  not built here) — the agent can only report status once something else populates them.
- `requestDocument(employeeCode, documentType)` / `checkDocumentRequestStatus(employeeCode)`

**Why `LeaveType`/`DocumentType` are real enum tool parameters but dates are strings:**
checked `FunctionCallingUtils.java` directly — enums map natively to a `STRING` schema with an
`enum` constraint, but `LocalDate` isn't special-cased and would fall into the generic POJO
branch, which throws if Jackson can't serialize the type (no `JavaTimeModule` registered here).
Dates are ISO-8601 strings (`yyyy-MM-dd`) at the tool boundary, parsed to real `LocalDate`
before hitting the entity.

**Not yet built**: nothing creates `EmployeeLeaveBalance` or `EmployeeLifecycleTask` rows — both
need to be seeded (by an HR onboarding process, in a later pass) before the agent has anything
real to report on. No leave-approval workflow. No REST endpoints outside the agent path.

`EmployeeSelfServiceServiceTest` covers the leave validation rules (date order, past dates,
insufficient balance, successful decrement) with mocked repositories.



## Still worth double-checking on your end

I have the `core`/`dev`/`a2a` source now, but not `core/pom.xml` or `dev/pom.xml` themselves (they weren't in the zip), so:
- Confirm `1.6.1-SNAPSHOT` is still the current `<version>` in the root `pom.xml` at the time you build (it was, as of your last upload).
- If you're using `dev`'s web/dashboard features (the `dev/adk/web` package I saw in the zip has its own controller/service/dto structure) alongside this Spring Boot app, decide whether that's a separate process or something to integrate with — not addressed here since it wasn't asked for.

## Running it

Build the whole reactor first - `adk-shrija` depends on the `core` module (`google-adk`), which
needs to be installed to your local Maven repo:

```bash
cd java-adk001
mvn -pl adk-shrija -am clean install
```

Then set the required environment variables and run:

```bash
export GEMINI_API_KEY=your-key
export DB_URL=jdbc:postgresql://localhost:5432/shrija_ai
export DB_USERNAME=shrija_app
export DB_PASSWORD=your-db-password
export JWT_SECRET=a-long-random-secret

mvn -pl adk-shrija spring-boot:run
```

Check it's up:
```bash
curl http://localhost:8080/actuator/health
```

**Seed data**: `data.sql` now loads automatically on every startup (3 employees, leave balances
for two of them, and a few onboarding tasks for a third) - idempotent via `WHERE NOT EXISTS`
guards, so restarts don't duplicate rows. This is local/demo data only; gate it behind a profile
or delete it before any real deployment. Requires `spring.jpa.defer-datasource-initialization`
and `spring.sql.init.mode=always` (both already set in `application.yml`) so it runs *after*
Hibernate creates the schema, not before.

Try it end to end:
```bash
curl -X POST http://localhost:8080/api/v1/chat \
  -H "Content-Type: application/json" \
  -d '{"userId":"u1","message":"Find employee EMP1024"}'

curl -X POST http://localhost:8080/api/v1/chat \
  -H "Content-Type: application/json" \
  -d '{"userId":"u1","message":"Apply for 2 days annual leave for EMP1024 from 2026-08-03 to 2026-08-04"}'

curl -X POST http://localhost:8080/api/v1/chat \
  -H "Content-Type: application/json" \
  -d '{"userId":"hr1","message":"Show me pending leave requests"}'
```
Use the same `sessionId` returned in a response's body on your next call to continue that
specific conversation - omit it (or leave it blank) to start a new one.

**Not wired in yet**: JWT authentication - the `/api/v1/chat` endpoint is open right now despite
`spring-security`/`jjwt` being on the classpath; no filter chain exists to enforce anything.


## Leave approval workflow (Employee → HR)

Leave requests are created `PENDING` by the Employee Agent and can only be resolved by the
HR Agent — an employee has no tool that can approve or reject their own request.

- `HrLeaveApprovalTools.listPendingLeaveRequests()` — global pending queue, oldest first
- `HrLeaveApprovalTools.approveLeaveRequest(requestId)` — no balance change (it was already
  reserved at application time)
- `HrLeaveApprovalTools.rejectLeaveRequest(requestId, reason)` — **refunds** the reserved days
  back to the employee's balance, and stores the reason (now a column on `LeaveRequest`,
  returned to the employee if they ask about their request's status)
- Both actions reject acting on a request that isn't still `PENDING`
  (`LeaveRequestNotPendingException`, HTTP 400) rather than silently overwriting a decision

`ManagerAgentFactory`'s instruction now explicitly splits routing: applying for leave / checking
one's own balance → Employee Agent; reviewing/approving/rejecting → HR Agent. This is the one
place a routing ambiguity was foreseeable enough to spell out rather than leave to the model's
general judgement.

`LeaveApprovalServiceTest` covers approve (no balance change), reject (balance refunded), and
both failure paths (unknown id, already-decided request).

**Still not built**: no notification back to the employee when their request is decided (that's
squarely the Notification Agent's job, once it exists) — right now an employee only finds out by
asking again.

## Document fulfillment workflow (Employee → HR)

Same pattern as leave approval. `DocumentType` now includes `OFFER_LETTER` and `JOINING_LETTER`
alongside the original three. Document requests are created `REQUESTED` by the Employee Agent;
only the HR Agent can progress them.

- `HrDocumentFulfillmentTools.listPendingDocumentRequests()` — global `REQUESTED` queue, oldest first
- `HrDocumentFulfillmentTools.markDocumentReady(requestId)` — `REQUESTED` → `READY`
- `HrDocumentFulfillmentTools.markDocumentDelivered(requestId)` — `READY` → `DELIVERED`
- Both reject the wrong starting state (`DocumentRequestNotActionableException`, HTTP 400)

**Scoped out of this pass, on purpose:** no reject path for a document request (unlike leave) —
if HR genuinely can't fulfill one, the agent is instructed to say so rather than force a status
change. Add `DocumentFulfillmentService.rejectDocumentRequest(...)` if that's needed later; the
pattern from `LeaveApprovalService.rejectLeaveRequest` carries over directly.

`ManagerAgentFactory`'s instruction now covers this same Employee-request/HR-fulfillment split
for documents, mirroring the leave routing hint.

`DocumentFulfillmentServiceTest` covers the happy path and both invalid-state transitions.

## Suggested next steps (in order)

1. `mvn clean install` from the reactor root and fix any remaining compile errors — flag them to me directly, I'll trace them against the real source the same way as above.
2. Pick **one** department agent to implement first (HR is usually simplest to demo). Replace its stub's `build()` with a real `LlmAgent` + tools.
3. Add JWT authentication (`spring-security` dependency is already in the POM; no filter/config exists yet).
4. Swap `InMemoryRunner`'s session service for a persistent one (e.g. the `firestore-session-service` module already in your reactor) — in-memory state is lost on restart.
5. Add per-agent unit tests once the first real agent exists.

Not implemented in this pass, on purpose: JWT filter chain, persistent sessions, Docker/CI config, and any department agent logic.
