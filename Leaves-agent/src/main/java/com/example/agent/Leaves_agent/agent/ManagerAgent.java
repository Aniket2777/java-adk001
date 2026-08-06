package com.example.agent.Leaves_agent.agent;

import com.example.agent.Leaves_agent.tools.AuthenticationTool;
import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.tools.FunctionTool;

public final class ManagerAgent {

  private ManagerAgent() {}

  // This is the fix for the gap in the reference project: .subAgents(...)
  // actually gives this agent a way to hand off control. ADK's LlmAgent
  // reads each sub-agent's `description` and decides, per user turn,
  // whether to answer itself or transfer to one of them.
  public static final BaseAgent ROOT_AGENT =
      LlmAgent.builder()
          .name("manager_agent")
          .model("gemini-3.1-flash-lite")
          .description(
              "Enterprise front door: authenticates users and routes to the right specialist agent.")
          .instruction(
              """
                            You are the Enterprise Manager Agent.

                            1. Always authenticate the user first with the authenticate tool,
                               using the employee ID and password they give you.
                            2. If authentication fails, tell them and stop — do not proceed.
                            3. Once authenticated, read their request and delegate:
                               - Employee record questions (name, department, designation, role,
                                 joining date) -> employee_agent
                               - Leave balance questions -> leave_agent
                               - Payslip / salary questions (basic, deductions, net pay) -> payroll_agent
                            4. Never answer employee-record, leave, or payroll questions
                               yourself — always delegate to the matching sub-agent.
                            """)
          .tools(FunctionTool.create(AuthenticationTool.class, "authenticate"))
          .subAgents(EmployeeAgent.ROOT_AGENT, LeaveAgent.ROOT_AGENT, PayrollAgent.ROOT_AGENT)
          .build();
}
