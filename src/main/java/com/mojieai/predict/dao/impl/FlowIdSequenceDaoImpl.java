package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.FlowIdSequenceDao;
import com.mojieai.predict.entity.po.FlowIdSequence;
import org.springframework.stereotype.Repository;

@Repository
public class FlowIdSequenceDaoImpl extends BaseDao implements FlowIdSequenceDao {

    @Override
    public Long getFlowIdSequence() {
        FlowIdSequence flowIdSequence = new FlowIdSequence();
        flowIdSequence.setStub("a");
        sqlSessionTemplate.insert("FlowIdSequence.insertFlowIdSeq", flowIdSequence);
        Long flowIdSeq = flowIdSequence.getFlowIdSeq();
        return flowIdSeq;
    }
}