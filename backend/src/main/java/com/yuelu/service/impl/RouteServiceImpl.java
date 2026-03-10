package com.yuelu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuelu.dto.RouteDTO;
import com.yuelu.entity.Route;
import com.yuelu.entity.RouteSpot;
import com.yuelu.entity.Spot;
import com.yuelu.mapper.RouteMapper;
import com.yuelu.mapper.RouteSpotMapper;
import com.yuelu.mapper.SpotMapper;
import com.yuelu.service.RouteService;
import com.yuelu.vo.RouteVO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 线路服务实现类。
 *
 * <p>继承 ServiceImpl<RouteMapper, Route>，自动获得基础的 CRUD 能力。</p>
 */
@Service
public class RouteServiceImpl extends ServiceImpl<RouteMapper, Route> implements RouteService {

    private final RouteSpotMapper routeSpotMapper;
    private final SpotMapper spotMapper;

    public RouteServiceImpl(RouteSpotMapper routeSpotMapper, SpotMapper spotMapper) {
        this.routeSpotMapper = routeSpotMapper;
        this.spotMapper = spotMapper;
    }

    @Override
    public List<Route> listAllRoutes() {
        // 查询所有线路（后续可按需添加排序）
        return this.list();
    } 

    @Override
    public Route getRouteById(Long id) {
        return this.getById(id);
    }

    @Override
    public List<RouteVO> listAllRoutesWithSpots() {
        // 1. 查询所有线路
        List<Route> routes = this.list();
        if (routes.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        // 2. 提取所有 routeId，批量查询 t_route_spot 关联表
        java.util.Set<Long> routeIds = new java.util.HashSet<>();
        for (Route r : routes) {
            routeIds.add(r.getId());
        }
        List<RouteSpot> routeSpots = routeSpotMapper.selectList(
                new LambdaQueryWrapper<RouteSpot>()
                        .in(RouteSpot::getRouteId, routeIds)
                        .orderByAsc(RouteSpot::getRouteId)
                        .orderByAsc(RouteSpot::getSort)
        );

        if (routeSpots.isEmpty()) {
            // 没有关联景点时，仅返回线路基本信息
            List<RouteVO> result = new java.util.ArrayList<>();
            for (Route r : routes) {
                RouteVO vo = new RouteVO();
                vo.setId(r.getId());
                vo.setName(r.getName());
                vo.setIntro(r.getIntro());
                vo.setMapImg(r.getMapImg());
                vo.setTags(r.getTags());
                vo.setSpots(java.util.Collections.emptyList());
                result.add(vo);
            }
            return result;
        }

        // 3. 提取所有涉及到的 spotId，一次性从 t_spot 查询详细信息
        java.util.Set<Long> spotIds = new java.util.HashSet<>();
        for (RouteSpot rs : routeSpots) {
            if (rs.getSpotId() != null) {
                spotIds.add(rs.getSpotId());
            }
        }
        java.util.Map<Long, Spot> spotMap = new java.util.HashMap<>();
        if (!spotIds.isEmpty()) {
            List<Spot> spots = spotMapper.selectBatchIds(spotIds);
            for (Spot s : spots) {
                spotMap.put(s.getId(), s);
            }
        }

        // 4. 将每条线路的景点按 sort 顺序组装到 RouteVO 中
        java.util.Map<Long, java.util.List<Spot>> routeIdToSpots = new java.util.HashMap<>();
        for (RouteSpot rs : routeSpots) {
            Spot spot = spotMap.get(rs.getSpotId());
            if (spot == null) {
                continue;
            }
            routeIdToSpots
                    .computeIfAbsent(rs.getRouteId(), k -> new java.util.ArrayList<>())
                    .add(spot);
        }

        List<RouteVO> voList = new java.util.ArrayList<>();
        for (Route r : routes) {
            RouteVO vo = new RouteVO();
            vo.setId(r.getId());
            vo.setName(r.getName());
            vo.setIntro(r.getIntro());
            vo.setMapImg(r.getMapImg());
            vo.setTags(r.getTags());
            List<Spot> spots = routeIdToSpots.get(r.getId());
            vo.setSpots(spots == null ? java.util.Collections.emptyList() : spots);
            voList.add(vo);
        }
        return voList;
    }

    @Override
    public IPage<Route> listAdminRoutes(Page<Route> page, String keyword) {
        LambdaQueryWrapper<Route> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Route::getName, keyword);
        }
        wrapper.orderByDesc(Route::getId);
        return this.page(page, wrapper);
    }

    @Override
    public void addRoute(RouteDTO dto) {
        Route route = new Route();
        route.setName(dto.getName());
        route.setIntro(dto.getIntro());
        route.setMapImg(dto.getMapImg());
        route.setTags(dto.getTags());
        this.save(route);

        Long routeId = route.getId();
        if (!CollectionUtils.isEmpty(dto.getSpotIds())) {
            for (int i = 0; i < dto.getSpotIds().size(); i++) {
                RouteSpot rs = new RouteSpot();
                rs.setRouteId(routeId);
                rs.setSpotId(dto.getSpotIds().get(i));
                rs.setSort(i + 1);
                routeSpotMapper.insert(rs);
            }
        }
    }

    @Override
    public void updateRoute(RouteDTO dto) {
        if (dto.getId() == null) {
            throw new IllegalArgumentException("线路 ID 不能为空");
        }
        Route route = new Route();
        route.setId(dto.getId());
        route.setName(dto.getName());
        route.setIntro(dto.getIntro());
        route.setMapImg(dto.getMapImg());
        route.setTags(dto.getTags());
        this.updateById(route);

        routeSpotMapper.delete(new LambdaQueryWrapper<RouteSpot>().eq(RouteSpot::getRouteId, dto.getId()));

        if (!CollectionUtils.isEmpty(dto.getSpotIds())) {
            for (int i = 0; i < dto.getSpotIds().size(); i++) {
                RouteSpot rs = new RouteSpot();
                rs.setRouteId(dto.getId());
                rs.setSpotId(dto.getSpotIds().get(i));
                rs.setSort(i + 1);
                routeSpotMapper.insert(rs);
            }
        }
    }

    @Override
    public void deleteRoute(Long id) {
        routeSpotMapper.delete(new LambdaQueryWrapper<RouteSpot>().eq(RouteSpot::getRouteId, id));
        this.removeById(id);
    }

    @Override
    public List<Long> getSpotIdsByRouteId(Long routeId) {
        List<RouteSpot> list = routeSpotMapper.selectList(
                new LambdaQueryWrapper<RouteSpot>()
                        .eq(RouteSpot::getRouteId, routeId)
                        .orderByAsc(RouteSpot::getSort)
        );
        List<Long> spotIds = new ArrayList<>();
        for (RouteSpot rs : list) {
            if (rs.getSpotId() != null) {
                spotIds.add(rs.getSpotId());
            }
        }
        return spotIds;
    }
}
