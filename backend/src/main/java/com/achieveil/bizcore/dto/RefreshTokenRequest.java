package com.achieveil.bizcore.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {

    @NotBlank(message = "会话ID不能为空")
    private String sessionId;

    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;
}
