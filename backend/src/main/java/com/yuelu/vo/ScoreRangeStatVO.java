package com.yuelu.vo;

import lombok.Data;

/**
 * 评分区间统计展示对象。
 */
@Data
public class ScoreRangeStatVO {

    private String range;

    private Integer count;
}
