package com.yuelu.dto;

import lombok.Data;

/**
 * 后台用户状态更新参数。
 */
@Data
public class UserStatusDTO {

    /**
     * 用户 ID。
     */
    private Long id;

    /**
     * 账号状态：0=正常，1=封禁。
     */
    private Integer status;
}
