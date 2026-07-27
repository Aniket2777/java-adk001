package com.shrija.domain.exception;

public class DocumentRequestNotActionableException extends BusinessRuleViolationException {

  public DocumentRequestNotActionableException(
      Long requestId, String currentStatus, String expectedStatus) {
    super(
        "Document request "
            + requestId
            + " is "
            + currentStatus
            + ", expected "
            + expectedStatus
            + " for this action");
  }
}
