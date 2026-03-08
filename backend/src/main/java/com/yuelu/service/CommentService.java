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
}
