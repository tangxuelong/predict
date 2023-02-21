package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserTitleDao;
import com.mojieai.predict.entity.po.UserTitle;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserTitleDaoImpl extends BaseDao implements UserTitleDao {

    @Override
    public UserTitle getUserTitleByUserIdAndTitleId(long gameId, Long userId, Integer titleId, boolean isLock) {
        Map params = new HashMap<>();

        params.put("gameId", gameId);
        params.put("userId", userId);
        params.put("titleId", titleId);
        params.put("isLock", isLock);
        return sqlSessionTemplate.selectOne("UserTitle.getUserTitleByUserIdAndTitleId", params);
    }

    @Override
    public List<UserTitle> getUserAllTitle(Long userId) {
        return sqlSessionTemplate.selectList("UserTitle.getUserAllTitle", userId);
    }

    @Override
    public Integer insert(UserTitle userTitle) {
        return sqlSessionTemplate.insert("UserTitle.insert", userTitle);
    }

    @Override
    public Integer updateUserTitleAviable(long gameId, Long userId, Integer titleId, Integer counts, Timestamp endTime) {
        Map params = new HashMap();

        params.put("gameId", gameId);
        params.put("userId", userId);
        params.put("titleId", titleId);
        params.put("counts", counts);
        params.put("endTime", endTime);
        return sqlSessionTemplate.update("UserTitle.updateUserTitleAviable", params);
    }


}
