package com.shrija.attendance.tool;

import com.shrija.attendance.a2a.EmployeeAgentClient;
import com.shrija.attendance.mcp.AttendanceMcpClient;
import com.shrija.attendance.service.AuthorizationService;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class TodayAttendanceTool {

  private final AttendanceMcpClient mcpClient;
  private final EmployeeAgentClient employeeAgentClient;
  private final AuthorizationService authorizationService;

  public TodayAttendanceTool(
          AttendanceMcpClient mcpClient,
          EmployeeAgentClient employeeAgentClient,
          AuthorizationService authorizationService) {
    this.mcpClient = mcpClient;
    this.employeeAgentClient = employeeAgentClient;
    this.authorizationService = authorizationService;
  }

  public Map<String, Object> getTodayAttendance(
          String requesterEmployeeId, String requesterRole, String employeeId, String date) {
    authorizationService.requireSelfOrPrivileged(requesterEmployeeId, requesterRole, employeeId);
    verify(employeeId);
    String effectiveDate = date == null || date.isBlank() ? LocalDate.now().toString() : date;
    return mcpClient.call(
            "getAttendanceForRange",
            Map.of(
                    "employeeId", Long.valueOf(employeeId),
                    "start", effectiveDate,
                    "end", effectiveDate));
  }

  private void verify(String employeeId) {
    if (!Boolean.TRUE.equals(employeeAgentClient.verifyEmployee(employeeId).get("verified"))) {
      throw new IllegalStateException("Employee could not be verified by Employee Agent.");
    }
  }
}