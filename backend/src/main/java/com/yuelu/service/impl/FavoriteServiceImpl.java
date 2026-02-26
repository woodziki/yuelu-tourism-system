package com.yuelu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuelu.entity.Favorite;
import com.yuelu.mapper.FavoriteMapper;
import com.yuelu.service.FavoriteService;
import org.springframework.stereotype.Service;

/**
 * 收藏服务实现类。
 *
 * <p>继承 ServiceImpl<FavoriteMapper, Favorite>，自动获得基础的 CRUD 能力。</p>
 */
@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {
}
