package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.ActivityAwardLevelDao;
import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.VipProgramDao;
import com.mojieai.predict.entity.po.ActivityAwardLevel;
import com.mojieai.predict.entity.po.VipProgram;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class VipProgramDaoImpl extends BaseDao implements VipProgramDao {
    @Override
    public VipProgram getVipProgramByProgramId(String programId, Boolean isLock) {
        Map<String, Object> params = new HashMap<>();
        params.put("programId", programId);
        params.put("isLock", isLock);
        return sqlSessionTemplate.selectOne("VipProgram.getVipProgramByProgramId", params);
    }

    @Override
    public List<VipProgram> getVipProgramNotStart() {
        return sqlSessionTemplate.selectList("VipProgram.getVipProgramNotStart");
    }

    @Override
    public List<VipProgram> getVipProgramEnded() {
        return sqlSessionTemplate.selectList("VipProgram.getVipProgramEnded");
    }

    @Override
    public List<VipProgram> getNotCalculateMatchVipProgram(String matchId) {
        Map<String, Object> params = new HashMap<>();
        params.put("matchId", matchId);
        return sqlSessionTemplate.selectList("VipProgram.getNotCalculateMatchVipProgram", params);
    }

    @Override
    public List<VipProgram> getVipProgramByStatus(Timestamp beginTime, Timestamp endTime, Integer status) {
        Map<String, Object> params = new HashMap<>();
        params.put("beginTime", beginTime);
        params.put("endTime", endTime);
        params.put("status", status);
        return sqlSessionTemplate.selectList("VipProgram.getVipProgramByStatus", params);
    }

    @Override
    public Integer getVipProgramByIsRight(Timestamp beginTime, Timestamp endTime, Integer isRight) {
        Map<String, Object> params = new HashMap<>();
        params.put("beginTime", beginTime);
        params.put("endTime", endTime);
        params.put("isRight", isRight);
        return sqlSessionTemplate.selectOne("VipProgram.getVipProgramByIsRight", params);
    }

    @Override
    public void update(VipProgram vipProgram) {
        sqlSessionTemplate.update("VipProgram.update", vipProgram);
    }

    @Override
    public Integer updateVipProgramStatus(String programId, String newProgramInfo, Integer isRight, Integer
            oldIsRight, Integer status, Integer oldStatus, Integer newMatchCount, Integer oldMatchCount) {
        Map<String, Object> params = new HashMap<>();
        params.put("programId", programId);
        params.put("programInfo", newProgramInfo);
        params.put("isRight", isRight);
        params.put("oldIsRight", oldIsRight);
        params.put("status", status);
        params.put("oldStatus", oldStatus);
        params.put("newMatchCount", newMatchCount);
        params.put("oldMatchCount", oldMatchCount);
        return sqlSessionTemplate.update("VipProgram.updateVipProgramStatus", params);
    }

    @Override
    public Integer insert(VipProgram vipProgram) {
        return sqlSessionTemplate.insert("VipProgram.insert", vipProgram);
    }
}
