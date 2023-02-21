package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.PayClientChannelDao;
import com.mojieai.predict.entity.po.PayClientChannel;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class PayClientChannelDaoImpl extends BaseDao implements PayClientChannelDao {
    @Override
    public List<PayClientChannel> getAllClientChannel() {
        return sqlSessionTemplate.selectList("PayClientChannel.getAllClientChannel");
    }

    @Override
    public PayClientChannel getClientChannel(Integer clientId, Integer channelId) {
        Map<String, Object> params = new HashMap<>();
        params.put("clientId", clientId);
        params.put("channelId", channelId);
        return sqlSessionTemplate.selectOne("PayClientChannel.getClientChannel", params);
    }

    @Override
    public void update(PayClientChannel payClientChannel) {
        sqlSessionTemplate.update("PayClientChannel.update", payClientChannel);
    }

    @Override
    public void insert(PayClientChannel payClientChannel) {
        sqlSessionTemplate.insert("PayClientChannel.insert", payClientChannel);
    }
}
