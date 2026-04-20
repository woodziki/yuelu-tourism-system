package com.yuelu.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 首页推荐页展示对象。
 */
@Data
public class RecommendPageVO {

    /**
     * 用户兴趣画像。
     */
    private InterestProfileVO profile;

    /**
     * 推荐景点列表。
     */
    private List<SpotRecommendVO> recommendations = new ArrayList<>();
}
