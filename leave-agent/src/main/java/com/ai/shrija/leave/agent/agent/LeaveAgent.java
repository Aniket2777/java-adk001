package com.ai.shrija.leave.agent.agent;

import com.ai.shrija.leave.agent.tool.ApplyLeaveTool;
import com.ai.shrija.leave.agent.tool.ApprovalTool;
import com.ai.shrija.leave.agent.tool.CancelLeaveTool;
import com.ai.shrija.leave.agent.tool.LeaveBalanceTool;
import org.springframework.stereotype.Component;

/**
 * The Leave Agent: an LLM-backed agent, configured via config/AdkConfig.java,
 * that can converse with an employee or another agent and decide when to
 * call the leave tools to fulfill their request.
 *
 * This class is intentionally framework-light — the actual ADK
 * Agent/Runner/Session wiring lives in config/AdkConfig.java so this class
 * stays focused on what makes the Leave Agent specifically a *leave* agent:
 * its name, its instructions, and its tool set.
 */
@Component
public class LeaveAgent {

    public static final String AGENT_NAME = "leave-agent";

    private final LeaveInstructions instructions;
    private final ApplyLeaveTool applyLeaveTool;
    private final CancelLeaveTool cancelLeaveTool;
    private final LeaveBalanceTool leaveBalanceTool;
    private final ApprovalTool approvalTool;

    public LeaveAgent(LeaveInstructions instructions,
                       ApplyLeaveTool applyLeaveTool,
                       CancelLeaveTool cancelLeaveTool,
                       LeaveBalanceTool leaveBalanceTool,
                       ApprovalTool approvalTool) {
        this.instructions = instructions;
        this.applyLeaveTool = applyLeaveTool;
        this.cancelLeaveTool = cancelLeaveTool;
        this.leaveBalanceTool = leaveBalanceTool;
        this.approvalTool = approvalTool;
    }

    public String name() {
        return AGENT_NAME;
    }

    public String description() {
        return "Handles employee leave: checking balances, applying for leave, "
                + "routing manager approval, and cancelling leave requests.";
    }

    public String systemPrompt() {
        return instructions.get();
    }

    public Object[] tools() {
        return new Object[] { applyLeaveTool, cancelLeaveTool, leaveBalanceTool, approvalTool };
    }
}
