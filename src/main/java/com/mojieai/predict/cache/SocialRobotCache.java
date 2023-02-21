package com.mojieai.predict.cache;

import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.SocialEncircleKillConstant;
import com.mojieai.predict.dao.SocialRobotDao;
import com.mojieai.predict.dao.SportsRobotRecommendDao;
import com.mojieai.predict.entity.po.SocialRobot;
import com.mojieai.predict.entity.po.SportRobotRecommend;
import com.mojieai.predict.enums.SportsRobotEnum;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class SocialRobotCache {
    protected static Logger log = LogConstant.commonLog;

    private static List<SocialRobot> robots = null;
    private static Map<Integer, List<Integer>> sportsRobots = null;

    @Autowired
    private SocialRobotDao socialRobotDao;
    @Autowired
    private SportsRobotRecommendDao sportsRobotRecommendDao;

    public SocialRobotCache() {
    }

    public void init() {
        refresh();
    }

    public void refresh() {
        robots = socialRobotDao.getAllSocialRobot(SocialEncircleKillConstant.SOCIAL_ROBOT_ENABLE,
                SocialEncircleKillConstant.SOCIAL_ROBOT_TYPE_DIGIT);
        sportsRobots = new HashMap<>();
        for (SportsRobotEnum sre : SportsRobotEnum.values()) {
            List<Integer> sportRobot = sportsRobotRecommendDao.getRobotIdByDate(sre.getBatchNum());
            sportsRobots.put(sre.getBatchNum(), sportRobot);
        }
    }

    public static List<SocialRobot> getSomeRobotByRandom(int count) {
        List<SocialRobot> result = new ArrayList<>();
        if (robots == null || robots.size() <= 0) {
            return null;
        }
        if (count > robots.size()) {
            count = robots.size();
        }
        Collections.shuffle(robots, new Random());
        for (int i = 0; i < count; i++) {
            result.add(robots.get(i));
        }
        return result;
    }

    public static List<SocialRobot> getAllRobots() {
        return robots;
    }

    public static List<Integer> getSportsRobot(Integer batchNum) {
        if (sportsRobots == null || !sportsRobots.containsKey(batchNum)) {
            return null;
        }
        return sportsRobots.get(batchNum);
    }
}
