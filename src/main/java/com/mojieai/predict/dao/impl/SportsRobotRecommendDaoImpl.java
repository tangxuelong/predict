package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.SocialRobotDao;
import com.mojieai.predict.dao.SportsRobotRecommendDao;
import com.mojieai.predict.entity.po.SocialRobot;
import com.mojieai.predict.entity.po.SportRobotRecommend;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class SportsRobotRecommendDaoImpl extends BaseDao implements SportsRobotRecommendDao {

    @Override
    public SportRobotRecommend getRobotRecommendById(Integer robotId) {
        Map param = new HashMap<>();
        param.put("robotId", robotId);
        return sqlSessionTemplate.selectOne("SportRobotRecommend.getRobotRecommendById", param);
    }

    @Override
    public List<SportRobotRecommend> getRobotByDate(Integer recommendDate) {
        Map param = new HashMap<>();
        param.put("recommendDate", recommendDate);
        return sqlSessionTemplate.selectList("SportRobotRecommend.getRobotByDate", param);
    }

    @Override
    public List<Integer> getRobotIdByDate(Integer batchNum) {
        Map param = new HashMap<>();
        param.put("recommendDate", batchNum);
        return sqlSessionTemplate.selectList("SportRobotRecommend.getRobotIdByDate", param);
    }

    @Override
    public List<Long> getRobotUserIdByDate(Integer batchNum) {
        Map param = new HashMap<>();
        param.put("recommendDate", batchNum);
        return sqlSessionTemplate.selectList("SportRobotRecommend.getRobotUserIdByDate", param);
    }

    @Override
    public int insert(SportRobotRecommend recommend) {
        return sqlSessionTemplate.insert("SportRobotRecommend.insert", recommend);
    }

    @Override
    public void insertBatch(List<SportRobotRecommend> robots) {
        Map param = new HashMap<>();
        param.put("robots", robots);
        sqlSessionTemplate.insert("", param);
    }

    @Override
    public int updateByPrimaryKeySelective(SportRobotRecommend recommend) {
        return sqlSessionTemplate.update("SportRobotRecommend.updateByPrimaryKeySelective", recommend);
    }

    @Override
    public int batchUpdateSportRobotRecommendTimes(Integer recommendTimes) {
        Map params = new HashMap();
        params.put("recommendTimes", recommendTimes);
        return sqlSessionTemplate.update("SportRobotRecommend.batchUpdateSportRobotRecommendTimes", params);
    }
}
