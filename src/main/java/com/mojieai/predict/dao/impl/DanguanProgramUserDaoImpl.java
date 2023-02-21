package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.DanguanProgramDao;
import com.mojieai.predict.dao.DanguanProgramUserDao;
import com.mojieai.predict.entity.po.DanguanProgram;
import com.mojieai.predict.entity.po.DanguanProgramUser;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class DanguanProgramUserDaoImpl extends BaseDao implements DanguanProgramUserDao {

    @Override
    public DanguanProgramUser getDanguanProgramUserLog(Long userId, String matchId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("matchId", matchId);
        return sqlSessionTemplate.selectOne("DanguanProgramUser.getDanguanProgramUserLog", params);
    }

    @Override
    public void update(DanguanProgramUser danguanProgramUser) {
        sqlSessionTemplate.update("DanguanProgramUser.update", danguanProgramUser);
    }

    @Override
    public void insert(DanguanProgramUser danguanProgramUser) {
        sqlSessionTemplate.insert("DanguanProgramUser.insert", danguanProgramUser);
    }
}
