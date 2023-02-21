package com.mojieai.predict.entity.po;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.PayConstant;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class UserResonanceLog implements Serializable {
    private static final long serialVersionUID = -8834247222108721165L;

    private String resonanceLogId;
    private Long userId;
    private long gameId;
    private Integer startPeriod;
    private Integer lastPeriod;
    private Long amount;
    private Long payAmount;
    private Integer isPay;
    private Integer status;
    private Timestamp createTime;
    private Timestamp updateTime;

    public void initUserResonanceLog(String resonanceLogId, Long userId, long gameId, Integer startPeriod, Integer
            lastPeriod, Long amount, Long payAmount) {
        this.resonanceLogId = resonanceLogId;
        this.userId = userId;
        this.gameId = gameId;
        this.startPeriod = startPeriod;
        this.lastPeriod = lastPeriod;
        this.amount = amount;
        this.isPay = CommonConstant.PROGRAM_IS_PAY_NO;
        this.status = PayConstant.OUT_TRADE_ORDER_CONFLICT_INIT;
        this.payAmount = payAmount;
    }
}
