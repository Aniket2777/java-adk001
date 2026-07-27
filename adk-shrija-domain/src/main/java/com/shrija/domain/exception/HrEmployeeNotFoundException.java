package com.shrija.domain.exception;

public class HrEmployeeNotFoundException extends ResourceNotFoundException {

  public HrEmployeeNotFoundException(String employeeCode) {
    super("No employee found with code '" + employeeCode + "'");
  }
}
