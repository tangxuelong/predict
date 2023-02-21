package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.SocialRobotDao;
import com.mojieai.predict.entity.po.SocialRobot;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class SocialRobotDaoImpl extends BaseDao implements SocialRobotDao {

    @Override
    public List<SocialRobot> getAllSocialRobot(Integer isEnable, Integer robotType) {
        Map param = new HashMap<>();
        param.put("isEnable", isEnable);
        param.put("robotType", robotType);
        return sqlSessionTemplate.selectList("SocialRobot.getAllSocialRobot", param);
    }

    @Override
    public List<Long> getAllRobotUserIds() {
        return sqlSessionTemplate.selectList("SocialRobot.getAllRobotUserIds");
    }

    @Override
    public int insert(SocialRobot record) {
        return sqlSessionTemplate.insert("SocialRobot.insert", record);
    }

    @Override
    public int insertSelective(SocialRobot record) {
        return insert(record);
    }

    @Override
    public SocialRobot selectByPrimaryKey(Integer robotId) {
        return sqlSessionTemplate.selectOne("SocialRobot.selectByPrimaryKey", robotId);
    }

    @Override
    public int updateByPrimaryKeySelective(SocialRobot record) {
        return sqlSessionTemplate.update("SocialRobot.updateByPrimaryKeySelective", record);
    }
}
