package com.yuelu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yuelu.entity.Comment;
import com.yuelu.entity.Favorite;
import com.yuelu.entity.Spot;
import com.yuelu.entity.ViewRecord;
import com.yuelu.mapper.CommentMapper;
import com.yuelu.mapper.FavoriteMapper;
import com.yuelu.mapper.SpotMapper;
import com.yuelu.mapper.ViewRecordMapper;
import com.yuelu.service.RecommendService;
import com.yuelu.util.RecommendUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 推荐服务实现类。
 *
 * <p>本类负责：</p>
 * <ul>
 *     <li>融合浏览、收藏、评分三维行为信号；</li>
 *     <li>引入时间衰减，降低旧行为对推荐的影响；</li>
 *     <li>通过 User-based CF 产出候选集合，再做标签多样性打散；</li>
 *     <li>最终返回“相关性 + 多样性”平衡后的 TopN。</li>
 * </ul>
 */
@Service
public class RecommendServiceImpl implements RecommendService {

    /**
     * 浏览权重 W_view（隐式反馈，强度低于收藏与评分）。
     */
    private static final double W_VIEW = 1.0;

    /**
     * 收藏权重 W_fav（显式偏好，权重高）。
     */
    private static final double W_FAV = 3.0;

    /**
     * 评分权重 W_rate（评分强度由 star 提供）。
     */
    private static final double W_RATE = 2.0;

    /**
     * 时间衰减系数 lambda：decay(days)=exp(-lambda*days)。
     */
    private static final double LAMBDA = 0.05;

    /**
     * CF 候选集合大小。
     */
    private static final int CANDIDATE_SIZE = 20;

    /**
     * 标签配额：同一标签最多保留的景点数量。
     */
    private static final int TAG_QUOTA_PER_LABEL = 2;

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SpotMapper spotMapper;

    @Autowired
    private ViewRecordMapper viewRecordMapper;

    @Override
    public List<Spot> recommendForUser(Long userId, int topN) {
        if (userId == null || topN <= 0) {
            return Collections.emptyList();
        }

        // 1) 加载全量行为数据（浏览 + 收藏 + 评分）
        List<ViewRecord> views = viewRecordMapper.selectList(Wrappers.emptyWrapper());
        List<Favorite> favorites = favoriteMapper.selectList(Wrappers.emptyWrapper());
        List<Comment> comments = commentMapper.selectList(Wrappers.emptyWrapper());

        // 2) 构建用户-景点兴趣矩阵
        Map<Long, Map<Long, Double>> userBehavior = buildUserBehaviorMatrix(views, favorites, comments);

        // 3) 冷启动：用户无行为时回退热门推荐
        Map<Long, Double> targetVector = userBehavior.get(userId);
        if (targetVector == null || targetVector.isEmpty()) {
            return queryHotSpots(topN, null);
        }

        // 4) CF 产出候选 Top20，再做多样性重排
        List<Long> candidateSpotIds = RecommendUtils.recommendForUser(userId, userBehavior, CANDIDATE_SIZE);
        if (candidateSpotIds == null || candidateSpotIds.isEmpty()) {
            List<Spot> hotCandidates = queryHotSpots(CANDIDATE_SIZE, targetVector.keySet());
            return diversifyByTagQuota(hotCandidates, topN);
        }

        List<Spot> candidates = fetchSpotsByIdsPreserveOrder(candidateSpotIds);
        if (candidates.isEmpty()) {
            List<Spot> hotCandidates = queryHotSpots(CANDIDATE_SIZE, targetVector.keySet());
            return diversifyByTagQuota(hotCandidates, topN);
        }

        return diversifyByTagQuota(candidates, topN);
    }

    /**
     * 构建用户行为兴趣矩阵。
     *
     * <p>算法推导公式：</p>
     * <pre>
     * decay(days) = exp(-0.05 * days)
     * score_behavior = weight_behavior * value * decay(days)
     * score_total(u,s) = Σ score_behavior
     * </pre>
     * <ul>
     *     <li>浏览：value=1，表示弱偏好；</li>
     *     <li>收藏：value=1，表示强偏好；</li>
     *     <li>评分：value=star（1~5），表示显式喜好强度。</li>
     * </ul>
     *
     * <p>设计意图：用时间衰减保证“近期行为更重要”，使推荐能随用户兴趣变化而动态调整。</p>
     *
     * @param views     浏览记录列表
     * @param favorites 收藏记录列表
     * @param comments  评论记录列表
     * @return 用户行为兴趣度矩阵 userId -> (spotId -> score)
     */
    private Map<Long, Map<Long, Double>> buildUserBehaviorMatrix(List<ViewRecord> views,
                                                                 List<Favorite> favorites,
                                                                 List<Comment> comments) {
        Map<Long, Map<Long, Double>> behavior = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();

        // 1) 浏览行为（隐式反馈）
        for (ViewRecord view : views) {
            Long uid = view.getUserId();
            Long sid = view.getSpotId();
            if (uid == null || sid == null) {
                continue;
            }
            double decay = decayByTime(view.getCreateTime(), now);
            mergeBehaviorScore(behavior, uid, sid, W_VIEW * decay);
        }

        // 2) 收藏行为（显式强偏好）
        for (Favorite fav : favorites) {
            Long uid = fav.getUserId();
            Long sid = fav.getSpotId();
            if (uid == null || sid == null) {
                continue;
            }
            double decay = decayByTime(fav.getCreateTime(), now);
            mergeBehaviorScore(behavior, uid, sid, W_FAV * decay);
        }

        // 3) 评分行为（显式偏好强度）
        for (Comment comment : comments) {
            Long uid = comment.getUserId();
            Long sid = comment.getSpotId();
            Integer star = comment.getStar();
            if (uid == null || sid == null || star == null) {
                continue;
            }
            double decay = decayByTime(comment.getCreateTime(), now);
            double scoreIncrement = W_RATE * star * decay;
            mergeBehaviorScore(behavior, uid, sid, scoreIncrement);
        }

        return behavior;
    }

