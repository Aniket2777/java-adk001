package com.ai.shrija.leave.agent.config;

import com.ai.shrija.leave.agent.agent.LeaveAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the Agent Development Kit (ADK) runtime for the LeaveAgent:
 * which model backs it, generation parameters, and how its tool set (see
 * agent/LeaveAgent.java#tools) is registered with the runner/session
 * machinery.
 *
 * The bean shapes below (AdkModelConfig, AdkRunner) are illustrative — wire
 * them up to whichever concrete ADK-for-Java classes your build pulls in
 * (e.g. com.google.adk.agents.LlmAgent / com.google.adk.runner.Runner) once
 * the dependency is added to pom.xml.
 */
@Configuration
public class AdkConfig {

    @Value("${adk.model.name:gemini-2.0-flash}")
    private String modelName;

    @Value("${adk.model.temperature:0.2}")
    private double temperature;

    /**
     * Model/generation settings passed to the ADK runtime when it builds the
     * underlying LlmAgent for {@link LeaveAgent}.
     */
    public record AdkModelConfig(String modelName, double temperature) {
    }

    @Bean
    public AdkModelConfig adkModelConfig() {
        return new AdkModelConfig(modelName, temperature);
    }

    /**
     * Placeholder for the ADK Runner/Session service that actually drives
     * LeaveAgent conversations (turn management, tool-call loop, memory).
     * Replace with the real ADK Runner bean, passing it leaveAgent.tools()
     * and leaveAgent.systemPrompt() as the agent definition.
     *
     * e.g.
     *   LlmAgent adkAgent = LlmAgent.builder()
     *       .name(leaveAgent.name())
     *       .description(leaveAgent.description())
     *       .instruction(leaveAgent.systemPrompt())
     *       .model(adkModelConfig().modelName())
     *       .tools(leaveAgent.tools())
     *       .build();
     *   return new Runner(adkAgent, ...);
     */
    @Bean
    public LeaveAgentRunner leaveAgentRunner(LeaveAgent leaveAgent, AdkModelConfig adkModelConfig) {
        return new LeaveAgentRunner(leaveAgent, adkModelConfig);
    }

    /**
     * Minimal placeholder runner so the module compiles and the controller
     * has something to call. Swap for the real ADK Runner implementation.
     */
    public static class LeaveAgentRunner {

        private final LeaveAgent leaveAgent;
        private final AdkModelConfig adkModelConfig;

        public LeaveAgentRunner(LeaveAgent leaveAgent, AdkModelConfig adkModelConfig) {
            this.leaveAgent = leaveAgent;
            this.adkModelConfig = adkModelConfig;
        }

        public String run(String sessionId, String userMessage) {
            // TODO: delegate to the real ADK Runner: build/reuse a session for
            // sessionId, append userMessage, run the agent loop (which may
            // invoke leaveAgent.tools()), and return the final model reply.
            throw new UnsupportedOperationException(
                    "Wire this up to the ADK Runner for model " + adkModelConfig.modelName()
                            + " and agent " + leaveAgent.name());
        }
    }
}
