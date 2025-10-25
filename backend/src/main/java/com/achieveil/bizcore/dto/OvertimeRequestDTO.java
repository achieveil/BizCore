package com.achieveil.bizcore.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 加班申请DTO
 */
@Data
public class OvertimeRequestDTO {
    private Long id;
    private Long empId;
    private String empName;
    private String empNo;
    private Long deptId;
    private String deptName;
    private String position;
    private LocalDate overtimeDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Double hours;
    private String overtimeType;
    private String overtimeTypeName;  // 加班类型中文名
    private String reason;
    private String status;
    private String statusName;        // 状态中文名
    private Long approverId;
    private String approverName;
    private String approvalComment;
    private LocalDateTime approvalTime;
    private Boolean compensated;
    private LocalDateTime createdTime;
}
