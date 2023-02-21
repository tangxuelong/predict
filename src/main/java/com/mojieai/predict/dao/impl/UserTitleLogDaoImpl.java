package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserTitleLogDao;
import com.mojieai.predict.entity.po.UserTitleLog;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserTitleLogDaoImpl extends BaseDao implements UserTitleLogDao {

    @Override
    public UserTitleLog getUserTitleLogByDistributeId(long gameId, Long userId, Integer titleId, String dateStr) {
        Map params = new HashMap<>();

        params.put("gameId", gameId);
        params.put("userId", userId);
        params.put("titleId", titleId);
        params.put("dateStr", dateStr);
        return sqlSessionTemplate.selectOne("UserTitleLog.getUserTitleLogByDistributeId", params);
    }

    @Override
    public List<UserTitleLog> getAllNeedDistributeTitle(Integer count) {
        return sqlSessionTemplate.selectList("UserTitleLog.getAllNeedDistributeTitle", count);
    }

    @Override
    public Integer insert(UserTitleLog userTitleLog) {
        return sqlSessionTemplate.insert("UserTitleLog.insert", userTitleLog);
    }

    @Override
    public Integer updateUserTitleLogDistributeStatus(Long userId, String titleLogId, Integer isDistribute) {
        Map params = new HashMap();

        params.put("userId", userId);
        params.put("titleLogId", titleLogId);
        params.put("isDistribute", isDistribute);
        return sqlSessionTemplate.update("UserTitleLog.updateUserTitleLogDistributeStatus", params);
    }
}
