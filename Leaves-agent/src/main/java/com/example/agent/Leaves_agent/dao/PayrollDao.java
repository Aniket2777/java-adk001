package com.example.agent.Leaves_agent.dao;

import com.example.agent.Leaves_agent.config.DatabaseConfig;
import com.example.agent.Leaves_agent.entity.Payslip;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Deliberately read-only: only a getter is exposed. Editing payroll data should never be reachable
 * from a chat agent — that stays in a proper HR system with its own auth/approval workflow.
 */
public class PayrollDao {

  private static final Logger LOG = Logger.getLogger(PayrollDao.class.getName());

  public Optional<Payslip> getPayslip(String employeeId, String payMonth) {
    String sql =
        """
                SELECT employee_id, pay_month, basic_salary, deductions, net_salary
                FROM payslips
                WHERE employee_id = ? AND pay_month = ?
                """;

    try (Connection connection = DatabaseConfig.getConnection();
        PreparedStatement ps = connection.prepareStatement(sql)) {

      ps.setString(1, employeeId);
      ps.setString(2, payMonth);

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          Payslip payslip = new Payslip();
          payslip.setEmployeeId(rs.getString("employee_id"));
          payslip.setPayMonth(rs.getString("pay_month"));
          payslip.setBasicSalary(rs.getBigDecimal("basic_salary"));
          payslip.setDeductions(rs.getBigDecimal("deductions"));
          payslip.setNetSalary(rs.getBigDecimal("net_salary"));
          return Optional.of(payslip);
        }
      }
    } catch (SQLException e) {
      LOG.log(Level.SEVERE, "Failed to look up payslip for " + employeeId, e);
    }

    return Optional.empty();
  }
}
