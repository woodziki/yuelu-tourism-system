package com.yuelu.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuelu.common.Result;
import com.yuelu.dto.LoginDTO;
import com.yuelu.dto.RegisterDTO;
import com.yuelu.entity.User;
import com.yuelu.service.UserService;
import com.yuelu.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户认证控制器。
 *
 * <p>提供注册、登录接口；白名单内接口无需 Token。
 * 后台管理接口（如 adminList）需携带 Token 访问。</p>
 */

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 注册。
     *
     * <p>用户名唯一，密码 BCrypt 加密后存库。成功后返回用户信息（不含密码）。</p>
     *
     * @param dto username / password / nickname
     * @return 注册成功的用户信息
     */
    @PostMapping("/register")
    public Result<User> register(@RequestBody RegisterDTO dto) {
        User user = userService.register(dto);
        return Result.success(user);
    }

    /**
     * 登录。
     *
     * <p>校验用户名与密码，成功则返回 JWT Token 及用户信息。
     * 前端将 Token 放入请求头：Authorization: Bearer &lt;token&gt;</p>
     *
     * @param dto username / password
     * @return token、userId、username、nickname
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO dto) {
        LoginVO vo = userService.login(dto);
        return Result.success(vo);
    }

    /**
     * 后台管理：分页查询用户列表，支持按用户名或昵称模糊搜索。
     * 返回数据已脱敏（password 为 null）。
     *
     * @param current 页码（默认 1）
     * @param size    每页条数（默认 10）
     * @param keyword 关键字（可选，对 username、nickname 模糊匹配）
     * @return 用户分页数据
     */
    @GetMapping("/adminList")
    public Result<IPage<User>> adminList(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String keyword) {
        Page<User> page = new Page<>(current, size);
        IPage<User> result = userService.listAdminUsers(page, keyword);
        return Result.success(result);
    }
}
