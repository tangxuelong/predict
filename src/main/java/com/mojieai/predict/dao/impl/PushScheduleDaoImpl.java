package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.PushScheduleDao;
import com.mojieai.predict.entity.po.PushSchedule;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class PushScheduleDaoImpl extends BaseDao implements PushScheduleDao {
    @Override
    public void insert(PushSchedule pushSchedule) {
        sqlSessionTemplate.insert("PushSchedule.insert", pushSchedule);
    }

    @Override
    public PushSchedule getPushSchedule(long gameId, String periodId) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        return sqlSessionTemplate.selectOne("PushSchedule.getPushSchedule", params);
    }

    @Override
    public int updatePushSchedule(long gameId, String periodId, String flagColumn, String timeColumn) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        params.put("flagColumn", flagColumn);
        params.put("timeColumn", timeColumn);
        return sqlSessionTemplate.update("PushSchedule.updatePushSchedule", params);
    }
}
