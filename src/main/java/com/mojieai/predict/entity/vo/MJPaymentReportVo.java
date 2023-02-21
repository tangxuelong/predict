package com.mojieai.predict.entity.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class MJPaymentReportVo implements Serializable {
    private static final long serialVersionUID = 4652666018345488209L;

    private String date;
    private Integer total_income;
    private Integer total_pay_user_num;
    private Integer vip_income;
    private Integer coin_income;
    private Integer program_income;
    private Integer subscribe_income;
    private Integer big_data_income;
    private Integer prediction_income;
    private Integer weixin_income;
    private Integer weixin_num;
    private Integer zhifubao_num;
    private Integer zhifubao_income;
    private Integer apple_income;
    private Integer apple_num;
    private String repurchase_rate;
    private Integer repurchase_income;
}
