package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.PredictColdHotModelDao;
import com.mojieai.predict.entity.po.PredictColdHotModel;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class PredictColdHotModelDaoImpl extends BaseDao implements PredictColdHotModelDao {

    @Override
    public PredictColdHotModel getColdHotModelByPk(long gameId, String periodId, Integer periodCount, Integer numType) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        params.put("periodCount", periodCount);
        params.put("numType", numType);
        return sqlSessionTemplate.selectOne("PredictColdHotModel.getColdHotModelByPk", params);
    }

    @Override
    public Integer insert(PredictColdHotModel predictColdHotModel){
        return sqlSessionTemplate.insert("PredictColdHotModel.insert", predictColdHotModel);
    }
}
