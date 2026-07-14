package com.ai.shrija.manager.tools;

import com.ai.shrija.manager.dao.EmployeeDao;
import com.ai.shrija.manager.entity.Employee;

import java.util.Map;

public class AuthenticationTool {

    private static final EmployeeDao employeeDao = new EmployeeDao();

    public static Map<String, Object> authenticate(
            String employeeId,
            String password) {

        Employee employee =
                employeeDao.authenticate(employeeId, password);

        if (employee == null) {

            return Map.of(
                    "authenticated", false,
                    "message", "Invalid Employee ID or Password"
            );
        }

        return Map.of(
                "authenticated", true,
                "employeeId", employee.getEmployeeId(),
                "employeeName", employee.getEmployeeName(),
                "department", employee.getDepartment(),
                "designation", employee.getDesignation(),
                "role", employee.getRole()
        );
    }
}