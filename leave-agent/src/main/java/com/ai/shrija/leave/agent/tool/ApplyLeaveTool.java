package com.ai.shrija.leave.agent.tool;

import com.ai.shrija.leave.agent.dto.ApplyLeaveRequest;
import com.ai.shrija.leave.agent.dto.ApplyLeaveResponse;
import com.ai.shrija.leave.agent.model.Leave;
import com.ai.shrija.leave.agent.service.LeaveService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * ADK tool exposed to the LeaveAgent LLM so it can submit a new leave
 * application on behalf of an employee.
 *
 * The agent framework introspects {@link #name()}, {@link #description()}
 * and the {@link #execute(String, String, LocalDate, LocalDate, String)}
 * signature to build the tool schema advertised to the model. Wire this bean
 * into the ADK tool registry in config/AdkConfig.java.
 */
@Component
public class ApplyLeaveTool {

    private final LeaveService leaveService;

    public ApplyLeaveTool(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    public String name() {
        return "apply_leave";
    }

    public String description() {
        return "Submits a new leave application for an employee for a given leave type and date range. "
                + "Returns the created leave's id and its initial status (usually PENDING_APPROVAL).";
    }

    public ApplyLeaveResponse execute(String employeeId, String leaveType, LocalDate startDate,
                                       LocalDate endDate, String reason) {
        ApplyLeaveRequest request = new ApplyLeaveRequest(
                employeeId, Leave.Type.valueOf(leaveType.toUpperCase()), startDate, endDate, reason);
        Leave leave = leaveService.applyLeave(request);
        String message = leave.isStartsOnCompanyHoliday()
                ? "Leave application submitted and is pending approval. Note: the start date falls on a "
                        + "declared company holiday, per the HRMS holiday calendar."
                : "Leave application submitted and is pending approval.";
        return ApplyLeaveResponse.from(leave, message);
    }
}
