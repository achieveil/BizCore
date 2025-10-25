package com.achieveil.bizcore.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 创建加班申请请求
 */
@Data
public class OvertimeRequestCreateDTO {
    @NotNull(message = "员工ID不能为空")
    private Long empId;

    @NotNull(message = "加班日期不能为空")
    private LocalDate overtimeDate;

    @NotNull(message = "开始时间不能为空")
    private LocalTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalTime endTime;

    @NotNull(message = "加班类型不能为空")
    private String overtimeType;

    @NotNull(message = "加班原因不能为空")
    private String reason;
}
