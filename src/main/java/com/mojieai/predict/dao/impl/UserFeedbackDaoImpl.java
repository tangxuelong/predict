package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserFeedbackDao;
import com.mojieai.predict.entity.po.UserFeedback;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tangxuelong on 2017/8/24.
 */
@Repository
public class UserFeedbackDaoImpl extends BaseDao implements UserFeedbackDao {
    @Override
    public List<UserFeedback> getUnSendContent() {
        return sqlSessionTemplate.selectList("UserFeedback.getUnSendContent");
    }

    @Override
    public void insert(UserFeedback userFeedback) {
        sqlSessionTemplate.insert("UserFeedback.insert", userFeedback);
    }

    @Override
    public int update(Integer feedbackId) {
        Map<String, Object> params = new HashMap<>();
        params.put("feedbackId", feedbackId);
        return sqlSessionTemplate.update("UserFeedback.update", feedbackId);
    }
}
