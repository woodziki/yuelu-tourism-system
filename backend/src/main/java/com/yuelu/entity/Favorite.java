package com.yuelu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 收藏表实体：t_favorite
 *
 * <p>严格对应 PRD 5.5：id/user_id/spot_id。</p>
 */
@Data
@TableName("t_favorite")
public class Favorite {

    /**
     * 主键（自增）。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 景点 ID。
     */
    private Long spotId;
}

