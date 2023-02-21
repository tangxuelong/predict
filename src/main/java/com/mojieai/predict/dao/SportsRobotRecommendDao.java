package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.SportRobotRecommend;
import com.mojieai.predict.thread.SportRobotTask;

import java.util.List;

public interface SportsRobotRecommendDao {

    SportRobotRecommend getRobotRecommendById(Integer robotId);

    List<SportRobotRecommend> getRobotByDate(Integer recommendDate);

    List<Integer> getRobotIdByDate(Integer batchNum);

    List<Long> getRobotUserIdByDate(Integer batchNum);

    int insert(SportRobotRecommend recommend);

    void insertBatch(List<SportRobotRecommend> robots);

    int updateByPrimaryKeySelective(SportRobotRecommend recommend);

    int batchUpdateSportRobotRecommendTimes(Integer recommendTimes);
}
