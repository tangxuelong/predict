package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.SsqSocialResonanceDataDao;
import com.mojieai.predict.entity.po.SsqSocialResonanceData;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class SsqSocialResonanceDataDaoImpl extends BaseDao implements SsqSocialResonanceDataDao {
    @Override
    public SsqSocialResonanceData getTypeResonanceCurrentPeriod(String periodId, Integer socialType, Integer
            resonanceType) {
        Map<String, Object> params = new HashMap<>();
        params.put("periodId", periodId);
        params.put("socialType", socialType);
        params.put("resonanceType", resonanceType);
        return sqlSessionTemplate.selectOne("SsqSocialResonanceData.getTypeResonanceCurrentPeriod", params);
    }

    @Override
    public List<SsqSocialResonanceData> getAllTypeResonanceCurrentPeriod(String periodId, Integer socialType) {
        Map<String, Object> params = new HashMap<>();
        params.put("periodId", periodId);
        params.put("socialType", socialType);
        return sqlSessionTemplate.selectList("SsqSocialResonanceData.getAllTypeResonanceCurrentPeriod", params);
    }

    @Override
    public void update(SsqSocialResonanceData dltSocialResonanceData) {
        sqlSessionTemplate.update("SsqSocialResonanceData.update", dltSocialResonanceData);
    }

    @Override
    public void insert(SsqSocialResonanceData dltSocialResonanceData) {
        sqlSessionTemplate.insert("SsqSocialResonanceData.insert", dltSocialResonanceData);
    }
}
