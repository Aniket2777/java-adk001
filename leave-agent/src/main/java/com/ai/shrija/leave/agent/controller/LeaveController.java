package com.ai.shrija.leave.agent.controller;

import com.ai.shrija.leave.agent.dto.ApplyLeaveRequest;
import com.ai.shrija.leave.agent.dto.ApplyLeaveResponse;
import com.ai.shrija.leave.agent.dto.LeaveApprovalRequest;
import com.ai.shrija.leave.agent.dto.LeaveBalanceResponse;
import com.ai.shrija.leave.agent.model.Leave;
import com.ai.shrija.leave.agent.service.LeaveService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST surface for the Leave Agent.
 *
 * - /api/leaves/**   plain CRUD-style endpoints, useful for direct
 *                     integration or for other services that don't want to
 *                     talk to the agent conversationally.
 * - /.well-known/agent.json  the A2A AgentCard for discovery by peer agents.
 * - /a2a/tasks        where peer agents (or this agent itself, in the demo
 *                     ManagerAgentClient/PayrollAgentClient/EmployeeAgentClient)
 *                     would post A2A tasks. Wire to the real ADK Runner via
 *                     config/AdkConfig.java#leaveAgentRunner in production.
 */
@RestController
public class LeaveController {

    private final LeaveService leaveService;
    private final Map<String, Object> leaveAgentCard;

    public LeaveController(LeaveService leaveService, Map<String, Object> leaveAgentCard) {
        this.leaveService = leaveService;
        this.leaveAgentCard = leaveAgentCard;
    }

    @GetMapping("/.well-known/agent.json")
    public Map<String, Object> agentCard() {
        return leaveAgentCard;
    }

    @PostMapping("/api/leaves")
    public ResponseEntity<ApplyLeaveResponse> applyLeave(@Valid @RequestBody ApplyLeaveRequest request) {
        Leave leave = leaveService.applyLeave(request);
        return ResponseEntity.ok(ApplyLeaveResponse.from(leave, "Leave application submitted."));
    }

    @GetMapping("/api/leaves/{leaveId}")
    public ResponseEntity<Leave> getLeave(@PathVariable String leaveId) {
        return ResponseEntity.ok(leaveService.getLeave(leaveId));
    }

    @PostMapping("/api/leaves/{leaveId}/cancel")
    public ResponseEntity<ApplyLeaveResponse> cancelLeave(@PathVariable String leaveId,
                                                           @RequestParam String requestedBy) {
        Leave leave = leaveService.cancelLeave(leaveId, requestedBy);
        return ResponseEntity.ok(ApplyLeaveResponse.from(leave, "Leave cancelled."));
    }

    @PostMapping("/api/leaves/approvals")
    public ResponseEntity<ApplyLeaveResponse> decideApproval(@Valid @RequestBody LeaveApprovalRequest request) {
        Leave leave = leaveService.decideApproval(
                request.getLeaveId(), request.getApproverId(), request.isApproved(), request.getComments());
        String message = request.isApproved() ? "Leave approved." : "Leave rejected.";
        return ResponseEntity.ok(ApplyLeaveResponse.from(leave, message));
    }

    @GetMapping("/api/employees/{employeeId}/leave-balance")
    public ResponseEntity<LeaveBalanceResponse> getBalance(@PathVariable String employeeId) {
        return ResponseEntity.ok(new LeaveBalanceResponse(employeeId, leaveService.getAllBalances(employeeId)));
    }
}
