package com.shrija.domain.dto;

/**
 * Read-only view of an HR employee record, returned by the HR service and exposed to the agent's
 * tools. Never expose {@code HrEmployee} the JPA entity itself past the service layer - this DTO is
 * the boundary.
 */
public record HrEmployeeDto(
    String employeeCode,
    String fullName,
    String email,
    String department,
    String designation,
    String employmentStatus) {}
