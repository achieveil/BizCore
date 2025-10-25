package com.achieveil.bizcore.dto;

import com.achieveil.bizcore.entity.Employee;
import lombok.Data;

@Data
public class CreateEmployeeWithAccountRequest {
    private Employee employee;
    private String password;
    private String securityQuestion;
    private String securityAnswer;
}
