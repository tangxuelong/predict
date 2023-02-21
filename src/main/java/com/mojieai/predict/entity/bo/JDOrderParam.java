package com.mojieai.predict.entity.bo;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.util.DateUtil;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
public class JDOrderParam implements Serializable {
    private static final long serialVersionUID = -4505890910798830805L;

    private String version;
    private String sign;
    private String merchant;
    private String tradeNum;
    private String tradeName;
    private String tradeTime;
    private Long amount;
    private String orderType;
    private String currency;
    private String callBackUrl;
    private String notifyUrl;
    private String ip;
    private String specCardNo;
    private String userId;

    public JDOrderParam(String sign, String merchant, String tradeNum, Timestamp tradeTime, Long amount, String
            callBackUrl, String notifyUrl, String ip, String specCardNo, String userId) {
        this.version = "V2.0";
        this.sign = sign;
        this.merchant = merchant;
        this.tradeNum = tradeNum;
        this.tradeName = CommonConstant.OUT_TRADE_GOODS_NAME;
        this.tradeTime = DateUtil.formatTime(tradeTime, "yyyyMMddHHmmss");
        this.amount = amount;
        this.orderType = "1";
        this.currency = "CNY";
        this.callBackUrl = callBackUrl;
        this.notifyUrl = notifyUrl;
        this.ip = ip;
        this.specCardNo = specCardNo;
        this.userId = userId;
    }
}
