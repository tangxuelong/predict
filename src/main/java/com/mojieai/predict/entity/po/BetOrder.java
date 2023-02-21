package com.mojieai.predict.entity.po;

import com.mojieai.predict.util.DateUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class BetOrder {
    private Integer orderId;
    private Long userId;
    private String matchId;
    private Integer amount;
    private Integer award;
    private Integer status;
    private String content;
    private Integer isMoreAward; // 是否嘉奖
    private Timestamp createTime;
    private Timestamp updateTime;

    public BetOrder(Integer orderId, Long userId, String matchId, Integer amount, Integer award, Integer status,
                    String content, Integer isMoreAward) {
        this.orderId = orderId;
        this.userId = userId;
        this.matchId = matchId;
        this.amount = amount;
        this.award = award;
        this.status = status;
        this.content = content;
        this.isMoreAward = isMoreAward;
        this.createTime = DateUtil.getCurrentTimestamp();
        this.updateTime = DateUtil.getCurrentTimestamp();
    }
}
