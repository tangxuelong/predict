package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.SocialIntegralLogDao;
import com.mojieai.predict.entity.po.SocialIntegralLog;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class SocialIntegralLogDaoImpl extends BaseDao implements SocialIntegralLogDao {

    @Override
    public SocialIntegralLog getSocialIntegralLogByPk(Long userId, Integer socialType, Long socialCode) {
        Map param = new HashMap<>();
        param.put("userId", userId);
        param.put("socialType", socialType);
        param.put("socialCode", socialCode);
        return sqlSessionTemplate.selectOne("SocialIntegralLog.getSocialIntegralLogByPk", param);
    }

    @Override
    public List<String> getSomePeriodIntervalPeriodId(Long userId, long gameId, String lastPeriodId, int pageSize) {
        Map params = new HashMap();
        params.put("userId", userId);
        params.put("gameId", gameId);
        params.put("lastPeriodId", lastPeriodId);
        params.put("pageSize", pageSize);
        return sqlSessionTemplate.selectList("SocialIntegralLog.getSomePeriodIntervalPeriodId", params);
    }

    @Override
    public List<SocialIntegralLog> getUserIntegralBySectionPeriodId(Long userId, long gameId, String maxPeriodId, String
            minPeriodId) {
        Map params = new HashMap();
        params.put("userId", userId);
        params.put("gameId", gameId);
        params.put("maxPeriodId", maxPeriodId);
        params.put("minPeriodId", minPeriodId);
        return sqlSessionTemplate.selectList("SocialIntegralLog.getUserIntegralBySectionPeriodId", params);
    }

    @Override
    public Integer updateIntegralLogDistribute(Long userId, Integer isDistribute, Integer socialType, Long socialCode) {
        Map param = new HashMap<>();
        param.put("userId", userId);
        param.put("socialType", socialType);
        param.put("socialCode", socialCode);
        param.put("isDistribute", isDistribute);
        return sqlSessionTemplate.update("SocialIntegralLog.updateIntegralLogDistribute", param);
    }

    @Override
    public Integer insert(SocialIntegralLog socialIntegralLog) {
        return sqlSessionTemplate.insert("SocialIntegralLog.insert", socialIntegralLog);
    }


}
