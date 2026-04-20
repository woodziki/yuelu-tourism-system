package com.yuelu.service;

import com.yuelu.vo.AdminDashboardVO;

/**
 * 后台数据看板服务接口。
 */
public interface DashboardService {

    /**
     * 查询后台数据看板聚合数据。
     *
     * @return 后台看板数据
     */
    AdminDashboardVO getDashboard();
}
