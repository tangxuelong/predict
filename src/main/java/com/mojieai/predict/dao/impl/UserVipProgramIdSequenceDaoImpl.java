package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserVipProgramIdSequenceDao;
import com.mojieai.predict.entity.po.UserVipProgramIdSeq;
import org.springframework.stereotype.Repository;

@Repository
public class UserVipProgramIdSequenceDaoImpl extends BaseDao implements UserVipProgramIdSequenceDao {

    @Override
    public Long insertIdSeq() {
        UserVipProgramIdSeq userVipIdSeq = new UserVipProgramIdSeq();
        userVipIdSeq.setStub("a");
        sqlSessionTemplate.insert("UserVipProgramIdSeq.insertIdSeq", userVipIdSeq);
        return userVipIdSeq.getVipProgramIdSeq();
    }
}
