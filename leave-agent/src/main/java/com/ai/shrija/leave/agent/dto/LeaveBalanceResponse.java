package com.ai.shrija.leave.agent.dto;

import java.util.Map;

/**
 * Response returned by LeaveBalanceTool / LeaveService#getBalance.
 * balances maps Leave.Type name -> remaining days.
 */
public class LeaveBalanceResponse {

    private String employeeId;
    private Map<String, Double> balances;

    public LeaveBalanceResponse() {
    }

    public LeaveBalanceResponse(String employeeId, Map<String, Double> balances) {
        this.employeeId = employeeId;
        this.balances = balances;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public Map<String, Double> getBalances() {
        return balances;
    }

    public void setBalances(Map<String, Double> balances) {
        this.balances = balances;
    }
}
