package com.shrija.attendance.tool;

import com.shrija.attendance.a2a.EmployeeAgentClient;
import com.shrija.attendance.mcp.AttendanceMcpClient;
import com.shrija.attendance.service.AuthorizationService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
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
          String requesterEmployeeId,
          String requesterRole,
          Long employeeId) {

    // 1. Authorization
    authorizationService.requireSelfOrPrivileged(
            requesterEmployeeId,
            requesterRole,
            String.valueOf(employeeId));

    // 2. Verify employee through Employee Agent
    verifyEmployee(String.valueOf(employeeId));

    // 3. Get CURRENT date and time when checkIn is executed
    LocalDateTime now =
            LocalDateTime.now(ZoneId.of("Asia/Kolkata"));

    LocalDate workDate = now.toLocalDate();
    LocalTime checkInTime = now.toLocalTime();

    System.out.println(
            "Check-in timestamp = " + now);

    // 4. Send current timestamp to MCP server
    return mcpClient.call(
            "checkIn",
            Map.of(
                    "employeeId", employeeId,
                    "workDate", workDate,
                    "checkInTime", checkInTime));
  }

  private void verifyEmployee(String employeeId) {

    System.out.println(
            "Calling Employee Agent with employeeId = " + employeeId);

    Map<String, Object> result =
            employeeAgentClient.verifyEmployee(employeeId);

    System.out.println(
            "Employee Agent response = " + result);

    if (!Boolean.TRUE.equals(result.get("verified"))) {
      throw new IllegalStateException(
              "Employee could not be verified by Employee Agent. Response: "
                      + result);
    }
  }
}