package com.shrija.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "leave_request")
public class LeaveRequest {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "employee_code", nullable = false, length = 32)
  private String employeeCode;

  @Enumerated(EnumType.STRING)
  @Column(name = "leave_type", nullable = false, length = 20)
  private LeaveType leaveType;

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(name = "end_date", nullable = false)
  private LocalDate endDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Status status;

  @Column(name = "applied_at", nullable = false)
  private Instant appliedAt;

  @Column(name = "rejection_reason", length = 500)
  private String rejectionReason;

  protected LeaveRequest() {}

  public LeaveRequest(
      String employeeCode,
      LeaveType leaveType,
      LocalDate startDate,
      LocalDate endDate,
      Status status,
      Instant appliedAt) {
    this.employeeCode = employeeCode;
    this.leaveType = leaveType;
    this.startDate = startDate;
    this.endDate = endDate;
    this.status = status;
    this.appliedAt = appliedAt;
  }

  /**
   * State transitions are exposed as intent-revealing methods, not a raw status setter - whether a
   * transition is *allowed* (e.g. only from PENDING) is the service's business rule to enforce and
   * reject with a proper {@code LeaveRequestNotPendingException}; these methods only perform the
   * mutation once the service has decided it's valid.
   */
  public void markApproved() {
    this.status = Status.APPROVED;
  }

  public void markRejected(String reason) {
    this.status = Status.REJECTED;
    this.rejectionReason = reason;
  }

  public String getRejectionReason() {
    return rejectionReason;
  }

  /** Number of calendar days requested, inclusive of both endpoints. */
  public long requestedDayCount() {
    return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
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

  public LocalDate getStartDate() {
    return startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public Status getStatus() {
    return status;
  }

  public Instant getAppliedAt() {
    return appliedAt;
  }

  public enum Status {
    PENDING,
    APPROVED,
    REJECTED
  }
}
