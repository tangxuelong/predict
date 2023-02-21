package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserIdSequenceDao;
import com.mojieai.predict.entity.po.UserIdSequence;
import org.springframework.stereotype.Repository;

@Repository
public class UserIdSequenceDaoImpl extends BaseDao implements UserIdSequenceDao {

    @Override
    public Long getUserIdSequence() {
        UserIdSequence userIdSequence = new UserIdSequence();
        userIdSequence.setStub("a");
        sqlSessionTemplate.insert("UserIdSequence.insertUserIdSeq", userIdSequence);
        Long userIdSeq = userIdSequence.getUserIdSeq();
        return userIdSeq;
    }
}