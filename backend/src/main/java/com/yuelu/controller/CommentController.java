package com.yuelu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuelu.common.Result;
import com.yuelu.dto.CommentAddDTO;
import com.yuelu.entity.Comment;
import com.yuelu.entity.User;
import com.yuelu.exception.AuthException;
import com.yuelu.mapper.UserMapper;
import com.yuelu.service.CommentService;
import com.yuelu.vo.CommentVO;
import com.yuelu.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评论控制器。
 *
 * <p>提供针对景点的发表评论和查询评论接口。</p>
 *
 * <p>安全要求：
 * - 查询评论接口允许所有人访问（不需要 Token）；
 * - 发布评论必须先登录，且 userId 一律从 Token 中解析，绝不信任前端传来的 userId。</p>
 */
@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtUtil jwtUtil;

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String PREFIX_BEARER = "Bearer ";

    /**
     * 发布评论（需要登录）。
     *
     * <p>前端只提交景点 ID、评论内容与评分；后端从 Token 中解析 userId，
     * 然后写入评论记录。</p>
     *
     * @param dto     发布评论请求参数（spotId/content/star）
     * @param request HttpServletRequest，用于读取请求头中的 Token
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result<Void> addComment(@RequestBody CommentAddDTO dto, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);

        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setSpotId(dto.getSpotId());
        comment.setContent(dto.getContent());
        comment.setStar(dto.getStar());

        commentService.save(comment);
        return Result.success();
    }

    /**
     * 后台管理：分页查询全站评论（支持关键字搜索）。
     *
     * <p>接口：GET /comment/adminList?current=1&size=10&keyword=xxx</p>
     *
     * @param current 当前页（默认 1）
     * @param size    每页大小（默认 10）
     * @param keyword 关键字（可为空，对 content 做 LIKE）
     * @return 评论分页数据（CommentVO）
     */
    @GetMapping("/adminList")
    public Result<IPage<CommentVO>> adminList(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String keyword) {
        Page<Comment> page = new Page<>(current, size);
        IPage<CommentVO> result = commentService.listAdminComments(page, keyword);
        return Result.success(result);
    }

    /**
     * 后台管理：物理删除评论（违规内容处置）。
     *
     * @param id 评论 ID
     * @return 操作结果
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        commentService.removeById(id);
        return Result.success();
    }

    /**
     * 查询某个景点的所有评论列表（按时间倒序）。
     *
     * <p>本接口对所有用户开放访问权限，不需要携带 Token。</p>
     *
     * <p>返回 CommentVO 列表，包含评价人昵称、评分、内容与时间等信息，便于前端直接展示。</p>
     *
     * @param spotId 景点 ID（必填）
     * @return 评论视图对象列表
     */
    @GetMapping("/list")
    public Result<List<CommentVO>> listComments(@RequestParam Long spotId) {
        // 1. 查询该景点的所有评论记录，按 create_time（若为空则按 id）倒序
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<Comment>()
                .eq(Comment::getSpotId, spotId)
                .orderByDesc(Comment::getCreateTime)
                .orderByDesc(Comment::getId);
        List<Comment> comments = commentService.list(wrapper);
        if (comments.isEmpty()) {
            return Result.success(new ArrayList<>());
        }

        // 2. 收集所有涉及到的用户 ID，批量查询用户信息以获取昵称
        java.util.Set<Long> userIds = new java.util.HashSet<>();
        for (Comment c : comments) {
            if (c.getUserId() != null) {
                userIds.add(c.getUserId());
            }
        }
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            for (User u : users) {
                userMap.put(u.getId(), u);
            }
        }

        // 3. 组装 CommentVO 列表
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        List<CommentVO> voList = new ArrayList<>();
        for (Comment c : comments) {
            CommentVO vo = new CommentVO();
            vo.setId(c.getId());
            vo.setUserId(c.getUserId());
            vo.setSpotId(c.getSpotId());
            vo.setContent(c.getContent());
            vo.setStar(c.getStar());
            User user = c.getUserId() == null ? null : userMap.get(c.getUserId());
            vo.setNickname(user == null ? "匿名用户" : user.getNickname());
            if (c.getCreateTime() != null) {
                vo.setTime(c.getCreateTime().format(formatter));
            }
            voList.add(vo);
        }
        return Result.success(voList);
    }

    /**
     * 从请求头中解析出当前登录用户的 userId。
     *
     * <p>步骤与收藏模块一致：
     * 1. 从 Header 中读取 Authorization 字段；
     * 2. 解析出 Bearer 后面的 Token；
     * 3. 调用 JwtUtil#getUserId(token) 得到 userId。
     * 若解析失败，统一抛出 AuthException，由全局异常处理器返回「请先登录」。</p>
     *
     * @param request HttpServletRequest
     * @return 当前登录用户的 userId
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String auth = request.getHeader(HEADER_AUTHORIZATION);
        String token = null;
        if (auth != null && auth.startsWith(PREFIX_BEARER)) {
            token = auth.substring(PREFIX_BEARER.length()).trim();
        }
        Long userId = jwtUtil.getUserId(token);
        if (userId == null) {
            throw new AuthException();
        }
        return userId;
    }
}

