package com.yuelu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuelu.entity.Spot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 景点表 Mapper 接口。
 *
 * <p>继承 MyBatis-Plus 的 BaseMapper，自动提供基础的 CRUD 方法。
 * 如需自定义 SQL，可在此接口中添加方法并在 resources/mapper 下编写 XML。</p>
 */
@Mapper
public interface SpotMapper extends BaseMapper<Spot> {

    /**
     * 原子自增景点浏览量。
     *
     * <p>等价 SQL：update t_spot set view_count = view_count + 1 where id = ?</p>
     *
     * @param id 景点 ID
     * @return 影响行数
     */
    @Update("update t_spot set view_count = view_count + 1 where id = #{id}")
    int incrementViewCount(@Param("id") Long id);

    /**
     * 根据评论均值刷新景点评分。
     *
     * @param spotId 景点 ID
     * @return 影响行数
     */
    @Update("update t_spot set score = (select avg_score from (select avg(star) avg_score from t_comment where spot_id = #{spotId}) t) where id = #{spotId}")
    int refreshScoreByComments(@Param("spotId") Long spotId);
}
