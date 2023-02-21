package com.mojieai.predict.util.yopBill;

import com.yeepay.g3.yop.sdk.YopClientException;
import com.yeepay.g3.yop.sdk.auth.*;
import com.yeepay.g3.yop.sdk.config.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PrivateKey;
import java.security.PublicKey;

public class CustomYopCredentialFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(YopCredentialFactory.class);
    private static volatile CustomYopCredentials defaultyYopCredentials;

    public CustomYopCredentialFactory() {
    }

    private static void initCredential(String partPath) {
        Class var0 = YopCredentialFactory.class;
        synchronized (YopCredentialFactory.class) {
            if (defaultyYopCredentials == null || !defaultyYopCredentials.getPartPath().equals(partPath)) {
                PathSDKConfig sdkConfig = PathSDKConfig.getConfig(partPath);
                PublicKey yopPubKey = ConfigUtils.loadPublicKey(sdkConfig.getYopPublicKey()[0]);
                PrivateKey isvPriKey = ConfigUtils.loadPrivateKey(sdkConfig.getIsvPrivateKey()[0]);
                defaultyYopCredentials = new CustomYopCredentialsWithRSA(sdkConfig.getAppKey(), yopPubKey, isvPriKey,
                        partPath).getCustomYopCredentials();
            }

        }
    }

    public static CustomYopCredentials getDefaultCredentials(String partPath) {
        if (defaultyYopCredentials == null || !defaultyYopCredentials.getPartPath().equals(partPath)) {
            initCredential(partPath);
        }

        return defaultyYopCredentials;
    }

    public static CustomYopCredentials getCustomCredentials(CustomCredential customCredential, String partPath) {
        if (defaultyYopCredentials == null) {
            initCredential(partPath);
        }

        Object yopCredentials;
        if (customCredential instanceof CustomCredentialWithAES) {
            yopCredentials = new YopCredentialsWithAES(customCredential.getAppKey(), defaultyYopCredentials.getYopPublicKey(), ((CustomCredentialWithAES) customCredential).getAesKey());
        } else {
            if (!(customCredential instanceof CustomCredentialWithRSA)) {
                throw new YopClientException("UnSupport CustomCredential.");
            }

            yopCredentials = new CustomYopCredentialsWithRSA(customCredential.getAppKey(), defaultyYopCredentials
                    .getYopPublicKey(), ((CustomCredentialWithRSA) customCredential).getPrivateKey(), partPath);
        }

        return (CustomYopCredentials) yopCredentials;
    }
}
