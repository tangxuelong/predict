package com.mojieai.predict.util.yopBill;

import com.yeepay.g3.yop.sdk.services.yos.YosClient;
import com.yeepay.g3.yop.sdk.services.yos.YosClientConfiguration;
import org.apache.commons.lang3.StringUtils;

public class PathConfiguration {
    private static volatile PartPathYosClient defaultYosClient;

    public PathConfiguration() {
    }

    public static YosClient getDefaultYosClient(String partPath) {
        if (isNeedNewYosClient(defaultYosClient, partPath)) {
            initYosClient(partPath);
        }
        return defaultYosClient.getYosClient();
    }

    private static void initYosClient(String partPath) {
        Class var0 = PathConfiguration.class;
        synchronized (PathConfiguration.class) {
            if (isNeedNewYosClient(defaultYosClient, partPath)) {
                YosClientConfiguration configuration = new YosClientConfiguration();
                configuration.setCredentials(CustomYopCredentialFactory.getDefaultCredentials(partPath));
                configuration.setConnectionTimeoutInMillis(PathSDKConfig.getConfig(partPath).getConnectTimeout());
                configuration.setEndpoint(StringUtils.removeEnd(PathSDKConfig.getConfig(partPath).getYosServerRoot(), "/yop-center"));
                defaultYosClient = new PartPathYosClient(new YosClient(configuration), partPath);
            }

        }
    }

    private static Boolean isNeedNewYosClient(PartPathYosClient defaultYosClient, String partPath) {
        if (defaultYosClient == null || defaultYosClient.getYosClient() == null || !defaultYosClient.getPartPath()
                .equals(partPath)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
}
