package com.yuelu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 线路-景点关联表实体：t_route_spot
 *
 * <p>严格对应 PRD 5.4：id/route_id/spot_id/sort。</p>
 */
@Data
@TableName("t_route_spot")
public class RouteSpot {

    /**
     * 主键（自增）。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 线路 ID（外键语义）。
     */
    private Long routeId;

    /**
     * 景点 ID（外键语义）。
     */
    private Long spotId;

    /**
     * 游玩顺序 (1, 2, 3...)。
     */
    private Integer sort;
}

