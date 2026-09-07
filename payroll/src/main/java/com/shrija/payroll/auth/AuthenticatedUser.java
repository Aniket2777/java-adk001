package com.shrija.payroll.auth;

public record AuthenticatedUser(String userId, String role, String employeeCode) {}
