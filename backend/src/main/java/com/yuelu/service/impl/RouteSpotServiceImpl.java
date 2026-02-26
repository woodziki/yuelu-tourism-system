package com.yuelu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuelu.entity.RouteSpot;
import com.yuelu.mapper.RouteSpotMapper;
import com.yuelu.service.RouteSpotService;
import org.springframework.stereotype.Service;

/**
 * 线路-景点关联服务实现类。
 *
 * <p>继承 ServiceImpl<RouteSpotMapper, RouteSpot>，自动获得基础的 CRUD 能力。</p>
 */
@Service
public class RouteSpotServiceImpl extends ServiceImpl<RouteSpotMapper, RouteSpot> implements RouteSpotService {
}
