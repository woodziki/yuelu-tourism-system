package com.yuelu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuelu.entity.Spot;
import com.yuelu.mapper.SpotMapper;
import com.yuelu.service.SpotService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 景点服务实现类。
 *
 * <p>继承 ServiceImpl<SpotMapper, Spot>，自动获得基础的 CRUD 能力。
 * 实现自定义的业务方法，如分页查询、条件筛选等。</p>
 */
@Service
public class SpotServiceImpl extends ServiceImpl<SpotMapper, Spot> implements SpotService {

    @Override
    public IPage<Spot> listSpots(Page<Spot> page, String name, String tags) {
        // 构建查询条件（Lambda 表达式，避免字段名写错）
        LambdaQueryWrapper<Spot> queryWrapper = new LambdaQueryWrapper<>();

        // 如果传入了名称，进行模糊搜索（LIKE %name%）
        if (StringUtils.hasText(name)) {
            queryWrapper.like(Spot::getName, name);
        }

        // 如果传入了标签，进行精确匹配（tags 字段包含该标签）
        // 注意：PRD 中 tags 是逗号分隔的字符串，这里使用 LIKE 匹配
        if (StringUtils.hasText(tags)) {
            queryWrapper.like(Spot::getTags, tags);
        }

        // 按浏览量降序排列（热门优先）
        queryWrapper.orderByDesc(Spot::getViewCount);

        // 执行分页查询
        return this.page(page, queryWrapper);
    }

    @Override
    public Spot getSpotById(Long id) {
        return this.getById(id);
    }
}
