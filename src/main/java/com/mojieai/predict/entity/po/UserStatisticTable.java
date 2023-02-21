package com.mojieai.predict.entity.po;

import com.mojieai.predict.util.DateUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * 活动奖级配置表
 *
 * @author Singal
 */
@Data
@NoArgsConstructor
public class UserStatisticTable {
    private Integer dateId;
    private Integer newDeviceCount;
    private Integer newUserRegisterCount;
    private Timestamp createTime;
    private Timestamp updateTime;

    public UserStatisticTable(Integer dateId, Integer newDeviceCount, Integer newUserRegisterCount) {
        this.dateId = dateId;
        this.newDeviceCount = newDeviceCount;
        this.newUserRegisterCount = newUserRegisterCount;
        this.createTime = DateUtil.getCurrentTimestamp();
        this.updateTime = DateUtil.getCurrentTimestamp();
    }
}