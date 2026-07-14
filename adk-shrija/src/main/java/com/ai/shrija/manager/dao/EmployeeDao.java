package com.ai.shrija.manager.dao;


import com.ai.shrija.manager.service.DatabaseConnection;
import com.ai.shrija.manager.entity.Employee;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class EmployeeDao {

    public Employee authenticate(String employeeId, String password) {

        String sql = """
                SELECT *
                FROM employees
                WHERE employee_id = ?
                AND password = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {

            ps.setString(1, employeeId);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                Employee employee = new Employee();

                employee.setId(rs.getLong("id"));
                employee.setEmployeeId(rs.getString("employee_id"));
                employee.setEmployeeName(rs.getString("employee_name"));
                employee.setDepartment(rs.getString("department"));
                employee.setDesignation(rs.getString("designation"));
                employee.setRole(rs.getString("role"));


                return employee;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    public Employee getEmployee(String employeeId) {

        String sql = "SELECT * FROM employees WHERE employee_id = ?";

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {

            ps.setString(1, employeeId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                Employee employee = new Employee();

                employee.setId(rs.getLong("id"));
                employee.setEmployeeId(rs.getString("employee_id"));
                employee.setEmployeeName(rs.getString("employee_name"));
                employee.setPassword(rs.getString("password"));
                employee.setDepartment(rs.getString("department"));
                employee.setDesignation(rs.getString("designation"));
                employee.setRole(rs.getString("role"));

                Date joiningDate = rs.getDate("joining_date");
                if (joiningDate != null) {
                    employee.setJoiningDate(joiningDate.toLocalDate());
                }

                employee.setActive(rs.getBoolean("active"));

                return employee;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}