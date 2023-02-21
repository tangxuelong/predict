package com.mojieai.predict.entity.po;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.util.DateUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * 账户流水
 *
 * @author Singal
 */
@Data
@NoArgsConstructor
public class UserAccountFlow {
    private String flowId; //流水ID
    private Long userId; //用户ID
    private String payId; //需要支付的订单ID
    private Long totalAmount; //需要支付的总金额
    private Integer payType;  //支付的方式
    private Integer channel;  //支付的渠道类型
    private Long payAmount; // 需要支付的金额
    private Timestamp payTime; // 支付时间
    private String payDesc;   //支付描述
    private Integer status;   // 支付状态
    private String clientIp;
    private Integer clientId;
    private String prePayId;
    private Integer operateType;  //增加 减少
    private String transactionId;
    private String remark;
    private Timestamp createTime;
    private Timestamp updateTime;

    public UserAccountFlow(String flowId, Long userId, String payId, Long totalAmount, Integer payType, Integer channel,
                           Long payAmount, String payDesc, String clientIp, Integer clientId, String prePayId, Integer
                                   operateType) {
        this.flowId = flowId;
        this.userId = userId;
        this.payId = payId;
        this.totalAmount = totalAmount;
        this.payType = payType;
        this.channel = channel;
        this.payAmount = payAmount;
        this.payDesc = payDesc;
        this.status = CommonConstant.PAY_STATUS_START;
        this.clientIp = clientIp;
        this.clientId = clientId;
        this.prePayId = prePayId;
        this.operateType = operateType;
        this.createTime = DateUtil.getCurrentTimestamp();
        this.updateTime = DateUtil.getCurrentTimestamp();
    }
}