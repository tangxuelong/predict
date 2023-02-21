package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.PeriodScheduleDao;
import com.mojieai.predict.entity.po.PeriodSchedule;
import com.mojieai.predict.util.DateUtil;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class PeriodScheduleDaoImpl extends BaseDao implements PeriodScheduleDao {
    @Override
    public PeriodSchedule insert(Long gameId, String periodId) {
        PeriodSchedule periodSchedule = new PeriodSchedule(gameId, periodId, DateUtil.getCurrentTimestamp());
        sqlSessionTemplate.insert("PeriodSchedule.insert", periodSchedule);
        return periodSchedule;
    }

    @Override
    public void insert(PeriodSchedule periodSchedule) {
        sqlSessionTemplate.insert("PeriodSchedule.insert", periodSchedule);
    }

    @Override
    public List<PeriodSchedule> getUnFinishedSchedules(Long gameId, String periodId) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        return sqlSessionTemplate.selectList("PeriodSchedule.getUnFinishedSchedules", params);
    }

    @Override
    public PeriodSchedule getPeriodSchedule(Long gameId, String periodId) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        return sqlSessionTemplate.selectOne("PeriodSchedule.getPeriodSchedule", params);
    }


    @Override
    public int updatePeriodSchedule(Long gameId, String periodId, String flagColumn, String timeColumn) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        params.put("flagColumn", flagColumn);
        params.put("timeColumn", timeColumn);
        return sqlSessionTemplate.update("PeriodSchedule.updatePeriodSchedule", params);
    }
}
