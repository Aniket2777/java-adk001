package com.yourproject.dto;

public class LoginResponse {

    private Long userId;
    private String username;
    private String role;
    private String message;
    private String token; // JWT the frontend/Orchestration Agent will carry on later requests

    public LoginResponse(Long userId, String username, String role, String message, String token) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.message = message;
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public String getusername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }
}
