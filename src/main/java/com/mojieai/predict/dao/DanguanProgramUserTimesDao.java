package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.DanguanProgramUser;
import com.mojieai.predict.entity.po.DanguanProgramUserTimes;

public interface DanguanProgramUserTimesDao {
    DanguanProgramUserTimes getDanguanProgramUserTimes(Long userId,Boolean isLock);

    void update(DanguanProgramUserTimes danguanProgramUserTimes);

    void insert(DanguanProgramUserTimes danguanProgramUserTimes);
}
