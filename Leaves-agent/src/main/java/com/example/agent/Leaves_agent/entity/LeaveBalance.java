package com.example.agent.Leaves_agent.entity;

public class LeaveBalance {

  private String employeeId;
  private String leaveType;
  private int balanceDays;

  public String getEmployeeId() {
    return employeeId;
  }

  public void setEmployeeId(String employeeId) {
    this.employeeId = employeeId;
  }

  public String getLeaveType() {
    return leaveType;
  }

  public void setLeaveType(String leaveType) {
    this.leaveType = leaveType;
  }

  public int getBalanceDays() {
    return balanceDays;
  }

  public void setBalanceDays(int balanceDays) {
    this.balanceDays = balanceDays;
  }
}
