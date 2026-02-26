package com.yuelu.dto;

import lombok.Data;

/**
 * 发布评论请求参数。
 *
 * <p>前端只需要提交景点 ID、评论内容和评分，userId 一律由后端根据 Token 解析，
 * 避免前端伪造他人用户 ID 进行恶意评论。</p>
 */
@Data
public class CommentAddDTO {

    /**
     * 景点 ID。
     */
    private Long spotId;

    /**
     * 评论内容。
     */
    private String content;

    /**
     * 评分（1-5 星）。
     */
    private Integer star;
}

