package com.yuelu.vo;

import com.yuelu.entity.Spot;
import lombok.Data;

/**
 * 景点排行榜展示对象。
 */
@Data
public class SpotRankingVO {

    /**
     * 排名序号，从 1 开始。
     */
    private Integer rank;

    /**
     * 景点信息。
     */
    private Spot spot;

    /**
     * 排行榜指标值。
     */
    private Double value;

    /**
     * 指标展示文案。
     */
    private String label;
}
