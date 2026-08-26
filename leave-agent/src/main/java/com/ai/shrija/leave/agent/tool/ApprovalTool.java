package com.ai.shrija.leave.agent.tool;

import com.ai.shrija.leave.agent.client.ManagerAgentClient;
import com.ai.shrija.leave.agent.dto.ApplyLeaveResponse;
import com.ai.shrija.leave.agent.model.Leave;
import com.ai.shrija.leave.agent.service.LeaveService;
import org.springframework.stereotype.Component;

/**
 * ADK tool exposed to the LeaveAgent LLM to progress a pending leave through
 * approval. It first delegates the approval decision to the employee's
 * Manager Agent over A2A, then records the decision locally.
 */
@Component
public class ApprovalTool {

    private final LeaveService leaveService;
    private final ManagerAgentClient managerAgentClient;

    public ApprovalTool(LeaveService leaveService, ManagerAgentClient managerAgentClient) {
        this.leaveService = leaveService;
        this.managerAgentClient = managerAgentClient;
    }

    public String name() {
        return "request_leave_approval";
    }

    public String description() {
        return "Requests an approval decision from the employee's manager agent (via A2A) for a pending "
                + "leave application, and records the resulting decision (approved/rejected).";
    }

    public ApplyLeaveResponse execute(String leaveId) {
        Leave leave = leaveService.getLeave(leaveId);

        ManagerAgentClient.ApprovalDecision decision = managerAgentClient.requestApproval(leave);

        Leave updated = leaveService.decideApproval(
                leaveId, decision.approverId(), decision.approved(), decision.comments());

        String message = updated.getStatus() == Leave.Status.APPROVED
                ? "Leave " + leaveId + " was approved by " + decision.approverId() + "."
                : "Leave " + leaveId + " was rejected by " + decision.approverId() + ".";

        return ApplyLeaveResponse.from(updated, message);
    }
}
