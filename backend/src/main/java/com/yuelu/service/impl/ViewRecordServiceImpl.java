package com.yuelu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuelu.entity.ViewRecord;
import com.yuelu.mapper.ViewRecordMapper;
import com.yuelu.service.ViewRecordService;
import org.springframework.stereotype.Service;

/**
 * 浏览记录服务实现。
 */
@Service
public class ViewRecordServiceImpl extends ServiceImpl<ViewRecordMapper, ViewRecord> implements ViewRecordService {
}
