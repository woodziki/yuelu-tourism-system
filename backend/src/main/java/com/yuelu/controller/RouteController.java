package com.yuelu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuelu.common.Result;
import com.yuelu.dto.RouteDTO;
import com.yuelu.entity.Comment;
import com.yuelu.entity.Favorite;
import com.yuelu.entity.Route;
import com.yuelu.service.RouteService;
import com.yuelu.service.CommentService;
import com.yuelu.service.FavoriteService;
import com.yuelu.util.JwtUtil;
import com.yuelu.vo.RouteVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 线路控制器。
 *
 * <p>提供线路相关的 REST API 接口。
 * 按照 PRD 要求，所有接口返回统一使用 Result<T> 包装。</p>
 */
@RestController
@RequestMapping("/route")
public class RouteController {

    @Autowired
    private RouteService routeService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private CommentService commentService;

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String PREFIX_BEARER = "Bearer ";

    /**
     * 查询所有线路列表（含线路下的景点信息）。
     *
     * <p>通过线路-景点关联表 t_route_spot 查询每条线路包含的景点，
     * 并按 sort 字段（游玩顺序）排序后封装为 RouteVO 返回。</p>
     *
     * @return 线路视图对象列表
     */
    @GetMapping("/list")
    public Result<List<RouteVO>> list(HttpServletRequest request) {
        // 允许未登录：解析不到 token 或 token 非法时 userId 置空，不抛错
        Long userId = null;
        try {
            String auth = request.getHeader(HEADER_AUTHORIZATION);
            if (auth != null && auth.startsWith(PREFIX_BEARER)) {
                String token = auth.substring(PREFIX_BEARER.length()).trim();
                userId = jwtUtil.getUserId(token);
            }
        } catch (Exception ignored) {
            userId = null;
        }

        // 1) 获取线路列表（含 spots）
        List<RouteVO> routes = routeService.listAllRoutesWithSpots();
        if (routes == null || routes.isEmpty()) {
            return Result.success(Collections.emptyList());
        }

        // 2) 提取用户偏好集合 U（收藏 + 高分评论>=4），去重
        Set<Long> userSpotIds = new HashSet<>();
        if (userId != null) {
            List<Favorite> favorites = favoriteService.list(
                    new LambdaQueryWrapper<Favorite>()
                            .eq(Favorite::getUserId, userId)
                            .select(Favorite::getSpotId)
            );
            for (Favorite f : favorites) {
                if (f.getSpotId() != null) userSpotIds.add(f.getSpotId());
            }

            List<Comment> highStarComments = commentService.list(
                    new LambdaQueryWrapper<Comment>()
                            .eq(Comment::getUserId, userId)
                            .ge(Comment::getStar, 4)
                            .select(Comment::getSpotId)
            );
            for (Comment c : highStarComments) {
                if (c.getSpotId() != null) userSpotIds.add(c.getSpotId());
            }
        }

        // 3) 计算 Score = |U ∩ R|，并写入 matchScore
        for (RouteVO route : routes) {
            int score = 0;
            if (route != null && route.getSpots() != null && !userSpotIds.isEmpty()) {
                for (com.yuelu.entity.Spot s : route.getSpots()) {
                    if (s != null && s.getId() != null && userSpotIds.contains(s.getId())) {
                        score++;
                    }
                }
            }
            route.setMatchScore(score);
        }

        // 4) 若已登录，则按 matchScore 降序排序
        if (userId != null) {
            routes.sort(
                    Comparator.comparing(RouteVO::getMatchScore, Comparator.nullsFirst(Integer::compareTo))
                            .reversed()
                            .thenComparing(RouteVO::getId, Comparator.nullsLast(Comparator.reverseOrder()))
            );
        }
        return Result.success(routes);
    }

    /**
     * 查询线路详情。
     *
     * <p>根据线路 ID 查询详细信息。
     * 注意：当前版本暂不返回关联的景点列表，后续步骤会添加。
     * 例如：GET /route/1</p>
     *
     * @param id 线路 ID
     * @return 线路详情
     */
    @GetMapping("/{id}")
    public Result<Route> getById(@PathVariable Long id) {
        Route route = routeService.getRouteById(id);
        if (route == null) {
            return Result.error("线路不存在");
        }
        return Result.success(route);
    }

    /**
     * 获取线路关联的景点 ID 列表（按游玩顺序）。
     *
     * @param id 线路 ID
     * @return 景点 ID 列表
     */
    @GetMapping("/{id}/spotIds")
    public Result<List<Long>> getSpotIds(@PathVariable Long id) {
        List<Long> spotIds = routeService.getSpotIdsByRouteId(id);
        return Result.success(spotIds);
    }

    /**
     * 后台管理：分页查询线路列表。
     *
     * @param current 页码（默认 1）
     * @param size    每页条数（默认 10）
     * @param keyword 关键字（可选，对 name 模糊匹配）
     * @return 线路分页数据
     */
    @GetMapping("/adminList")
    public Result<IPage<Route>> adminList(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String keyword) {
        Page<Route> page = new Page<>(current, size);
        IPage<Route> result = routeService.listAdminRoutes(page, keyword);
        return Result.success(result);
    }

    /**
     * 后台管理：新增线路。
     *
     * @param dto 线路 DTO（含基本信息和 spotIds）
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result<Void> add(@RequestBody RouteDTO dto) {
        routeService.addRoute(dto);
        return Result.success();
    }

    /**
     * 后台管理：更新线路。
     *
     * @param dto 线路 DTO（必须含 id）
     * @return 操作结果
     */
    @PutMapping("/update")
    public Result<Void> update(@RequestBody RouteDTO dto) {
        if (dto.getId() == null) {
            return Result.error("线路 ID 不能为空");
        }
        routeService.updateRoute(dto);
        return Result.success();
    }

    /**
     * 后台管理：删除线路。
     *
     * @param id 线路 ID
     * @return 操作结果
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        routeService.deleteRoute(id);
        return Result.success();
    }
}
