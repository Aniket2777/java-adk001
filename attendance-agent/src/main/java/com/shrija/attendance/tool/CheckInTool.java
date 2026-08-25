package com.shrija.attendance.tool;

import com.shrija.attendance.a2a.EmployeeAgentClient;
import com.shrija.attendance.mcp.AttendanceMcpClient;
import com.shrija.attendance.service.AuthorizationService;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class CheckInTool {

  private final AttendanceMcpClient mcpClient;
  private final EmployeeAgentClient employeeAgentClient;
  private final AuthorizationService authorizationService;

  public CheckInTool(
      AttendanceMcpClient mcpClient,
      EmployeeAgentClient employeeAgentClient,
      AuthorizationService authorizationService) {
    this.mcpClient = mcpClient;
    this.employeeAgentClient = employeeAgentClient;
    this.authorizationService = authorizationService;
  }

  public Map<String, Object> checkIn(
      String requesterEmployeeId, String requesterRole, String employeeId, String timestamp) {
    authorizationService.requireSelfOrPrivileged(requesterEmployeeId, requesterRole, employeeId);
    verifyEmployee(employeeId);
    String effectiveTimestamp =
        timestamp == null || timestamp.isBlank() ? LocalDateTime.now().toString() : timestamp;
    return mcpClient.call(
        "checkIn", Map.of("employeeId", employeeId, "timestamp", effectiveTimestamp));
  }

  private void verifyEmployee(String employeeId) {
    Map<String, Object> result = employeeAgentClient.verifyEmployee(employeeId);
    if (!Boolean.TRUE.equals(result.get("verified"))) {
      throw new IllegalStateException("Employee could not be verified by Employee Agent.");
    }
  }
}
