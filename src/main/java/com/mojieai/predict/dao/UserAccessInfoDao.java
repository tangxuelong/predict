package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.UserAccessInfo;

public interface UserAccessInfoDao {
    UserAccessInfo getUserAccessInfo(Integer accessId);

    void insert(UserAccessInfo userAccessInfo);

    void update(UserAccessInfo userAccessInfo);
}
