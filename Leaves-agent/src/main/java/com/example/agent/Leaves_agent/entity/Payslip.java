package com.example.agent.Leaves_agent.entity;

import java.math.BigDecimal;

public class Payslip {

  private String employeeId;
  private String payMonth; // e.g. "2026-06"
  private BigDecimal basicSalary;
  private BigDecimal deductions;
  private BigDecimal netSalary;

  public String getEmployeeId() {
    return employeeId;
  }

  public void setEmployeeId(String employeeId) {
    this.employeeId = employeeId;
  }

  public String getPayMonth() {
    return payMonth;
  }

  public void setPayMonth(String payMonth) {
    this.payMonth = payMonth;
  }

  public BigDecimal getBasicSalary() {
    return basicSalary;
  }

  public void setBasicSalary(BigDecimal basicSalary) {
    this.basicSalary = basicSalary;
  }

  public BigDecimal getDeductions() {
    return deductions;
  }

  public void setDeductions(BigDecimal deductions) {
    this.deductions = deductions;
  }

  public BigDecimal getNetSalary() {
    return netSalary;
  }

  public void setNetSalary(BigDecimal netSalary) {
    this.netSalary = netSalary;
  }
}
