package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserResonanceLogDetailDao;
import com.mojieai.predict.entity.po.UserResonanceLogDetail;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class UserResonanceLogDetailDaoImpl extends BaseDao implements UserResonanceLogDetailDao {
    @Override
    public Integer updatePayStatusByResonanceLogId(String resonanceLogId, Integer setPayStatus, Integer
            originPayStatus, Long userId) {
        Map params = new HashMap<>();

        params.put("resonanceLogId", resonanceLogId);
        params.put("setPayStatus", setPayStatus);
        params.put("originPayStatus", originPayStatus);
        params.put("userId", userId);
        return sqlSessionTemplate.update("UserResonanceLogDetail.updatePayStatusByResonanceLogId", params);
    }

    @Override
    public Integer insert(UserResonanceLogDetail userResonanceLogDetail) {
        return sqlSessionTemplate.insert("UserResonanceLogDetail.insert", userResonanceLogDetail);
    }
}
