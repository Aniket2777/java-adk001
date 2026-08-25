package com.hrms.mcpserver.tools;

import com.hrms.mcpserver.domain.LeaveBalance;
import com.hrms.mcpserver.domain.LeaveRequest;
import com.hrms.mcpserver.repository.LeaveBalanceRepository;
import com.hrms.mcpserver.repository.LeaveRequestRepository;
import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * Tools backing the Leave Agent — "Leave requests and balances". Called by the Manager Agent
 * (approvals), HR Agent (lifecycle checks) and Payroll Agent (unpaid-leave deductions), per the
 * responsibility table.
 */
@Component
public class LeaveTools {

  private final LeaveRequestRepository leaveRequestRepository;
  private final LeaveBalanceRepository leaveBalanceRepository;

  public LeaveTools(
      LeaveRequestRepository leaveRequestRepository,
      LeaveBalanceRepository leaveBalanceRepository) {
    this.leaveRequestRepository = leaveRequestRepository;
    this.leaveBalanceRepository = leaveBalanceRepository;
  }

  @Tool(description = "Submit a new leave request for an employee")
  public LeaveRequest applyForLeave(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Leave type: SICK, CASUAL, EARNED, UNPAID, MATERNITY, PATERNITY")
          LeaveRequest.LeaveType leaveType,
      @ToolParam(description = "Start date (yyyy-MM-dd)") LocalDate startDate,
      @ToolParam(description = "End date (yyyy-MM-dd)") LocalDate endDate,
      @ToolParam(description = "Reason for leave") String reason) {
    LeaveRequest request =
        LeaveRequest.builder()
            .employeeId(employeeId)
            .leaveType(leaveType)
            .startDate(startDate)
            .endDate(endDate)
            .status(LeaveRequest.LeaveStatus.PENDING)
            .reason(reason)
            .build();
    return leaveRequestRepository.save(request);
  }

  @Tool(description = "Approve or reject a pending leave request (used by the Manager Agent)")
  public LeaveRequest decideOnLeaveRequest(
      @ToolParam(description = "Leave request ID") Long leaveRequestId,
      @ToolParam(description = "true to approve, false to reject") boolean approve,
      @ToolParam(description = "Manager employee ID making the decision")
          String managerEmployeeId) {
    LeaveRequest request =
        leaveRequestRepository
            .findById(leaveRequestId)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "No leave request found with id " + leaveRequestId));
    request.setStatus(
        approve ? LeaveRequest.LeaveStatus.APPROVED : LeaveRequest.LeaveStatus.REJECTED);
    request.setApprovedByManagerId(managerEmployeeId);
    LeaveRequest saved = leaveRequestRepository.save(request);
    if (approve) {
      applyLeaveToBalance(request);
    }
    return saved;
  }

  private void applyLeaveToBalance(LeaveRequest request) {
    int year = request.getStartDate().getYear();
    double days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
    LeaveBalance balance =
        leaveBalanceRepository
            .findByEmployeeIdAndLeaveTypeAndYear(
                request.getEmployeeId(), request.getLeaveType(), year)
            .orElseGet(
                () ->
                    LeaveBalance.builder()
                        .employeeId(request.getEmployeeId())
                        .leaveType(request.getLeaveType())
                        .year(year)
                        .totalDays(0)
                        .usedDays(0)
                        .remainingDays(0)
                        .build());
    balance.setUsedDays(balance.getUsedDays() + days);
    balance.setRemainingDays(balance.getTotalDays() - balance.getUsedDays());
    leaveBalanceRepository.save(balance);
  }

  @Tool(description = "Get all leave requests for an employee, optionally filtered by status")
  public List<LeaveRequest> getLeaveRequests(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(
              description =
                  "Status filter: PENDING, APPROVED, REJECTED, CANCELLED, or null for all")
          LeaveRequest.LeaveStatus status) {
    if (status == null) return leaveRequestRepository.findByEmployeeId(employeeId);
    return leaveRequestRepository.findByEmployeeIdAndStatus(employeeId, status);
  }

  @Tool(
      description = "Get an employee's leave balances for a given year (defaults to current year)")
  public List<LeaveBalance> getLeaveBalances(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Year, or null for current year") Integer year) {
    int y = (year != null) ? year : Year.now().getValue();
    return leaveBalanceRepository.findByEmployeeIdAndYear(employeeId, y);
  }

  @Tool(
      description =
          "Get total unpaid-leave days taken by an employee in a given month/year (used by the Payroll Agent for salary deductions)")
  public double getUnpaidLeaveDaysForMonth(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Month (1-12)") int month,
      @ToolParam(description = "Year") int year) {
    return leaveRequestRepository
        .findByEmployeeIdAndStatus(employeeId, LeaveRequest.LeaveStatus.APPROVED)
        .stream()
        .filter(r -> r.getLeaveType() == LeaveRequest.LeaveType.UNPAID)
        .filter(r -> overlapsMonth(r, month, year))
        .mapToDouble(r -> ChronoUnit.DAYS.between(r.getStartDate(), r.getEndDate()) + 1)
        .sum();
  }

  private boolean overlapsMonth(LeaveRequest r, int month, int year) {
    LocalDate monthStart = LocalDate.of(year, month, 1);
    LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
    return !r.getStartDate().isAfter(monthEnd) && !r.getEndDate().isBefore(monthStart);
  }
}
