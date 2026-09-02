package com.shrija.attendance.tool;

import com.shrija.attendance.a2a.EmployeeAgentClient;
import com.shrija.attendance.a2a.ManagerAgentClient;
import com.shrija.attendance.a2a.PayrollAgentClient;
import com.shrija.attendance.mcp.AttendanceMcpClient;
import com.shrija.attendance.service.AuthorizationService;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class AttendanceReportTool {

  private final AttendanceMcpClient mcpClient;
  private final EmployeeAgentClient employeeAgentClient;
  private final PayrollAgentClient payrollAgentClient;
  private final ManagerAgentClient managerAgentClient;
  private final AuthorizationService authorizationService;

  public AttendanceReportTool(
          AttendanceMcpClient mcpClient,
          EmployeeAgentClient employeeAgentClient,
          PayrollAgentClient payrollAgentClient,
          ManagerAgentClient managerAgentClient,
          AuthorizationService authorizationService) {
    this.mcpClient = mcpClient;
    this.employeeAgentClient = employeeAgentClient;
    this.payrollAgentClient = payrollAgentClient;
    this.managerAgentClient = managerAgentClient;
    this.authorizationService = authorizationService;
  }

  public Map<String, Object> getAttendanceSummary(
          String requesterEmployeeId, String requesterRole, String employeeId, String month) {

    authorizationService.requireSelfOrPrivileged(
            requesterEmployeeId,
            requesterRole,
            employeeId);

    verify(employeeId);

    String effectiveMonth =
            month == null || month.isBlank()
                    ? YearMonth.now().toString()
                    : month;

    return mcpClient.call(
            "getAttendanceSummary",
            Map.of(
                    "employeeId", employeeId,
                    "month", effectiveMonth));
  }

  public Map<String, Object> getWorkingHours(
          String requesterEmployeeId,
          String requesterRole,
          String employeeId,
          String date) {

    authorizationService.requireSelfOrPrivileged(
            requesterEmployeeId,
            requesterRole,
            employeeId);

    verify(employeeId);

    String effectiveDate =
            date == null || date.isBlank()
                    ? LocalDate.now().toString()
                    : date;

    return mcpClient.call(
            "getWorkingHours",
            Map.of(
                    "employeeId", Long.valueOf(employeeId),
                    "workDate", effectiveDate));
  }

  public Map<String, Object> getOvertime(
          String requesterEmployeeId,
          String requesterRole,
          String employeeId,
          String month) {

    authorizationService.requireSelfOrPrivileged(
            requesterEmployeeId,
            requesterRole,
            employeeId);

    verify(employeeId);

    String effectiveMonth =
            month == null || month.isBlank()
                    ? YearMonth.now().toString()
                    : month;

    return mcpClient.call(
            "getOvertime",
            Map.of(
                    "employeeId", employeeId,
                    "month", effectiveMonth));
  }

  public Map<String, Object> getTeamAttendance(
          String requesterEmployeeId,
          String requesterRole,
          String date) {

    authorizationService.requirePrivileged(
            requesterEmployeeId,
            requesterRole);

    String effectiveDate =
            date == null || date.isBlank()
                    ? LocalDate.now().toString()
                    : date;

    return mcpClient.call(
            "getTeamAttendance",
            Map.of("date", effectiveDate));
  }

  public Map<String, Object> sendSummaryToPayroll(
          String requesterEmployeeId,
          String requesterRole,
          String employeeId,
          String month) {

    authorizationService.requireSelfOrPrivileged(
            requesterEmployeeId,
            requesterRole,
            employeeId);

    verify(employeeId);

    String effectiveMonth =
            month == null || month.isBlank()
                    ? YearMonth.now().toString()
                    : month;

    Map<String, Object> summary =
            mcpClient.call(
                    "getAttendanceSummary",
                    Map.of(
                            "employeeId", employeeId,
                            "month", effectiveMonth));

    return payrollAgentClient.sendAttendanceSummary(
            employeeId,
            effectiveMonth,
            summary);
  }

  public Map<String, Object> sendTeamAttendanceToManager(
          String requesterEmployeeId,
          String requesterRole,
          String date) {

    authorizationService.requirePrivileged(
            requesterEmployeeId,
            requesterRole);

    String effectiveDate =
            date == null || date.isBlank()
                    ? LocalDate.now().toString()
                    : date;

    Map<String, Object> team =
            mcpClient.call(
                    "getTeamAttendance",
                    Map.of("date", effectiveDate));

    return managerAgentClient.sendTeamAttendance(
            effectiveDate,
            team);
  }

  private void verify(String employeeId) {

    if (!Boolean.TRUE.equals(
            employeeAgentClient
                    .verifyEmployee(employeeId)
                    .get("verified"))) {

      throw new IllegalStateException(
              "Employee could not be verified by Employee Agent.");
    }
  }

  private Object valueOrZero(Map<?, ?> values, String key) {
    Object value = values.get(key);
    return value == null ? 0 : value;
  }
}
