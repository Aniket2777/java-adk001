package com.ai.shrija.leave.agent.client;

import com.ai.shrija.leave.agent.model.Leave;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * A2A client for the Manager Agent. Sends the manager agent a task asking it
 * to approve or reject a pending leave request and blocks for the resulting
 * decision.
 *
 * This wraps the raw A2A SDK client behind a small typed interface so that
 * tool/ApprovalTool.java doesn't need to know about A2A message envelopes.
 */
@Component
public class ManagerAgentClient {

    private final WebClient webClient;

    public record ApprovalDecision(String approverId, boolean approved, String comments) {
    }

    public ManagerAgentClient(WebClient.Builder webClientBuilder,
                               @Value("${agents.manager.url}") String managerAgentUrl) {
        this.webClient = webClientBuilder.baseUrl(managerAgentUrl).build();
    }

    /**
     * Sends an A2A task to the Manager Agent asking it to review the given
     * leave and returns its decision.
     *
     * TODO: replace this simplified WebClient call with the A2A Java SDK's
     * A2AClient#sendTask(...) once the AgentCard for the Manager Agent is
     * resolved (see config/A2AConfig.java), so retries, streaming and task
     * state polling are handled by the SDK.
     */
    public ApprovalDecision requestApproval(Leave leave) {
        Map<String, Object> taskPayload = Map.of(
                "skill", "review-leave-request",
                "input", Map.of(
                        "leaveId", leave.getLeaveId(),
                        "employeeId", leave.getEmployeeId(),
                        "leaveType", leave.getType().name(),
                        "startDate", leave.getStartDate().toString(),
                        "endDate", leave.getEndDate().toString(),
                        "numberOfDays", leave.getNumberOfDays(),
                        "reason", leave.getReason()
                )
        );

        return webClient.post()
                .uri("/a2a/tasks")
                .bodyValue(taskPayload)
                .retrieve()
                .bodyToMono(ApprovalDecision.class)
                .block();
    }
}
