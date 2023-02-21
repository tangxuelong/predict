package com.mojieai.predict.service.aliyunpush;

/**
 * Created by tangxuelong on 2017/11/9.
 */

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.mojieai.predict.cache.IniCache;

/**
 * 推送的OpenAPI文档 https://help.aliyun.com/document_detail/mobilepush/api-reference/openapi.html
 */
public class BasePush {
    protected static String region;
    //protected static long appKey;
    protected static String deviceId;

    protected static DefaultAcsClient client;

    /**
     * 从配置文件中读取配置值，初始化Client
     * <p>
     * 1. 如何获取 accessKeyId/accessKeySecret/appKey 照见README.md 中的说明<br/>
     * 2. 先在 push.properties 配置文件中 填入你的获取的值
     */
    public void init() {

        String accessKeyId = IniCache.getIniValue("accessKeyId", "LTAIBc5TWxsqBjfT");

        String accessKeySecret = IniCache.getIniValue("accessKeySecret", "D486pxacyLMFobBe3OkRfXrGBTfLyR");

        String key = IniCache.getIniValue("appKey", "24689647");
        //appKey = Long.parseLong(key);

        region = IniCache.getIniValue("regionId", "cn-beijing");

        IClientProfile profile = DefaultProfile.getProfile(region, accessKeyId, accessKeySecret);
        client = new DefaultAcsClient(profile);
    }
}
