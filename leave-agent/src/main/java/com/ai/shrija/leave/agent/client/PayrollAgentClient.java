package com.ai.shrija.leave.agent.client;

import com.ai.shrija.leave.agent.model.Leave;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * A2A client for the Payroll Agent. Notifies payroll once a leave is
 * approved or cancelled so unpaid-leave deductions / accruals can be
 * recalculated for the relevant pay cycle.
 */
@Component
public class PayrollAgentClient {

    private final WebClient webClient;

    public PayrollAgentClient(WebClient.Builder webClientBuilder,
                               @Value("${agents.payroll.url}") String payrollAgentUrl) {
        this.webClient = webClientBuilder.baseUrl(payrollAgentUrl).build();
    }

    /**
     * Fire-and-forget notification to the Payroll Agent about a leave status
     * change. TODO: swap for the A2A SDK's async task submission so payroll
     * can push back a task-status-update if it needs more information.
     */
    public void notifyLeaveStatusChange(Leave leave) {
        Map<String, Object> taskPayload = Map.of(
                "skill", "sync-leave-status",
                "input", Map.of(
                        "leaveId", leave.getLeaveId(),
                        "employeeId", leave.getEmployeeId(),
                        "leaveType", leave.getType().name(),
                        "status", leave.getStatus().name(),
                        "numberOfDays", leave.getNumberOfDays()
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
