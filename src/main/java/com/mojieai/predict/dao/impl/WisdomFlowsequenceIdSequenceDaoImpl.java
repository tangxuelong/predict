package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.WisdomFlowsequenceIdSequenceDao;
import com.mojieai.predict.entity.po.WisdomFlowIdSequence;
import org.springframework.stereotype.Repository;

@Repository
public class WisdomFlowsequenceIdSequenceDaoImpl extends BaseDao implements WisdomFlowsequenceIdSequenceDao {

    @Override
    public Long insertIdSeq() {
        WisdomFlowIdSequence wisdomFlowIdSequence = new WisdomFlowIdSequence();
        wisdomFlowIdSequence.setStub("a");
        sqlSessionTemplate.insert("WisdomFlowIdSequence.insertIdSeq", wisdomFlowIdSequence);
        return wisdomFlowIdSequence.getFlowIdSeq();
    }

}
