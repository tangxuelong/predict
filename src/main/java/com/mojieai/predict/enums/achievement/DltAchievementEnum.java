package com.mojieai.predict.enums.achievement;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.GameConstant;
import com.mojieai.predict.constant.SocialEncircleKillConstant;
import com.mojieai.predict.entity.po.UserSocialRecord;
import com.mojieai.predict.entity.vo.AchievementVo;

import java.util.*;
import java.util.stream.Collectors;

public enum DltAchievementEnum {
    DLT_ENCIRCLE_RED(SocialEncircleKillConstant.SOCIAL_OPERATE_NUM_ENCIRCLE_RED, GameConstant.DLT, 5,
            SocialEncircleKillConstant.SOCIAL_ENCIRCLE_MAX_COUNT) {
        @Override
        String getAchieveName(Integer recordType) {
            return DltEncircleAchievementEnum.getNameByType(recordType);
        }

        @Override
        public SocialAchievement[] getSocialAchievement() {
            return DltEncircleAchievementEnum.values();
        }

        @Override
        public Map<String, List<AchievementVo>> makeAchievementVo(List<Map> userRecentSocial, List<UserSocialRecord>
                userRecentAchieves) {
            Map<String, List<AchievementVo>> result = new HashMap<>();
            List<AchievementVo> encircleAchievements = new ArrayList<>();
            List<AchievementVo> encircleRecentAchieves = new ArrayList<>();

            Map<String, Map<String, Object>> periodAchieve = new HashMap<>();
            for (Map userSocial : userRecentSocial) {//PERIOD_ID, SOCIAL_CODE_TYPE, SOCIAL_COUNT, SOCIAL_RIGHT_COUNT
                if (periodAchieve.size() > getRecentAchieveCount()) {
                    break;
                }
                Map<String, Object> innerMap = new HashMap<>();
                String periodId = userSocial.get("PERIOD_ID").toString();
                String socialCount = userSocial.get("SOCIAL_COUNT").toString();
                Integer socialRightCount = Integer.valueOf(userSocial.get("SOCIAL_RIGHT_COUNT").toString());
                if (periodAchieve.containsKey(periodId)) {
                    innerMap = periodAchieve.get(periodId);
                    Set<String> desc = (Set<String>) innerMap.get("achieveDesc");
                    String achieveDesc = "围" + socialCount + "中" + socialRightCount;
                    if (socialRightCount > 0) {
                        achieveDesc = CommonConstant.COMMON_BRACKET_LEFT + achieveDesc + CommonConstant
                                .COMMON_BRACKET_RIGHT;
                    }
                    desc.add(achieveDesc);
                    innerMap.put("achieveDesc", desc);
                } else {
                    Set<String> desc = new HashSet();
                    String achieveDesc = "围" + socialCount + "中" + socialRightCount;
                    if (socialRightCount > 0) {
                        achieveDesc = CommonConstant.COMMON_BRACKET_LEFT + achieveDesc + CommonConstant
                                .COMMON_BRACKET_RIGHT;
                    }
                    desc.add(achieveDesc);
                    innerMap.put("achieveName", periodId + "期:");
                    innerMap.put("achieveDesc", desc);
                    innerMap.put("ifHighLight", 0);
                }
                periodAchieve.put(periodId, innerMap);
            }
            //转化为List<AchievementVo>
            for (Map<String, Object> periodMap : periodAchieve.values()) {
                StringBuffer desc = new StringBuffer();
                Set<String> descSet = (Set<String>) periodMap.get("achieveDesc");
                for (String descTmp : descSet) {
                    desc.append(descTmp).append(CommonConstant.SPACE_SPLIT_STR + CommonConstant.SPACE_SPLIT_STR);
                }
                AchievementVo achievementVo = new AchievementVo(periodMap.get("achieveName").toString(), desc
                        .toString().trim(), Integer.valueOf(periodMap.get("ifHighLight").toString()));
                encircleRecentAchieves.add(achievementVo);
            }
            //转化encircleAchievements
            for (UserSocialRecord userAcheve : userRecentAchieves) {
                Integer ifHighLight = 0;
                if (userAcheve.getTotalCount() > 0) {
                    ifHighLight = 1;
                }
                AchievementVo achievementVo = new AchievementVo(getAchieveName(userAcheve.getRecordType()), userAcheve
                        .getTotalCount() + "次", ifHighLight);
                encircleAchievements.add(achievementVo);
            }
            //成就初始化
            if (encircleAchievements.size() == 0) {
                for (SocialAchievement acNull : getSocialAchievement()) {
                    AchievementVo achievementVo = new AchievementVo(acNull.getAchievementName(), 0 + "次", 0);
                    encircleAchievements.add(achievementVo);
                }
            }

            encircleRecentAchieves = encircleRecentAchieves.stream().sorted(Comparator.comparing
                    (AchievementVo::getAchieveName).reversed()).collect(Collectors.toList());

            result.put("encircleRecentAchieves", encircleRecentAchieves);
            result.put("encircleAchievements", encircleAchievements);
            return result;
        }
    }, DLT_KILL_NUM_RED_NEW(SocialEncircleKillConstant.SOCIAL_OPERATE_NUM_KILL_RED, GameConstant.DLT, 3,
            SocialEncircleKillConstant.SOCIAL_KILL_NUM_MAX_COUNT) {
        @Override
        String getAchieveName(Integer recordType) {
            return DltKillNumAchievementEnumNew.getNameByType(recordType);
        }

        @Override
        public SocialAchievement[] getSocialAchievement() {
            return DltKillNumAchievementEnumNew.values();
        }

        @Override
        public Map<String, List<AchievementVo>> makeAchievementVo(List<Map> userRecentSocial, List<UserSocialRecord>
                userRecentAchieves) {
            Map<String, List<AchievementVo>> result = new HashMap<>();
            List<AchievementVo> killAchievements = new ArrayList<>();
            //转化encircleAchievements
            for (UserSocialRecord userAcheve : userRecentAchieves) {
                Integer ifHighLight = 0;
                if (userAcheve.getTotalCount() > 0) {
                    ifHighLight = 1;
                }
                for (SocialAchievement acNull : getSocialAchievement()) {
                    if (acNull.getAchievementType().equals(userAcheve.getRecordType())) {
                        AchievementVo achievementVo = new AchievementVo(getAchieveName(userAcheve.getRecordType()),
                                userAcheve.getTotalCount() + "次", ifHighLight);
                        if (achievementVo != null) {
                            killAchievements.add(achievementVo);
                        }
                    }
                }
            }
            //成就初始化
            if (killAchievements.size() == 0) {
                for (SocialAchievement acNull : getSocialAchievement()) {
                    AchievementVo achievementVo = new AchievementVo(acNull.getAchievementName(), 0 + "次", 0);
                    killAchievements.add(achievementVo);
                }
            }
            result.put("killAchievementsNew", killAchievements);
            return result;
        }
    };

    private Integer socialType;
    private String gameEn;
    private Integer recentAchieveCount;
    private Integer maxSocialTimes;

    public Integer getSocialType() {
        return this.socialType;
    }

    public String getGameEn() {
        return this.gameEn;
    }

    public Integer getRecentAchieveCount() {
        return this.recentAchieveCount;
    }

    public Integer getMaxSocialTimes() {
        return this.maxSocialTimes;
    }

    DltAchievementEnum(Integer socialType, String gameEn, Integer recentAchieveCount, Integer maxSocialTimes) {
        this.socialType = socialType;
        this.gameEn = gameEn;
        this.recentAchieveCount = recentAchieveCount;
        this.maxSocialTimes = maxSocialTimes;
    }

    public static DltAchievementEnum getAchievementEnumBySocialType(Integer socialType) {
        for (DltAchievementEnum ac : DltAchievementEnum.values()) {
            if (ac.getSocialType() == socialType) {
                return ac;
            }
        }
        return null;
    }

    abstract String getAchieveName(Integer recordType);

    abstract public SocialAchievement[] getSocialAchievement();

    abstract public Map<String, List<AchievementVo>> makeAchievementVo(List<Map> userRecentSocial,
                                                                       List<UserSocialRecord> userRecentAchieves);
}
