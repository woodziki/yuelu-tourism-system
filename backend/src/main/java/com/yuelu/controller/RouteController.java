package com.yuelu.controller;

import com.yuelu.common.Result;
import com.yuelu.entity.Route;
import com.yuelu.service.RouteService;
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
     * 查询所有线路列表。
     *
     * <p>返回所有线路的基本信息（包含 tags 和简介）。
     * 例如：GET /route/list</p>
     *
     * @return 线路列表
     */
    @GetMapping("/list")
    public Result<List<Route>> list() {
        List<Route> routes = routeService.listAllRoutes();
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
}
