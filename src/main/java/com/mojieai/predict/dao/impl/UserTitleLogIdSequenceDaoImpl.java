package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserTitleLogIdSequenceDao;
import com.mojieai.predict.entity.po.UserTitleLog;
import com.mojieai.predict.entity.po.UserTitleLogIdSequence;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserTitleLogIdSequenceDaoImpl extends BaseDao implements UserTitleLogIdSequenceDao {

    @Override
    public Long getUserTitleLogIdSequence() {
        UserTitleLogIdSequence userTitleLogIdSequence = new UserTitleLogIdSequence();
        userTitleLogIdSequence.setStub("a");
        sqlSessionTemplate.insert("UserTitleLogIdSequence.insertTitleLogIdSeq", userTitleLogIdSequence);
        Long userTitleLogId = userTitleLogIdSequence.getTitleLogIdSeq();
        return userTitleLogId;
    }
}
