package com.yuelu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuelu.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表 Mapper 接口。
 *
 * <p>继承 MyBatis-Plus 的 BaseMapper，提供用户表的 CRUD。
 * 用于注册时插入用户、登录时按 username 查询。</p>
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
