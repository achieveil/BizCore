package com.achieveil.bizcore.dto;

import lombok.Data;

@Data
public class DepartmentManagerAssignRequest {
    private Long managerId;
    private Long assignedBy;
}
