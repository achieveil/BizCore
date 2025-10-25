package com.achieveil.bizcore.dto;

import lombok.Data;

/**
 * 考勤统计数据
 */
@Data
public class AttendanceStatsDTO {
    private Long empId;
    private String empName;
    private Integer year;
    private Integer month;
    private Long attendanceDays;      // 出勤天数
    private Long lateDays;             // 迟到次数
    private Long earlyLeaveDays;       // 早退次数
    private Long absenceDays;          // 缺勤天数
    private Double totalWorkHours;     // 总工作时长
    private Double overtimeHours;      // 加班时长
}
