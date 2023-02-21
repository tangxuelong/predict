package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.PayClientChannel;

import java.util.List;

public interface PayClientChannelDao {
    List<PayClientChannel> getAllClientChannel();

    PayClientChannel getClientChannel(Integer clientId, Integer channelId);

    void update(PayClientChannel payClientChannel);

    void insert(PayClientChannel payClientChannel);
}
