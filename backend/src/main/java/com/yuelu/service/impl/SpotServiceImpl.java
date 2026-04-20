package com.yuelu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuelu.entity.Favorite;
import com.yuelu.entity.Spot;
import com.yuelu.mapper.FavoriteMapper;
import com.yuelu.mapper.SpotMapper;
import com.yuelu.service.SpotService;
import com.yuelu.vo.SpotRankingVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 景点服务实现类。
 *
 * <p>继承 ServiceImpl<SpotMapper, Spot>，自动获得基础的 CRUD 能力。
 * 实现自定义的业务方法，如分页查询、条件筛选等。</p>
 */
@Service
public class SpotServiceImpl extends ServiceImpl<SpotMapper, Spot> implements SpotService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Override
    public IPage<Spot> listSpots(Page<Spot> page, String name, String tags) {
        // 构建查询条件（Lambda 表达式，避免字段名写错）
        LambdaQueryWrapper<Spot> queryWrapper = new LambdaQueryWrapper<>();

        // 如果传入了名称，进行模糊搜索（LIKE %name%）
        if (StringUtils.hasText(name)) {
            queryWrapper.like(Spot::getName, name);
        }

        // 如果传入了标签，进行精确匹配（tags 字段包含该标签）
        // 注意：PRD 中 tags 是逗号分隔的字符串，这里使用 LIKE 匹配
        if (StringUtils.hasText(tags)) {
            queryWrapper.like(Spot::getTags, tags);
        }

        // 按浏览量降序排列（热门优先）
        queryWrapper.orderByDesc(Spot::getViewCount);

        // 执行分页查询
        return this.page(page, queryWrapper);
    }

    @Override
    public IPage<Spot> listAdminSpots(Page<Spot> page, String name, String tags) {
        // 后台管理查询：与前台相同的筛选条件，但排序规则按 ID 降序（最新录入的景点排在前面）
        LambdaQueryWrapper<Spot> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(name)) {
            queryWrapper.like(Spot::getName, name);
        }
        if (StringUtils.hasText(tags)) {
            queryWrapper.like(Spot::getTags, tags);
        }

        queryWrapper.orderByDesc(Spot::getId);

        return this.page(page, queryWrapper);
    }

    @Override
    public Spot getSpotById(Long id) {
        // 通过 SQL 原子更新实现并发安全：
        // update t_spot set view_count = view_count + 1 where id = ?
        this.baseMapper.incrementViewCount(id);
        return this.getById(id);
    }

    @Override
    public Spot getSpotByIdPlain(Long id) {
        return this.getById(id);
    }

    @Override
    public List<SpotRankingVO> listRankings(String type, Integer limit) {
        int safeLimit = limit == null ? 10 : Math.max(1, Math.min(limit, 20));
        String rankingType = StringUtils.hasText(type) ? type : "hot";
        List<Spot> spots = this.list();
        Map<Long, Integer> favoriteCountMap = "favorite".equals(rankingType) ? queryFavoriteCountMap() : new HashMap<>();

        spots.sort((a, b) -> {
            int valueCompare = Double.compare(rankingValue(b, rankingType, favoriteCountMap), rankingValue(a, rankingType, favoriteCountMap));
            if (valueCompare != 0) {
                return valueCompare;
            }
            return Long.compare(b.getId() == null ? 0 : b.getId(), a.getId() == null ? 0 : a.getId());
        });

        List<SpotRankingVO> rankings = new ArrayList<>();
        for (Spot spot : spots) {
            if (spot == null || spot.getId() == null) {
                continue;
            }
            SpotRankingVO vo = new SpotRankingVO();
            vo.setRank(rankings.size() + 1);
            vo.setSpot(spot);
            double value = rankingValue(spot, rankingType, favoriteCountMap);
            vo.setValue(value);
            vo.setLabel(buildRankingLabel(rankingType, value));
            rankings.add(vo);
            if (rankings.size() >= safeLimit) {
                break;
            }
        }
        return rankings;
    }

    private Map<Long, Integer> queryFavoriteCountMap() {
        List<Favorite> favorites = favoriteMapper.selectList(null);
        Map<Long, Integer> countMap = new HashMap<>();
        for (Favorite favorite : favorites) {
            if (favorite != null && favorite.getSpotId() != null) {
                countMap.merge(favorite.getSpotId(), 1, Integer::sum);
            }
        }
        return countMap;
    }

    private double rankingValue(Spot spot, String type, Map<Long, Integer> favoriteCountMap) {
        if (spot == null) {
            return 0;
        }
        if ("score".equals(type)) {
            return spot.getScore() == null ? 0 : spot.getScore();
        }
        if ("favorite".equals(type)) {
            return favoriteCountMap.getOrDefault(spot.getId(), 0);
        }
        return spot.getViewCount() == null ? 0 : spot.getViewCount();
    }

    private String buildRankingLabel(String type, double value) {
        if ("score".equals(type)) {
            return String.format("%.1f 分", value);
        }
        if ("favorite".equals(type)) {
            return (int) value + " 次收藏";
        }
        return (int) value + " 次浏览";
    }
}
