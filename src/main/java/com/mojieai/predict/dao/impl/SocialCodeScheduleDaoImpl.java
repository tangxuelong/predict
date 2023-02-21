package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.SocialCodeScheduleDao;
import com.mojieai.predict.entity.po.SocialCodeSchedule;
import com.mojieai.predict.util.DateUtil;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class SocialCodeScheduleDaoImpl extends BaseDao implements SocialCodeScheduleDao {


    @Override
    public SocialCodeSchedule insert(Long gameId, String periodId) {
        SocialCodeSchedule socialCodeSchedule = new SocialCodeSchedule(gameId, periodId, DateUtil.getCurrentTimestamp
                ());
        sqlSessionTemplate.insert("SocialCodeSchedule.insert", socialCodeSchedule);
        return socialCodeSchedule;
    }

    @Override
    public void insert(SocialCodeSchedule socialCodeSchedule) {
        sqlSessionTemplate.insert("SocialCodeSchedule.insert", socialCodeSchedule);
    }

    @Override
    public List<SocialCodeSchedule> getUnFinishedSchedules(Long gameId, String periodId) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        return sqlSessionTemplate.selectList("SocialCodeSchedule.getUnFinishedSchedules", params);
    }

    @Override
    public SocialCodeSchedule getSocialCodeSchedule(long gameId, String periodId) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        return sqlSessionTemplate.selectOne("SocialCodeSchedule.getSocialCodeSchedule", params);
    }

    @Override
    public int updateSocialCodeSchedule(long gameId, String periodId, String flagColumn, String timeColumn) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        params.put("flagColumn", flagColumn);
        params.put("timeColumn", timeColumn);
        return sqlSessionTemplate.update("SocialCodeSchedule.updateSocialCodeSchedule", params);
    }
}
