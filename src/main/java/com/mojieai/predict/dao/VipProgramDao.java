package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.VipProgram;

import java.sql.Timestamp;
import java.util.List;

public interface VipProgramDao {
    VipProgram getVipProgramByProgramId(String programId, Boolean isLock);

    List<VipProgram> getVipProgramNotStart();

    List<VipProgram> getVipProgramEnded();

    List<VipProgram> getNotCalculateMatchVipProgram(String matchId);

    List<VipProgram> getVipProgramByStatus(Timestamp beginTime, Timestamp endTime, Integer status);

    Integer getVipProgramByIsRight(Timestamp beginTime, Timestamp endTime, Integer isRight);

    void update(VipProgram vipProgram);

    Integer updateVipProgramStatus(String programId, String newProgramInfo, Integer isRight, Integer oldIsRight,
                                   Integer status, Integer oldStatus, Integer newMatchCount, Integer oldMatchCount);

    Integer insert(VipProgram vipProgram);
}
