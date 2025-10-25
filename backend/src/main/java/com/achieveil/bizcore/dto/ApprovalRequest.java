package com.achieveil.bizcore.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 审批请求（请假、加班通用）
 */
@Data
public class ApprovalRequest {
    @NotNull(message = "请求ID不能为空")
    private Long requestId;

    @NotNull(message = "审批人ID不能为空")
    private Long approverId;

    @NotNull(message = "审批结果不能为空")
    private String action;  // APPROVE-批准, REJECT-拒绝

    private String comment;  // 审批意见
}
