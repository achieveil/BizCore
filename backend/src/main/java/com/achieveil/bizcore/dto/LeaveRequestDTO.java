package com.achieveil.bizcore.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 请假申请DTO
 */
@Data
public class LeaveRequestDTO {
    private Long id;
    private Long empId;
    private String empName;
    private String empNo;
    private Long deptId;
    private String deptName;
    private String position;
    private String leaveType;
    private String leaveTypeName;  // 请假类型中文名
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer days;
    private String reason;
    private String status;
    private String statusName;     // 状态中文名
    private Long approverId;
    private String approverName;
    private String approvalComment;
    private LocalDateTime approvalTime;
    private String attachmentUrl;
    private LocalDateTime createdTime;
}
