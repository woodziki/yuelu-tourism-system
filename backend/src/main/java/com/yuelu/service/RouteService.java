package com.yuelu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yuelu.dto.RouteDTO;
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

    /**
     * 后台管理：分页查询线路列表，按名称模糊搜索，按 ID 降序。
     *
     * @param page    分页对象
     * @param keyword 关键字（可为空，对 name 做 LIKE）
     * @return 线路分页数据
     */
    IPage<Route> listAdminRoutes(Page<Route> page, String keyword);

    /**
     * 后台管理：新增线路，并关联景点。
     *
     * @param dto 线路 DTO（含基本信息和 spotIds）
     */
    void addRoute(RouteDTO dto);

    /**
     * 后台管理：更新线路，先删除旧关联再插入新关联。
     *
     * @param dto 线路 DTO（必须含 id）
     */
    void updateRoute(RouteDTO dto);

    /**
     * 后台管理：删除线路及其关联的 t_route_spot 记录。
     *
     * @param id 线路 ID
     */
    void deleteRoute(Long id);

    /**
     * 获取线路关联的景点 ID 列表（按 sort 升序）。
     *
     * @param routeId 线路 ID
     * @return 景点 ID 列表
     */
    List<Long> getSpotIdsByRouteId(Long routeId);
}
