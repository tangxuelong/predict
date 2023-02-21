package com.mojieai.predict.entity.po;

import com.mojieai.predict.constant.UserAccountConstant;
import com.mojieai.predict.util.UserAccountUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class UserWisdomCoinFlow implements Serializable {
    private static final long serialVersionUID = 2521285541707468911L;

    private String flowId;
    private Long userId;
    private Integer exchangeType;
    private Long exchangeAmount;
    private Long exchangeWisdomAmount;
    private String exchangeName;
    private Integer isPay;
    private String memo;
    private Integer goodsId;
    private String exchangeFlowId;
    private Timestamp createTime;
    private Timestamp updateTime;

    public void initUserWisdomCoinFlow(String flowId, Long userId, String exchangeName, Integer exchangeType, Long
            amount, Long exchangeWisdomAmount, Integer goodsId) {
        this.flowId = flowId;
        this.userId = userId;
        this.exchangeType = exchangeType;
        this.exchangeName = exchangeName;
        this.exchangeAmount = amount;
        this.exchangeWisdomAmount = exchangeWisdomAmount;
        this.isPay = UserAccountConstant.IS_PAIED_NO;
        this.goodsId = goodsId;
        this.memo = UserAccountUtil.getInitUserWisdomCoinMemo(exchangeType, amount, exchangeWisdomAmount);
    }

    public void initConsumeUserWisdomFlow(String flowId, Long userId, String exchangeName, Long wisdomAmount, String
            exchangeFlowId) {
        this.flowId = flowId;
        this.userId = userId;
        this.exchangeType = UserAccountConstant.WISDOM_COIN_EXCHANGE_TYPE_CONSUME;
        this.exchangeName = exchangeName;
        this.exchangeAmount = 0l;
        this.exchangeWisdomAmount = wisdomAmount;
        this.isPay = UserAccountConstant.IS_PAIED_YES;
        this.goodsId = null;
        this.exchangeFlowId = exchangeFlowId;
        this.memo = UserAccountUtil.getInitUserWisdomCoinMemo(this.exchangeType, null, wisdomAmount);
    }
}
