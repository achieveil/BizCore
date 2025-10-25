package com.achieveil.bizcore.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 创建请假申请请求
 */
@Data
public class LeaveRequestCreateDTO {
    @NotNull(message = "员工ID不能为空")
    private Long empId;

    @NotNull(message = "请假类型不能为空")
    private String leaveType;

    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;

    @NotNull(message = "请假原因不能为空")
    private String reason;

    private String attachmentUrl;
}
