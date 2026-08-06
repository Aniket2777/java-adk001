package com.example.agent.Leaves_agent.dao;

import com.example.agent.Leaves_agent.config.DatabaseConfig;
import com.example.agent.Leaves_agent.entity.Employee;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmployeeDao {

  private static final Logger LOG = Logger.getLogger(EmployeeDao.class.getName());

  /**
   * Looks up the employee by ID, then compares the supplied password directly against the plaintext
   * value stored in the database.
   *
   * <p>SECURITY NOTE: storing and comparing plaintext passwords is not safe for anything beyond
   * local learning/testing — see README.md.
   */
  public Optional<Employee> authenticate(String employeeId, String plainPassword) {
    Optional<Employee> employee = findByEmployeeId(employeeId);
    if (employee.isEmpty()) {
      return Optional.empty();
    }
    if (!employee.get().getPassword().equals(plainPassword)) {
      return Optional.empty();
    }
    return employee;
  }

  public Optional<Employee> findByEmployeeId(String employeeId) {
    String sql =
        """
                SELECT id, employee_id, employee_name, password,
                       department, designation, role, active, joining_date
                FROM employees
                WHERE employee_id = ?
                """;

    try (Connection connection = DatabaseConfig.getConnection();
        PreparedStatement ps = connection.prepareStatement(sql)) {

      ps.setString(1, employeeId);

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return Optional.of(mapRow(rs));
        }
      }
    } catch (SQLException e) {
      LOG.log(Level.SEVERE, "Failed to look up employee " + employeeId, e);
    }

    return Optional.empty();
  }

  private Employee mapRow(ResultSet rs) throws SQLException {
    Employee employee = new Employee();
    employee.setId(rs.getLong("id"));
    employee.setEmployeeId(rs.getString("employee_id"));
    employee.setEmployeeName(rs.getString("employee_name"));
    employee.setPassword(rs.getString("password"));
    employee.setDepartment(rs.getString("department"));
    employee.setDesignation(rs.getString("designation"));
    employee.setRole(rs.getString("role"));
    employee.setActive(rs.getBoolean("active"));

    Date joiningDate = rs.getDate("joining_date");
    if (joiningDate != null) {
      employee.setJoiningDate(joiningDate.toLocalDate());
    }

    return employee;
  }
}
