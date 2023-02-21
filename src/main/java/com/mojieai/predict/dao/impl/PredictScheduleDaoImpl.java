package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.PredictScheduleDao;
import com.mojieai.predict.entity.po.PeriodSchedule;
import com.mojieai.predict.entity.po.PredictSchedule;
import com.mojieai.predict.util.DateUtil;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class PredictScheduleDaoImpl extends BaseDao implements PredictScheduleDao {

    @Override
    public PredictSchedule insert(Long gameId, String periodId) {
        PredictSchedule predictSchedule = new PredictSchedule(gameId, periodId, DateUtil.getCurrentTimestamp());
        sqlSessionTemplate.insert("PredictSchedule.insert", predictSchedule);
        return predictSchedule;
    }

    @Override
    public void insert(PredictSchedule periodSchedule) {
        sqlSessionTemplate.insert("PredictSchedule.insert", periodSchedule);
    }

    @Override
    public List<PredictSchedule> getUnFinishedSchedules(Long gameId, String periodId) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        return sqlSessionTemplate.selectList("PredictSchedule.getUnFinishedSchedules", params);
    }

    @Override
    public PredictSchedule getPredictSchedule(long gameId, String periodId) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        return sqlSessionTemplate.selectOne("PredictSchedule.getPredictSchedule", params);
    }

    @Override
    public int updatePredictSchedule(long gameId, String periodId, String flagColumn, String timeColumn) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        params.put("flagColumn", flagColumn);
        params.put("timeColumn", timeColumn);
        return sqlSessionTemplate.update("PredictSchedule.updatePredictSchedule", params);
    }
}
