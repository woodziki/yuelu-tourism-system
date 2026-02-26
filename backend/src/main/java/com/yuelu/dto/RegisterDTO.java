package com.yuelu.dto;

import lombok.Data;

/**
 * 注册请求参数。
 *
 * <p>前端提交用户名、密码、昵称，后端校验后加密密码再入库。</p>
 */
@Data
public class RegisterDTO {

    /** 用户名（唯一，登录用） */
    private String username;

    /** 密码（明文，后端用 BCrypt 加密后存库） */
    private String password;

    /** 昵称（展示用） */
    private String nickname;
}
