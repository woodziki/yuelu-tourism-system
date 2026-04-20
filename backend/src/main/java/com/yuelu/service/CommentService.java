package com.yuelu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yuelu.entity.Comment;
import com.yuelu.vo.CommentVO;

/**
 * 评论服务接口。
 *
 * <p>继承 MyBatis-Plus 的 IService，提供基础的 CRUD 方法。</p>
 */
public interface CommentService extends IService<Comment> {

    /**
     * 后台管理：分页查询全站评论（支持按关键字搜索评论内容）。
     *
     * <p>用于后台评论管理模块，返回 CommentVO（包含景点名称、用户昵称、评分、内容与时间）。</p>
     *
     * @param page    分页参数
     * @param keyword 关键字（对 content 做 LIKE 模糊查询，可为空）
     * @return 评论分页数据
     */
    IPage<CommentVO> listAdminComments(Page<Comment> page, String keyword);

    /**
     * 前台景点详情：分页查询指定景点评论。
     *
     * @param page   分页参数
     * @param spotId 景点 ID
     * @return 评论分页数据
     */
    IPage<CommentVO> listSpotComments(Page<Comment> page, Long spotId);

    /**
     * 用户中心：分页查询当前用户发布过的评论。
     *
     * @param page   分页参数
     * @param userId 当前用户 ID
     * @return 评论分页数据
     */
    IPage<CommentVO> listMyComments(Page<Comment> page, Long userId);
}