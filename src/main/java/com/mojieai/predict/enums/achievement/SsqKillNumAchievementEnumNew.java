package com.mojieai.predict.enums.achievement;

import com.mojieai.predict.constant.SocialEncircleKillConstant;
import com.mojieai.predict.entity.po.IndexUserSocialCode;
import com.mojieai.predict.entity.po.UserSocialRecord;
import com.mojieai.predict.util.SocialEncircleKillCodeUtil;

import java.util.List;

public enum SsqKillNumAchievementEnumNew implements SocialAchievement {

    KILL_NUM_10_10("10中10", SocialEncircleKillConstant.SOCIAL_KILL_ACHIEVEMENT_KILL_NEW_10, 10, 10) {

    }, KILL_NUM_9_9("9中9", SocialEncircleKillConstant.SOCIAL_KILL_ACHIEVEMENT_KILL_NEW_9, 9, 10) {

    }, KILL_NUM_8_8("8中8", SocialEncircleKillConstant.SOCIAL_KILL_ACHIEVEMENT_KILL_NEW_8, 8, 10) {

    }, KILL_NUM_7_7("7中7", SocialEncircleKillConstant.SOCIAL_KILL_ACHIEVEMENT_KILL_NEW_7, 7, 10) {

    }, KILL_NUM_6_6("6中6", SocialEncircleKillConstant.SOCIAL_KILL_ACHIEVEMENT_KILL_NEW_6, 6, 10);

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

    SsqKillNumAchievementEnumNew(String name, Integer type, Integer socialCount, Integer rightCount) {
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
        for (SsqKillNumAchievementEnumNew killAchieve : SsqKillNumAchievementEnumNew.values()) {
            if (killAchieve.getType() == type) {
                return killAchieve.getName();
            }
        }
        return "";
    }
}
