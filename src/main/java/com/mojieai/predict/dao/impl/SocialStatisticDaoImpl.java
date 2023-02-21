package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.SocialStatisticDao;
import com.mojieai.predict.entity.po.SocialStatistic;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class SocialStatisticDaoImpl extends BaseDao implements SocialStatisticDao {

    @Override
    public List<SocialStatistic> getOnePeriodSocialStatistic(long gameId, String periodId, Timestamp
            currentStaticTime, boolean ifContainEnd) {
        Map params = new HashMap<>();

        params.put("gameId", gameId);
        params.put("periodId", periodId);
        params.put("statisticTime", currentStaticTime);
        params.put("ifContainEnd", ifContainEnd);
        return sqlSessionTemplate.selectList("SocialStatistic.getOnePeriodSocialStatistic", params);
    }

    @Override
    public SocialStatistic getSocialStatisticByIdForUpdate(Long statisticId, String periodId, boolean isLock) {
        Map params = new HashMap();
        params.put("isLock", isLock);
        params.put("periodId", periodId);
        params.put("statisticId", statisticId);
        return sqlSessionTemplate.selectOne("SocialStatistic.getSocialStatisticByIdForUpdate", params);
    }

    @Override
    public SocialStatistic getSocialStatisticByUnitKey(long gameId, String periodId, Timestamp statisticTime, Integer
            dataType) {
        Map params = new HashMap();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        params.put("statisticTime", statisticTime);
        params.put("dataType", dataType);
        return sqlSessionTemplate.selectOne("SocialStatistic.getSocialStatisticByUnitKey", params);
    }

    @Override
    public Integer updateSocialBigData(Long statisticId, String periodId, String socialData) {
        Map params = new HashMap();
        params.put("statisticId", statisticId);
        params.put("periodId", periodId);
        params.put("socialData", socialData);
        return sqlSessionTemplate.update("SocialStatistic.updateSocialBigData", params);
    }

    @Override
    public Integer insert(SocialStatistic socialStatistic) {
        return sqlSessionTemplate.insert("SocialStatistic.insert", socialStatistic);
    }
}
