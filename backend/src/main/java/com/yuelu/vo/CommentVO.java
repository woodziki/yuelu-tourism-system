package com.yuelu.vo;

import lombok.Data;

/**
 * 评论展示视图对象。
 *
 * <p>用于将评论表与用户表的信息组合后返回前端，包含：</p>
 * <ul>
 *     <li>评论 ID / 用户 ID / 景点 ID；</li>
 *     <li>用户昵称（来自 t_user.nickname）；</li>
 *     <li>评分星级（1-5）；</li>
 *     <li>评论内容；</li>
 *     <li>评论时间（createTime 的字符串形式）。</li>
 * </ul>
 */
@Data
public class CommentVO {

    private Long id;

    private Long userId;

    private Long spotId;

    /**
     * 评价人昵称。
     */
    private String nickname;

    /**
     * 评分：1-5 星。
     */
    private Integer star;

    /**
     * 评价内容。
     */
    private String content;

    /**
     * 评论时间（格式化后的字符串，如 "2026-03-04 12:30"）。
     */
    private String time;

    /**
     * 景点名称（后台评论管理使用）。
     */
    private String spotName;

    /**
     * 评论创建时间（后台评论管理使用，格式化字符串）。
     *
     * <p>与 time 含义一致，保留两个字段是为了兼容前台旧代码与后台新表格字段名。</p>
     */
    private String createTime;
}

