package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.PayChannelInfoDao;
import com.mojieai.predict.entity.po.PayChannelInfo;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PayChannelInfoDaoImpl extends BaseDao implements PayChannelInfoDao {
    @Override
    public List<PayChannelInfo> getAllChannel() {
        return sqlSessionTemplate.selectList("PayChannelInfo.getAllChannel");
    }

    @Override
    public PayChannelInfo getChannel(Integer channelId) {
        return sqlSessionTemplate.selectOne("PayChannelInfo.getChannel", channelId);
    }

    @Override
    public void update(PayChannelInfo payChannelInfo) {
        sqlSessionTemplate.update("PayChannelInfo.update", payChannelInfo);
    }

    @Override
    public void insert(PayChannelInfo payChannelInfo) {
        sqlSessionTemplate.insert("PayChannelInfo.insert", payChannelInfo);
    }
}
