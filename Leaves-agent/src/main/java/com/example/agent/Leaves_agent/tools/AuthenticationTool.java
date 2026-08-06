package com.example.agent.Leaves_agent.tools;

import com.example.agent.Leaves_agent.dao.EmployeeDao;
import com.example.agent.Leaves_agent.entity.Employee;
import com.google.adk.tools.Annotations.Schema;
import java.util.Map;
import java.util.Optional;

/**
 * The ManagerAgent's only tool. It authenticates the caller once, up front, and returns
 * department/role info the agent's instructions use to decide which sub-agent should handle the
 * request.
 */
public class AuthenticationTool {

  private static final EmployeeDao employeeDao = new EmployeeDao();

  @Schema(
      description = "Authenticate a user by employee ID and password before handling any request")
  public static Map<String, Object> authenticate(
      @Schema(name = "employeeId", description = "Employee ID") String employeeId,
      @Schema(name = "password", description = "Employee password") String password) {

    Optional<Employee> employee = employeeDao.authenticate(employeeId, password);

    if (employee.isEmpty()) {
      return Map.of(
          "status", "FAILED",
          "message", "Invalid employee ID or password");
    }

    Employee e = employee.get();
    return Map.of(
        "status", "SUCCESS",
        "employeeId", e.getEmployeeId(),
        "employeeName", e.getEmployeeName(),
        "department", e.getDepartment(),
        "designation", e.getDesignation(),
        "role", e.getRole());
  }
}
