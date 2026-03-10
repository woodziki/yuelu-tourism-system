package com.yuelu.dto;

import lombok.Data;

import java.util.List;

/**
 * 线路数据传输对象。
 *
 * <p>用于后台新增/修改线路，包含线路基本信息和关联的景点 ID 列表。
 * spotIds 的顺序即为游玩顺序（sort 字段）。</p>
 */
@Data
public class RouteDTO {

    /** 线路 ID（新增时为空，修改时必填） */
    private Long id;

    /** 线路名称 */
    private String name;

    /** 线路简介 */
    private String intro;

    /** 静态地图/横幅图片路径 */
    private String mapImg;

    /** 线路标签（如：人文,古迹） */
    private String tags;

    /** 关联的景点 ID 列表，顺序即为游玩顺序 */
    private List<Long> spotIds;
}
