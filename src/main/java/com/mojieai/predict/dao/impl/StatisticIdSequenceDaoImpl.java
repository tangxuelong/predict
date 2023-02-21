package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.StatisticIdSequenceDao;
import com.mojieai.predict.entity.po.StatisticIdSequence;
import org.springframework.stereotype.Repository;

@Repository
public class StatisticIdSequenceDaoImpl extends BaseDao implements StatisticIdSequenceDao {

    @Override
    public Long insertStatisticIdSeq() {
        StatisticIdSequence statisticIdSequence = new StatisticIdSequence();
        statisticIdSequence.setStub("a");
        sqlSessionTemplate.insert("StatisticIdSequence.insertStatisticIdSeq", statisticIdSequence);
        Long vipFollowIdSeq = statisticIdSequence.getStatisticIdSeq();
        return vipFollowIdSeq;

    }
}
