package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.DanguanProgramUserDao;
import com.mojieai.predict.dao.DanguanProgramUserTimesDao;
import com.mojieai.predict.entity.po.DanguanProgramUser;
import com.mojieai.predict.entity.po.DanguanProgramUserTimes;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class DanguanProgramUserTimesDaoImpl extends BaseDao implements DanguanProgramUserTimesDao {

    @Override
    public DanguanProgramUserTimes getDanguanProgramUserTimes(Long userId,Boolean isLock) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("isLock", isLock);
        return sqlSessionTemplate.selectOne("DanguanProgramUserTimes.getDanguanProgramUserTimes", params);
    }

    @Override
    public void update(DanguanProgramUserTimes danguanProgramUserTimes) {
        sqlSessionTemplate.update("DanguanProgramUserTimes.update", danguanProgramUserTimes);
    }

    @Override
    public void insert(DanguanProgramUserTimes danguanProgramUserTimes) {
        sqlSessionTemplate.insert("DanguanProgramUserTimes.insert", danguanProgramUserTimes);
    }
}
