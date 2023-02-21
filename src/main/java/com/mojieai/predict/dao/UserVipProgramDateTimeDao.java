package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.UserVipProgramDateTime;


public interface UserVipProgramDateTimeDao {
    UserVipProgramDateTime getUserVipProgramTimes(Long userId, String dateId, boolean isLock);

    void update(UserVipProgramDateTime userVipProgramDateTime);

    Integer updateUserVipProgramDateTimeUseTimes(Long userId, String dateId, Integer newUseTimes, Integer oldUseTimes);

    void insert(UserVipProgramDateTime userVipProgramDateTime);
}
