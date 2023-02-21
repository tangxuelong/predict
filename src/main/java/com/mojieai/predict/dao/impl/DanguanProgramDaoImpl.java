package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.ActivityAwardLevelDao;
import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.DanguanProgramDao;
import com.mojieai.predict.entity.po.ActivityAwardLevel;
import com.mojieai.predict.entity.po.DanguanProgram;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class DanguanProgramDaoImpl extends BaseDao implements DanguanProgramDao {
    @Override
    public List<DanguanProgram> getNotAwardDuanguanProgram() {
        return sqlSessionTemplate.selectList("DanguanProgram.getNotAwardDuanguanProgram");
    }

    @Override
    public DanguanProgram getDuanguanProgram(String matchId) {
        Map<String, Object> params = new HashMap<>();
        params.put("matchId", matchId);
        return sqlSessionTemplate.selectOne("DanguanProgram.getDuanguanProgram", params);
    }

    @Override
    public List<DanguanProgram> getDanguanProgramListByLimitDate(Timestamp beginDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("beginDate", beginDate);
        return sqlSessionTemplate.selectList("DanguanProgram.getDanguanProgramListByLimitDate", params);
    }

    @Override
    public List<DanguanProgram> getAwardDuanguanProgram() {
        return sqlSessionTemplate.selectList("DanguanProgram.getAwardDuanguanProgram");
    }

    @Override
    public void update(DanguanProgram danguanProgram) {
        sqlSessionTemplate.update("DanguanProgram.update", danguanProgram);
    }

    @Override
    public void insert(DanguanProgram danguanProgram) {
        sqlSessionTemplate.insert("DanguanProgram.insert", danguanProgram);
    }
}
