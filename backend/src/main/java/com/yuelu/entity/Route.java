package com.yuelu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 线路表实体：t_route
 *
 * <p>严格对应 PRD 5.3：id/name/intro/map_img/tags。</p>
 */
@Data
@TableName("t_route")
public class Route {

    /**
     * 主键（自增）。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 线路名称。
     */
    private String name;

    /**
     * 线路简介。
     */
    private String intro;

    /**
     * 静态地图路径。
     */
    private String mapImg;

    /**
     * 线路标签。
     */
    private String tags;
}

