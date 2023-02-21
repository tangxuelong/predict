package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.AppVersion;

import java.util.List;

public interface AppVersionDao {

    List<AppVersion> getAllAppVersion(Integer clientId);

    AppVersion getAppVersionByUnikey(Integer clientId, Integer versionCode);

    AppVersion getLatestAppVersionByClientId(Integer clientId);

    AppVersion getAppVersionByPk(Integer versionId);

    Integer insert(AppVersion appVersion);

    Integer update(AppVersion appVersion);
}
