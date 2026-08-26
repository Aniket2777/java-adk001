package com.ai.shrija.leave.agent.dto;

import com.ai.shrija.leave.agent.model.Leave;

public class ApplyLeaveResponse {

    private String leaveId;
    private Leave.Status status;
    private double numberOfDays;
    private String message;
    private boolean startsOnCompanyHoliday;

    public ApplyLeaveResponse() {
    }

    public ApplyLeaveResponse(String leaveId, Leave.Status status, double numberOfDays, String message,
                               boolean startsOnCompanyHoliday) {
        this.leaveId = leaveId;
        this.status = status;
        this.numberOfDays = numberOfDays;
        this.message = message;
        this.startsOnCompanyHoliday = startsOnCompanyHoliday;
    }

    public static ApplyLeaveResponse from(Leave leave, String message) {
        return new ApplyLeaveResponse(leave.getLeaveId(), leave.getStatus(), leave.getNumberOfDays(), message,
                leave.isStartsOnCompanyHoliday());
    }

    public String getLeaveId() {
        return leaveId;
    }

    public void setLeaveId(String leaveId) {
        this.leaveId = leaveId;
    }

    public Leave.Status getStatus() {
        return status;
    }

    public void setStatus(Leave.Status status) {
        this.status = status;
    }

    public double getNumberOfDays() {
        return numberOfDays;
    }

    public void setNumberOfDays(double numberOfDays) {
        this.numberOfDays = numberOfDays;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isStartsOnCompanyHoliday() {
        return startsOnCompanyHoliday;
    }

    public void setStartsOnCompanyHoliday(boolean startsOnCompanyHoliday) {
        this.startsOnCompanyHoliday = startsOnCompanyHoliday;
    }
}
