package com.achieveil.bizcore.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String refreshToken;
    private String sessionId;
    private long expiresIn;
    private long refreshTokenExpiresIn;
    private String username;
    private String realName;
    private String role;
    private Long userId;
    private Long empId; // Employee ID (only for EMPLOYEE and MANAGER roles)
    private Long deptId; // Department ID (only for EMPLOYEE and MANAGER roles)
    private String email;
    private String phone;
    private LocalDateTime createdTime;
}
