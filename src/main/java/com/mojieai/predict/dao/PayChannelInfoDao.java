package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.PayChannelInfo;

import java.util.List;

public interface PayChannelInfoDao {
    List<PayChannelInfo> getAllChannel();

    PayChannelInfo getChannel(Integer channelId);

    void update(PayChannelInfo payChannelInfo);

    void insert(PayChannelInfo payChannelInfo);
}
