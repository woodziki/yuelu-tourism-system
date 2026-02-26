package com.yuelu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 景点表实体：t_spot
 *
 * <p>严格对应 PRD 5.2：id/name/intro/content/price/duration/tags/view_count/score。</p>
 */
@Data
@TableName("t_spot")
public class Spot {

    /**
     * 主键（自增）。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 景点名称。
     */
    private String name;

    /**
     * 简短介绍。
     */
    private String intro;

    /**
     * 富文本详情。
     */
    private String content;

    /**
     * 门票/缆车费。
     */
    private BigDecimal price;

    /**
     * 建议时长 (e.g. "2小时")。
     */
    private String duration;

    /**
     * 标签字符串 (e.g. "人文,古迹")。
     */
    private String tags;

    /**
     * 浏览量（用于热度排序）。
     */
    private Integer viewCount;

    /**
     * 平均评分（展示用）。
     */
    private Double score;
}

