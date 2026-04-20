package com.yuelu.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 后台数据看板展示对象。
 */
@Data
public class AdminDashboardVO {

    private DashboardStatsVO stats;

    private List<TagStatVO> tagStats = new ArrayList<>();

    private List<ScoreRangeStatVO> scoreRangeStats = new ArrayList<>();

    private List<SpotRankingVO> hotRankings = new ArrayList<>();

    private List<SpotRankingVO> scoreRankings = new ArrayList<>();

    private List<SpotRankingVO> favoriteRankings = new ArrayList<>();

    private List<CommentVO> latestComments = new ArrayList<>();
}
