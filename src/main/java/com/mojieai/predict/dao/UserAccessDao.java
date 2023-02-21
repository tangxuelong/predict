package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.UserAccess;

public interface UserAccessDao {
    UserAccess getUserAccess(Long userId, String periodId, Long gameId);

    void insert(UserAccess userAccess);

    void update(UserAccess userAccess);
}
