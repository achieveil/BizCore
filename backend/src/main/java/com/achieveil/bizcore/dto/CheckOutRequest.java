package com.achieveil.bizcore.dto;

import lombok.Data;

/**
 * 签退请求
 */
@Data
public class CheckOutRequest {
    private Long empId;
    private String remark;
}
