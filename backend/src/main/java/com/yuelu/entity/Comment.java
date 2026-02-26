package com.yuelu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 评论表实体：t_comment
 *
 * <p>严格对应 PRD 5.5：id/user_id/spot_id/content/star。</p>
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
}

