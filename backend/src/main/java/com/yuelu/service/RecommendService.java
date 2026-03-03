package com.yuelu.service;

import com.yuelu.entity.Spot;

import java.util.List;

/**
 * 推荐服务接口。
 *
 * <p>负责从行为表 t_favorite、t_comment 中读取全量数据，构建用户行为兴趣矩阵，
 * 调用 RecommendUtils 计算协同过滤推荐结果，并根据 PRD 3.2 处理冷启动场景。</p>
 */
public interface RecommendService {

    /**
     * 为指定用户计算推荐景点列表。
     *
     * <p>核心逻辑：</p>
     * <ol>
     *     <li>从 t_favorite、t_comment 表中加载全量用户行为数据；</li>
     *     <li>按 PRD 兴趣度公式计算每个用户对每个景点的兴趣度 Score：
     *     Score = (W_fav * Is_fav) + (W_rate * Rating)，其中 W_fav = 3，W_rate = 1；</li>
     *     <li>将行为矩阵传给 RecommendUtils，获得 Top N 推荐景点 ID；</li>
     *     <li>根据推荐景点 ID 去 t_spot 查询完整景点信息并返回；</li>
     *     <li>若目标用户无任何行为，则触发冷启动策略：按 view_count 降序返回 Top 10 热门景点。</li>
     * </ol>
     *
     * @param userId 当前登录用户 ID
     * @param topN   需要推荐的景点数量（协同过滤阶段的 Top N）
     * @return 推荐的景点列表（含完整信息）
     */
    List<Spot> recommendForUser(Long userId, int topN);
}

