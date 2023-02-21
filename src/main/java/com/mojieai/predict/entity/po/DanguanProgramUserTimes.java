package com.mojieai.predict.entity.po;

import com.mojieai.predict.util.DateUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * 单关方案 用户购买卡后剩余的次数
 *
 * @author tangxuelong
 */
@Data
@NoArgsConstructor
public class DanguanProgramUserTimes {
    private Long userId;
    private Integer leftTimes;
    private Timestamp createTime;
    private Timestamp updateTime;

    public DanguanProgramUserTimes(Long userId,Integer leftTimes){
        this.userId = userId;
        this.leftTimes = leftTimes;
        this.createTime = DateUtil.getCurrentTimestamp();
        this.updateTime = DateUtil.getCurrentTimestamp();
    }
}