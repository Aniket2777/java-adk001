package com.shrija.domain.exception;

public class LeaveRequestNotPendingException extends BusinessRuleViolationException {

  public LeaveRequestNotPendingException(Long requestId, String currentStatus) {
    super(
        "Leave request "
            + requestId
            + " is already "
            + currentStatus
            + " and cannot be acted on again");
  }
}
