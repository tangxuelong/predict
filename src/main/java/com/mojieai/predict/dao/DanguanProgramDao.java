package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.ActivityAwardLevel;
import com.mojieai.predict.entity.po.DanguanProgram;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public interface DanguanProgramDao {
    List<DanguanProgram> getNotAwardDuanguanProgram();

    DanguanProgram getDuanguanProgram(String matchId);

    List<DanguanProgram> getDanguanProgramListByLimitDate(Timestamp beginDate);

    List<DanguanProgram> getAwardDuanguanProgram();

    void update(DanguanProgram danguanProgram);

    void insert(DanguanProgram danguanProgram);
}
