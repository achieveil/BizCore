package com.achieveil.bizcore.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String realName;
    private String email;
    private String phone;
    private String role;
    private Integer status;
    private LocalDateTime createdTime;
}
