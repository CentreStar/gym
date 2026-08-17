package com.example.gym_server.dto;

import com.example.gym_server.entity.User;

public class LoginResponse {

    private String token;
    private Long id;
    private String username;
    private String role;
    private String status;

    public LoginResponse(String token, User user) {
        this.token = token;
        this.id = user.getId();
        this.username = user.getUsername();
        this.role = user.getRole();
        this.status = user.getStatus();
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
