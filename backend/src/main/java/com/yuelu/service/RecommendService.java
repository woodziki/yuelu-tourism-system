package com.yuelu.service;

import com.yuelu.entity.Spot;
import com.yuelu.vo.RecommendPageVO;

import java.util.List;

/**
 * 推荐服务接口。
 *
 * <p>负责读取用户浏览、收藏、评论评分等行为数据，构建用户行为兴趣矩阵，
 * 调用 RecommendUtils 计算协同过滤推荐结果，并处理冷启动场景。</p>
 */
public interface RecommendService {

    /**
     * 为指定用户计算推荐景点列表。
     *
     * @param userId 当前登录用户 ID
     * @param topN   需要推荐的景点数量
     * @return 推荐的景点列表（含完整信息）
     */
    List<Spot> recommendForUser(Long userId, int topN);

    /**
     * 为首页生成用户画像和带推荐理由的推荐结果。
     *
     * @param userId 当前登录用户 ID
     * @param topN   需要推荐的景点数量
     * @return 首页推荐展示数据
     */
    RecommendPageVO recommendPageForUser(Long userId, int topN);
}
