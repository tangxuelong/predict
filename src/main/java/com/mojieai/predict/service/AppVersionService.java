package com.mojieai.predict.service;

import com.mojieai.predict.entity.po.AppVersion;

import java.util.Map;

public interface AppVersionService {

    Map<String, Object> versionControl(String clientType, String versionCode, String channelName);

    Map<String, Object> getAllVersion(Integer clientId);

    Map<String,Object> getIosReview(Integer versionCode, Integer clientType, String visitorIp);

    Map<String, Object> addAppVersion(AppVersion appVersion);

    Map<String,Object> updateForceUpgrade(Integer versionId, Integer forceUpgrade);
}
