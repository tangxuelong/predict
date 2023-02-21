package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.UserFeedback;

import java.util.List;

/**
 * Created by tangxuelong on 2017/8/24.
 */
public interface UserFeedbackDao {
    List<UserFeedback> getUnSendContent();

    void insert(UserFeedback userFeedback);

    int update(Integer feedbackId);
}
