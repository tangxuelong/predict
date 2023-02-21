package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserSocialTaskAwardDao;
import com.mojieai.predict.entity.po.UserSocialTaskAward;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserSocialTaskAwardDaoImpl extends BaseDao implements UserSocialTaskAwardDao {
    @Override
    public UserSocialTaskAward getUserSocialTaskAwardById(String taskId, Long userId, boolean isLock) {
        Map params = new HashMap();

        params.put("taskId", taskId);
        params.put("isLock", isLock);
        params.put("userId", userId);
        return sqlSessionTemplate.selectOne("UserSocialTaskAward.getUserSocialTaskAwardById", params);
    }

    @Override
    public UserSocialTaskAward getUserSocialTaskAwardByUnitKey(Long gameId, String periodId, Long userId, Integer
            taskType) {
        Map params = new HashMap<>();

        params.put("gameId", gameId);
        params.put("periodId", periodId);
        params.put("userId", userId);
        params.put("taskType", taskType);
        return sqlSessionTemplate.selectOne("UserSocialTaskAward.getUserSocialTaskAwardByUnitKey", params);
    }

    @Override
    public Integer updateTaskTimesById(String taskId, Long userId, Integer taskTimes, Integer isAward) {

        return updateTaskTimesById(taskId, userId, taskTimes, null, isAward);
    }

    @Override
    public Integer updateTaskTimesById(String taskId, Long userId, Integer taskTimes, Integer oldTaskTimes, Integer
            isAward) {
        Map params = new HashMap<>();

        params.put("taskId", taskId);
        params.put("taskTimes", taskTimes);
        params.put("oldTaskTimes", oldTaskTimes);
        params.put("userId", userId);
        params.put("isAward", isAward);
        return sqlSessionTemplate.update("UserSocialTaskAward.updateTaskTimesById", params);
    }

    @Override
    public Integer updateTaskIsAward(String taskId, Long userId, Integer isAward, Integer lastIsAwardType) {
        Map params = new HashMap<>();

        params.put("taskId", taskId);
        params.put("isAward", isAward);
        params.put("lastIsAward", lastIsAwardType);
        params.put("userId", userId);
        return sqlSessionTemplate.update("UserSocialTaskAward.updateTaskIsAward", params);
    }

    @Override
    public Integer insert(UserSocialTaskAward userSocialTaskAward) {
        return sqlSessionTemplate.insert("UserSocialTaskAward.insert", userSocialTaskAward);
    }

    @Override
    public List<UserSocialTaskAward> getEarlistNumUserSocialTask(Long userId, Integer isAward, Integer limitNum) {
        Map params = new HashMap<>();

        params.put("userId", userId);
        params.put("isAward", isAward);
        params.put("limitNum", limitNum);
        return sqlSessionTemplate.selectList("UserSocialTaskAward.getEarlistNumUserSocialTask", params);
    }
}
