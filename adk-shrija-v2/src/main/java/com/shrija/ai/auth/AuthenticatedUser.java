package com.shrija.ai.auth;

public record AuthenticatedUser(Long userId, String username, String role, String employeeCode) {}
