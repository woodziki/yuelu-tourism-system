package com.yuelu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 浏览记录实体：t_view_record
 *
 * <p>用于采集隐式反馈行为，后续参与推荐算法。</p>
 */
@Data
@TableName("t_view_record")
public class ViewRecord {

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
     * 浏览时间。
     */
    @TableField("create_time")
    private LocalDateTime createTime;
}
