package com.mojieai.predict.entity.po;

import com.mojieai.predict.util.DateUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class UserDeviceWeekReport implements Serializable {

    private Integer newActivetionUserNum;  //活跃用户
    private Integer newUserNum;  // 新用户
    private Long dailyIncome;  // 日收入
    private Long maxDailyIncome; //
    private Integer totalUserNum;
    private Long totalIncome;
    private Integer totalActivetionUserNum;
    private Integer wauDevice;
    private Integer wau;
    private Integer payMoneyUserNum;
    private String dateId;
    private Timestamp createTime;
    private Timestamp updateTime;

    public UserDeviceWeekReport(String dateId) {
        this.dateId = dateId;
        this.createTime = DateUtil.getCurrentTimestamp();
        this.updateTime = DateUtil.getCurrentTimestamp();
    }


//    "backup_01": 0//都是0不知道干啥,
//            "apple_daily_amount": 603900//没用,
//            "apple_all_amount": 48800//没用,
//            "pay_money_new_user_num": 56//没用到,
//            "daily_per_cnt": 0//不知道干啥的每个都是0,

//            "new_activetion_user_num": 1303//新增设备数,
//            "new_user_num": 874//新增用户数,
//            "daily_income": 3695100//周收入以及备注收入,
//            "max_daily_income": 259800//最改付费金额,
//            "total_user_num": 248243//总注册用户数,
//            "total_income": 877552887//总收入,
//            "total_activetion_user_num": 486839//总设备数,
//            "wau_device": 33842//周活跃设备数,
//            "wau": 24054//周活跃用户数,
//            "pay_money_user_num": 349//周付费用户数,
//            "date": "20180730~20180731"//日期
}
