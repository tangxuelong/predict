package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.KillCodeIdSequenceDao;
import com.mojieai.predict.entity.po.KillCodeIdSequence;
import org.springframework.stereotype.Repository;

@Repository
public class KillCodeSequenceDaoImpl extends BaseDao implements KillCodeIdSequenceDao {

    @Override
    public Long getKillCodeIdSequence() {
        KillCodeIdSequence killCodeIdSequence = new KillCodeIdSequence();
        killCodeIdSequence.setStub("a");
        sqlSessionTemplate.insert("KillCodeIdSequence.insertKillCodeIdSeq", killCodeIdSequence);
        Long encircleIdSeq = killCodeIdSequence.getKillCodeIdSeq();
        return encircleIdSeq;
    }
}
