package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.RobotEncircleDao;
import com.mojieai.predict.entity.po.RobotEncircle;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class RobotEncircleDaoImpl extends BaseDao implements RobotEncircleDao {
    @Override
    public RobotEncircle getRobotEncircleById(long gameId, Integer robotId, String periodId) {
        Map params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("robotId", robotId);
        params.put("periodId", periodId);
        return sqlSessionTemplate.selectOne("RobotEncircle.getRobotEncircleById", params);
    }

    @Override
    public Integer robotKillNumSuccessUpdateInfo(long gameId, String periodId, Integer robotId) {
        Map params = new HashMap();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        params.put("robotId", robotId);
        return sqlSessionTemplate.update("RobotEncircle.robotKillNumSuccessUpdateInfo", params);
    }

    @Override
    public Integer robotEncircleNumSuccessUpdateInfo(long gameId, String periodId, Integer robotId) {
        Map params = new HashMap();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        params.put("robotId", robotId);
        return sqlSessionTemplate.update("RobotEncircle.robotEncircleNumSuccessUpdateInfo", params);
    }

    @Override
    public int deleteByPrimaryKey(RobotEncircle key) {
        return 0;
    }

    @Override
    public int insert(RobotEncircle record) {
        return sqlSessionTemplate.insert("RobotEncircle.insert", record);
    }

    @Override
    public int insertSelective(RobotEncircle record) {
        return sqlSessionTemplate.insert("RobotEncircle.insertSelective", record);
    }

    @Override
    public RobotEncircle selectByPrimaryKey(RobotEncircle key) {
        return sqlSessionTemplate.selectOne("RobotEncircle.selectByPrimaryKey", key);
    }

    @Override
    public int updateByPrimaryKeySelective(RobotEncircle record) {
        return sqlSessionTemplate.update("RobotEncircle.updateByPrimaryKeySelective", record);
    }
}
