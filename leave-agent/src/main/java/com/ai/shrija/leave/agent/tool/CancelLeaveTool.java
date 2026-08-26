package com.ai.shrija.leave.agent.tool;

import com.ai.shrija.leave.agent.dto.ApplyLeaveResponse;
import com.ai.shrija.leave.agent.model.Leave;
import com.ai.shrija.leave.agent.service.LeaveService;
import org.springframework.stereotype.Component;

/**
 * ADK tool exposed to the LeaveAgent LLM so it can cancel an existing leave
 * application (pending or already approved). If the leave was already
 * approved, the deducted balance is credited back automatically.
 */
@Component
public class CancelLeaveTool {

    private final LeaveService leaveService;

    public CancelLeaveTool(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    public String name() {
        return "cancel_leave";
    }

    public String description() {
        return "Cancels an existing leave application by its leaveId. "
                + "If it had already been approved, the leave balance is restored.";
    }

    public ApplyLeaveResponse execute(String leaveId, String requestedBy) {
        Leave leave = leaveService.cancelLeave(leaveId, requestedBy);
        return ApplyLeaveResponse.from(leave, "Leave " + leaveId + " has been cancelled.");
    }
}
