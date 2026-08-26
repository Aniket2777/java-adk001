package com.ai.shrija.leave.agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class LeaveApprovalRequest {

    @NotBlank
    private String leaveId;

    @NotBlank
    private String approverId;

    @NotNull
    private boolean approved;

    private String comments;

    public LeaveApprovalRequest() {
    }

    public LeaveApprovalRequest(String leaveId, String approverId, boolean approved, String comments) {
        this.leaveId = leaveId;
        this.approverId = approverId;
        this.approved = approved;
        this.comments = comments;
    }

    public String getLeaveId() {
        return leaveId;
    }

    public void setLeaveId(String leaveId) {
        this.leaveId = leaveId;
    }

    public String getApproverId() {
        return approverId;
    }

    public void setApproverId(String approverId) {
        this.approverId = approverId;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
