package com.mojieai.predict.enums.achievement;

import com.mojieai.predict.entity.po.IndexUserSocialCode;
import com.mojieai.predict.entity.po.UserSocialRecord;

import java.util.List;

public interface SocialAchievement {
    void generateNewRecord(String openAwardPeriodId, List<IndexUserSocialCode> userAllSocial, UserSocialRecord
            lastUserSocialRecord);

    Integer getAchievementType();

    String getAchievementName();

    Integer getAchievementSocialCount();

    Integer getAchievementRightCount();
}
