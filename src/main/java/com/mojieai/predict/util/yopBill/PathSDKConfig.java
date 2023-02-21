package com.mojieai.predict.util.yopBill;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yeepay.g3.yop.sdk.YopClientException;
import com.yeepay.g3.yop.sdk.config.CertConfig;
import com.yeepay.g3.yop.sdk.config.ConfigUtils;
import com.yeepay.g3.yop.sdk.util.JsonUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

@Data
public class PathSDKConfig implements Serializable {
    private static final long serialVersionUID = -6361038050739193240L;

    private static final String DEFAULT_SDK_CONFIG_FILE = "/config/yop_sdk_config_default.json";
    private static volatile PathSDKConfig config;
    @JsonProperty("app_key")
    private String appKey;
    @JsonProperty("aes_secret_key")
    private String aesSecretKey;
    @JsonProperty("server_root")
    private String serverRoot;
    @JsonProperty("yos_server_root")
    private String yosServerRoot;
    @JsonProperty("yop_public_key")
    private CertConfig[] yopPublicKey;
    @JsonProperty("isv_private_key")
    private CertConfig[] isvPrivateKey;
    @JsonProperty("connect_timeout")
    private Integer connectTimeout = 30000;
    @JsonProperty("read_timeout")
    private Integer readTimeout = 60000;
    private String partPath;

    public PathSDKConfig() {
    }

    public static PathSDKConfig getConfig(String partPath) {
        if (config == null || !config.getPartPath().equals(partPath)) {
            Class var0 = PathSDKConfig.class;
            synchronized (PathSDKConfig.class) {
                if (config == null || !config.getPartPath().equals(partPath)) {
                    if (StringUtils.isBlank(partPath)) {
                        partPath = DEFAULT_SDK_CONFIG_FILE;
                    }
                    config = loadConfig(partPath);
                    config.setPartPath(partPath);
                }
            }
        }

        return config;
    }

    private static PathSDKConfig loadConfig(String configFile) {
        InputStream fis = null;

        PathSDKConfig config;
        try {
            fis = ConfigUtils.getInputStream(configFile);
            config = (PathSDKConfig) JsonUtils.loadFrom(fis, PathSDKConfig.class);
        } catch (Exception var11) {
            throw new YopClientException("Errors occurred when loading SDK Config.", var11);
        } finally {
            if (null != fis) {
                try {
                    fis.close();
                } catch (IOException var10) {
                    var10.printStackTrace();
                }
            }

        }

        return config;
    }

}
