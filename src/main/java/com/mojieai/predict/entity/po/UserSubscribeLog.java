package com.mojieai.predict.entity.po;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.PayConstant;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class UserSubscribeLog implements Serializable {
    private static final long serialVersionUID = 410753447820399847L;

    private String subscribeId;
    private Long userId;
    private Integer programId;
    private Integer payStatus;
    private Long payAmount;
    private Long programAmount;
    private Integer beginPeriod;
    private Integer lastPeriod;
    private Integer status;
    private Timestamp createTime;
    private Timestamp updateTime;

    public void initUserSubscribeLog(String subscribeId, Long userId, Integer programId, Long payAmount, Long
            programAmount, Integer beginPeriod, Integer lastPeriod) {
        this.subscribeId = subscribeId;
        this.userId = userId;
        this.programId = programId;
        this.payStatus = CommonConstant.PROGRAM_IS_PAY_NO;
        this.payAmount = payAmount;
        this.programAmount = programAmount;
        this.beginPeriod = beginPeriod;
        this.lastPeriod = lastPeriod;
        this.status = PayConstant.OUT_TRADE_ORDER_CONFLICT_INIT;
    }
}
