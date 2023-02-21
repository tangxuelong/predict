package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.AppChannel;

import java.util.List;

public interface AppChannelDao {

    List<AppChannel> getAllAppChannel();

    AppChannel getAppChannelByChannelName(String channelName);

    Integer insert(AppChannel appChannel);
}
