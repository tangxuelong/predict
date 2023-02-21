package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class PaymentStatisticReport implements Serializable {
    private static final long serialVersionUID = 913551018357760854L;

    private Integer statisticDate;
    private Long incomeAmount;
    private Integer payPersonCount;
    private String incomeDetail;
    private String payChannelDetail;
    private Long repurchaseAmount;
    private Integer statisticFlag;
    private Timestamp createTime;
    private Timestamp updateTime;
}
