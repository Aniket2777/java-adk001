package com.ai.shrija.leave.agent.tool;

import com.ai.shrija.leave.agent.dto.LeaveBalanceResponse;
import com.ai.shrija.leave.agent.service.LeaveService;
import org.springframework.stereotype.Component;

/**
 * ADK tool exposed to the LeaveAgent LLM so it can look up an employee's
 * remaining leave balance across all leave types before deciding whether to
 * suggest applying for leave.
 */
@Component
public class LeaveBalanceTool {

    private final LeaveService leaveService;

    public LeaveBalanceTool(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    public String name() {
        return "get_leave_balance";
    }

    public String description() {
        return "Returns the remaining leave balance (in days) for every leave type for a given employee.";
    }

    public LeaveBalanceResponse execute(String employeeId) {
        return new LeaveBalanceResponse(employeeId, leaveService.getAllBalances(employeeId));
    }
}
