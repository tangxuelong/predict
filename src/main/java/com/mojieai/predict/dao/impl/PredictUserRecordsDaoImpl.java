package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.PredictUserRecordsDao;
import com.mojieai.predict.entity.po.PredictUserRecords;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class PredictUserRecordsDaoImpl extends BaseDao implements PredictUserRecordsDao {
    @Override
    public int insert(PredictUserRecords predictUserRecords) {
        return sqlSessionTemplate.insert("PredictUserRecords.insert", predictUserRecords);
    }

    @Override
    public int updateNumStr(String recordId, String strNum, Integer isAward) {
        Map<String, Object> params = new HashMap<>();
        params.put("recordId", recordId);
        params.put("numStr", strNum);
        params.put("isAward", isAward);
        return sqlSessionTemplate.update("PredictUserRecords.updateNumStr", params);
    }

    @Override
    public List<PredictUserRecords> getAwardPredictRecords(long gameId, String periodId) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        return sqlSessionTemplate.selectList("PredictUserRecords.getAwardPredictRecords", params);
    }

    @Override
    public List<PredictUserRecords> getUnAwardPredictRecords(long gameId, String periodId) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        return sqlSessionTemplate.selectList("PredictUserRecords.getUnAwardPredictRecords", params);
    }
}
