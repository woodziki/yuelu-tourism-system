package com.yuelu.controller;

import com.yuelu.common.Result;
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
}
