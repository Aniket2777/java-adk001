package com.shrija.domain.dto;

public record LeaveBalanceDto(
    String employeeCode, String leaveType, int totalDays, int usedDays, int remainingDays) {}
