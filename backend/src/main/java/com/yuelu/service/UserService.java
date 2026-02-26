package com.yuelu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuelu.dto.LoginDTO;
import com.yuelu.dto.RegisterDTO;
import com.yuelu.entity.User;
import com.yuelu.vo.LoginVO;

/**
 * 用户服务接口。
 *
 * <p>负责注册（密码 BCrypt 加密）、登录（校验密码并签发 JWT）。</p>
 */
public interface UserService extends IService<User> {

    /**
     * 注册：校验用户名是否已存在，密码加密后入库。
     *
     * @param dto 注册参数（username, password, nickname）
     * @return 注册成功后的用户（不含密码）
     */
    User register(RegisterDTO dto);

    /**
     * 登录：校验用户名与密码，成功则生成 JWT 并返回 Token 及用户信息。
     *
     * @param dto 登录参数（username, password）
     * @return Token + userId、username、nickname
     */
    LoginVO login(LoginDTO dto);
}
