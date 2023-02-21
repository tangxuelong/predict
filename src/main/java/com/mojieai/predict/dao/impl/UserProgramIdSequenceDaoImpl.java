package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserProgramIdSequenceDao;
import com.mojieai.predict.entity.po.UserProgramIdSequence;
import org.springframework.stereotype.Repository;

@Repository
public class UserProgramIdSequenceDaoImpl extends BaseDao implements UserProgramIdSequenceDao {

    @Override
    public Long insertIdSeq() {
        UserProgramIdSequence userProgramId = new UserProgramIdSequence();
        userProgramId.setStub("a");
        sqlSessionTemplate.insert("UserProgramIdSequence.insertIdSeq", userProgramId);
        Long programIdSeq = userProgramId.getProgramIdSeq();
        return programIdSeq;
    }
}
