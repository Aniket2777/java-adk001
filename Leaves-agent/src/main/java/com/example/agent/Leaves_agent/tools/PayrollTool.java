package com.example.agent.Leaves_agent.tools;

import com.example.agent.Leaves_agent.dao.PayrollDao;
import com.example.agent.Leaves_agent.entity.Payslip;
import com.google.adk.tools.Annotations.Schema;
import java.util.Map;
import java.util.Optional;

public class PayrollTool {

  private static final PayrollDao payrollDao = new PayrollDao();

  @Schema(
      description =
          "Get an employee's payslip (basic salary, deductions, net salary) for a given month")
  public static Map<String, Object> getPayslip(
      @Schema(name = "employeeId", description = "Employee ID") String employeeId,
      @Schema(name = "payMonth", description = "Month in YYYY-MM format, e.g. 2026-06")
          String payMonth) {

    Optional<Payslip> payslip = payrollDao.getPayslip(employeeId, payMonth);

    if (payslip.isEmpty()) {
      return Map.of(
          "status", "FAILED",
          "message", "No payslip found for that employee and month");
    }

    Payslip p = payslip.get();
    return Map.of(
        "status", "SUCCESS",
        "employeeId", p.getEmployeeId(),
        "payMonth", p.getPayMonth(),
        "basicSalary", p.getBasicSalary().toString(),
        "deductions", p.getDeductions().toString(),
        "netSalary", p.getNetSalary().toString());
  }
}
