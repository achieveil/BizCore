package com.achieveil.bizcore.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 部门-经理关联DTO
 */
@Data
public class DepartmentManagerDTO {
    private Long id;
    private Long deptId;
    private String deptName;
    private Long managerId;
    private String managerName;
    private String managerEmpNo;
    private Long assignedBy;
    private String assignedByName;
    private LocalDateTime assignedTime;
    private Integer employeeCount;
}
