package com.mojieai.predict.enums;

import com.mojieai.predict.cache.SocialRobotCache;
import com.mojieai.predict.util.DateUtil;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public enum SportsRobotEnum {
    BATCH_TWELVE_ROBOT(12, 5) {
        @Override
        public List<Integer> getRobotIdByRandom() {
            return getRobotIdByRandom(7, 3);
        }
    }, BATCH_EIGHTEEN_ROBOT(18, 5) {
        @Override
        public List<Integer> getRobotIdByRandom() {
            return getRobotIdByRandom(16, 3);
        }
    }, BATCH_TWENTY_THREE_ROBOT(23, 5) {
        @Override
        public List<Integer> getRobotIdByRandom() {
            return getRobotIdByRandom(16, 3);
        }
    };

    private Integer batchNum;
    private Integer recommendTimes;

    SportsRobotEnum(Integer batchNum, Integer recommendTimes) {
        this.batchNum = batchNum;
        this.recommendTimes = recommendTimes;
    }

    public Integer getBatchNum() {
        return batchNum;
    }

    public Integer getRecommendTimes() {
        return recommendTimes;
    }

    public static SportsRobotEnum getCurrentTimeRobot() {
        String hour = DateUtil.getHour(DateUtil.getCurrentTimestamp());
        Integer hourInt = 23;
        if (Integer.valueOf(hour) < 12) {
            hourInt = 12;
        } else if (Integer.valueOf(hour) < 18) {
            hourInt = 18;
        } else if (Integer.valueOf(hour) < 23) {
            hourInt = 23;
        } else {
            return null;
        }
        return getSportsRobotEnum(hourInt);
    }

    public static SportsRobotEnum getSportsRobotEnum(Integer batchNum) {
        for (SportsRobotEnum sre : SportsRobotEnum.values()) {
            if (sre.batchNum.equals(batchNum)) {
                return sre;
            }
        }
        return null;
    }

    abstract public List<Integer> getRobotIdByRandom();

    public List<Integer> getRobotIdByRandom(Integer robotCount, Integer bound) {
        List<Integer> robotIds = SocialRobotCache.getSportsRobot(getBatchNum());
        Collections.shuffle(robotIds);
        int count = new Random().nextInt(bound) + robotCount;
        return robotIds.subList(0, count);
    }
}
