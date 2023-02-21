package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserSocialTaskIdSequenceDao;
import com.mojieai.predict.entity.po.UserSocialTaskIdSequence;
import org.springframework.stereotype.Repository;

@Repository
public class UserSocialTaskIdSequenceDaoImpl extends BaseDao implements UserSocialTaskIdSequenceDao {
    @Override
    public Long insertTaskIdSeq() {
        UserSocialTaskIdSequence taskIdSequence = new UserSocialTaskIdSequence();
        taskIdSequence.setStub("a");
        sqlSessionTemplate.insert("UserSocialTaskIdSequence.insertTaskIdSeq", taskIdSequence);
        Long taskIdSeq = taskIdSequence.getTaskIdSeq();
        return taskIdSeq;
    }
}
