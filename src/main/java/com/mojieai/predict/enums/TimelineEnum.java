package com.mojieai.predict.enums;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.RedisConstant;

import java.util.ArrayList;
import java.util.List;

public enum TimelineEnum {

    START_TIME("startTime"), END_TIME("endTime"), OPEN_TIME("openTime"), AWARD_TIME("awardTime");

    private String name;

    TimelineEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getTimelineKey(Long gameId) {
        return new StringBuffer().append(RedisConstant.PREFIX_PERIOD).append(gameId).append(CommonConstant
                .COMMON_COLON_STR).append(getName()).toString();
    }

    public static List<String> getTimelineKeys(Long gameId) {
        List<String> allKeys = new ArrayList<>();
        for (TimelineEnum timelineEnum : values()) {
            allKeys.add(timelineEnum.getTimelineKey(gameId));
        }
        return allKeys;
    }
}
