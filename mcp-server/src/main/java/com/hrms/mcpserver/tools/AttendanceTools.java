package com.hrms.mcpserver.tools;

import com.hrms.mcpserver.domain.Attendance;
import com.hrms.mcpserver.repository.AttendanceRepository;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * Tools backing the Attendance Agent — "Attendance and working hours". Consumed by the Payroll
 * Agent when generating salaries, per the responsibility table.
 */
@Component
public class AttendanceTools {

  private final AttendanceRepository attendanceRepository;

  public AttendanceTools(AttendanceRepository attendanceRepository) {
    this.attendanceRepository = attendanceRepository;
  }

  @Tool(description = "Record a check-in for an employee on a given work date")
  public Attendance checkIn(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Work date (yyyy-MM-dd)") LocalDate workDate,
      @ToolParam(description = "Check-in time (HH:mm)") LocalTime checkInTime) {
    Attendance attendance =
        Attendance.builder()
            .employeeId(employeeId)
            .workDate(workDate)
            .checkIn(checkInTime)
            .status(Attendance.AttendanceStatus.PRESENT)
            .build();
    return attendanceRepository.save(attendance);
  }

  @Tool(
      description =
          "Record a check-out for an employee's existing attendance record and compute hours worked")
  public Attendance checkOut(
      @ToolParam(description = "Attendance record ID") Long attendanceId,
      @ToolParam(description = "Check-out time (HH:mm)") LocalTime checkOutTime) {
    Attendance attendance =
        attendanceRepository
            .findById(attendanceId)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "No attendance record found with id " + attendanceId));
    attendance.setCheckOut(checkOutTime);
    double hours = Duration.between(attendance.getCheckIn(), checkOutTime).toMinutes() / 60.0;
    attendance.setHoursWorked(Math.max(hours, 0));
    attendance.setStatus(
        hours < 4 ? Attendance.AttendanceStatus.HALF_DAY : Attendance.AttendanceStatus.PRESENT);
    return attendanceRepository.save(attendance);
  }

  @Tool(
      description =
          "Mark an employee absent, on leave, on a holiday, or on a week-off for a given date")
  public Attendance markStatus(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Work date (yyyy-MM-dd)") LocalDate workDate,
      @ToolParam(description = "Status: ABSENT, ON_LEAVE, HOLIDAY, WEEK_OFF")
          Attendance.AttendanceStatus status) {
    Attendance attendance =
        Attendance.builder()
            .employeeId(employeeId)
            .workDate(workDate)
            .status(status)
            .hoursWorked(0)
            .build();
    return attendanceRepository.save(attendance);
  }

  @Tool(description = "Get an employee's attendance records between two dates (inclusive)")
  public List<Attendance> getAttendanceForRange(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Start date (yyyy-MM-dd)") LocalDate start,
      @ToolParam(description = "End date (yyyy-MM-dd)") LocalDate end) {
    return attendanceRepository.findByEmployeeIdAndWorkDateBetween(employeeId, start, end);
  }

  @Tool(
      description =
          "Get total hours worked and present-days count for an employee in a given month (used by the Payroll Agent)")
  public MonthlyAttendanceSummary getMonthlyAttendanceSummary(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Month (1-12)") int month,
      @ToolParam(description = "Year") int year) {
    LocalDate start = LocalDate.of(year, month, 1);
    LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
    List<Attendance> records =
        attendanceRepository.findByEmployeeIdAndWorkDateBetween(employeeId, start, end);

    double totalHours = records.stream().mapToDouble(Attendance::getHoursWorked).sum();
    long presentDays =
        records.stream()
            .filter(
                a ->
                    a.getStatus() == Attendance.AttendanceStatus.PRESENT
                        || a.getStatus() == Attendance.AttendanceStatus.HALF_DAY)
            .count();
    long absentDays =
        records.stream().filter(a -> a.getStatus() == Attendance.AttendanceStatus.ABSENT).count();

    return new MonthlyAttendanceSummary(
        employeeId, month, year, totalHours, presentDays, absentDays);
  }

  public record MonthlyAttendanceSummary(
      Long employeeId,
      int month,
      int year,
      double totalHoursWorked,
      long presentDays,
      long absentDays) {}
}
