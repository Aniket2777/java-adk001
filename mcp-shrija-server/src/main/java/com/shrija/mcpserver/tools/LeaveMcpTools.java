package com.shrija.mcpserver.tools;

import com.shrija.domain.dto.LeaveRequestDto;
import com.shrija.domain.exception.ShrijaAiException;
import com.shrija.domain.model.LeaveType;
import com.shrija.domain.service.EmployeeSelfServiceService;
import com.shrija.domain.service.LeaveApprovalService;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * MCP tools for leave: the employee-side actions (check balance, apply) call into {@link
 * EmployeeSelfServiceService}; the HR-side review actions (list pending, approve, reject) call into
 * {@link LeaveApprovalService}. Both are exposed from one MCP tool class since they operate on the
 * same {@code LeaveRequest} concept, even though the underlying services stay split by actor - same
 * reasoning documented on those two services already.
 */
@Component
public class LeaveMcpTools {

  private static final Logger log = LoggerFactory.getLogger(LeaveMcpTools.class);

  private final EmployeeSelfServiceService employeeSelfServiceService;
  private final LeaveApprovalService leaveApprovalService;

  public LeaveMcpTools(
      EmployeeSelfServiceService employeeSelfServiceService,
      LeaveApprovalService leaveApprovalService) {
    this.employeeSelfServiceService = employeeSelfServiceService;
    this.leaveApprovalService = leaveApprovalService;
  }

  @Tool(description = "Check an employee's remaining leave balance for a given leave type")
  public Map<String, Object> checkLeaveBalance(
      @ToolParam(description = "The employee's unique code, e.g. EMP1024") String employeeCode,
      @ToolParam(description = "The type of leave to check") LeaveType leaveType) {
    try {
      return Map.of(
          "found",
          true,
          "balance",
          employeeSelfServiceService.getLeaveBalance(employeeCode, leaveType));
    } catch (ShrijaAiException ex) {
      log.debug("checkLeaveBalance failed for {}/{}: {}", employeeCode, leaveType, ex.getMessage());
      return Map.of("found", false, "message", ex.getMessage());
    }
  }

  @Tool(
      description =
          "Apply for leave. Always creates a PENDING request - approval is a separate action.")
  public Map<String, Object> applyForLeave(
      @ToolParam(description = "The employee's unique code, e.g. EMP1024") String employeeCode,
      @ToolParam(description = "The type of leave to apply for") LeaveType leaveType,
      @ToolParam(description = "First day of leave, yyyy-MM-dd") String startDate,
      @ToolParam(description = "Last day of leave, inclusive, yyyy-MM-dd") String endDate) {
    try {
      LeaveRequestDto request =
          employeeSelfServiceService.applyForLeave(employeeCode, leaveType, startDate, endDate);
      return Map.of("success", true, "request", request);
    } catch (ShrijaAiException ex) {
      log.debug("applyForLeave failed for {}/{}: {}", employeeCode, leaveType, ex.getMessage());
      return Map.of("success", false, "message", ex.getMessage());
    }
  }

  @Tool(description = "List every PENDING leave request, oldest first - for HR review")
  public Map<String, Object> listPendingLeaveRequests() {
    var pending = leaveApprovalService.listPendingLeaveRequests();
    return Map.of("count", pending.size(), "requests", pending);
  }

  @Tool(description = "Approve a pending leave request")
  public Map<String, Object> approveLeaveRequest(
      @ToolParam(description = "The leave request's id, from listPendingLeaveRequests")
          long requestId) {
    try {
      LeaveRequestDto approved = leaveApprovalService.approveLeaveRequest(requestId);
      return Map.of("success", true, "request", approved);
    } catch (ShrijaAiException ex) {
      log.debug("approveLeaveRequest failed for {}: {}", requestId, ex.getMessage());
      return Map.of("success", false, "message", ex.getMessage());
    }
  }

  @Tool(description = "Reject a pending leave request, refunding the reserved balance")
  public Map<String, Object> rejectLeaveRequest(
      @ToolParam(description = "The leave request's id, from listPendingLeaveRequests")
          long requestId,
      @ToolParam(description = "Why the request is being rejected, shown to the employee")
          String reason) {
    try {
      LeaveRequestDto rejected =
          leaveApprovalService.rejectLeaveRequest(
              requestId, (reason == null || reason.isBlank()) ? "No reason provided" : reason);
      return Map.of("success", true, "request", rejected);
    } catch (ShrijaAiException ex) {
      log.debug("rejectLeaveRequest failed for {}: {}", requestId, ex.getMessage());
      return Map.of("success", false, "message", ex.getMessage());
    }
  }
}
