package com.example.agent.Leaves_agent.tools;

import com.example.agent.Leaves_agent.dao.EmployeeDao;
import com.example.agent.Leaves_agent.entity.Employee;
import com.google.adk.tools.Annotations.Schema;
import java.util.Map;
import java.util.Optional;

/**
 * Tools for the EmployeeAgent. Authentication already happened at the ManagerAgent level, so this
 * only exposes record lookups — no duplicate auth logic here (that was one of the bugs in the
 * reference project).
 */
public class EmployeeTool {

  private static final EmployeeDao employeeDao = new EmployeeDao();

  @Schema(description = "Get employee details by employee ID")
  public static Map<String, Object> getEmployee(
      @Schema(name = "employeeId", description = "Employee ID") String employeeId) {

    Optional<Employee> employee = employeeDao.findByEmployeeId(employeeId);

    if (employee.isEmpty()) {
      return Map.of(
          "status", "FAILED",
          "message", "Employee not found");
    }

    Employee e = employee.get();
    return Map.of(
        "status", "SUCCESS",
        "employeeId", e.getEmployeeId(),
        "employeeName", e.getEmployeeName(),
        "department", e.getDepartment(),
        "designation", e.getDesignation(),
        "role", e.getRole(),
        "joiningDate", String.valueOf(e.getJoiningDate()));
  }
}
