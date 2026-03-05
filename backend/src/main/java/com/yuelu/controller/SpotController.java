package com.yuelu.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuelu.common.Result;
import com.yuelu.entity.Spot;
import com.yuelu.exception.AuthException;
import com.yuelu.service.RecommendService;
import com.yuelu.service.SpotService;
import com.yuelu.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 景点控制器。
 *
 * <p>提供景点相关的 REST API 接口。
 * 按照 PRD 要求，所有接口返回统一使用 Result<T> 包装。</p>
 */
@RestController
@RequestMapping("/spot")
public class SpotController {

    @Autowired
    private SpotService spotService;

    @Autowired
    private RecommendService recommendService;

    @Autowired
    private JwtUtil jwtUtil;

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String PREFIX_BEARER = "Bearer ";

    /**
     * 分页查询景点列表。
     *
     * <p>支持按名称模糊搜索和按标签筛选。
     * 例如：GET /spot/list?current=1&size=10&name=爱晚亭&tags=人文</p>
     *
     * @param current 当前页码（默认 1）
     * @param size 每页大小（默认 10）
     * @param name 景点名称（模糊搜索，可选）
     * @param tags 标签（筛选，可选）
     * @return 分页结果
     */
    @GetMapping("/list")
    public Result<IPage<Spot>> list(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String tags) {
        Page<Spot> page = new Page<>(current, size);
        IPage<Spot> result = spotService.listSpots(page, name, tags);
        return Result.success(result);
    }

    /**
     * 后台管理专用：分页查询景点列表（按 ID 降序，最新优先）。
     *
     * <p>与前台 /spot/list 接口的入参一致，同样支持按名称和标签筛选。</p>
     */
    @GetMapping("/adminList")
    public Result<IPage<Spot>> adminList(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String tags) {
        Page<Spot> page = new Page<>(current, size);
        IPage<Spot> result = spotService.listAdminSpots(page, name, tags);
        return Result.success(result);
    }

    /**
     * 查询景点详情。
     *
     * <p>根据景点 ID 查询详细信息。
     * 例如：GET /spot/1</p>
     *
     * @param id 景点 ID
     * @return 景点详情
     */
    @GetMapping("/{id}")
    public Result<Spot> getById(@PathVariable Long id) {
        Spot spot = spotService.getSpotById(id);
        if (spot == null) {
            return Result.error("景点不存在");
        }
        return Result.success(spot);
    }

    /**
     * 根据当前登录用户的行为数据，返回个性化推荐景点列表。
     *
     * <p>安全要求：
     * 必须从请求头 Authorization 中获取 JWT Token，并通过 JwtUtil 解析出当前用户 ID，
     * 绝不能信任前端传来的 userId 参数。</p>
     *
     * <p>推荐逻辑：</p>
     * <ul>
     *     <li>当用户有收藏/评分等行为数据时：调用 User-based CF 协同过滤算法，基于余弦相似度计算推荐结果；</li>
     *     <li>当用户无任何行为数据时：触发冷启动策略，按 view_count 降序返回 Top 10 热门景点。</li>
     * </ul>
     *
     * @param request HttpServletRequest，用于获取请求头中的 Token
     * @return 推荐景点列表（Result<List<Spot>>）
     */
    @GetMapping("/recommend")
    public Result<List<Spot>> recommend(HttpServletRequest request) {
        String auth = request.getHeader(HEADER_AUTHORIZATION);
        String token = null;
        if (auth != null && auth.startsWith(PREFIX_BEARER)) {
            token = auth.substring(PREFIX_BEARER.length()).trim();
        }
        Long userId = jwtUtil.getUserId(token);
        if (userId == null) {
            // 未登录或 Token 无效，直接抛出认证异常，由全局异常处理器返回“请先登录”
            throw new AuthException();
        }

        // 这里 Top N 取 20，前端可以只展示前 10 或 20 条
        List<Spot> spots = recommendService.recommendForUser(userId, 20);
        return Result.success(spots);
    }

    /**
     * 后台管理：新增景点。
     *
     * <p>接收前端提交的 Spot 对象，调用 spotService.save(spot) 写入数据库。</p>
     *
     * @param spot 景点实体（id 可为空，自增）
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result<Void> add(@RequestBody Spot spot) {
        spotService.save(spot);
        return Result.success();
    }

    /**
     * 后台管理：更新景点。
     *
     * <p>根据 spot.id 更新记录，调用 spotService.updateById(spot)。</p>
     *
     * @param spot 景点实体（必须包含 id）
     * @return 操作结果
     */
    @PutMapping("/update")
    public Result<Void> update(@RequestBody Spot spot) {
        if (spot.getId() == null) {
            return Result.error("景点 ID 不能为空");
        }
        spotService.updateById(spot);
        return Result.success();
    }

    /**
     * 后台管理：删除景点。
     *
     * <p>根据路径参数 id 调用 spotService.removeById(id)。</p>
     *
     * @param id 景点 ID
     * @return 操作结果
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        spotService.removeById(id);
        return Result.success();
    }
}
