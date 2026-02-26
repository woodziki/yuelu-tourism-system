package com.yuelu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuelu.entity.Comment;
import com.yuelu.mapper.CommentMapper;
import com.yuelu.service.CommentService;
import org.springframework.stereotype.Service;

/**
 * 评论服务实现类。
 *
 * <p>继承 ServiceImpl<CommentMapper, Comment>，自动获得基础的 CRUD 能力。</p>
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
}
