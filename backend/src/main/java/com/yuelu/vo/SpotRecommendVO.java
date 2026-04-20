package com.yuelu.vo;

import com.yuelu.entity.Spot;
import lombok.Data;

/**
 * 带推荐理由的景点展示对象。
 */
@Data
public class SpotRecommendVO {

    /**
     * 景点信息。
     */
    private Spot spot;

    /**
     * 推荐理由。
     */
    private String reason;
}
