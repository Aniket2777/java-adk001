package com.shrija.domain.exception;

public class EmployeeAlreadyExistsException extends ConflictException {

  public EmployeeAlreadyExistsException(String employeeCode) {
    super("An employee with code '" + employeeCode + "' or the same email already exists");
  }
}