    /**
     * 查询“热门景点”：按 view_count 降序排列，取 Top N。
     *
     * <p>该方法用于冷启动或 CF 候选为空时的兜底策略。</p>
     *
     * @param topN          需要返回的景点数量
     * @param excludeSpotIds 需要排除的景点 ID 集合（可为 null 或空）
     * @return 热门景点列表
     */
    private List<Spot> queryHotSpots(int topN, Set<Long> excludeSpotIds) {
        LambdaQueryWrapper<Spot> wrapper = new LambdaQueryWrapper<Spot>()
                .orderByDesc(Spot::getViewCount)
                .last("LIMIT " + topN);
        if (excludeSpotIds != null && !excludeSpotIds.isEmpty()) {
            wrapper.notIn(Spot::getId, excludeSpotIds);
        }
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

    /**
     * 时间衰减函数：decay(days)=exp(-lambda*days)。
     */
    private double decayByTime(LocalDateTime behaviorTime, LocalDateTime now) {
        if (behaviorTime == null) {
            return 1.0;
        }
        long days = Math.max(0, ChronoUnit.DAYS.between(behaviorTime.toLocalDate(), now.toLocalDate()));
        return Math.exp(-LAMBDA * days);
    }

    /**
     * 累加用户行为得分。
     */
    private void mergeBehaviorScore(Map<Long, Map<Long, Double>> behavior,
                                    Long userId,
                                    Long spotId,
                                    double increment) {
        Map<Long, Double> spotScoreMap = behavior.computeIfAbsent(userId, k -> new HashMap<>());
        spotScoreMap.merge(spotId, increment, Double::sum);
    }

    /**
     * MMR 工程化近似：使用“标签配额”抑制过度同质化。
     *
     * <p>设计意图：经典 MMR 目标是平衡相关性与多样性：
     * score = lambda * rel - (1-lambda) * sim。
     * 在景区业务中，标签是天然内容语义，因此用“同标签最多 2 个”的约束近似 sim 惩罚，
     * 能以较低复杂度获得可解释、可控的多样化结果。</p>
     */
    private List<Spot> diversifyByTagQuota(List<Spot> candidates, int topN) {
        if (candidates == null || candidates.isEmpty() || topN <= 0) {
            return Collections.emptyList();
        }
        List<Spot> result = new ArrayList<>();
        Map<String, Integer> tagCount = new HashMap<>();

        // 第一轮：执行标签配额约束
        for (Spot spot : candidates) {
            if (spot == null) {
                continue;
            }
            String primaryTag = primaryTag(spot.getTags());
            int count = tagCount.getOrDefault(primaryTag, 0);
            if (count >= TAG_QUOTA_PER_LABEL) {
                continue;
            }
            result.add(spot);
            tagCount.put(primaryTag, count + 1);
            if (result.size() >= topN) {
                return result;
            }
        }

        // 第二轮：若配额导致不足 topN，按候选顺序补齐
        for (Spot spot : candidates) {
            if (spot == null || result.size() >= topN) {
                continue;
            }
            if (!containsSpot(result, spot.getId())) {
                result.add(spot);
            }
        }
        return result;
    }

    private String primaryTag(String tags) {
        if (tags == null || tags.trim().isEmpty()) {
            return "unknown";
        }
        String[] arr = tags.split(",");
        for (String tag : arr) {
            String t = tag == null ? "" : tag.trim();
            if (!t.isEmpty()) {
                return t;
            }
        }
        return "unknown";
    }

    private boolean containsSpot(List<Spot> spots, Long spotId) {
        if (spotId == null) {
            return false;
        }
        for (Spot s : spots) {
            if (s != null && spotId.equals(s.getId())) {
                return true;
            }
        }
        return false;
    }
}

