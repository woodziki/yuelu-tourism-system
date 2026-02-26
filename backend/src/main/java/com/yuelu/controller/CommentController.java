package com.yuelu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuelu.common.Result;
import com.yuelu.dto.CommentAddDTO;
import com.yuelu.entity.Comment;
import com.yuelu.exception.AuthException;
import com.yuelu.service.CommentService;
import com.yuelu.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

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
     * 分页查询某个景点的所有评论（按时间倒序）。
     *
     * <p>本接口对所有用户开放访问权限，不需要携带 Token。</p>
     *
     * <p>由于 PRD 中评论表仅包含 id/user_id/spot_id/content/star，
     * 没有单独的时间字段，这里采用 id 倒序来近似表示「按时间倒序」。</p>
     *
     * @param spotId  景点 ID（必填）
     * @param current 当前页码（默认 1）
     * @param size    每页大小（默认 10）
     * @return 分页结果
     */
    @GetMapping("/list")
    public Result<IPage<Comment>> listComments(
            @RequestParam Long spotId,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        Page<Comment> page = new Page<>(current, size);
        LambdaQueryWrapper<Comment> query = new LambdaQueryWrapper<Comment>()
                .eq(Comment::getSpotId, spotId)
                .orderByDesc(Comment::getId);
        IPage<Comment> result = commentService.page(page, query);
        return Result.success(result);
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

