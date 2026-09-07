package com.shrija.manager.tool;

import com.shrija.manager.a2a.AttendanceAgentClient;
import com.shrija.manager.service.AuthorizationService;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class TeamAttendanceCoordinationTool {

  private final AttendanceAgentClient attendanceAgentClient;
  private final AuthorizationService authorizationService;

  public TeamAttendanceCoordinationTool(
      AttendanceAgentClient attendanceAgentClient, AuthorizationService authorizationService) {
    this.attendanceAgentClient = attendanceAgentClient;
    this.authorizationService = authorizationService;
  }

  public String getTeamAttendanceReport(
      String requesterEmployeeId, String requesterRole, String date) {
    authorizationService.requireManagerPrivilege(requesterEmployeeId, requesterRole);
    String effectiveDate = date == null || date.isBlank() ? LocalDate.now().toString() : date;
    return attendanceAgentClient.getTeamAttendanceReport(requesterEmployeeId, effectiveDate);
  }
}
