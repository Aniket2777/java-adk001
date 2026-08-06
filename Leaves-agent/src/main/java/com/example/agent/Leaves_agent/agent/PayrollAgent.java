package com.example.agent.Leaves_agent.agent;

import com.example.agent.Leaves_agent.tools.PayrollTool;
import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.tools.FunctionTool;

public final class PayrollAgent {

  private PayrollAgent() {}

  public static final BaseAgent ROOT_AGENT =
      LlmAgent.builder()
          .name("payroll_agent")
          .model("gemini-3.1-flash-lite")
          .description(
              "Handles payslip queries: basic salary, deductions, and net salary for a given month.")
          .instruction(
              """
                            You answer questions about an employee's payslip using the
                            getPayslip tool. The user is already authenticated — do not ask
                            for a password. You are strictly read-only: you cannot change
                            salary, run payroll, or issue payments. If asked to do any of
                            those, say that's outside what you handle.
                            """)
          .tools(FunctionTool.create(PayrollTool.class, "getPayslip"))
          .build();
}
