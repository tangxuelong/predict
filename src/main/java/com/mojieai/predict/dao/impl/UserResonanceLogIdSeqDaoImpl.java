package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserResonanceLogIdSeqDao;
import com.mojieai.predict.entity.po.ResonanceLogIdSeq;
import org.springframework.stereotype.Repository;

@Repository
public class UserResonanceLogIdSeqDaoImpl extends BaseDao implements UserResonanceLogIdSeqDao {

    @Override
    public Long insertIdSeq() {
        ResonanceLogIdSeq logIdSequence = new ResonanceLogIdSeq();
        logIdSequence.setStub("a");
        sqlSessionTemplate.insert("ResonanceLogIdSeq.insertIdSeq", logIdSequence);
        return logIdSequence.getLogIdSeq();
    }
}
