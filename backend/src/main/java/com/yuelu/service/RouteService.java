  package com.yuelu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuelu.entity.Route;

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
     * 根据 ID 查询线路详情。
     *
     * @param id 线路 ID
     * @return 线路对象
     */
    Route getRouteById(Long id);
}
