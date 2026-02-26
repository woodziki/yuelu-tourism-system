package com.yuelu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuelu.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收藏表 Mapper 接口。
 *
 * <p>继承 MyBatis-Plus 的 BaseMapper，自动提供基础的 CRUD 方法。</p>
 */
@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
}
