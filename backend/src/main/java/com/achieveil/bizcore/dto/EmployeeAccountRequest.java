package com.achieveil.bizcore.dto;

import lombok.Data;

@Data
public class EmployeeAccountRequest {
    private String password;
    private String securityQuestion;
    private String securityAnswer;
}
