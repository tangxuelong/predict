package com.mojieai.predict.entity.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class WithdrawMerchantBalanceResponse implements Serializable {
    private static final long serialVersionUID = 4768715596917741491L;

    private Integer code;
    private String msg;
    //分
    private Long merchantBalance;
    private Long merchantFrozenAmount;

    public WithdrawMerchantBalanceResponse(Integer code) {
        this.code = code;
    }
}
