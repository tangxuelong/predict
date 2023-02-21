package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserSubscribeLogIdSequenceDao;
import com.mojieai.predict.entity.po.SubScribeIdSequence;
import org.springframework.stereotype.Repository;

@Repository
public class UserSubscribeLogIdSequenceDaoImpl extends BaseDao implements UserSubscribeLogIdSequenceDao {

    @Override
    public Long insertIdSeq() {
        SubScribeIdSequence logIdSequence = new SubScribeIdSequence();
        logIdSequence.setStub("a");
        sqlSessionTemplate.insert("SubScribeIdSequence.insertIdSeq", logIdSequence);
        return logIdSequence.getLogIdSeq();
    }
}
