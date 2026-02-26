package com.yuelu.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录成功返回数据。
 *
 * <p>包含 JWT Token 和当前用户基本信息，前端将 Token 放入请求头后即可访问需登录接口。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {

    /** JWT Token，请求头格式：Authorization: Bearer &lt;token&gt; */
    private String token;

    /** 用户 ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;
}
