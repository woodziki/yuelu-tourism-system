package com.yuelu.controller;

import com.yuelu.common.Result;
import com.yuelu.service.DashboardService;
import com.yuelu.vo.AdminDashboardVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台数据看板控制器。
 */
@RestController
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * 查询后台数据看板。
     *
     * @return 后台数据看板聚合数据
     */
    @GetMapping
    public Result<AdminDashboardVO> dashboard() {
        return Result.success(dashboardService.getDashboard());
    }
}
