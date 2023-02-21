package com.mojieai.predict.entity.bo;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.VipMemberConstant;
import com.mojieai.predict.util.ProgramUtil;
import lombok.Data;

@Data
public class PrePayInfo {
    private String goodsId;
    private String goodsName;
    private Long price;
    private Long vipPrice;
    private Integer vipDiscount;
    private String iosMallId;
    private String vipIosMallId;
    private Integer vipDiscountWay = CommonConstant.VIP_DISCOUNT_WAY_DISCOUNT;
    private Integer goodsVipType = VipMemberConstant.VIP_MEMBER_TYPE_DIGIT;

    public PrePayInfo(String goodsId, String goodsName, Long price, Integer vipDiscount) {
        this.goodsId = goodsId;
        this.price = price;
        this.goodsName = goodsName;
        this.vipDiscount = vipDiscount;
    }

    public Long getUserNeedPayAmount(boolean isVip) {
        if (!isVip) {
            return this.price;
        }

        if (this.getVipDiscountWay().equals(CommonConstant.VIP_DISCOUNT_WAY_DISCOUNT)) {
            return ProgramUtil.getVipPrice(this.price, this.vipDiscount).longValue();
        } else {
            if (vipPrice != null) {
                return this.vipPrice;
            }
            return ProgramUtil.getVipPrice(this.price, this.vipDiscount).longValue();
        }
    }
}
