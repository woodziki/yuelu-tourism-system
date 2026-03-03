package com.yuelu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yuelu.entity.Comment;
import com.yuelu.entity.Favorite;
import com.yuelu.entity.Spot;
import com.yuelu.mapper.CommentMapper;
import com.yuelu.mapper.FavoriteMapper;
import com.yuelu.mapper.SpotMapper;
import com.yuelu.service.RecommendService;
import com.yuelu.util.RecommendUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 推荐服务实现类。
 *
 * <p>本类负责：</p>
 * <ul>
 *     <li>从 t_favorite、t_comment 表加载全量用户行为数据；</li>
 *     <li>将行为数据根据 PRD 的兴趣度公式转换为“用户-景点兴趣度矩阵”；</li>
 *     <li>调用 RecommendUtils（User-based CF + 余弦相似度）计算推荐结果；</li>
 *     <li>根据推荐的景点 ID 查询 t_spot 表，返回完整景点信息；</li>
 *     <li>如果目标用户无行为数据，则回退到“热门景点推荐”（按 view_count 降序 Top 10）。</li>
 * </ul>
 *
 * <p>兴趣度公式（忽略 W_view）：</p>
 * <pre>
 *     Score = (W_fav * Is_fav) + (W_rate * Rating)
 *     其中：
 *       W_fav = 3（有收藏记录则加 3 分）
 *       Is_fav ∈ {0,1}（是否收藏）
 *       W_rate = 1（评分权重）
 *       Rating = star（1-5 分）
 * </pre>
 */
@Service
public class RecommendServiceImpl implements RecommendService {

    /**
     * 收藏权重 W_fav（PRD 指定为 3）。
     */
    private static final double W_FAV = 3.0;

    /**
     * 评分权重 W_rate（PRD 指定为 1）。
     */
    private static final double W_RATE = 1.0;

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SpotMapper spotMapper;

    @Override
    public List<Spot> recommendForUser(Long userId, int topN) {
        // 1. 从数据库加载所有收藏和评论数据
        List<Favorite> favorites = favoriteMapper.selectList(Wrappers.emptyWrapper());
        List<Comment> comments = commentMapper.selectList(Wrappers.emptyWrapper());

        // 2. 构建用户行为兴趣度矩阵：userId -> (spotId -> score)
        Map<Long, Map<Long, Double>> userBehavior = buildUserBehaviorMatrix(favorites, comments);

        // 3. 如果目标用户在行为矩阵中不存在或没有任何行为，触发冷启动策略
        Map<Long, Double> targetVector = userBehavior.get(userId);
        if (targetVector == null || targetVector.isEmpty()) {
            return queryHotSpots(10);
        }

        // 4. 调用协同过滤推荐工具类，获取推荐景点 ID 列表
        List<Long> recommendSpotIds = RecommendUtils.recommendForUser(userId, userBehavior, topN);

        // 5. 若协同过滤结果为空（例如没有相似用户），也降级为热门推荐
        if (recommendSpotIds == null || recommendSpotIds.isEmpty()) {
            return queryHotSpots(10);
        }

        // 6. 根据推荐的景点 ID 查询 t_spot 表，保持推荐顺序
        return fetchSpotsByIdsPreserveOrder(recommendSpotIds);
    }

    /**
     * 构建用户行为兴趣度矩阵。
     *
     * <p>遍历收藏表和评论表，按用户和景点两级维度累计兴趣度分值：</p>
     * <ul>
     *     <li>如果某用户收藏了某个景点，则在对应 Score 上加上 W_fav（即 +3 分）；</li>
     *     <li>如果某用户对某个景点评分 star（1-5），则在对应 Score 上加上 W_rate * star（即 +star 分）；</li>
     *     <li>同一用户对同一景点既收藏又评分时，两部分得分会累加。</li>
     * </ul>
     *
     * @param favorites 收藏记录列表
     * @param comments  评论记录列表
     * @return 用户行为兴趣度矩阵 userId -> (spotId -> score)
     */
    private Map<Long, Map<Long, Double>> buildUserBehaviorMatrix(List<Favorite> favorites,
                                                                 List<Comment> comments) {
        Map<Long, Map<Long, Double>> behavior = new HashMap<>();

        // 2.1 处理收藏行为：Is_fav = 1 => + W_fav
        for (Favorite fav : favorites) {
            Long userId = fav.getUserId();
            Long spotId = fav.getSpotId();
            if (userId == null || spotId == null) {
                continue;
            }
            Map<Long, Double> spotScoreMap = behavior.computeIfAbsent(userId, k -> new HashMap<>());
            spotScoreMap.merge(spotId, W_FAV, Double::sum);
        }

        // 2.2 处理评分行为：Rating = star => + W_rate * star
        for (Comment comment : comments) {
            Long userId = comment.getUserId();
            Long spotId = comment.getSpotId();
            Integer star = comment.getStar();
            if (userId == null || spotId == null || star == null) {
                continue;
            }
            double scoreIncrement = W_RATE * star;
            Map<Long, Double> spotScoreMap = behavior.computeIfAbsent(userId, k -> new HashMap<>());
            spotScoreMap.merge(spotId, scoreIncrement, Double::sum);
        }

        return behavior;
    }

    /**
     * 查询“热门景点”：按 view_count 降序排列，取 Top N。
     *
     * <p>该方法用于冷启动场景（用户无行为数据）或协同过滤结果为空时的兜底推荐，
     * 对应 PRD 3.2 中的“热门景点推荐”。</p>
     *
     * @param topN 需要返回的景点数量
     * @return 热门景点列表
     */
    private List<Spot> queryHotSpots(int topN) {
        LambdaQueryWrapper<Spot> wrapper = new LambdaQueryWrapper<Spot>()
                .orderByDesc(Spot::getViewCount)
                .last("LIMIT " + topN);
        return spotMapper.selectList(wrapper);
    }

    /**
     * 根据一组景点 ID 查询完整信息，并保持输入 ID 的顺序不变。
     *
     * <p>由于数据库查询通常不保证 IN(...) 的顺序，这里需要在内存中按照推荐顺序重新排序，
     * 以便前端展示的列表顺序与算法打分顺序一致，便于论文中截图和说明。</p>
     *
     * @param spotIds 推荐的景点 ID 列表（已按推荐度降序）
     * @return 对应的景点实体列表，顺序与 spotIds 相同
     */
    private List<Spot> fetchSpotsByIdsPreserveOrder(List<Long> spotIds) {
        if (spotIds == null || spotIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Spot> spots = spotMapper.selectBatchIds(spotIds);
        // 先按 id 映射，便于按照推荐顺序重新组装
        Map<Long, Spot> spotMap = new HashMap<>();
        for (Spot spot : spots) {
            spotMap.put(spot.getId(), spot);
        }
        List<Spot> ordered = new ArrayList<>();
        for (Long id : spotIds) {
            Spot spot = spotMap.get(id);
            if (spot != null) {
                ordered.add(spot);
            }
        }
        return ordered;
    }
}

