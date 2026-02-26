package com.yuelu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yuelu.entity.Spot;

/**
 * 景点服务接口。
 *
 * <p>继承 MyBatis-Plus 的 IService，提供基础的 CRUD 和批量操作方法。
 * 自定义业务方法在此接口中定义，由实现类实现。</p>
 */
public interface SpotService extends IService<Spot> {

    /**
     * 分页查询景点列表（支持按名称模糊搜索、按标签筛选）。
     *
     * @param page 分页对象（包含页码、每页大小）
     * @param name 景点名称（模糊搜索，可为空）
     * @param tags 标签（筛选，可为空）
     * @return 分页结果
     */
    IPage<Spot> listSpots(Page<Spot> page, String name, String tags);

    /**
     * 根据 ID 查询景点详情。
     *
     * @param id 景点 ID
     * @return 景点对象
     */
    Spot getSpotById(Long id);
}
