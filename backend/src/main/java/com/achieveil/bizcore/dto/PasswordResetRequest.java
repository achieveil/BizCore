package com.achieveil.bizcore.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 管理员重置密码请求
 */
@Data
public class PasswordResetRequest {
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "新密码不能为空")
    private String newPassword;

    @NotNull(message = "管理员ID不能为空")
    private Long adminId;
}
