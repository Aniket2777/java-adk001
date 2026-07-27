package com.shrija.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * One row per employee per leave type. Deliberately not derived from {@code LeaveRequest} counts on
 * the fly - balances are their own record (set by HR/payroll processes outside this agent's scope)
 * so a leave application only ever *decrements against* this, never recalculates it.
 */
@Entity
@Table(
    name = "employee_leave_balance",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"employee_code", "leave_type"})})
public class EmployeeLeaveBalance {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "employee_code", nullable = false, length = 32)
  private String employeeCode;

  @Enumerated(EnumType.STRING)
  @Column(name = "leave_type", nullable = false, length = 20)
  private LeaveType leaveType;

  @Column(name = "total_days", nullable = false)
  private int totalDays;

  @Column(name = "used_days", nullable = false)
  private int usedDays;

  protected EmployeeLeaveBalance() {}

  public EmployeeLeaveBalance(
      String employeeCode, LeaveType leaveType, int totalDays, int usedDays) {
    this.employeeCode = employeeCode;
    this.leaveType = leaveType;
    this.totalDays = totalDays;
    this.usedDays = usedDays;
  }

  public Long getId() {
    return id;
  }

  public String getEmployeeCode() {
    return employeeCode;
  }

  public LeaveType getLeaveType() {
    return leaveType;
  }

  public int getTotalDays() {
    return totalDays;
  }

  public int getUsedDays() {
    return usedDays;
  }

  public int getRemainingDays() {
    return totalDays - usedDays;
  }

  public void addUsedDays(int days) {
    this.usedDays += days;
  }

  /**
   * Gives back days previously decremented by {@link #addUsedDays(int)} - used when a leave request
   * is rejected after the balance was already reserved at application time. Floored at zero
   * defensively; it should never actually go negative if callers only refund what they previously
   * deducted.
   */
  public void refundDays(int days) {
    this.usedDays = Math.max(0, this.usedDays - days);
  }
}
