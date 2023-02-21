package com.mojieai.predict.entity.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class MJOrderStatisticReportVo implements Serializable {
    private static final long serialVersionUID = 5868616307639434305L;

    private Integer date;
    private Integer total_order_num;
    private Integer total_user_num;
    private Integer old_user_num;
    private Integer new_user_num;
    private String repurchase_rate;
    private Integer total_amount;
    private Integer old_user_amount;
    private Integer new_user_amount;
    private Integer real_pay_num;
    private Integer real_pay_amount;
}
