package com.shrija.domain.dto;

public record LeaveRequestDto(
    Long id,
    String employeeCode,
    String leaveType,
    String startDate,
    String endDate,
    String status,
    String rejectionReason) {}
