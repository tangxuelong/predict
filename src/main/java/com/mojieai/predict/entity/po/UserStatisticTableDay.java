package com.mojieai.predict.entity.po;

import com.mojieai.predict.util.DateUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * 用户统计日报
 *
 * @author Tangxuelong
 */
@Data
@NoArgsConstructor
public class UserStatisticTableDay {
    private Integer dateId;
    private Integer deviceTotalNum;
    private Integer deviceNewNum;
    private Integer deviceDayActiveNum;
    private Integer deviceAndroidActiveNum;
    private Integer deviceIosActiveNum;
    private Integer deviceDayActiveHistoryNum;
    private Integer userTotalRegisterNum;
    private Integer userNewNum;
    private Integer userDayActiveNum;
    private Integer userAndroidActiveNum;
    private Integer userIosActiveNum;
    private Integer userDayVipActiveNum;
    private Integer userDayPayNum;
    private Integer dayIncome;
    private Integer remarkIncome;
    private Integer maxPay;
    private Integer totalIncome;
    private Timestamp createTime;
    private Timestamp updateTime;

    public UserStatisticTableDay(Integer dateId) {
        this.dateId = dateId;
        this.createTime = DateUtil.getCurrentTimestamp();
        this.updateTime = DateUtil.getCurrentTimestamp();
    }
}