package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.PredictNumbersDao;
import com.mojieai.predict.entity.po.PredictNumbers;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class PredictNumbersDaoImpl extends BaseDao implements PredictNumbersDao {

    @Override
    public int insert(PredictNumbers predictNumbers) {
        return sqlSessionTemplate.insert("PredictNumbers.insert", predictNumbers);
    }

    @Override
    public List<PredictNumbers> getPredictNumsByGameId(long gameId) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        return sqlSessionTemplate.selectList("PredictNumbers.getPredictNumsByGameId", params);
    }

    @Override
    public BigDecimal getLastHistoryAwardSum(long gameId, String periodId) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        return sqlSessionTemplate.selectOne("PredictNumbers.getLastHistoryAwardSum", params);
    }

    @Override
    public PredictNumbers getPredictNums(long gameId, String periodId) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        return sqlSessionTemplate.selectOne("PredictNumbers.getPredictNums", params);
    }

    @Override
    public String getPredictNumAwardLevel(long gameId, String periodId){
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        return sqlSessionTemplate.selectOne("PredictNumbers.getPredictNumAwardLevel", params);
    }

    @Override
    public String getHistoryAwardLevelSum(long gameId, String periodId) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        return sqlSessionTemplate.selectOne("PredictNumbers.getHistoryAwardLevelSum", params);
    }

    @Override
    public Map<String, String> getAllAwardLevelStr(long gameId, String periodId){
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        return sqlSessionTemplate.selectOne("PredictNumbers.getAllAwardLevelStr", params);
    }

    @Override
    public List<Map<String, Object>> getPredictNumsPartInfo(long gameId, Integer count) {
        Map<String, Object> params = new HashMap<>();
        params.put("count", count);
        params.put("gameId", gameId);
        return sqlSessionTemplate.selectList("PredictNumbers.getPredictNumsPartInfo", params);
    }

    @Override
    public void updatePredictNums(PredictNumbers predictNumbers){
        sqlSessionTemplate.update("PredictNumbers.updatePredictNums", predictNumbers);
    }

    @Override
    public int updateHistoryAwardSum(long gameId, String periodId, long historyAwardSum){
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        params.put("historyAwardSum", historyAwardSum);
        return sqlSessionTemplate.update("PredictNumbers.updateHistoryAwardSum", params);
    }

    @Override
    public int updatePredictNumAwardLevel(long gameId, String periodId, String awardLevel, String historyAwardLevelSum){
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        params.put("awardLevel", awardLevel);
        params.put("historyAwardLevelSum", historyAwardLevelSum);
        return sqlSessionTemplate.update("PredictNumbers.updatePredictNumAwardLevel", params);
    }

    @Override
    public void updatePeriodId(long gameId, String oldPeriodId, String newPeriodId) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("oldPeriodId", oldPeriodId);
        params.put("newPeriodId", newPeriodId);
        sqlSessionTemplate.update("PredictNumbers.updatePeriodId", params);
    }

}
