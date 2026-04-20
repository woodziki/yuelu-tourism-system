package com.yuelu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuelu.entity.Comment;
import com.yuelu.entity.Spot;
import com.yuelu.entity.User;
import com.yuelu.mapper.CommentMapper;
import com.yuelu.service.CommentService;
import com.yuelu.service.SpotService;
import com.yuelu.service.UserService;
import com.yuelu.vo.CommentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 评论服务实现类。
 *
 * <p>继承 ServiceImpl<CommentMapper, Comment>，自动获得基础的 CRUD 能力。</p>
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Autowired
    private UserService userService;

    @Autowired
    private SpotService spotService;

    @Override
    public IPage<CommentVO> listAdminComments(Page<Comment> page, String keyword) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();

        // 关键字搜索：对评论内容做 LIKE
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Comment::getContent, keyword.trim());
        }

        // 按创建时间倒序（最新优先）
        wrapper.orderByDesc(Comment::getCreateTime);

        // 1) 先分页查询出 Comment
        IPage<Comment> commentPage = this.page(page, wrapper);
        return buildCommentVoPage(commentPage, true);
    }

    @Override
    public IPage<CommentVO> listSpotComments(Page<Comment> page, Long spotId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<Comment>()
                .eq(Comment::getSpotId, spotId)
                .orderByDesc(Comment::getCreateTime)
                .orderByDesc(Comment::getId);
        IPage<Comment> commentPage = this.page(page, wrapper);
        return buildCommentVoPage(commentPage, false);
    }

    @Override
    public IPage<CommentVO> listMyComments(Page<Comment> page, Long userId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<Comment>()
                .eq(Comment::getUserId, userId)
                .orderByDesc(Comment::getCreateTime)
                .orderByDesc(Comment::getId);
        IPage<Comment> commentPage = this.page(page, wrapper);
        return buildCommentVoPage(commentPage, true);
    }

    private IPage<CommentVO> buildCommentVoPage(IPage<Comment> commentPage, boolean includeSpotName) {
        List<Comment> records = commentPage.getRecords();
        List<CommentVO> voRecords = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        Map<Long, User> userMap = queryUserMap(records);
        Map<Long, Spot> spotMap = includeSpotName ? querySpotMap(records) : new HashMap<>();

        for (Comment c : records) {
            CommentVO vo = new CommentVO();
            vo.setId(c.getId());
            vo.setUserId(c.getUserId());
            vo.setSpotId(c.getSpotId());
            vo.setContent(c.getContent());
            vo.setStar(c.getStar());

            User user = c.getUserId() == null ? null : userMap.get(c.getUserId());
            vo.setNickname(user == null ? "未知用户" : user.getNickname());

            if (includeSpotName) {
                Spot spot = c.getSpotId() == null ? null : spotMap.get(c.getSpotId());
                vo.setSpotName(spot == null ? "未知景点" : spot.getName());
            }

            if (c.getCreateTime() != null) {
                String t = c.getCreateTime().format(formatter);
                vo.setTime(t);
                vo.setCreateTime(t);
            }

            voRecords.add(vo);
        }

        Page<CommentVO> voPage = new Page<>(commentPage.getCurrent(), commentPage.getSize());
        voPage.setTotal(commentPage.getTotal());
        voPage.setRecords(voRecords);
        return voPage;
    }

    private Map<Long, User> queryUserMap(List<Comment> records) {
        Set<Long> userIds = new HashSet<>();
        for (Comment c : records) {
            if (c.getUserId() != null) {
                userIds.add(c.getUserId());
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

    private Map<Long, Spot> querySpotMap(List<Comment> records) {
        Set<Long> spotIds = new HashSet<>();
        for (Comment c : records) {
            if (c.getSpotId() != null) {
                spotIds.add(c.getSpotId());
            }
        }
        Map<Long, Spot> spotMap = new HashMap<>();
        for (Long spotId : spotIds) {
            Spot spot = spotService.getSpotByIdPlain(spotId);
            if (spot != null) {
                spotMap.put(spot.getId(), spot);
            }
        }
        return spotMap;
    }
}
