package com.yuelu.vo;

import lombok.Data;

/**
 * 用户画像中的标签权重展示对象。
 */
@Data
public class TagWeightVO {

    /**
     * 标签名称。
     */
    private String tag;

    /**
     * 标签兴趣权重。
     */
    private Double weight;
}
