package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserBuyRecommendIdSequenceDao;
import com.mojieai.predict.entity.po.UserBuyRecommendIdSeq;
import org.springframework.stereotype.Repository;

@Repository
public class UserBuyRecommendIdSequenceDaoImpl extends BaseDao implements UserBuyRecommendIdSequenceDao {

    @Override
    public Long insertIdSeq() {
        UserBuyRecommendIdSeq logIdSequence = new UserBuyRecommendIdSeq();
        logIdSequence.setStub("a");
        sqlSessionTemplate.insert("UserBuyRecommendIdSeq.insertIdSeq", logIdSequence);
        return logIdSequence.getLogIdSeq();
    }
}
