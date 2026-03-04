package com.yuelu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论表实体：t_comment
 *
 * <p>对应 PRD 5.5：id/user_id/spot_id/content/star，并在不破坏原有字段的前提下，
 * 增加 create_time 字段用于记录评论时间，便于前端展示“评价时间”。</p>
 */
@Data
@TableName("t_comment")
public class Comment {

    /**
     * 主键（自增）。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 景点 ID。
     */
    private Long spotId;

    /**
     * 评论内容。
     */
    private String content;

    /**
     * 评分：1-5 星。
     */
    private Integer star;

    /**
     * 评论创建时间。
     */
    @TableField("create_time")
    private LocalDateTime createTime;
}

