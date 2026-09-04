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
          "Record a check-out for an employee for the given work date and compute hours worked")
  public Attendance checkOut(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Work date (yyyy-MM-dd)") LocalDate workDate,
      @ToolParam(description = "Check-out time (HH:mm)") LocalTime checkOutTime) {

    Attendance attendance =
        attendanceRepository
            .findByEmployeeIdAndWorkDate(employeeId, workDate)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "No attendance record found for employee "
                            + employeeId
                            + " on "
                            + workDate));

    if (attendance.getCheckIn() == null) {
      throw new IllegalStateException("Employee has no check-in record for " + workDate);
    }

    if (attendance.getCheckOut() != null) {
      throw new IllegalStateException("Employee has already checked out for " + workDate);
    }

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

  @Tool(
          description =
                  "Calculate an employee's working hours and overtime for a specific work date")
  public WorkingHoursResult getWorkingHours(
          @ToolParam(description = "Employee ID") Long employeeId,
          @ToolParam(description = "Work date (yyyy-MM-dd)") LocalDate workDate) {

    Attendance attendance =
            attendanceRepository
                    .findByEmployeeIdAndWorkDate(employeeId, workDate)
                    .orElseThrow(
                            () ->
                                    new IllegalArgumentException(
                                            "No attendance record found for employee "
                                                    + employeeId
                                                    + " on "
                                                    + workDate));

    LocalTime checkIn = attendance.getCheckIn();
    LocalTime checkOut = attendance.getCheckOut();

    if (checkIn == null || checkOut == null) {
      return new WorkingHoursResult(
              employeeId,
              workDate,
              checkIn,
              checkOut,
              0,
              0);
    }

    long workingMinutes =
            Duration.between(checkIn, checkOut).toMinutes();

    long standardWorkingMinutes = 8 * 60;

    long overtimeMinutes =
            Math.max(
                    workingMinutes - standardWorkingMinutes,
                    0);

    return new WorkingHoursResult(
            employeeId,
            workDate,
            checkIn,
            checkOut,
            workingMinutes,
            overtimeMinutes);
  }

  public record WorkingHoursResult(
          Long employeeId,
          LocalDate workDate,
          LocalTime checkIn,
          LocalTime checkOut,
          long workingMinutes,
          long overtimeMinutes) {}

  @Tool(description = "Get total overtime minutes/hours for an employee for a given month")
  public OvertimeSummary getOvertime(
          @ToolParam(description = "Employee ID") Long employeeId,
          @ToolParam(description = "Month (1-12)") int month,
          @ToolParam(description = "Year") int year) {

    LocalDate start = LocalDate.of(year, month, 1);
    LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

    List<Attendance> records =
            attendanceRepository.findByEmployeeIdAndWorkDateBetween(employeeId, start, end);

    long standardWorkingMinutes = 8 * 60;

    long totalOvertimeMinutes = records.stream()
            .filter(a -> a.getCheckIn() != null && a.getCheckOut() != null)
            .mapToLong(a -> {
              long workedMinutes = Duration.between(a.getCheckIn(), a.getCheckOut()).toMinutes();
              return Math.max(workedMinutes - standardWorkingMinutes, 0);
            })
            .sum();

    return new OvertimeSummary(employeeId, month, year, totalOvertimeMinutes, totalOvertimeMinutes / 60.0);
  }

  public record OvertimeSummary(
          Long employeeId,
          int month,
          int year,
          long totalOvertimeMinutes,
          double totalOvertimeHours) {}

  @Tool(description = "Get attendance status for all employees on a given date (used by managers)")
  public List<TeamAttendanceEntry> getTeamAttendance(
          @ToolParam(description = "Date (yyyy-MM-dd)") LocalDate date) {

    List<Attendance> records = attendanceRepository.findByWorkDate(date);

    return records.stream()
            .map(a -> new TeamAttendanceEntry(
                    a.getEmployeeId(),
                    a.getWorkDate(),
                    a.getStatus(),
                    a.getCheckIn(),
                    a.getCheckOut(),
                    a.getHoursWorked()))
            .toList();
  }

  public record TeamAttendanceEntry(
          Long employeeId,
          LocalDate workDate,
          Attendance.AttendanceStatus status,
          LocalTime checkIn,
          LocalTime checkOut,
          double hoursWorked) {}
}
