package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.ActivityAwardLevelDao;
import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserFollowMatchesDao;
import com.mojieai.predict.entity.po.ActivityAwardLevel;
import com.mojieai.predict.entity.po.UserFollowMatches;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserFollowMatchesDaoImpl extends BaseDao implements UserFollowMatchesDao {

    @Override
    public List<UserFollowMatches> getUserFollowMatchesByUserId(Long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        return sqlSessionTemplate.selectList("UserFollowMatches.getUserFollowMatchesByUserId", params);

    }

    @Override
    public UserFollowMatches getUserFollowMatchByUserIdAndUserId(Long userId, String matchId, Boolean isLock) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("matchId", matchId);
        params.put("isLock", isLock);
        return sqlSessionTemplate.selectOne("UserFollowMatches.getUserFollowMatchByUserIdAndUserId", params);

    }

    @Override
    public void update(UserFollowMatches userFollowMatches) {
        sqlSessionTemplate.update("UserFollowMatches.update", userFollowMatches);
    }

    @Override
    public void insert(UserFollowMatches userFollowMatches) {
        sqlSessionTemplate.insert("UserFollowMatches.insert", userFollowMatches);
    }
}
