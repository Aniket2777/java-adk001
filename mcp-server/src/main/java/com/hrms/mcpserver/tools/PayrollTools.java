package com.hrms.mcpserver.tools;

import com.hrms.mcpserver.domain.SalarySlip;
import com.hrms.mcpserver.repository.SalarySlipRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * Tools backing the Payroll Agent — "Salary generation". Per the responsibility table, Payroll
 * calls Attendance (hours worked) and Leave (unpaid-leave deductions). Since all domains live in
 * this one shared MCP server, those calls are made in-process against {@link AttendanceTools} and
 * {@link LeaveTools} rather than over the wire.
 */
@Component
public class PayrollTools {

  private static final double STANDARD_WORKING_DAYS_PER_MONTH = 26.0;

  private final SalarySlipRepository salarySlipRepository;
  private final AttendanceTools attendanceTools;
  private final LeaveTools leaveTools;

  public PayrollTools(
      SalarySlipRepository salarySlipRepository,
      AttendanceTools attendanceTools,
      LeaveTools leaveTools) {
    this.salarySlipRepository = salarySlipRepository;
    this.attendanceTools = attendanceTools;
    this.leaveTools = leaveTools;
  }

  @Tool(
      description =
          "Generate (or regenerate) a salary slip for an employee for a given month/year, pulling attendance and unpaid-leave data automatically")
  public SalarySlip generateSalarySlip(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Month (1-12)") int month,
      @ToolParam(description = "Year") int year,
      @ToolParam(description = "Basic salary for the month") double basicSalary,
      @ToolParam(description = "Allowances for the month") double allowances,
      @ToolParam(description = "Per-day salary rate, used to compute the unpaid-leave deduction")
          double perDayRate) {

    double unpaidLeaveDays = leaveTools.getUnpaidLeaveDaysForMonth(employeeId, month, year);
    double unpaidLeaveDeduction = unpaidLeaveDays * perDayRate;

    // Pulls attendance so the slip reflects actual days worked; kept as
    // informational context for now (hook point for more advanced pro-rating).
    attendanceTools.getMonthlyAttendanceSummary(employeeId, month, year);

    double deductions = unpaidLeaveDeduction;
    double netSalary = basicSalary + allowances - deductions;

    SalarySlip slip =
        salarySlipRepository
            .findByEmployeeIdAndMonthAndYear(employeeId, month, year)
            .orElseGet(SalarySlip::new);

    slip.setEmployeeId(employeeId);
    slip.setMonth(month);
    slip.setYear(year);
    slip.setBasicSalary(basicSalary);
    slip.setAllowances(allowances);
    slip.setUnpaidLeaveDeduction(unpaidLeaveDeduction);
    slip.setDeductions(deductions);
    slip.setNetSalary(netSalary);
    slip.setStatus(SalarySlip.PayrollStatus.GENERATED);

    return salarySlipRepository.save(slip);
  }

  @Tool(description = "Mark a generated salary slip as paid")
  public SalarySlip markSalarySlipAsPaid(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Month (1-12)") int month,
      @ToolParam(description = "Year") int year) {
    SalarySlip slip =
        salarySlipRepository
            .findByEmployeeIdAndMonthAndYear(employeeId, month, year)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "No salary slip found for employee "
                            + employeeId
                            + " for "
                            + month
                            + "/"
                            + year));
    slip.setStatus(SalarySlip.PayrollStatus.PAID);
    return salarySlipRepository.save(slip);
  }

  @Tool(description = "Get an employee's salary slip for a given month/year")
  public SalarySlip getSalarySlip(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Month (1-12)") int month,
      @ToolParam(description = "Year") int year) {
    return salarySlipRepository
        .findByEmployeeIdAndMonthAndYear(employeeId, month, year)
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "No salary slip found for employee "
                        + employeeId
                        + " for "
                        + month
                        + "/"
                        + year));
  }
}
