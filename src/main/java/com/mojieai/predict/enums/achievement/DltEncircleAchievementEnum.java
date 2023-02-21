package com.mojieai.predict.enums.achievement;

import com.mojieai.predict.constant.SocialEncircleKillConstant;
import com.mojieai.predict.entity.po.IndexUserSocialCode;
import com.mojieai.predict.entity.po.UserSocialRecord;
import com.mojieai.predict.util.SocialEncircleKillCodeUtil;

import java.util.List;

public enum DltEncircleAchievementEnum implements SocialAchievement {
    ENCIRCLE_10_6("10中5", SocialEncircleKillConstant.SOCIAL_ENCIRCLE_ACHIEVEMENT_10_5, 10, 5) {

    }, ENCIRCLE_5_5("5中5", SocialEncircleKillConstant.SOCIAL_ENCIRCLE_ACHIEVEMENT_5_5, 5, 5) {

    }, ENCIRCLE_15_6("15中5", SocialEncircleKillConstant.SOCIAL_ENCIRCLE_ACHIEVEMENT_15_5, 15, 5) {

    }, ENCIRCLE_5_4("5中4", SocialEncircleKillConstant.SOCIAL_ENCIRCLE_ACHIEVEMENT_5_4, 5, 4) {

    }, ENCIRCLE_10_5("10中4", SocialEncircleKillConstant.SOCIAL_ENCIRCLE_ACHIEVEMENT_10_4, 10, 4) {

    }, ENCIRCLE_20_6("20中5", SocialEncircleKillConstant.SOCIAL_ENCIRCLE_ACHIEVEMENT_20_5, 20, 5) {

    };
    private String name;
    private Integer type;
    private Integer socialCount;
    private Integer rightCount;

    DltEncircleAchievementEnum(String name, Integer type, Integer socialCount, Integer rightCount) {
        this.name = name;
        this.type = type;
        this.socialCount = socialCount;
        this.rightCount = rightCount;
    }

    public String getName() {
        return this.name;
    }

    public Integer getType() {
        return this.type;
    }

    public Integer getSocialCount() {
        return this.socialCount;
    }

    public Integer getRightCount() {
        return this.rightCount;
    }

    @Override
    public void generateNewRecord(String openAwardPeriodId, List<IndexUserSocialCode> userAllSocial, UserSocialRecord
            lastUserSocialRecord) {
        SocialEncircleKillCodeUtil.generateNewRecord(openAwardPeriodId, userAllSocial, lastUserSocialRecord,
                getRightCount());
    }

    @Override
    public Integer getAchievementType() {
        return getType();
    }

    @Override
    public String getAchievementName() {
        return getName();
    }

    @Override
    public Integer getAchievementSocialCount() {
        return getSocialCount();
    }

    @Override
    public Integer getAchievementRightCount() {
        return getRightCount();
    }

    public static String getNameByType(Integer type) {
        for (DltEncircleAchievementEnum encircleAchieve : DltEncircleAchievementEnum.values()) {
            if (encircleAchieve.getType() == type) {
                return encircleAchieve.getName();
            }
        }
        return "";
    }

}
