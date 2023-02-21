package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.EncircleCodeIdSequenceDao;
import com.mojieai.predict.entity.po.EncircleCodeIdSequence;
import org.springframework.stereotype.Repository;

@Repository
public class EncircleCodeIdSequenceDaoImpl extends BaseDao implements EncircleCodeIdSequenceDao {

    @Override
    public Long getEncircleCodeIdSequence() {
        EncircleCodeIdSequence encircleCodeIdSequence = new EncircleCodeIdSequence();
        encircleCodeIdSequence.setStub("a");
        sqlSessionTemplate.insert("EncircleCodeIdSequence.insertEncircleCodeIdSeq", encircleCodeIdSequence);
        Long encircleIdSeq = encircleCodeIdSequence.getEncircleCodeIdSeq();
        return encircleIdSeq;
    }
}
