package com.yuelu.util;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 协同过滤推荐算法工具类（User-based CF，余弦相似度）。
 *
 * <p>本工具类严格对应 PRD 4.1 的算法规范，实现“基于用户的协同过滤”。
 * 整体流程可以概括为三步：</p>
 *
 * <ol>
 *     <li>构建评分向量：将每个用户对各景点的兴趣度 Score 视为一个高维向量；</li>
 *     <li>计算用户相似度：使用余弦相似度 (Cosine Similarity) 度量目标用户与其他用户之间的相似性；</li>
 *     <li>加权汇总推荐：用「相似用户的兴趣度」按相似度加权求和，得到每个景点的“推荐度分数”，取 Top N 作为推荐结果。</li>
 * </ol>
 *
 * <p>数学公式说明：</p>
 * <ul>
 *     <li>设目标用户为 \(u\)，任意其他用户为 \(v\)。</li>
 *     <li>它们在所有景点上的兴趣度向量分别为 \(\\vec{A}\) 和 \(\\vec{B}\)。</li>
 *     <li>余弦相似度定义为： \\(cos(\\theta) = \\frac{\\vec{A} \\cdot \\vec{B}}{\\lVert \\vec{A} \\rVert \\cdot \\lVert \\vec{B} \\rVert}\\)。</li>
 *     <li>其中点积为： \\(\\vec{A} \\cdot \\vec{B} = \\sum\_{i} a\_i \\times b\_i\\)。</li>
 *     <li>向量模长为： \\(\\lVert \\vec{A} \\rVert = \\sqrt{\\sum\_{i} a\_i^2}\\)。</li>
 * </ul>
 *
 * <p>在代码中，我们不显式构建高维稠密向量，而是使用 Map 只在“两个用户都评分过的景点集合”上累加点积与模长，
 * 既节省空间也简化实现。</p>
 */
public class RecommendUtils {

    private RecommendUtils() {
        // 工具类不需要实例化
    }

    /**
     * 基于 User-based CF + 余弦相似度，计算给定用户的 Top N 推荐景点 ID 列表。
     *
     * <p>输入是“全量用户行为得分矩阵”：外层 Key 为 userId，内层 Map 的 Key 为 spotId，Value 为兴趣度 Score。
     * Score 已经按照 PRD 的兴趣度公式计算好，例如：
     * Score = (W_fav * Is_fav) + (W_rate * Rating)</p>
     *
     * <p>推荐核心思想：</p>
     * <ol>
     *     <li>先计算目标用户与其他用户之间的相似度（余弦相似度）；</li>
     *     <li>对于目标用户尚未产生过行为的景点，使用「相似用户的兴趣度 × 相似度」累加，作为该景点的推荐分数；</li>
     *     <li>按推荐分数从高到低排序，取前 N 个景点 ID 作为推荐结果。</li>
     * </ol>
     *
     * @param targetUserId  目标用户 ID（当前登录用户）
     * @param userBehavior  全量用户行为得分矩阵，userId -> (spotId -> Score)
     * @param topN          需要返回的景点数量（Top N）
     * @return 推荐度最高的 Top N 景点 ID 列表（按推荐度降序排列）
     */
    public static List<Long> recommendForUser(Long targetUserId,
                                              Map<Long, Map<Long, Double>> userBehavior,
                                              int topN) {
        if (targetUserId == null || userBehavior == null || userBehavior.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Double> targetVector = userBehavior.get(targetUserId);
        if (targetVector == null || targetVector.isEmpty()) {
            // 目标用户没有任何行为数据时，返回空列表，由业务层做“热门推荐”冷启动逻辑
            return Collections.emptyList();
        }

        // 1. 计算目标用户与每个其他用户之间的余弦相似度
        Map<Long, Double> similarityMap = new HashMap<>();
        for (Map.Entry<Long, Map<Long, Double>> entry : userBehavior.entrySet()) {
            Long otherUserId = entry.getKey();
            if (otherUserId.equals(targetUserId)) {
                continue;
            }
            double sim = cosineSimilarity(targetVector, entry.getValue());
            if (sim > 0) {
                similarityMap.put(otherUserId, sim);
            }
        }

        if (similarityMap.isEmpty()) {
            // 没有任何相似用户时，返回空列表，由业务层降级为热门推荐
            return Collections.emptyList();
        }

        // 2. 统计“目标用户尚未评分/收藏的景点”的推荐分数：sum(sim(u,v) * score(v, spot))
        Map<Long, Double> recommendScoreMap = new HashMap<>();

        // 目标用户已交互的景点集合，用于排除
        Set<Long> targetSpots = targetVector.keySet();

        for (Map.Entry<Long, Double> simEntry : similarityMap.entrySet()) {
            Long otherUserId = simEntry.getKey();
            double sim = simEntry.getValue();

            Map<Long, Double> otherVector = userBehavior.get(otherUserId);
            if (otherVector == null || otherVector.isEmpty()) {
                continue;
            }

            for (Map.Entry<Long, Double> spotScoreEntry : otherVector.entrySet()) {
                Long spotId = spotScoreEntry.getKey();
                Double otherScore = spotScoreEntry.getValue();

                // 只推荐“目标用户尚未产生行为”的景点，避免推荐已经看过/收藏过的项目
                if (targetSpots.contains(spotId)) {
                    continue;
                }

                double weightedScore = sim * otherScore;
                recommendScoreMap.merge(spotId, weightedScore, Double::sum);
            }
        }

        if (recommendScoreMap.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. 按推荐分数从高到低排序，取 Top N 的景点 ID
        return recommendScoreMap.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 计算两个用户兴趣向量之间的余弦相似度。
     *
     * <p>这里的向量使用 Map<spotId, score> 的稀疏表示形式，
     * 只在“两个用户都对某个景点有行为数据”的交集上参与点积与模长计算。</p>
     *
     * @param a 第一个用户的兴趣向量（spotId -> Score）
     * @param b 第二个用户的兴趣向量（spotId -> Score）
     * @return 余弦相似度，取值范围 [0,1]，值越大表示越相似；如无交集或模长为 0，则返回 0
     */
    public static double cosineSimilarity(Map<Long, Double> a, Map<Long, Double> b) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty()) {
            return 0.0;
        }

        // 计算点积 numerator = sum(ai * bi)，只在交集景点上累加
        double numerator = 0.0;
        for (Map.Entry<Long, Double> entry : a.entrySet()) {
            Long spotId = entry.getKey();
            Double ai = entry.getValue();
            Double bi = b.get(spotId);
            if (bi != null) {
                numerator += ai * bi;
            }
        }

        // 计算两个向量的模长
        double sumSqA = 0.0;
        for (Double ai : a.values()) {
            sumSqA += ai * ai;
        }
        double sumSqB = 0.0;
        for (Double bi : b.values()) {
            sumSqB += bi * bi;
        }

        double denominator = Math.sqrt(sumSqA) * Math.sqrt(sumSqB);
        if (denominator == 0.0) {
            // 若任一向量模长为 0，则无法定义余弦相似度，记为 0
            return 0.0;
        }

        return numerator / denominator;
    }
}

