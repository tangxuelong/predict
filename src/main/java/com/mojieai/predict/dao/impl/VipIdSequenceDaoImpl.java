package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserIdSequenceDao;
import com.mojieai.predict.dao.VipIdSequenceDao;
import com.mojieai.predict.entity.po.UserIdSequence;
import com.mojieai.predict.entity.po.VipIdSequence;
import org.springframework.stereotype.Repository;

@Repository
public class VipIdSequenceDaoImpl extends BaseDao implements VipIdSequenceDao {

    @Override
    public Long getVipIdSequence() {
        VipIdSequence vipIdSequence = new VipIdSequence();
        vipIdSequence.setStub("a");
        sqlSessionTemplate.insert("VipIdSequence.insertVipIdSeq", vipIdSequence);
        Long vipIdSeq = vipIdSequence.getVipIdSeq();
        return vipIdSeq;
    }
}