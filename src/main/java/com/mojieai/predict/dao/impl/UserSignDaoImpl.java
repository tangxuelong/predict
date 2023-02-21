package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserSignDao;
import com.mojieai.predict.entity.po.UserSign;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserSignDaoImpl extends BaseDao implements UserSignDao {

    @Override
    public UserSign getUserSignByUserIdAndDate(Long userId, String signDate, Integer signType) {
        Map params = new HashMap<>();

        params.put("userId", userId);
        params.put("signDate", signDate);
        params.put("signType", signType);
        return sqlSessionTemplate.selectOne("UserSign.getUserSignByUserIdAndDate", params);
    }

    @Override
    public List<UserSign> getAllNeedRewardSign(Long userId) {
        Map params = new HashMap<>();
        params.put("userId", userId);
        params.put("ifReward", 0);
        return sqlSessionTemplate.selectList("UserSign.getAllNeedRewardSign", params);
    }

    @Override
    public Integer getUserSignCountByIntervalDate(Long userId, Integer signType, String beginDate, String endDate) {
        Map params = new HashMap<>();
        params.put("userId", userId);
        params.put("beginDate", beginDate);
        params.put("endDate", endDate);
        params.put("signType", signType);
        return sqlSessionTemplate.selectOne("UserSign.getUserSignCountByIntervalDate", params);
    }

    @Override
    public Integer updateUserSignRewardStatus(Long userId, String signDate, Integer signType, Integer ifReward) {
        Map params = new HashMap();

        params.put("userId", userId);
        params.put("ifReward", ifReward);
        params.put("signDate", signDate);
        params.put("signType", signType);
        return sqlSessionTemplate.update("UserSign.updateUserSignRewardStatus", params);
    }

    @Override
    public Integer updateSignRewardStatusBySignCode(Long userId, Long signCode, Integer ifReward) {
        Map params = new HashMap();
        params.put("ifReward", ifReward);
        params.put("signCode", signCode);
        params.put("userId", userId);
        return sqlSessionTemplate.update("UserSign.updateSignRewardStatusBySignCode", params);
    }

    @Override
    public Integer insert(UserSign userSign) {
        return sqlSessionTemplate.insert("UserSign.insert", userSign);
    }
}
