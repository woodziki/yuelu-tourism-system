package com.yuelu.vo;

import lombok.Data;

/**
 * 后台首页统计卡片数据。
 */
@Data
public class DashboardStatsVO {

    private Long userCount;

    private Long spotCount;

    private Long commentCount;

    private Long favoriteCount;

    private Long viewRecordCount;

    private Long totalViewCount;

    private Double averageScore;
}
