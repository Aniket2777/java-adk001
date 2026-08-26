package com.ai.shrija.leave.agent.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain model representing a single leave application.
 */
public class Leave {

    public enum Status {
        PENDING_APPROVAL,
        APPROVED,
        REJECTED,
        CANCELLED
    }

    public enum Type {
        ANNUAL,
        SICK,
        CASUAL,
        UNPAID,
        MATERNITY,
        PATERNITY
    }

    private String leaveId;
    private String employeeId;
    private Type type;
    private LocalDate startDate;
    private LocalDate endDate;
    private double numberOfDays;
    private String reason;
    private Status status;
    private String approverId;
    private String approverComments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean startsOnCompanyHoliday;

    public Leave() {
        this.leaveId = UUID.randomUUID().toString();
        this.status = Status.PENDING_APPROVAL;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public String getLeaveId() {
        return leaveId;
    }

    public void setLeaveId(String leaveId) {
        this.leaveId = leaveId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public double getNumberOfDays() {
        return numberOfDays;
    }

    public void setNumberOfDays(double numberOfDays) {
        this.numberOfDays = numberOfDays;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public String getApproverId() {
        return approverId;
    }

    public void setApproverId(String approverId) {
        this.approverId = approverId;
    }

    public String getApproverComments() {
        return approverComments;
    }

    public void setApproverComments(String approverComments) {
        this.approverComments = approverComments;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /** True if the HRMS holiday-calendar (via MCP) flagged the start date as a company holiday. */
    public boolean isStartsOnCompanyHoliday() {
        return startsOnCompanyHoliday;
    }

    public void setStartsOnCompanyHoliday(boolean startsOnCompanyHoliday) {
        this.startsOnCompanyHoliday = startsOnCompanyHoliday;
    }
}
