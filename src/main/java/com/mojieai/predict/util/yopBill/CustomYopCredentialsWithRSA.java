package com.mojieai.predict.util.yopBill;

import com.yeepay.g3.yop.sdk.auth.YopCredentialsWithRSA;

import java.security.PrivateKey;
import java.security.PublicKey;

public class CustomYopCredentialsWithRSA extends YopCredentialsWithRSA {
    private static final long serialVersionUID = -1903803598956839329L;
    private PrivateKey isvPrivateKey;
    private String partPath;

    public CustomYopCredentialsWithRSA(String appKey, PublicKey yopPublicKey, PrivateKey isvPrivateKey, String
            partPath) {
        super(appKey, yopPublicKey, isvPrivateKey);
        this.isvPrivateKey = isvPrivateKey;
        this.partPath = partPath;
    }

    public PrivateKey getIsvPrivateKey() {
        return this.isvPrivateKey;
    }

    public String getPartPath() {
        return this.partPath;
    }

    public CustomYopCredentials getCustomYopCredentials() {
        return new CustomYopCredentials(getAppKey(), getYopPublicKey(), isvPrivateKey, this.partPath);
    }
}
