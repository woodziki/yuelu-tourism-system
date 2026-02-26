package com.yuelu.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuelu.common.Result;
import com.yuelu.entity.Spot;
import com.yuelu.service.SpotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 景点控制器。
 *
 * <p>提供景点相关的 REST API 接口。
 * 按照 PRD 要求，所有接口返回统一使用 Result<T> 包装。</p>
 */
@RestController
@RequestMapping("/spot")
public class SpotController {

    @Autowired
    private SpotService spotService;

    /**
     * 分页查询景点列表。
     *
     * <p>支持按名称模糊搜索和按标签筛选。
     * 例如：GET /spot/list?current=1&size=10&name=爱晚亭&tags=人文</p>
     *
     * @param current 当前页码（默认 1）
     * @param size 每页大小（默认 10）
     * @param name 景点名称（模糊搜索，可选）
     * @param tags 标签（筛选，可选）
     * @return 分页结果
     */
    @GetMapping("/list")
    public Result<IPage<Spot>> list(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String tags) {
        Page<Spot> page = new Page<>(current, size);
        IPage<Spot> result = spotService.listSpots(page, name, tags);
        return Result.success(result);
    }

    /**
     * 查询景点详情。
     *
     * <p>根据景点 ID 查询详细信息。
     * 例如：GET /spot/1</p>
     *
     * @param id 景点 ID
     * @return 景点详情
     */
    @GetMapping("/{id}")
    public Result<Spot> getById(@PathVariable Long id) {
        Spot spot = spotService.getSpotById(id);
        if (spot == null) {
            return Result.error("景点不存在");
        }
        return Result.success(spot);
    }
}
