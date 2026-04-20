package com.yuelu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuelu.entity.Comment;
import com.yuelu.entity.Spot;
import com.yuelu.entity.User;
import com.yuelu.mapper.CommentMapper;
import com.yuelu.mapper.FavoriteMapper;
import com.yuelu.mapper.ViewRecordMapper;
import com.yuelu.service.DashboardService;
import com.yuelu.service.SpotService;
import com.yuelu.service.UserService;
import com.yuelu.vo.AdminDashboardVO;
import com.yuelu.vo.CommentVO;
import com.yuelu.vo.DashboardStatsVO;
import com.yuelu.vo.ScoreRangeStatVO;
import com.yuelu.vo.TagStatVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台数据看板服务实现类。
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private UserService userService;

    @Autowired
    private SpotService spotService;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private ViewRecordMapper viewRecordMapper;

    @Override
    public AdminDashboardVO getDashboard() {
        List<Spot> spots = spotService.list();
        List<Comment> comments = commentMapper.selectList(null);
        AdminDashboardVO dashboard = new AdminDashboardVO();
        dashboard.setStats(buildStats(spots, comments));
        dashboard.setTagStats(buildTagStats(spots));
        dashboard.setScoreRangeStats(buildScoreRangeStats(spots));
        dashboard.setHotRankings(spotService.listRankings("hot", 5));
        dashboard.setScoreRankings(spotService.listRankings("score", 5));
        dashboard.setFavoriteRankings(spotService.listRankings("favorite", 5));
        dashboard.setLatestComments(buildLatestComments());
        return dashboard;
    }

    private DashboardStatsVO buildStats(List<Spot> spots, List<Comment> comments) {
        DashboardStatsVO stats = new DashboardStatsVO();
        stats.setUserCount(userService.count());
        stats.setSpotCount((long) spots.size());
        stats.setCommentCount((long) comments.size());
        stats.setFavoriteCount(favoriteMapper.selectCount(null));
        stats.setViewRecordCount(viewRecordMapper.selectCount(null));

        long totalViewCount = 0;
        double scoreSum = 0;
        int scoreCount = 0;
        for (Spot spot : spots) {
            if (spot.getViewCount() != null) {
                totalViewCount += spot.getViewCount();
            }
            if (spot.getScore() != null) {
                scoreSum += spot.getScore();
                scoreCount++;
            }
        }
        stats.setTotalViewCount(totalViewCount);
        stats.setAverageScore(scoreCount == 0 ? null : Math.round(scoreSum * 10.0 / scoreCount) / 10.0);
        return stats;
    }

    private List<TagStatVO> buildTagStats(List<Spot> spots) {
        Map<String, Integer> tagCountMap = new HashMap<>();
        for (Spot spot : spots) {
            for (String tag : parseTags(spot.getTags())) {
                tagCountMap.merge(tag, 1, Integer::sum);
            }
        }
        List<TagStatVO> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : tagCountMap.entrySet()) {
            TagStatVO vo = new TagStatVO();
            vo.setTag(entry.getKey());
            vo.setCount(entry.getValue());
            result.add(vo);
        }
        result.sort((a, b) -> Integer.compare(b.getCount(), a.getCount()));
        return result;
    }

    private List<ScoreRangeStatVO> buildScoreRangeStats(List<Spot> spots) {
        Map<String, Integer> ranges = new HashMap<>();
        ranges.put("暂无评分", 0);
        ranges.put("0-3分", 0);
        ranges.put("3-4分", 0);
        ranges.put("4-5分", 0);
        for (Spot spot : spots) {
            Double score = spot.getScore();
            if (score == null) {
                ranges.merge("暂无评分", 1, Integer::sum);
            } else if (score < 3) {
                ranges.merge("0-3分", 1, Integer::sum);
            } else if (score < 4) {
                ranges.merge("3-4分", 1, Integer::sum);
            } else {
                ranges.merge("4-5分", 1, Integer::sum);
            }
        }
        List<ScoreRangeStatVO> result = new ArrayList<>();
        for (String range : new String[]{"暂无评分", "0-3分", "3-4分", "4-5分"}) {
            ScoreRangeStatVO vo = new ScoreRangeStatVO();
            vo.setRange(range);
            vo.setCount(ranges.get(range));
            result.add(vo);
        }
        return result;
    }

    private List<CommentVO> buildLatestComments() {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<Comment>()
                .orderByDesc(Comment::getCreateTime)
                .orderByDesc(Comment::getId)
                .last("LIMIT 5");
        List<Comment> comments = commentMapper.selectList(wrapper);
        Map<Long, User> userMap = queryUserMap(comments);
        Map<Long, Spot> spotMap = querySpotMap(comments);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        List<CommentVO> result = new ArrayList<>();
        for (Comment comment : comments) {
            CommentVO vo = new CommentVO();
            vo.setId(comment.getId());
            vo.setUserId(comment.getUserId());
            vo.setSpotId(comment.getSpotId());
            vo.setContent(comment.getContent());
            vo.setStar(comment.getStar());
            User user = comment.getUserId() == null ? null : userMap.get(comment.getUserId());
            Spot spot = comment.getSpotId() == null ? null : spotMap.get(comment.getSpotId());
            vo.setNickname(user == null ? "未知用户" : user.getNickname());
            vo.setSpotName(spot == null ? "未知景点" : spot.getName());
            if (comment.getCreateTime() != null) {
                String time = comment.getCreateTime().format(formatter);
                vo.setTime(time);
                vo.setCreateTime(time);
            }
            result.add(vo);
        }
        return result;
    }

    private Map<Long, User> queryUserMap(List<Comment> comments) {
        List<Long> userIds = new ArrayList<>();
        for (Comment comment : comments) {
            if (comment.getUserId() != null && !userIds.contains(comment.getUserId())) {
                userIds.add(comment.getUserId());
            }
        }
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            for (User user : userService.listByIds(userIds)) {
                userMap.put(user.getId(), user);
            }
        }
        return userMap;
    }

    private Map<Long, Spot> querySpotMap(List<Comment> comments) {
        List<Long> spotIds = new ArrayList<>();
        for (Comment comment : comments) {
            if (comment.getSpotId() != null && !spotIds.contains(comment.getSpotId())) {
                spotIds.add(comment.getSpotId());
            }
        }
        Map<Long, Spot> spotMap = new HashMap<>();
        if (!spotIds.isEmpty()) {
            for (Spot spot : spotService.listByIds(spotIds)) {
                spotMap.put(spot.getId(), spot);
            }
        }
        return spotMap;
    }

    private List<String> parseTags(String tags) {
        List<String> result = new ArrayList<>();
        if (tags == null || tags.trim().isEmpty()) {
            return result;
        }
        String[] arr = tags.split(",");
        for (String tag : arr) {
            String t = tag == null ? "" : tag.trim();
            if (!t.isEmpty()) {
                result.add(t);
            }
        }
        return result;
    }
}
