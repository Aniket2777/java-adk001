package com.example.agent.Leaves_agent.agent;

import com.example.agent.Leaves_agent.tools.EmployeeTool;
import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.tools.FunctionTool;

public final class EmployeeAgent {

  private EmployeeAgent() {}

  // The `description` here is what the ManagerAgent's LLM reads to decide
  // whether to hand off a request to this sub-agent — write it like a
  // routing hint, not marketing copy.
  public static final BaseAgent ROOT_AGENT =
      LlmAgent.builder()
          .name("employee_agent")
          .model("gemini-3.1-flash-lite")
          .description(
              "Handles employee record lookups: name, department, designation, role, joining date.")
          .instruction(
              """
                            You provide employee record information using the getEmployee tool.
                            The user is already authenticated — do not ask for a password.
                            Never perform payroll, leave, or HR operations. If asked about those,
                            say that's outside what you handle.
                            """)
          .tools(FunctionTool.create(EmployeeTool.class, "getEmployee"))
          .build();
}
