package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.AppChannelVersionDao;
import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.entity.po.AppChannelVersion;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class AppChannelVersionDaoImpl extends BaseDao implements AppChannelVersionDao {
    @Override
    public List<AppChannelVersion> getAllAppChannelVersion() {
        return slaveSqlSessionTemplate.selectList("AppChannelVersion.getAllAppChannelVersion");
    }

    @Override
    public AppChannelVersion getAppChannelVersionByUniqueKey(Integer channelId, Integer versionId) {
        Map param = new HashMap<>();
        param.put("versionId", versionId);
        param.put("channelId", channelId);
        return sqlSessionTemplate.selectOne("AppChannelVersion.getAppChannelVersionByUniqueKey", param);
    }

    @Override
    public Integer insert(AppChannelVersion appChannelVersion) {
        return sqlSessionTemplate.insert("AppChannelVersion.insert", appChannelVersion);
    }
}
