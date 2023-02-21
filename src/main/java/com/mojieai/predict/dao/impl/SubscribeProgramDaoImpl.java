package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.SubscribeProgramDao;
import com.mojieai.predict.entity.po.SubscribeProgram;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class SubscribeProgramDaoImpl extends BaseDao implements SubscribeProgramDao {

    @Override
    public SubscribeProgram getSubscribePredictByProgramId(Integer programId) {
        Map params = new HashMap<>();
        params.put("programId", programId);
        return sqlSessionTemplate.selectOne("SubscribeProgram.getSubscribePredictByProgramId", params);
    }

    @Override
    public SubscribeProgram getSubscribePredictByUnique(long gameId, Integer programType, Integer predictType) {
        Map params = new HashMap<>();

        params.put("gameId", gameId);
        params.put("programType", programType);
        params.put("predictType", predictType);
        return sqlSessionTemplate.selectOne("SubscribeProgram.getSubscribePredictByUnique", params);
    }

    @Override
    public List<SubscribeProgram> getSubscribeProgramByProgramType(long gameId, Integer programType) {
        Map params = new HashMap<>();

        params.put("gameId", gameId);
        params.put("programType", programType);
        return sqlSessionTemplate.selectList("SubscribeProgram.getSubscribeProgramByProgramType", params);
    }

    @Override
    public Integer insert(SubscribeProgram subscribeProgram) {
        return sqlSessionTemplate.insert("SubscribeProgram.insert", subscribeProgram);
    }

}
