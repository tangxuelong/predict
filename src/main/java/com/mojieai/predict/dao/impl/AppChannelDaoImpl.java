package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.AppChannelDao;
import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.entity.po.AppChannel;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class AppChannelDaoImpl extends BaseDao implements AppChannelDao {
    @Override
    public List<AppChannel> getAllAppChannel() {
        return slaveSqlSessionTemplate.selectList("AppChannel.getAllAppChannel");
    }

    @Override
    public AppChannel getAppChannelByChannelName(String channelName) {
        Map param = new HashMap<>();
        param.put("channelName", channelName);
        return sqlSessionTemplate.selectOne("AppChannel.getAppChannelByChannelName", param);
    }

    @Override
    public Integer insert(AppChannel appChannel) {
        return sqlSessionTemplate.insert("AppChannel.insert", appChannel);
    }
}
