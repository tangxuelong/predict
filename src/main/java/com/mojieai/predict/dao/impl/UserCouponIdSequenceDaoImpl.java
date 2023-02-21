package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserCouponIdSequenceDao;
import com.mojieai.predict.entity.po.UserCouponIdSequence;
import org.springframework.stereotype.Repository;

@Repository
public class UserCouponIdSequenceDaoImpl extends BaseDao implements UserCouponIdSequenceDao {

    @Override
    public Long insertIdSeq() {
        UserCouponIdSequence userCouponIdSeq = new UserCouponIdSequence();
        userCouponIdSeq.setStub("a");
        sqlSessionTemplate.insert("UserCouponIdSequence.insertIdSeq", userCouponIdSeq);
        return userCouponIdSeq.getCouponIdSeq();
    }
}
