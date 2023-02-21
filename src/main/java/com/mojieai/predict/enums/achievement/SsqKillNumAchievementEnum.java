package com.mojieai.predict.enums.achievement;

import com.mojieai.predict.constant.SocialEncircleKillConstant;
import com.mojieai.predict.entity.po.IndexUserSocialCode;
import com.mojieai.predict.entity.po.UserSocialRecord;
import com.mojieai.predict.util.SocialEncircleKillCodeUtil;

import java.util.List;

public enum SsqKillNumAchievementEnum implements SocialAchievement {

    KILL_NUM_1("杀1码", SocialEncircleKillConstant.SOCIAL_KILL_ACHIEVEMENT_KILL_1, 1, 1) {

    }, KILL_NUM_3("杀3码", SocialEncircleKillConstant.SOCIAL_KILL_ACHIEVEMENT_KILL_3, 3, 1) {

    }, KILL_NUM_5("杀5码", SocialEncircleKillConstant.SOCIAL_KILL_ACHIEVEMENT_KILL_5, 5, 1) {

    }, KILL_NUM_8("杀8码", SocialEncircleKillConstant.SOCIAL_KILL_ACHIEVEMENT_KILL_8, 8, 1) {

    }, KILL_NUM_10("杀10码", SocialEncircleKillConstant.SOCIAL_KILL_ACHIEVEMENT_KILL_10, 10, 1);

    private String name;
    private Integer type;
    private Integer socialCount;
    private Integer rightCount;

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

    SsqKillNumAchievementEnum(String name, Integer type, Integer socialCount, Integer rightCount) {
        this.name = name;
        this.type = type;
        this.socialCount = socialCount;
        this.rightCount = rightCount;
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
        for (SsqKillNumAchievementEnum killAchieve : SsqKillNumAchievementEnum.values()) {
            if (killAchieve.getType() == type) {
                return killAchieve.getName();
            }
        }
        return "";
    }
}
