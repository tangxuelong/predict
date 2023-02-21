package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.RecommendIdSequenceDao;
import com.mojieai.predict.entity.po.RecommendIdSequence;
import org.springframework.stereotype.Repository;

@Repository
public class RecommendIdSequenceDaoImpl extends BaseDao implements RecommendIdSequenceDao {

    @Override
    public Long getRecommendIdSequence() {
        RecommendIdSequence recommendIdSequence = new RecommendIdSequence();
        recommendIdSequence.setStub("a");
        sqlSessionTemplate.insert("RecommendIdSequence.insertRecommendIdSeq", recommendIdSequence);
        Long recommendIdSeq = recommendIdSequence.getRecommendIdSeq();
        return recommendIdSeq;
    }
}