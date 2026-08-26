package com.ai.shrija.leave.agent.config;

import com.ai.shrija.leave.agent.agent.LeaveAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

/**
 * Configures this service's participation in the Agent-to-Agent (A2A)
 * protocol: the WebClient used by client/ManagerAgentClient,
 * client/PayrollAgentClient and client/EmployeeAgentClient to call peer
 * agents, plus the AgentCard this service publishes so peer agents can
 * discover the Leave Agent's own skills at /.well-known/agent.json
 * (see controller/LeaveController.java#agentCard).
 */
@Configuration
public class A2AConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${agents.self.base-url:http://localhost:8080}")
    private String selfBaseUrl;

    @Bean
    public WebClient.Builder a2aWebClientBuilder() {
        return WebClient.builder();
    }

    /**
     * Describes the Leave Agent's own capabilities in A2A AgentCard shape so
     * peer agents (Manager/Payroll/Employee) can discover what skills it
     * exposes over A2A. Exposed by LeaveController at
     * GET /.well-known/agent.json.
     */
    @Bean
    public Map<String, Object> leaveAgentCard(LeaveAgent leaveAgent) {
        return Map.of(
                "name", leaveAgent.name(),
                "description", leaveAgent.description(),
                "url", selfBaseUrl,
                "version", "1.0.0",
                "capabilities", Map.of(
                        "streaming", false,
                        "pushNotifications", false
                ),
                "skills", List.of(
                        Map.of(
                                "id", "apply-leave",
                                "name", "Apply for leave",
                                "description", "Submits a new leave application for an employee."
                        ),
                        Map.of(
                                "id", "cancel-leave",
                                "name", "Cancel leave",
                                "description", "Cancels an existing leave application."
                        ),
                        Map.of(
                                "id", "get-leave-balance",
                                "name", "Get leave balance",
                                "description", "Returns an employee's remaining leave balance by type."
                        ),
                        Map.of(
                                "id", "review-leave-decision",
                                "name", "Record leave decision",
                                "description", "Records an approve/reject decision made by a manager agent."
                        )
                )
        );
    }
}
