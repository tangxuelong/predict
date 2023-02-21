package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.VipIdSequenceDao;
import com.mojieai.predict.dao.VipOperateIdSequenceDao;
import com.mojieai.predict.entity.po.VipIdSequence;
import com.mojieai.predict.entity.po.VipOperateIdSequence;
import org.springframework.stereotype.Repository;

@Repository
public class VipOperateIdSequenceDaoImpl extends BaseDao implements VipOperateIdSequenceDao {

    @Override
    public Long getVipOperateIdSequence() {
        VipOperateIdSequence vipFollowIdSequence = new VipOperateIdSequence();
        vipFollowIdSequence.setStub("a");
        sqlSessionTemplate.insert("VipOperateIdSequence.insertVipOperateIdSeq", vipFollowIdSequence);
        Long vipFollowIdSeq = vipFollowIdSequence.getVipOperateIdSeq();
        return vipFollowIdSeq;
    }
}