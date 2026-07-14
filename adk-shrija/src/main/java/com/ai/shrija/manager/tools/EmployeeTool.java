package com.ai.shrija.manager.tools;



import com.ai.shrija.manager.dao.EmployeeDao;
import com.ai.shrija.manager.entity.Employee;
import com.google.adk.tools.Annotations.Schema;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployeeTool {

    private static final EmployeeDao employeeDao = new EmployeeDao();

    /**
     * Authenticate Employee
     */
    public static Map<String, Object> authenticateEmployee(

            @Schema(
                    name = "employeeId",
                    description = "Employee ID")
            String employeeId,

            @Schema(
                    name = "password",
                    description = "Employee Password")
            String password) {

        Employee employee = employeeDao.authenticate(employeeId, password);

        if (employee == null) {

            return Map.of(
                    "status", "FAILED",
                    "message", "Invalid Employee ID or Password"
            );
        }

        return Map.of(
                "status", "SUCCESS",
                "employeeId", employee.getEmployeeId(),
                "employeeName", employee.getEmployeeName(),
                "department", employee.getDepartment(),
                "designation", employee.getDesignation(),
                "role", employee.getRole()
        );
    }

    /**
     * Get Employee Details
     */
    public static Map<String, Object> getEmployee(

            @Schema(
                    name = "employeeId",
                    description = "Employee ID")
            String employeeId) {

        Employee employee = employeeDao.getEmployee(employeeId);

        if (employee == null) {

            return Map.of(
                    "status", "FAILED",
                    "message", "Employee Not Found"
            );
        }

        return Map.of(
                "status", "SUCCESS",
                "employeeId", employee.getEmployeeId(),
                "employeeName", employee.getEmployeeName(),
                "department", employee.getDepartment(),
                "designation", employee.getDesignation(),
                "role", employee.getRole(),

                "joiningDate", employee.getJoiningDate().toString()

        );
    }



}
