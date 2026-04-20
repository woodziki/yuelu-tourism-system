package com.yuelu.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户兴趣画像展示对象。
 */
@Data
public class InterestProfileVO {

    /**
     * 浏览行为数量。
     */
    private Integer viewCount = 0;

    /**
     * 收藏行为数量。
     */
    private Integer favoriteCount = 0;

    /**
     * 评论评分数量。
     */
    private Integer commentCount = 0;

    /**
     * 用户历史平均评分。
     */
    private Double averageRating;

    /**
     * 兴趣标签权重列表。
     */
    private List<TagWeightVO> tagWeights = new ArrayList<>();

    /**
     * Top 兴趣标签。
     */
    private List<String> topTags = new ArrayList<>();

    /**
     * 画像摘要文案。
     */
    private String summary;
}
