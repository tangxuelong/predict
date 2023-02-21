package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserSignStatisticDao;
import com.mojieai.predict.entity.po.UserSign;
import com.mojieai.predict.entity.po.UserSignStatistic;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserSignStatisticDaoImpl extends BaseDao implements UserSignStatisticDao {

    @Override
    public UserSignStatistic getUserSignStatisticByUserId(Long userId, Integer signType) {
        Map params = new HashMap<>();
        params.put("userId", userId);
        params.put("signType", signType);
        return sqlSessionTemplate.selectOne("UserSignStatistic.getUserSignStatisticByUserId", params);
    }

    @Override
    public Integer updateUserStatistic(UserSignStatistic userSignStatistic) {
        return sqlSessionTemplate.update("UserSignStatistic.updateUserStatistic", userSignStatistic);
    }

    @Override
    public Integer insert(UserSignStatistic userSignStatistic) {
        return sqlSessionTemplate.insert("UserSignStatistic.insert", userSignStatistic);
    }
}
