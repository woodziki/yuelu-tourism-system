package com.yuelu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户表实体：t_user
 *
 * <p>严格对应 PRD 5.1：id/username/password/nickname/create_time。</p>
 */
@Data
@TableName("t_user")
public class User {

    /**
     * 主键（自增）。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名（唯一）。
     */
    private String username;

    /**
     * 密码（PRD 仅定义字段，不在此阶段引入加密字段/盐等扩展）。
     */
    private String password;

    /**
     * 昵称。
     */
    private String nickname;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;
}

