package com.yuelu.dto;

import lombok.Data;

/**
 * 后台用户编辑参数。
 *
 * <p>仅允许编辑展示类信息，不修改密码。</p>
 */
@Data
public class UserUpdateDTO {

    /**
     * 用户 ID。
     */
    private Long id;

    /**
     * 用户昵称。
     */
    private String nickname;
}
