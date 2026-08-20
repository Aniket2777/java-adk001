package com.shrija.domain.dto;
public record EmployeeDto(Long id,String employeeCode,String firstName,String lastName,String email,
                          String phone,String department,String designation,String managerName,
                          String joiningDate,String employmentStatus) {}
