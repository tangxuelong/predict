package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class SysCardBin implements Serializable {
    private static final long serialVersionUID = 5327320178857445513L;

    private Integer cardId;
    private Long cardBin;
    private Integer cardType;
    private String cardName;
    private String primaryAccountNo;
    private Integer accountLen;
    private String issuerBankName;
    private String bankShortName;
    private String issuerBankCode;
    private String issuerBankProvince;
    private String issuerBankCity;
    private Integer status;
    private String bankImgUrl;
    private String bankBackUrl;
    private String memo;
}
