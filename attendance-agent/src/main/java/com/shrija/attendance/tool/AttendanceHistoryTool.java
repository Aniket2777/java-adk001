package com.shrija.attendance.tool;

import com.shrija.attendance.a2a.EmployeeAgentClient;
import com.shrija.attendance.mcp.AttendanceMcpClient;
import com.shrija.attendance.service.AuthorizationService;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class AttendanceHistoryTool {

  private final AttendanceMcpClient mcpClient;
  private final EmployeeAgentClient employeeAgentClient;
  private final AuthorizationService authorizationService;

  public AttendanceHistoryTool(
      AttendanceMcpClient mcpClient,
      EmployeeAgentClient employeeAgentClient,
      AuthorizationService authorizationService) {
    this.mcpClient = mcpClient;
    this.employeeAgentClient = employeeAgentClient;
    this.authorizationService = authorizationService;
  }

  public Map<String, Object> getAttendanceHistory(
          String requesterEmployeeId, String requesterRole, String employeeId, String from, String to) {
    authorizationService.requireSelfOrPrivileged(requesterEmployeeId, requesterRole, employeeId);
    verify(employeeId);
    String effectiveFrom =
            from == null || from.isBlank() ? LocalDate.now().withDayOfMonth(1).toString() : from;
    String effectiveTo = to == null || to.isBlank() ? LocalDate.now().toString() : to;
    return mcpClient.call(
            "getAttendanceForRange",
            Map.of(
                    "employeeId", Long.valueOf(employeeId),
                    "start", effectiveFrom,
                    "end", effectiveTo));
  }

  public Map<String, Object> getMonthlyAttendance(
          String requesterEmployeeId, String requesterRole, String employeeId, String month) {
    authorizationService.requireSelfOrPrivileged(requesterEmployeeId, requesterRole, employeeId);
    verify(employeeId);
    YearMonth effectiveMonth =
            month == null || month.isBlank() ? YearMonth.now() : YearMonth.parse(month);
    return mcpClient.call(
            "getMonthlyAttendanceSummary",
            Map.of(
                    "employeeId", Long.valueOf(employeeId),
                    "month", effectiveMonth.getMonthValue(),
                    "year", effectiveMonth.getYear()));
  }

  private void verify(String employeeId) {
    if (!Boolean.TRUE.equals(employeeAgentClient.verifyEmployee(employeeId).get("verified"))) {
      throw new IllegalStateException("Employee could not be verified by Employee Agent.");
    }
  }
}
