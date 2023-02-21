package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.ProgramDao;
import com.mojieai.predict.entity.po.Program;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ProgramDaoImpl extends BaseDao implements ProgramDao {
    @Override
    public List<Program> getProgramsByPeriod(Long gameId, String periodId) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        return sqlSessionTemplate.selectList("Program.getProgramsByPeriod", params);
    }

    @Override
    public Program getProgramById(String programId, boolean isLock) {
        Map<String, Object> params = new HashMap<>();
        params.put("programId", programId);
        params.put("isLock", isLock);
        return sqlSessionTemplate.selectOne("Program.getProgramById", params);
    }

    @Override
    public List<Program> getProgramsByCondition(Long gameId, String periodId, Integer programType) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        params.put("programType", programType);
        return sqlSessionTemplate.selectList("Program.getProgramsByCondition", params);
    }

    @Override
    public List<Program> getProgramsByType(Long gameId, String periodId, Integer programType, Integer buyType) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        params.put("programType", programType);
        params.put("buyType", buyType);
        return sqlSessionTemplate.selectList("Program.getProgramsByType", params);
    }

    @Override
    public List<String> getProgramPagePeriodId(Long gameId, String maxPeriodId, Integer isAward, int count) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("maxPeriodId", maxPeriodId);
        params.put("isAward", isAward);
        params.put("count", count);
        return sqlSessionTemplate.selectList("Program.getProgramPagePeriodId", params);
    }

    @Override
    public List<Program> getProgramsByIntervalPeriodId(Long gameId, String maxPeriodId, String minPeriodId, Integer
            isAward) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("maxPeriodId", maxPeriodId);
        params.put("minPeriodId", minPeriodId);
        params.put("isAward", isAward);
        return sqlSessionTemplate.selectList("Program.getProgramsByIntervalPeriodId", params);
    }

    @Override
    public List<String> getProgramIdByRefundType(Integer refundStatus) {
        Map<String, Object> params = new HashMap<>();
        params.put("refundStatus", refundStatus);
        return sqlSessionTemplate.selectList("Program.getProgramIdByRefundType", params);
    }

    @Override
    public int update(Program program) {
        return sqlSessionTemplate.update("Program.update", program);
    }

    @Override
    public int updateProgramRefundStatus(String programId, Integer refundStatus) {
        Map param = new HashMap();

        param.put("programId", programId);
        param.put("refundStatus", refundStatus);
        return sqlSessionTemplate.update("Program.updateProgramRefundStatus", param);
    }

    @Override
    public int insert(Program program) {
        return sqlSessionTemplate.insert("Program.insert", program);
    }
}
