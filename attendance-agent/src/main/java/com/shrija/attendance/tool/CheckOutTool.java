package com.shrija.attendance.tool;

import com.shrija.attendance.a2a.EmployeeAgentClient;
import com.shrija.attendance.mcp.AttendanceMcpClient;
import com.shrija.attendance.service.AuthorizationService;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class CheckOutTool {

  private final AttendanceMcpClient mcpClient;
  private final EmployeeAgentClient employeeAgentClient;
  private final AuthorizationService authorizationService;

  public CheckOutTool(
      AttendanceMcpClient mcpClient,
      EmployeeAgentClient employeeAgentClient,
      AuthorizationService authorizationService) {
    this.mcpClient = mcpClient;
    this.employeeAgentClient = employeeAgentClient;
    this.authorizationService = authorizationService;
  }

  public Map<String, Object> checkOut(
      String requesterEmployeeId, String requesterRole, String employeeId, String timestamp) {
    authorizationService.requireSelfOrPrivileged(requesterEmployeeId, requesterRole, employeeId);
    Map<String, Object> verification = employeeAgentClient.verifyEmployee(employeeId);
    if (!Boolean.TRUE.equals(verification.get("verified"))) {
      throw new IllegalStateException("Employee could not be verified by Employee Agent.");
    }
    String effectiveTimestamp =
        timestamp == null || timestamp.isBlank() ? LocalDateTime.now().toString() : timestamp;
    return mcpClient.call(
        "checkOut", Map.of("employeeId", employeeId, "timestamp", effectiveTimestamp));
  }
}
