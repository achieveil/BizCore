package com.achieveil.bizcore.dto;

import lombok.Data;

/**
 * 签到请求
 */
@Data
public class CheckInRequest {
    private Long empId;
    private String remark;
}
