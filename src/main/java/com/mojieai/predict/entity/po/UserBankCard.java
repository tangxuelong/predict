package com.mojieai.predict.entity.po;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class UserBankCard implements Serializable {
    private static final long serialVersionUID = 429246751113052193L;

    private Integer bankId;
    private String bankCard;
    private Integer cardType;
    private Long userId;
    private String accountName;
    private String mobile;
    private String bankCn;
    private String authenticateStatus;
    private Integer authenticateMerchant;
    private String remark;
    private Integer status;
    private Timestamp createTime;
    private Timestamp updateTime;

    public UserBankCard(String bankCard, Integer cardType, Long userId, String accountName, String mobile, String
            bankCn, String authenticateStatus, Integer authenticateMerchant, String jdBankCardEn, Integer status) {
        this.bankCard = bankCard;
        this.cardType = cardType;
        this.userId = userId;
        this.accountName = accountName;
        this.mobile = mobile;
        this.bankCn = bankCn;
        this.authenticateMerchant = authenticateMerchant;
        this.authenticateStatus = authenticateStatus;
        this.status = status;
        if (StringUtils.isNotBlank(jdBankCardEn)) {
            Map<String, Object> remarkMap = new HashMap<>();
            remarkMap.put("jdBankCardEn", jdBankCardEn);
            this.remark = JSONObject.toJSONString(remarkMap);
        }
    }
}
