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
import java.util.List;

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
        List<Comment> records = commentPage.getRecords();

        // 2) 组装 CommentVO
        List<CommentVO> voRecords = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (Comment c : records) {
            CommentVO vo = new CommentVO();
            vo.setId(c.getId());
            vo.setUserId(c.getUserId());
            vo.setSpotId(c.getSpotId());
            vo.setContent(c.getContent());
            vo.setStar(c.getStar());

            // 关联用户昵称
            if (c.getUserId() != null) {
                User user = userService.getById(c.getUserId());
                vo.setNickname(user == null ? "未知用户" : user.getNickname());
            } else {
                vo.setNickname("未知用户");
            }

            // 关联景点名称
            if (c.getSpotId() != null) {
                Spot spot = spotService.getSpotById(c.getSpotId());
                vo.setSpotName(spot == null ? "未知景点" : spot.getName());
            } else {
                vo.setSpotName("未知景点");
            }

            // 时间格式化（同时填充 time 与 createTime，兼容前台与后台字段名）
            if (c.getCreateTime() != null) {
                String t = c.getCreateTime().format(formatter);
                vo.setTime(t);
                vo.setCreateTime(t);
            }

            voRecords.add(vo);
        }

        // 3) 构建 IPage<CommentVO> 返回（保留 total/size/current 等分页信息）
        Page<CommentVO> voPage = new Page<>(commentPage.getCurrent(), commentPage.getSize());
        voPage.setTotal(commentPage.getTotal());
        voPage.setRecords(voRecords);
        return voPage;
    }
}
