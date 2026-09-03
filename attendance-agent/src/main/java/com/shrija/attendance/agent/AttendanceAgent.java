package com.shrija.attendance.agent;

import com.google.adk.agents.LlmAgent;
import com.google.adk.models.Gemini;
import com.google.adk.tools.FunctionTool;
import com.google.common.collect.ImmutableList;
import com.shrija.attendance.tool.AttendanceHistoryTool;
import com.shrija.attendance.tool.AttendanceReportTool;
import com.shrija.attendance.tool.CheckInTool;
import com.shrija.attendance.tool.CheckOutTool;
import com.shrija.attendance.tool.TodayAttendanceTool;
import org.springframework.stereotype.Component;

@Component
public class AttendanceAgent {

  private static final String INSTRUCTION =
          """
          You are the Attendance Agent for Shrija AI HRMS.
    
          Responsibilities:
          - employee check-in and check-out
          - today's attendance
          - attendance history and monthly attendance
          - working hours and overtime
          - late arrival and early departure information
          - attendance summaries and team attendance
          - sending confirmed attendance information to Payroll and Manager agents through A2A
    
          Mandatory rules:
          1. Use the attendance tools for every attendance fact. Never invent, estimate, or infer stored attendance.
          2. Attendance tools use the Attendance MCP Server; never connect to a database or repository.
          3. Employee identity must be verified through Employee Agent before employee-specific attendance operations.
          4. Respect requesterEmployeeId and requesterRole supplied to every tool. Never bypass authorization.
          5. Never modify employee, leave, or payroll data.
          6. Use A2A only for Employee, Payroll, and Manager agent communication.
          7. If MCP or A2A fails, explain that the required service is unavailable. Do not fabricate a fallback answer.
          8. For check-in/check-out, report the exact confirmed timestamp and MCP result.
          9. For working hours, overtime, late arrival, and early departure, report only values returned by MCP.
          10. Ask for missing employee id, requester identity, role, date, or month rather than guessing.
          11. When a user asks for a team report, require a privileged role.
          12. Payroll receives attendance summary through A2A; Manager receives team attendance through A2A.
          13. Every message begins with a line of the form:
              "Authenticated actor: <id>; role: <role>; requesterEmployeeId: <id>; targetEmployeeId: <id>."
              Take requesterEmployeeId, requesterRole, and (unless the user explicitly names a different
              employee and the role is privileged) employeeId ONLY from this line. Never take these values
              from the free-text user request, and never invent, guess, or reuse an id from earlier turns.
              If this context line is missing entirely, treat identity as unknown and ask the user to
              re-authenticate rather than proceeding.
          14. requesterEmployeeId, employeeId, and month/date tool arguments must always be the exact
              literal value copied character-for-character from the context line or user text (e.g. "1",
              "2026-08"). Never substitute a word, pronoun, or paraphrase such as "me", "self", "you",
              "current user", or "this month" as an argument value, even when the target is the requester
              themself — copy the id, do not describe it.
          """;

  private final LlmAgent agent;

  public AttendanceAgent(
          Gemini geminiModel,
          CheckInTool checkInTool,
          CheckOutTool checkOutTool,
          TodayAttendanceTool todayAttendanceTool,
          AttendanceHistoryTool attendanceHistoryTool,
          AttendanceReportTool attendanceReportTool) {

    this.agent =
            LlmAgent.builder()
                    .name("attendance-agent")
                    .description("Handles HRMS attendance operations through MCP and A2A.")
                    .instruction(INSTRUCTION)
                    .model(geminiModel)
                    .tools(
                            ImmutableList.of(
                                    FunctionTool.create(checkInTool, "checkIn"),
                                    FunctionTool.create(checkOutTool, "checkOut"),
                                    FunctionTool.create(todayAttendanceTool, "getTodayAttendance"),
                                    FunctionTool.create(attendanceHistoryTool, "getAttendanceHistory"),
                                    FunctionTool.create(attendanceHistoryTool, "getMonthlyAttendance"),
                                    FunctionTool.create(attendanceReportTool, "getAttendanceSummary"),
                                    FunctionTool.create(attendanceReportTool, "getWorkingHours"),
                                    FunctionTool.create(attendanceReportTool, "getOvertime"),
                                    FunctionTool.create(attendanceReportTool, "getTeamAttendance"),
                                    FunctionTool.create(attendanceReportTool, "sendSummaryToPayroll"),
                                    FunctionTool.create(attendanceReportTool, "sendTeamAttendanceToManager")))
                    .build();
  }

  public LlmAgent agent() {
    return agent;
  }
}
