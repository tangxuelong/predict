package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.RongShuAutLogDao;
import com.mojieai.predict.entity.po.RongShuAutLog;
import org.springframework.stereotype.Repository;

@Repository
public class RongShuAutLogDaoImpl extends BaseDao implements RongShuAutLogDao {

    @Override
    public Integer insert(RongShuAutLog rongShuAutLog) {
        return sqlSessionTemplate.insert("RongShuAutLog.insert");
    }
}
