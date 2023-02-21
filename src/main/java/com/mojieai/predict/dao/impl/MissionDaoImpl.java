package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.MissionDao;
import com.mojieai.predict.entity.po.Mission;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class MissionDaoImpl extends BaseDao implements MissionDao {
    @Override
    public Integer insert(Mission mission) {
        return sqlSessionTemplate.insert("Mission.insert", mission);
    }

    @Override
    public void insertBatch(List<Mission> missions) {
        Map<String, Object> params = new HashMap<>();
        params.put("missionList", missions);
        sqlSessionTemplate.insert("Mission.insertBatchForCollect", params);
    }

    @Override
    public Mission getTaskById(Long missionId) {
        Map<String, Object> params = new HashMap<>();
        params.put("missionId", missionId);
        return sqlSessionTemplate.selectOne("Mission.getTaskById", params);
    }

    @Override
    public Mission getPartMissionByKeyInfo(String partKeyInfo) {
        Map<String, Object> params = new HashMap<>();
        params.put("partKeyInfo", partKeyInfo);
        return sqlSessionTemplate.selectOne("Mission.getPartMissionByKeyInfo", params);
    }

    @Override
    public List<Mission> getSlaveMissionByClassId(String classId, Integer missionType, Integer missionStatus) {
        Map<String, Object> params = new HashMap<>();
        params.put("classId", classId);
        params.put("missionType", missionType);
        params.put("missionStatus", missionStatus);
        return slaveSqlSessionTemplate.selectList("Mission.getSlaveMissionByClassId", params);
    }

    @Override
    public Integer getCountByClassIdAndStatus(String classId, int missionType, int missionStatus) {
        Map<String, Object> params = new HashMap<>();
        params.put("classId", classId);
        params.put("missionType", missionType);
        params.put("missionStatus", missionStatus);
        return slaveSqlSessionTemplate.selectOne("Mission.getCountByClassIdAndStatus", params);
    }

    @Override
    public int updateMissionStatus(Long missionId, Integer setStatus, Integer status) {
        Map<String, Object> params = new HashMap<>();
        params.put("missionId", missionId);
        params.put("setStatus", setStatus);
        params.put("status", status);
        return sqlSessionTemplate.update("Mission.updateMissionStatus", params);
    }


    //below method are used only for testing purpose
    @Override
    public Mission getMissionByKeyInfo(String keyInfo, Integer type) {
        Map<String, Object> params = new HashMap<>();
        params.put("keyInfo", keyInfo);
        params.put("missionType", type);
        return sqlSessionTemplate.selectOne("Mission.getMissionByKeyInfo", params);

    }

    @Override
    public List<Mission> getSlaveMissionsByDate(Timestamp date) {
        Map<String, Object> params = new HashMap<>();
        params.put("date", date);
        return slaveSqlSessionTemplate.selectList("Mission.getSlaveMissionsByDate", params);
    }

    @Override
    public void insert2Bak(Mission mission) {
        sqlSessionTemplate.insert("Mission.insert2Bak", mission);
    }

    @Override
    public void deleteCompleteMissionById(Long missionId) {
        Map<String, Object> params = new HashMap<>();
        params.put("missionId", missionId);
        sqlSessionTemplate.delete("Mission.deleteCompleteMissionById", params);
    }

    @Override
    public Mission getSlaveBakMissionById(Long missionId) {
        Map<String, Object> params = new HashMap<>();
        params.put("missionId", missionId);
        return slaveSqlSessionTemplate.selectOne("Mission.getSlaveBakMissionById", params);
    }

    @Override
    public List<Long> getSlaveMissionIdsByTaskType(Integer missionType, Integer status) {
        Map<String, Object> params = new HashMap<>();
        params.put("missionType", missionType);
        params.put("status", status);
        return slaveSqlSessionTemplate.selectList("Mission.getSlaveMissionIdsByTaskType", params);
    }

    @Override
    public List<Long> getMonitorRefundMission() {
        return slaveSqlSessionTemplate.selectList("Mission.getMonitorRefundMission");
    }

    @Override
    public Integer updateMissionStatusByPartKey(String partKey, Integer setStatus, Integer originStatus) {
        Map params = new HashMap();

        params.put("partKey", partKey);
        params.put("setStatus", setStatus);
        params.put("originStatus", originStatus);
        return sqlSessionTemplate.update("Mission.updateMissionStatusByPartKey", params);
    }
}
