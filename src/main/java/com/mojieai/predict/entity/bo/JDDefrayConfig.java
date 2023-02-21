package com.mojieai.predict.entity.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class JDDefrayConfig implements Serializable {
    private static final long serialVersionUID = -4094666076741680935L;

    private String defrayPayUrl;//代付
    private String queryBalanceUrl;//查询帐户余额
    private String tradeQueryUrl;//查询帐户余额
    private String customerNo;
    private String callBackUrl;
    private String filePwd;//秘钥文件的密码
}
