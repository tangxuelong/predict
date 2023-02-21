package com.mojieai.predict.util.yopBill;

import com.yeepay.g3.yop.sdk.auth.YopCredentialsWithRSA;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;

public class CustomYopCredentials extends YopCredentialsWithRSA implements Serializable {
    private static final long serialVersionUID = -3111182583345389719L;
    private String appKey;
    private PublicKey yopPublicKey;
    private String partPath;

    public CustomYopCredentials(String appKey, PublicKey yopPublicKey, PrivateKey isvPrivateKey, String partPath) {
        super(appKey, yopPublicKey, isvPrivateKey);
        this.appKey = appKey;
        this.yopPublicKey = yopPublicKey;
        this.partPath = partPath;
    }

    public String getAppKey() {
        return this.appKey;
    }

    public PublicKey getYopPublicKey() {
        return this.yopPublicKey;
    }

    public String getPartPath() {
        return this.partPath;
    }
}
