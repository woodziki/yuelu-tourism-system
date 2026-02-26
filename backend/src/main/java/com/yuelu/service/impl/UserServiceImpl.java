package com.yuelu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuelu.dto.LoginDTO;
import com.yuelu.dto.RegisterDTO;
import com.yuelu.entity.User;
import com.yuelu.mapper.UserMapper;
import com.yuelu.service.UserService;
import com.yuelu.util.JwtUtil;
import com.yuelu.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 用户服务实现：注册（BCrypt 加密）、登录（签发 JWT）。
 *
 * <p>密码绝不明文存库，统一使用 BCrypt 加密与校验。</p>
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public User register(RegisterDTO dto) {
        if (!StringUtils.hasText(dto.getUsername()) || !StringUtils.hasText(dto.getPassword())) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }
        // 用户名唯一校验
        long count = this.count(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new IllegalArgumentException("用户名已存在");
        }
        User user = new User();
        user.setUsername(dto.getUsername().trim());
        user.setPassword(ENCODER.encode(dto.getPassword())); // 仅此处写入密码，密文存库
        user.setNickname(StringUtils.hasText(dto.getNickname()) ? dto.getNickname().trim() : dto.getUsername());
        user.setCreateTime(LocalDateTime.now());
        this.save(user);
        user.setPassword(null); // 返回前脱敏
        return user;
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        if (!StringUtils.hasText(dto.getUsername()) || !StringUtils.hasText(dto.getPassword())) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }
        User user = this.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (user == null || !ENCODER.matches(dto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        String token = jwtUtil.createToken(user.getId(), user.getUsername());
        return new LoginVO(token, user.getId(), user.getUsername(), user.getNickname());
    }
}
