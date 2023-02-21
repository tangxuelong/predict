package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.ProgramIdSequenceDao;
import com.mojieai.predict.entity.po.ProgramIdSequence;
import org.springframework.stereotype.Repository;

@Repository
public class ProgramIdSequenceDaoImpl extends BaseDao implements ProgramIdSequenceDao {


    @Override
    public Long getProgramIdSequence() {
        ProgramIdSequence programIdSequence = new ProgramIdSequence();
        programIdSequence.setStub("a");
        sqlSessionTemplate.insert("ProgramIdSequence.insertProgramIdSeq", programIdSequence);
        Long programIdSeq = programIdSequence.getProgramIdSeq();
        return programIdSeq;
    }
}