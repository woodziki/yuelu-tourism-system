package com.yuelu.dto;

import lombok.Data;

/**
 * 登录请求参数。
 *
 * <p>前端提交用户名和密码，后端校验后返回 JWT Token。</p>
 */
@Data
public class LoginDTO {

    /** 用户名 */
    private String username;

    /** 密码（明文，与库中 BCrypt 密文比对） */
    private String password;
}
