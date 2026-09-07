package com.ai.shrija.manager.agent;

import com.ai.shrija.manager.tools.AuthenticationTool;
import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.tools.FunctionTool;

public final class ManagerAgent {

  private ManagerAgent() {}

  public static final BaseAgent ROOT_AGENT =
      LlmAgent.builder()
          .name("manager_agent")
          .model("gemini-3.1-flash-lite")
          .description("Enterprise Manager Agent")
          .instruction(
              """
                            You are the Enterprise Manager Agent.

                            Authenticate every user.

                            Identify department.

                            Identify designation.

                            Route the request to the appropriate business agent.

                            Never perform business operations yourself.
                            """)
          .tools(FunctionTool.create(AuthenticationTool.class, "authenticate"))
          .build();
}
