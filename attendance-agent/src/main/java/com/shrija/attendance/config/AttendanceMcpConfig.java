package com.shrija.attendance.config;

import com.google.adk.JsonBaseModel;
import com.google.adk.tools.mcp.McpToolset;
import com.google.adk.tools.mcp.SseServerParameters;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AttendanceMcpConfig {

  private static final List<String> ATTENDANCE_TOOLS =
      List.of(
          "checkIn",
          "checkOut",
          "getTodayAttendance",
          "getAttendanceHistory",
          "getMonthlyAttendance",
          "getAttendanceSummary",
          "getTeamAttendance",
          "getOvertime");

  @Bean
  public McpToolset attendanceMcpToolset(AttendanceAiProperties properties) {
    return new McpToolset(
        SseServerParameters.builder().url(properties.mcpServerUrl()).build(),
        JsonBaseModel.getMapper(),
        ATTENDANCE_TOOLS);
  }
}
