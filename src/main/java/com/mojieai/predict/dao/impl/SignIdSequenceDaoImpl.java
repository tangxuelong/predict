package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.SignIdSequenceDao;
import com.mojieai.predict.entity.po.SignIdSequence;
import org.springframework.stereotype.Repository;

@Repository
public class SignIdSequenceDaoImpl extends BaseDao implements SignIdSequenceDao {

    @Override
    public Long getSignIdSequence() {
        SignIdSequence signIdSequence = new SignIdSequence();
        signIdSequence.setStub("a");
        sqlSessionTemplate.insert("SignIdSequence.insertSignIdSeq", signIdSequence);
        Long userIdSeq = signIdSequence.getSignIdSeq();
        return userIdSeq;
    }
}
