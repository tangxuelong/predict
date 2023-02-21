package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.PredictNumbersDao;
import com.mojieai.predict.dao.PredictNumbersOperateDao;
import com.mojieai.predict.entity.po.PredictNumbers;
import com.mojieai.predict.entity.po.PredictNumbersOperate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class PredictNumbersOperateDaoImpl extends BaseDao implements PredictNumbersOperateDao {

    @Override
    public int insert(PredictNumbersOperate predictNumbers) {
        return sqlSessionTemplate.insert("PredictNumbersOperate.insert", predictNumbers);
    }

    @Override
    public List<PredictNumbersOperate> getPredictNumsByGameId(long gameId) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        return sqlSessionTemplate.selectList("PredictNumbersOperate.getPredictNumsByGameId", params);
    }

    @Override
    public Map getPredictNumsByGameIdAndPeriodId(long gameId, String periodId) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        return sqlSessionTemplate.selectOne("PredictNumbersOperate.getPredictNumsByGameIdAndPeriodId", params);
    }

    @Override
    public PredictNumbersOperate getPredictNumPoByGameIdAndPeriodId(long gameId, String periodId) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        return sqlSessionTemplate.selectOne("PredictNumbersOperate.getPredictNumPoByGameIdAndPeriodId", params);
    }

    @Override
    public void updatePredictNumsOperate(PredictNumbersOperate predictNumbers) {
        sqlSessionTemplate.update("PredictNumbersOperate.updatePredictNums", predictNumbers);
    }

    @Override
    public int saveOperatePredictNums(long gameId, String periodId, byte[] operateNums) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        params.put("operateNums", operateNums);
        return sqlSessionTemplate.update("PredictNumbersOperate.saveOperatePredictNums", params);
    }

    @Override
    public int updateStatus(long gameId, String periodId, int status) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        params.put("status", status);
        return sqlSessionTemplate.update("PredictNumbersOperate.updateStatus", params);
    }

    @Override
    public int saveRuleStr(long gameId, String periodId, String ruleStr) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        params.put("ruleStr", ruleStr);
        return sqlSessionTemplate.update("PredictNumbersOperate.saveRuleStr", params);
    }

    @Override
    public List<PredictNumbersOperate> getPredictNumsByCondition(Long gameId, String minPeriodId, String maxPeriodId,
                                                                 String manualFlag) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("minPeriodId", minPeriodId);
        params.put("maxPeriodId", maxPeriodId);
        params.put("manualFlag", manualFlag);
        return sqlSessionTemplate.selectList("PredictNumbersOperate.getPredictNumsByCondition", params);
    }

}
