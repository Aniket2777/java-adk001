package com.ai.shrija.leave.agent.client;

import com.ai.shrija.leave.agent.model.Leave;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * A2A client for the Employee Agent. Used to push status notifications back
 * to the employee's personal agent (e.g. "your leave was approved") so it
 * can surface the update in whatever channel the employee prefers (chat,
 * email digest, calendar block, etc.).
 */
@Component
public class EmployeeAgentClient {

    private final WebClient webClient;

    public EmployeeAgentClient(WebClient.Builder webClientBuilder,
                                @Value("${agents.employee.url}") String employeeAgentUrl) {
        this.webClient = webClientBuilder.baseUrl(employeeAgentUrl).build();
    }

    /**
     * Notifies the employee's agent of a leave status change.
     * TODO: replace with the A2A SDK client once the Employee Agent's
     * AgentCard is registered in config/A2AConfig.java.
     */
    public void notifyEmployee(Leave leave, String message) {
        Map<String, Object> taskPayload = Map.of(
                "skill", "notify-employee",
                "input", Map.of(
                        "employeeId", leave.getEmployeeId(),
                        "leaveId", leave.getLeaveId(),
                        "status", leave.getStatus().name(),
                        "message", message
                )
        );

        webClient.post()
                .uri("/a2a/tasks")
                .bodyValue(taskPayload)
                .retrieve()
                .toBodilessEntity()
                .subscribe();
    }
}
