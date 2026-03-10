package com.yuelu.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuelu.common.Result;
import com.yuelu.dto.RouteDTO;
import com.yuelu.entity.Route;
import com.yuelu.service.RouteService;
import com.yuelu.vo.RouteVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /**
     * 查询所有线路列表（含线路下的景点信息）。
     *
     * <p>通过线路-景点关联表 t_route_spot 查询每条线路包含的景点，
     * 并按 sort 字段（游玩顺序）排序后封装为 RouteVO 返回。</p>
     *
     * @return 线路视图对象列表
     */
    @GetMapping("/list")
    public Result<List<RouteVO>> list() {
        List<RouteVO> routes = routeService.listAllRoutesWithSpots();
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
