package com.shrija.manager.agent;

import com.google.adk.agents.LlmAgent;
import com.google.adk.models.Gemini;
import com.google.adk.tools.FunctionTool;
import com.google.common.collect.ImmutableList;
import com.shrija.manager.tool.HrEscalationTool;
import com.shrija.manager.tool.LeaveApprovalCoordinationTool;
import com.shrija.manager.tool.TeamAttendanceCoordinationTool;
import com.shrija.manager.tool.TeamRosterTool;
import org.springframework.stereotype.Component;

/**
 * Manager Agent - the domain agent for the People Manager role.
 *
 * <p>Primary responsibility: team management and approvals. Unlike the Orchestrator Agent, this
 * agent does NOT do intent routing across the whole HRMS - it is one of the peer domain agents
 * (alongside Employee, Attendance, Payroll, Leave, HR) that the Orchestrator delegates to when a
 * request is manager-specific.
 */
@Component
public class ManagerAgent {

  private static final String INSTRUCTION =
      """
      You are the Manager Agent for the Shrija AI HRMS. You represent the People Manager role:
      team management and approvals.

      Responsibilities:
      - Viewing the roster of employees who report to the requesting manager.
      - Viewing an individual team member's profile (only for members of that manager's own team).
      - Viewing pending leave requests for the manager's team, and approving or rejecting them.
      - Viewing the manager's team attendance report for a given date.
      - Escalating team matters (policy exceptions, headcount/backfill requests, performance
        concerns) to HR when they are outside this agent's own authority.

      Mandatory rules:
      1. Use only the supplied tools. Never invent, estimate, or infer team, leave, or attendance data.
      2. Team roster and profile lookups use MCP (Employee/Team data) and are strictly read-only -
         this agent never creates or updates employee records.
      3. Leave approvals are never decided locally: always confirm and record the decision through
         the Leave Agent over A2A.
      4. Attendance data is never read from a database directly: always confirm it through the
         Attendance Agent over A2A.
      5. Every operation requires a MANAGER, HR, or ADMIN requester role. Never bypass this check
         and never infer authorization from the wording of the request - use the actual
         requesterEmployeeId and requesterRole supplied with the request.
      6. A manager may only view or act on employees who are confirmed to be on their own team.
      7. If MCP or A2A is unavailable, state plainly that the required service could not be
         reached. Do not fabricate a fallback answer.
      8. Ask for missing information (date, leaveRequestId, employeeId, approve/reject decision)
         rather than guessing.
      9. Do not perform Employee, Attendance, Payroll, Leave, HR, or Budget business logic that
         belongs to those agents - always cross the A2A boundary to their agent instead.
      """;

  private final LlmAgent agent;

  public ManagerAgent(
      Gemini managerGeminiModel,
      TeamRosterTool teamRosterTool,
      LeaveApprovalCoordinationTool leaveApprovalCoordinationTool,
      TeamAttendanceCoordinationTool teamAttendanceCoordinationTool,
      HrEscalationTool hrEscalationTool) {

    this.agent =
        LlmAgent.builder()
            .name("manager-agent")
            .description(
                "Handles People Manager team-management and approval operations: team roster, "
                    + "leave approvals/rejections, team attendance visibility, and HR escalation.")
            .instruction(INSTRUCTION)
            .model(managerGeminiModel)
            .tools(
                ImmutableList.of(
                    FunctionTool.create(teamRosterTool, "getTeamMembers"),
                    FunctionTool.create(teamRosterTool, "getTeamMemberProfile"),
                    FunctionTool.create(leaveApprovalCoordinationTool, "getPendingApprovals"),
                    FunctionTool.create(leaveApprovalCoordinationTool, "decideOnLeaveRequest"),
                    FunctionTool.create(teamAttendanceCoordinationTool, "getTeamAttendanceReport"),
                    FunctionTool.create(hrEscalationTool, "escalateToHr")))
            .build();
  }

  public LlmAgent agent() {
    return agent;
  }
}
