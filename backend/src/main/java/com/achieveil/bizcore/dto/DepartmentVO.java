package com.achieveil.bizcore.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门视图对象，包含经理信息和员工统计
 */
@Data
public class DepartmentVO {
    private Long id;
    private String deptName;
    private String deptCode;
    private String description;
    private Long parentId;
    private Integer status;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    // 扩展信息
    private List<ManagerInfo> managers;  // 部门经理列表
    private Integer employeeCount;        // 员工数量

    @Data
    public static class ManagerInfo {
        private Long managerId;
        private String managerName;
        private String managerEmpNo;
    }
}
