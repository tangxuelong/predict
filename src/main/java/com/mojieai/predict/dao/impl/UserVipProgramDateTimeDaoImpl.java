package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.ActivityDateUserInfoDao;
import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserVipProgramDateTimeDao;
import com.mojieai.predict.entity.po.ActivityDateUserInfo;
import com.mojieai.predict.entity.po.UserVipProgramDateTime;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserVipProgramDateTimeDaoImpl extends BaseDao implements UserVipProgramDateTimeDao {

    @Override
    public UserVipProgramDateTime getUserVipProgramTimes(Long userId, String dateId, boolean isLock) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("dateId", dateId);
        params.put("isLock", isLock);
        return sqlSessionTemplate.selectOne("UserVipProgramDateTime.getUserVipProgramTimes", params);
    }

    @Override
    public void update(UserVipProgramDateTime userVipProgramDateTime) {
        sqlSessionTemplate.update("UserVipProgramDateTime.update", userVipProgramDateTime);
    }

    @Override
    public Integer updateUserVipProgramDateTimeUseTimes(Long userId, String dateId, Integer newUseTimes, Integer
            oldUseTimes) {
        Map<String, Object> param = new HashMap<>();
        param.put("userId", userId);
        param.put("dateId", dateId);
        param.put("newUseTimes", newUseTimes);
        param.put("oldUseTimes", oldUseTimes);
        return sqlSessionTemplate.update("UserVipProgramDateTime.updateUserVipProgramDateTimeUseTimes", param);
    }

    @Override
    public void insert(UserVipProgramDateTime userVipProgramDateTime) {
        sqlSessionTemplate.update("UserVipProgramDateTime.insert", userVipProgramDateTime);
    }
}
