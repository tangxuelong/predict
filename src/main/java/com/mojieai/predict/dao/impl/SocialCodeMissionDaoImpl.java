package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.SocialCodeMissionDao;
import com.mojieai.predict.entity.po.Mission;
import com.mojieai.predict.entity.po.SocialCodeMission;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class SocialCodeMissionDaoImpl extends BaseDao implements SocialCodeMissionDao {
    @Override
    public void insert(SocialCodeMission mission) {
        sqlSessionTemplate.insert("SocialCodeMission.insert", mission);
    }

    @Override
    public void insertBatch(List<SocialCodeMission> missions) {
        Map<String, Object> params = new HashMap<>();
        params.put("missionList", missions);
        sqlSessionTemplate.insert("SocialCodeMission.insertBatchForCollect", params);
    }

    @Override
    public Mission getTaskById(Long missionId) {
        Map<String, Object> params = new HashMap<>();
        params.put("missionId", missionId);
        return sqlSessionTemplate.selectOne("SocialCodeMission.getTaskById", params);
    }

    @Override
    public int updateMissionStatus(Long missionId, Integer setStatus, Integer status) {
        Map<String, Object> params = new HashMap<>();
        params.put("missionId", missionId);
        params.put("setStatus", setStatus);
        params.put("status", status);
        return sqlSessionTemplate.update("SocialCodeMission.updateMissionStatus", params);
    }


    //below method are used only for testing purpose
    @Override
    public SocialCodeMission getMissionByKeyInfo(String keyInfo, Integer type) {
        Map<String, Object> params = new HashMap<>();
        params.put("keyInfo", keyInfo);
        params.put("missionType", type);
        return sqlSessionTemplate.selectOne("SocialCodeMission.getMissionByKeyInfo", params);

    }

    @Override
    public List<SocialCodeMission> getSlaveMissionsByDate(Timestamp date) {
        Map<String, Object> params = new HashMap<>();
        params.put("date", date);
        return slaveSqlSessionTemplate.selectList("SocialCodeMission.getSlaveMissionsByDate", params);
    }

    @Override
    public void insert2Bak(SocialCodeMission mission) {
        sqlSessionTemplate.insert("SocialCodeMission.insert2Bak", mission);
    }

    @Override
    public void deleteCompleteMissionById(Long missionId) {
        Map<String, Object> params = new HashMap<>();
        params.put("missionId", missionId);
        sqlSessionTemplate.delete("SocialCodeMission.deleteCompleteMissionById", params);
    }

    @Override
    public SocialCodeMission getSlaveBakMissionById(Long missionId) {
        Map<String, Object> params = new HashMap<>();
        params.put("missionId", missionId);
        return slaveSqlSessionTemplate.selectOne("SocialCodeMission.getSlaveBakMissionById", params);
    }

    @Override
    public List<Long> getSlaveMissionIdsByTaskType(Integer missionType, Integer status, String gameEn, Timestamp date) {
        Map<String, Object> params = new HashMap<>();
        params.put("missionType", missionType);
        params.put("status", status);
        params.put("gameEn", gameEn);
        params.put("date", date);
        return slaveSqlSessionTemplate.selectList("SocialCodeMission.getSlaveMissionIdsByTaskType", params);
    }
}
