package com.achieveil.bizcore.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

/**
 * 部门考勤规则DTO
 */
@Data
public class DepartmentAttendanceRuleDTO {
    private Long id;

    @NotNull(message = "部门ID不能为空")
    private Long deptId;

    private String deptName;

    @NotNull(message = "上班时间不能为空")
    private LocalTime checkInTime;

    @NotNull(message = "下班时间不能为空")
    private LocalTime checkOutTime;

    private Integer lateGraceMinutes = 10;

    private Integer earlyLeaveGraceMinutes = 10;

    private Boolean requireCheckOut = true;

    private Boolean flexibleWorkTime = false;

    private LocalTime flexibleCheckInStart;

    private LocalTime flexibleCheckInEnd;

    private Double standardWorkHours = 8.0;

    private String remark;
}
