package com.shrija.attendance.dto;

import jakarta.validation.constraints.NotBlank;

public record AttendanceChatRequest(
    @NotBlank String userId, String sessionId, @NotBlank String message) {}
