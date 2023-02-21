package com.mojieai.predict.entity.po;

import com.mojieai.predict.util.DateUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class UserDeviceMonthReport implements Serializable {
    private static final long serialVersionUID = -7569626553460823183L;

    private Integer newActivetionUserNum;
    private Integer newUserNum;
    private Long dailyIncome;
    private Long maxDailyIncome;
    private Integer totalUserNum;
    private Long totalIncome;
    private Integer totalActivetionUserNum;
    private Integer wauDevice;
    private Integer wau;
    private Integer payMoneyUserNum;
    private String dateId;
    private Timestamp createTime;
    private Timestamp updateTime;

    public UserDeviceMonthReport(String dateId) {
        this.dateId = dateId;
        this.createTime = DateUtil.getCurrentTimestamp();
        this.updateTime = DateUtil.getCurrentTimestamp();
    }



//    {
//        "apple_all_amount": 89800,
//            "apple_daily_amount": 67232600//,
//        "daily_per_cnt": 0,
//        "backup_01": 0,
//        "pay_money_new_user_num": 5871//,

//        "new_activetion_user_num": 153391//新增设备数,
//        "new_user_num": 82360//新增用户数,
//            "daily_income": 402985200//当月收入,
//        "max_daily_income": 259800//当月最高付费金额,
//            "total_user_num": 248260//总注册用户数,
//        "total_income": 877646787//总收入,
//        "total_activetion_user_num": 486863//总设备数,
//        "wau_device": 1006104//月活设备数,
//        "wau": 592726//越活用户,
//        "pay_money_user_num": 11984//月付费用户数,
//        "date": "201807"//月
//    }
}
