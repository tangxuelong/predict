package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class UserCouponFlow implements Serializable {
    private static final long serialVersionUID = -1583750264201313549L;

    private String couponFlowId;
    private Long userId;
    private String couponId;
    private String exchangeId;
    private String remark;
    private Timestamp createTime;
    private Timestamp updateTime;

    public UserCouponFlow(String couponFlowId, Long userId, String exchangeId, String couponId) {
        this.couponFlowId = couponFlowId;
        this.userId = userId;
        this.couponId = couponId;
        this.exchangeId = exchangeId;
    }
}
