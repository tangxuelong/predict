package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserCouponFlowIdSeqDao;
import com.mojieai.predict.entity.po.UserCouponFlowIdSeq;
import org.springframework.stereotype.Repository;

@Repository
public class UserCouponFlowIdSeqDaoImpl extends BaseDao implements UserCouponFlowIdSeqDao {

    @Override
    public Long insertIdSeq() {
        UserCouponFlowIdSeq userCouponFlowIdSeq = new UserCouponFlowIdSeq();
        userCouponFlowIdSeq.setStub("a");
        sqlSessionTemplate.insert("UserCouponFlowIdSeq.insertIdSeq", userCouponFlowIdSeq);
        return userCouponFlowIdSeq.getCouponFlowIdSeq();
    }
}
