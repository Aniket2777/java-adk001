package com.shrija.domain.exception;

public class InvalidLeaveRequestException extends BusinessRuleViolationException {

  public InvalidLeaveRequestException(String message) {
    super(message);
  }
}
