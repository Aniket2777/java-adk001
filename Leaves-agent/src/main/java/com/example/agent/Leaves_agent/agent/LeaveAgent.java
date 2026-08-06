package com.example.agent.Leaves_agent.agent;

import com.example.agent.Leaves_agent.tools.LeaveTool;
import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.tools.FunctionTool;

public final class LeaveAgent {

  private LeaveAgent() {}

  public static final BaseAgent ROOT_AGENT =
      LlmAgent.builder()
          .name("leave_agent")
          .model("gemini-3.1-flash-lite")
          .description("Handles leave balance queries for annual, sick, and casual leave.")
          .instruction(
              """
                            You answer questions about an employee's remaining leave balance
                            using the getLeaveBalance tool. The user is already authenticated —
                            do not ask for a password. Never modify employee records or approve
                            leave requests; you only report balances.
                            """)
          .tools(FunctionTool.create(LeaveTool.class, "getLeaveBalance"))
          .build();
}
