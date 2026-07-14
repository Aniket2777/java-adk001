package com.ai.shrija.manager.agent;

import com.ai.shrija.manager.tools.EmployeeTool;
import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.tools.FunctionTool;

public final class EmployeeAgent {

    public static final BaseAgent ROOT_AGENT =
            LlmAgent.builder()
                    .name("employee_agent")
                    .model("gemini-2.5-flash")
                    .description("Employee Management Agent")
                    .instruction("""
                        You authenticate employees.

                        You provide employee information.

                        You update employee information.

                        Never perform payroll or HR operations.
                        """)
                    .tools(
                            FunctionTool.create(EmployeeTool.class,
                                    "authenticateEmployee"),

                            FunctionTool.create(EmployeeTool.class,
                                    "getEmployee")
                    )
                    .build();

}