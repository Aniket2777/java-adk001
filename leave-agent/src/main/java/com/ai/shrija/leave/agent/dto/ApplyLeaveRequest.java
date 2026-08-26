package com.ai.shrija.leave.agent.dto;

import com.ai.shrija.leave.agent.model.Leave;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Request payload for applying for a new leave.
 * This is also the shape the ApplyLeaveTool exposes to the agent so the LLM
 * can populate structured arguments when it decides to call the tool.
 */
public class ApplyLeaveRequest {

    @NotBlank
    private String employeeId;

    @NotNull
    private Leave.Type type;

    @NotNull
    @FutureOrPresent
    private LocalDate startDate;

    @NotNull
    @FutureOrPresent
    private LocalDate endDate;

    private String reason;

    public ApplyLeaveRequest() {
    }

    public ApplyLeaveRequest(String employeeId, Leave.Type type, LocalDate startDate,
                              LocalDate endDate, String reason) {
        this.employeeId = employeeId;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public Leave.Type getType() {
        return type;
    }

    public void setType(Leave.Type type) {
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
