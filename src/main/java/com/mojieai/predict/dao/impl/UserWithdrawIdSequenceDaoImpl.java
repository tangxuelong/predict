package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserWithdrawIdSequenceDao;
import com.mojieai.predict.entity.po.UserWithdrawFlowIdSeq;
import org.springframework.stereotype.Repository;

@Repository
public class UserWithdrawIdSequenceDaoImpl extends BaseDao implements UserWithdrawIdSequenceDao {

    @Override
    public Long insertIdSeq() {
        UserWithdrawFlowIdSeq userWithdrawFlowIdSeq = new UserWithdrawFlowIdSeq();
        userWithdrawFlowIdSeq.setStub("a");
        sqlSessionTemplate.insert("UserWithdrawFlowIdSeq.insertIdSeq", userWithdrawFlowIdSeq);
        return userWithdrawFlowIdSeq.getWithdrawIdSequence();
    }
}
