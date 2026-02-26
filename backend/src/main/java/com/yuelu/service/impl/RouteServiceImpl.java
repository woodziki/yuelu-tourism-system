package com.yuelu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuelu.entity.Route;
import com.yuelu.mapper.RouteMapper;
import com.yuelu.service.RouteService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 线路服务实现类。
 *
 * <p>继承 ServiceImpl<RouteMapper, Route>，自动获得基础的 CRUD 能力。</p>
 */
@Service
public class RouteServiceImpl extends ServiceImpl<RouteMapper, Route> implements RouteService {

    @Override
    public List<Route> listAllRoutes() {
        // 查询所有线路（后续可按需添加排序）
        return this.list();
    } 

    @Override
    public Route getRouteById(Long id) {
        return this.getById(id);
    }
}
