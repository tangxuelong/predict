package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.AppVersionDao;
import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.entity.po.AppVersion;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class AppVersionDaoImpl extends BaseDao implements AppVersionDao {
    @Override
    public List<AppVersion> getAllAppVersion(Integer clientId) {
        Map params = new HashMap();
        params.put("clientId", clientId);
        return slaveSqlSessionTemplate.selectList("AppVersion.getAllAppVersion");
    }

    @Override
    public AppVersion getAppVersionByUnikey(Integer clientId, Integer versionCode) {
        Map params = new HashMap();
        params.put("clientId", clientId);
        params.put("versionCode", versionCode);
        return sqlSessionTemplate.selectOne("AppVersion.getAppVersionByUnikey", params);
    }

    @Override
    public AppVersion getLatestAppVersionByClientId(Integer clientId) {
        Map param = new HashMap<>();
        param.put("clientId", clientId);
        return sqlSessionTemplate.selectOne("AppVersion.getLatestAppVersionByClientId", param);
    }

    @Override
    public AppVersion getAppVersionByPk(Integer versionId) {
        Map param = new HashMap<>();
        param.put("versionId", versionId);
        return sqlSessionTemplate.selectOne("AppVersion.getAppVersionByPk", param);
    }

    @Override
    public Integer insert(AppVersion appVersion) {
        return sqlSessionTemplate.insert("AppVersion.insert", appVersion);
    }

    @Override
    public Integer update(AppVersion appVersion) {
        return sqlSessionTemplate.update("AppVersion.update", appVersion);
    }
}
