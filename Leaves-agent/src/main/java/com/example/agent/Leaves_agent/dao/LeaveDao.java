package com.example.agent.Leaves_agent.dao;

import com.example.agent.Leaves_agent.config.DatabaseConfig;
import com.example.agent.Leaves_agent.entity.LeaveBalance;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LeaveDao {

  private static final Logger LOG = Logger.getLogger(LeaveDao.class.getName());

  public Optional<LeaveBalance> getBalance(String employeeId, String leaveType) {
    String sql =
        """
                SELECT employee_id, leave_type, balance_days
                FROM leave_balance
                WHERE employee_id = ? AND leave_type = ?
                """;

    try (Connection connection = DatabaseConfig.getConnection();
        PreparedStatement ps = connection.prepareStatement(sql)) {

      ps.setString(1, employeeId);
      ps.setString(2, leaveType);

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          LeaveBalance balance = new LeaveBalance();
          balance.setEmployeeId(rs.getString("employee_id"));
          balance.setLeaveType(rs.getString("leave_type"));
          balance.setBalanceDays(rs.getInt("balance_days"));
          return Optional.of(balance);
        }
      }
    } catch (SQLException e) {
      LOG.log(Level.SEVERE, "Failed to look up leave balance for " + employeeId, e);
    }

    return Optional.empty();
  }
}
