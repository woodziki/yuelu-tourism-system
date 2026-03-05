package com.yuelu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuelu.entity.Route;
import com.yuelu.vo.RouteVO;

import java.util.List;

/**
 * 线路服务接口。
 *
 * <p>继承 MyBatis-Plus 的 IService，提供基础的 CRUD 方法。</p>
 */
public interface RouteService extends IService<Route> {

    /**
     * 查询所有线路列表。
     *
     * @return 线路列表
     */
    List<Route> listAllRoutes();

    /**
     * 查询所有线路及其包含的景点列表。
     *
     * <p>按 PRD 的线路-景点关联关系，通过 t_route_spot 的 sort 字段确定游玩顺序，
     * 并返回 RouteVO（包含线路基本信息 + List<Spot>）。</p>
     *
     * @return 线路视图对象列表
     */
    List<RouteVO> listAllRoutesWithSpots();

    /**
     * 根据 ID 查询线路详情。
     *
     * @param id 线路 ID
     * @return 线路对象
     */
    Route getRouteById(Long id);
}
