package com.mojieai.predict.entity.po;

import com.mojieai.predict.constant.VipMemberConstant;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.VipUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class VipOperateFollow {
    private String vipOperateCode;
    private Long userId;
    private String vipCode;
    private Integer operateType;
    private String operateDesc;
    private String transactionAmount;
    private Integer transactionDays;
    private Integer lastVipLevel;
    private Integer currentVipLevel;
    private Integer isPay;
    private String exchangeFlowId;
    private Integer vipType;
    private Timestamp payTime;
    private Timestamp createTime;

    public void toPayedInstance(String vipOperateCode, Long userId, String vipCode, Long dateCount, String exchangeFlowId, String
            transactionAmount, Integer operateType, Integer sourceType, Integer vipType) {
        this.vipOperateCode = vipOperateCode;
        this.userId = userId;
        this.vipCode = vipCode;
        this.transactionDays = dateCount.intValue();
        this.exchangeFlowId = exchangeFlowId;
        this.transactionAmount = transactionAmount;
        this.isPay = VipMemberConstant.VIP_IS_PAIED_YES;
        this.lastVipLevel = VipMemberConstant.VIP_LEVEL_ONE;
        this.currentVipLevel = VipMemberConstant.VIP_LEVEL_ONE;
        this.payTime = DateUtil.getCurrentTimestamp();
        this.operateType = operateType;
        String desc = VipUtils.getVipOperateFollowDesc(operateType, transactionAmount, dateCount.intValue(),
                sourceType);
        this.operateDesc = desc;
        this.vipType = vipType;
    }

    public void toUnpaidInstance(Long userId, String vipOperateId, int dateCount, String money, String vipCode,
                                 Integer sourceType, Integer vipType) {
        this.userId = userId;
        this.transactionDays = dateCount;
        this.transactionAmount = money;
        this.isPay = VipMemberConstant.VIP_IS_PAIED_NO;
        this.lastVipLevel = VipMemberConstant.VIP_LEVEL_ONE;
        this.currentVipLevel = VipMemberConstant.VIP_LEVEL_ONE;
        this.operateType = VipMemberConstant.VIP_FOLLOW_OPERATE_TYPE_CASH_PURCHASE;
        this.vipOperateCode = vipOperateId;
        this.vipCode = vipCode;
        String desc = VipUtils.getVipOperateFollowDesc(VipMemberConstant.VIP_FOLLOW_OPERATE_TYPE_CASH_PURCHASE, money,
                dateCount, sourceType);
        this.operateDesc = desc;
        this.vipType = vipType;
    }
}
