package com.yuelu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuelu.entity.Spot;
import org.apache.ibatis.annotations.Mapper;

/**
 * 景点表 Mapper 接口。
 *
 * <p>继承 MyBatis-Plus 的 BaseMapper，自动提供基础的 CRUD 方法。
 * 如需自定义 SQL，可在此接口中添加方法并在 resources/mapper 下编写 XML。</p>
 */
@Mapper
public interface SpotMapper extends BaseMapper<Spot> {
}
