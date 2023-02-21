package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.AppChannelVersion;

import java.util.List;

public interface AppChannelVersionDao {

    List<AppChannelVersion> getAllAppChannelVersion();

    AppChannelVersion getAppChannelVersionByUniqueKey(Integer channelId, Integer versionId);

    Integer insert(AppChannelVersion appChannelVersion);
}
