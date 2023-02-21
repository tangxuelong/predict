package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.Program;

import java.util.List;

public interface ProgramDao {
    List<Program> getProgramsByPeriod(Long gameId, String periodId);

    Program getProgramById(String programId, boolean isLock);

    List<Program> getProgramsByCondition(Long gameId, String periodId, Integer programType);

    List<Program> getProgramsByType(Long gameId, String periodId, Integer programType, Integer buyType);

    List<String> getProgramPagePeriodId(Long gameId, String lastPeriodId, Integer isAward, int count);

    List<Program> getProgramsByIntervalPeriodId(Long gameId, String maxPeriodId, String minPeriodId, Integer isAward);

    List<String> getProgramIdByRefundType(Integer refundStatus);

    int update(Program program);

    int updateProgramRefundStatus(String programId, Integer refundStatus);

    int insert(Program program);
}
