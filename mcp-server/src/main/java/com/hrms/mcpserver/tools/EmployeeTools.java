package com.hrms.mcpserver.tools;

import com.hrms.mcpserver.domain.Employee;
import com.hrms.mcpserver.repository.EmployeeRepository;
import java.util.List;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * Tools backing the Employee Agent — "Employee profile, employee queries". These are called by the
 * Orchestrator on behalf of the Employee sub-agent, and internally the Employee Agent calls Leave /
 * Attendance / Payroll tools too.
 */
@Component
public class EmployeeTools {

  private final EmployeeRepository employeeRepository;

  public EmployeeTools(EmployeeRepository employeeRepository) {
    this.employeeRepository = employeeRepository;
  }

  @Tool(description = "Get an employee's full profile by employeeId")
  public Employee getEmployeeProfile(
      @ToolParam(description = "The employee's ID") Long employeeId) {
    return employeeRepository
        .findById(employeeId)
        .orElseThrow(() -> new IllegalArgumentException("No employee found with id " + employeeId));
  }

  @Tool(description = "Look up an employee by their work email address")
  public Employee findEmployeeByEmail(
      @ToolParam(description = "Employee's email address") String email) {
    return employeeRepository
        .findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("No employee found with email " + email));
  }

  @Tool(description = "Create a new employee profile")
  public Employee createEmployee(Employee employee) {
    employee.setStatus(Employee.EmployeeStatus.ACTIVE);
    return employeeRepository.save(employee);
  }

  @Tool(description = "Update an employee's department, designation or manager")
  public Employee updateEmployeeDetails(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "New department, or null to leave unchanged") String department,
      @ToolParam(description = "New designation, or null to leave unchanged") String designation) {
    Employee employee = getEmployeeProfile(employeeId);
    if (department != null) employee.setDepartment(department);
    if (designation != null) employee.setDesignation(designation);
    return employeeRepository.save(employee);
  }

  @Tool(description = "List all employees, optionally filtered by department")
  public List<Employee> listEmployees(
      @ToolParam(description = "Department name, or null for all") String department) {
    List<Employee> all = employeeRepository.findAll();
    if (department == null || department.isBlank()) return all;
    return all.stream().filter(e -> department.equalsIgnoreCase(e.getDepartment())).toList();
  }
}
