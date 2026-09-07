package com.shrija.payroll.exception;

/**
 * Base type for all Payroll Agent application exceptions, mirroring {@code ShrijaAiException} in
 * adk-shrija-v2 so error handling stays consistent across Shrija AI services.
 */
public class ShrijaPayrollException extends RuntimeException {

  public ShrijaPayrollException(String message) {
    super(message);
  }

  public ShrijaPayrollException(String message, Throwable cause) {
    super(message, cause);
  }
}
