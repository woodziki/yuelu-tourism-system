package com.yuelu.vo;

import com.yuelu.entity.Spot;
import lombok.Data;

import java.util.List;

/**
 * 线路视图对象：用于向前端返回“线路 + 线路下所有景点”的组合信息。
 *
 * <p>相较于简单的 Route 实体，RouteVO 额外包含一个景点列表字段，
 * 按游玩顺序（t_route_spot.sort）排列。</p>
 */
@Data
public class RouteVO {

    /** 线路 ID */
    private Long id;

    /** 线路名称 */
    private String name;

    /** 线路简介 */
    private String intro;

    /** 静态地图路径 */
    private String mapImg;

    /** 线路标签 */
    private String tags;

    /** 该线路包含的景点列表（按游玩顺序排序） */
    private List<Spot> spots;
}

