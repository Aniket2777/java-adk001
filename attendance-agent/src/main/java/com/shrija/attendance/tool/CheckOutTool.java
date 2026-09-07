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
          String requesterEmployeeId,
          String requesterRole,
          String employeeId) {

    // 1. Authorization
    authorizationService.requireSelfOrPrivileged(
            requesterEmployeeId,
            requesterRole,
            employeeId);

    // 2. Verify employee through Employee Agent
    Map<String, Object> verification =
            employeeAgentClient.verifyEmployee(employeeId);

    if (!Boolean.TRUE.equals(verification.get("verified"))) {
      throw new IllegalStateException(
              "Employee could not be verified by Employee Agent.");
    }

    // 3. Generate checkout date/time on server
    LocalDateTime now =
            LocalDateTime.now(ZoneId.of("Asia/Kolkata"));

    LocalDate workDate = now.toLocalDate();
    LocalTime checkOutTime = now.toLocalTime();

    System.out.println(
            "Checkout employeeId = " + employeeId);

    System.out.println(
            "Checkout date = " + workDate);

    System.out.println(
            "Checkout time = " + checkOutTime);

    // 4. Call Attendance MCP
    return mcpClient.call(
            "checkOut",
            Map.of(
                    "employeeId", Long.valueOf(employeeId),
                    "workDate", workDate.toString(),
                    "checkOutTime", checkOutTime.toString()));
  }
}