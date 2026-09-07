package com.shrija.payroll.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
    @NotBlank String userId,
    String sessionId,
    @NotBlank String role,
    String employeeCode,
    @NotBlank String message) {}
