package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserNumBookIdSequenceDao;
import com.mojieai.predict.entity.po.UserNumBookIdSequence;
import org.springframework.stereotype.Repository;

@Repository
public class UserNumBookIdSequenceDaoImpl extends BaseDao implements UserNumBookIdSequenceDao {
    @Override
    public Long insertUserNumBookIdSeq() {
        UserNumBookIdSequence numIdSequence = new UserNumBookIdSequence();
        numIdSequence.setStub("a");
        sqlSessionTemplate.insert("UserNumBookIdSequence.insertUserNumBookIdSeq", numIdSequence);
        return numIdSequence.getNumIdSeq();
    }
}
