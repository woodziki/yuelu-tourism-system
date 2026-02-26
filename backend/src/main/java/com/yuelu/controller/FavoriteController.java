package com.yuelu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuelu.common.Result;
import com.yuelu.entity.Favorite;
import com.yuelu.exception.AuthException;
import com.yuelu.service.FavoriteService;
import com.yuelu.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 收藏控制器。
 *
 * <p>提供添加收藏、取消收藏、分页查询「我的收藏」接口。</p>
 *
 * <p>核心安全要求：绝不能信任前端传来的 userId，一律从 Token 中解析当前登录用户。
 * 这样可以避免恶意伪造 userId 操作他人数据，有利于后续基于真实用户行为做协同过滤推荐。</p>
 */
@RestController
@RequestMapping("/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private JwtUtil jwtUtil;

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String PREFIX_BEARER = "Bearer ";

    /**
     * 添加收藏。
     *
     * <p>前端只需要传景点 ID（spotId），后端从 Token 中解析出当前登录用户的 userId，
     * 然后以「userId + spotId」为维度写入收藏记录。</p>
     *
     * @param spotId  景点 ID
     * @param request HttpServletRequest，用于读取请求头中的 Token
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result<Void> addFavorite(@RequestParam Long spotId, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);

        // 为避免重复收藏，这里先根据 userId + spotId 查询是否已存在记录
        LambdaQueryWrapper<Favorite> query = new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getSpotId, spotId);
        Favorite existed = favoriteService.getOne(query);
        if (existed != null) {
            // 已收藏直接返回成功，保持幂等性，便于前端反复点击「收藏」按钮
            return Result.success();
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setSpotId(spotId);
        favoriteService.save(favorite);
        return Result.success();
    }

    /**
     * 取消收藏。
     *
     * <p>同样从 Token 中解析 userId，仅删除当前登录用户对某个景点的收藏记录。</p>
     *
     * @param spotId  景点 ID
     * @param request HttpServletRequest
     * @return 操作结果
     */
    @PostMapping("/cancel")
    public Result<Void> cancelFavorite(@RequestParam Long spotId, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);

        LambdaQueryWrapper<Favorite> query = new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getSpotId, spotId);
        favoriteService.remove(query);
        return Result.success();
    }

    /**
     * 分页查询「我的收藏」列表。
     *
     * <p>只根据当前登录用户的 userId 查询其所有收藏记录，
     * 这里返回的是收藏表记录本身（包含 spotId），前端如需展示景点详情，可以基于 spotId 再调用景点详情接口。</p>
     *
     * @param current 当前页码（默认 1）
     * @param size    每页大小（默认 10）
     * @param request HttpServletRequest
     * @return 分页结果
     */
    @GetMapping("/my")
    public Result<IPage<Favorite>> listMyFavorite(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);

        Page<Favorite> page = new Page<>(current, size);
        LambdaQueryWrapper<Favorite> query = new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId);
        IPage<Favorite> result = favoriteService.page(page, query);
        return Result.success(result);
    }

    /**
     * 从请求头中解析出当前登录用户的 userId。
     *
     * <p>步骤：
     * 1. 从 Header 中读取 Authorization 字段；
     * 2. 解析出 Bearer 后面的 Token；
     * 3. 调用 JwtUtil#getUserId(token) 得到 userId。
     * 若任一步骤失败，统一抛出 AuthException，由全局异常处理器返回「请先登录」。</p>
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
            // 理论上 JwtInterceptor 已经做过 verify，这里主要是兜底；若解析失败，统一视为未登录
            throw new AuthException();
        }
        return userId;
    }
}

