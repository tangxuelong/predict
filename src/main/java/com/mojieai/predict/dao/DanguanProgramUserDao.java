package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.DanguanProgram;
import com.mojieai.predict.entity.po.DanguanProgramUser;

import java.util.List;

public interface DanguanProgramUserDao {
    DanguanProgramUser getDanguanProgramUserLog(Long userId,String matchId);

    void update(DanguanProgramUser danguanProgramUser);

    void insert(DanguanProgramUser danguanProgramUser);
}
