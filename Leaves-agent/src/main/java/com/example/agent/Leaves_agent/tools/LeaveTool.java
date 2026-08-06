package com.example.agent.Leaves_agent.tools;

import com.example.agent.Leaves_agent.dao.LeaveDao;
import com.example.agent.Leaves_agent.entity.LeaveBalance;
import com.google.adk.tools.Annotations.Schema;
import java.util.Map;
import java.util.Optional;

public class LeaveTool {

  private static final LeaveDao leaveDao = new LeaveDao();

  @Schema(description = "Get an employee's remaining leave balance for a given leave type")
  public static Map<String, Object> getLeaveBalance(
      @Schema(name = "employeeId", description = "Employee ID") String employeeId,
      @Schema(name = "leaveType", description = "Leave type, e.g. ANNUAL, SICK, CASUAL")
          String leaveType) {

    Optional<LeaveBalance> balance = leaveDao.getBalance(employeeId, leaveType.toUpperCase());

    if (balance.isEmpty()) {
      return Map.of(
          "status", "FAILED",
          "message", "No leave balance found for that employee and leave type");
    }

    LeaveBalance b = balance.get();
    return Map.of(
        "status", "SUCCESS",
        "employeeId", b.getEmployeeId(),
        "leaveType", b.getLeaveType(),
        "balanceDays", b.getBalanceDays());
  }
}
