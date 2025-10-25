package com.achieveil.bizcore.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 考勤数据传输对象
 */
@Data
public class AttendanceDTO {
    private Long id;
    private Long empId;
    private String empName;
    private String empNo;
    private Long deptId;
    private String deptName;
    private String position;  // 职位
    private LocalDate attendanceDate;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private String status;
    private Boolean isLate;
    private Integer lateMinutes;
    private Boolean isEarlyLeave;
    private Integer earlyLeaveMinutes;
    private Double workHours;
    private String remark;
    private String role;
    private Boolean manager;
}
