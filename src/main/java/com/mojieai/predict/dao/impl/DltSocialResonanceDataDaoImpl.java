package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.DltSocialResonanceDataDao;
import com.mojieai.predict.entity.po.DltSocialResonanceData;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class DltSocialResonanceDataDaoImpl extends BaseDao implements DltSocialResonanceDataDao {
    @Override
    public DltSocialResonanceData getTypeResonanceCurrentPeriod(String periodId, Integer socialType, Integer
            resonanceType) {
        Map<String, Object> params = new HashMap<>();
        params.put("periodId", periodId);
        params.put("socialType", socialType);
        params.put("resonanceType", resonanceType);
        return sqlSessionTemplate.selectOne("DltSocialResonanceData.getTypeResonanceCurrentPeriod", params);
    }

    @Override
    public List<DltSocialResonanceData> getAllTypeResonanceCurrentPeriod(String periodId, Integer socialType) {
        Map<String, Object> params = new HashMap<>();
        params.put("periodId", periodId);
        params.put("socialType", socialType);
        return sqlSessionTemplate.selectList("DltSocialResonanceData.getAllTypeResonanceCurrentPeriod", params);
    }

    @Override
    public void update(DltSocialResonanceData dltSocialResonanceData) {
        sqlSessionTemplate.update("DltSocialResonanceData.update", dltSocialResonanceData);
    }

    @Override
    public void insert(DltSocialResonanceData dltSocialResonanceData) {
        sqlSessionTemplate.insert("DltSocialResonanceData.insert", dltSocialResonanceData);
    }
}
