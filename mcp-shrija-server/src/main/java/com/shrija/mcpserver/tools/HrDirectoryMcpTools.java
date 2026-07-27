package com.shrija.mcpserver.tools;

import com.shrija.domain.dto.HrEmployeeDto;
import com.shrija.domain.exception.ShrijaAiException;
import com.shrija.domain.service.HrEmployeeService;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * MCP tools for the employee directory: lookup, add, delete, transfer between departments. Every
 * method is a thin adapter over {@link HrEmployeeService} - no business logic here, same "MCP tool
 * as adapter, service as source of truth" pattern used throughout.
 *
 * <p>Known domain failures ({@link ShrijaAiException} and subtypes - not-found, duplicate employee)
 * are caught and returned as a structured result rather than left to propagate as a raw exception
 * across the MCP boundary, so the calling agent gets something it can relay to a user instead of an
 * opaque tool-call failure.
 */
@Component
public class HrDirectoryMcpTools {

  private static final Logger log = LoggerFactory.getLogger(HrDirectoryMcpTools.class);

  private final HrEmployeeService hrEmployeeService;

  public HrDirectoryMcpTools(HrEmployeeService hrEmployeeService) {
    this.hrEmployeeService = hrEmployeeService;
  }

  @Tool(description = "Look up a single employee by their employee code")
  public Map<String, Object> getEmployeeByCode(
      @ToolParam(description = "The employee's unique code, e.g. EMP1024") String employeeCode) {
    try {
      return Map.of("found", true, "employee", hrEmployeeService.getByEmployeeCode(employeeCode));
    } catch (ShrijaAiException ex) {
      log.debug("getEmployeeByCode miss for {}: {}", employeeCode, ex.getMessage());
      return Map.of("found", false, "message", ex.getMessage());
    }
  }

  @Tool(description = "List every employee in a given department")
  public Map<String, Object> listEmployeesByDepartment(
      @ToolParam(description = "Department name, e.g. Engineering") String department) {
    var employees = hrEmployeeService.listByDepartment(department);
    return Map.of("department", department, "count", employees.size(), "employees", employees);
  }

  @Tool(description = "Add a new employee to the directory")
  public Map<String, Object> addEmployee(
      @ToolParam(description = "Unique employee code to assign, e.g. EMP5001") String employeeCode,
      @ToolParam(description = "Full name") String fullName,
      @ToolParam(description = "Work email address") String email,
      @ToolParam(description = "Department name") String department,
      @ToolParam(description = "Job title/designation") String designation) {
    try {
      HrEmployeeDto created =
          hrEmployeeService.addEmployee(employeeCode, fullName, email, department, designation);
      return Map.of("success", true, "employee", created);
    } catch (ShrijaAiException ex) {
      log.debug("addEmployee failed for {}: {}", employeeCode, ex.getMessage());
      return Map.of("success", false, "message", ex.getMessage());
    }
  }

  @Tool(description = "Permanently remove an employee from the directory")
  public Map<String, Object> deleteEmployee(
      @ToolParam(description = "The employee's unique code, e.g. EMP1024") String employeeCode) {
    try {
      hrEmployeeService.deleteEmployee(employeeCode);
      return Map.of("success", true, "employeeCode", employeeCode);
    } catch (ShrijaAiException ex) {
      log.debug("deleteEmployee failed for {}: {}", employeeCode, ex.getMessage());
      return Map.of("success", false, "message", ex.getMessage());
    }
  }

  @Tool(description = "Move an employee to a different department")
  public Map<String, Object> transferEmployee(
      @ToolParam(description = "The employee's unique code, e.g. EMP1024") String employeeCode,
      @ToolParam(description = "The department to transfer them into") String newDepartment) {
    try {
      HrEmployeeDto updated = hrEmployeeService.transferEmployee(employeeCode, newDepartment);
      return Map.of("success", true, "employee", updated);
    } catch (ShrijaAiException ex) {
      log.debug("transferEmployee failed for {}: {}", employeeCode, ex.getMessage());
      return Map.of("success", false, "message", ex.getMessage());
    }
  }
}
