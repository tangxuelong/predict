package com.mojieai.predict.enums.achievement;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.GameConstant;
import com.mojieai.predict.constant.SocialEncircleKillConstant;
import com.mojieai.predict.entity.po.UserSocialRecord;
import com.mojieai.predict.entity.vo.AchievementVo;

import java.util.*;
import java.util.stream.Collectors;

public enum SsqAchievementEnum {
    SSQ_ENCIRCLE_RED(SocialEncircleKillConstant.SOCIAL_OPERATE_NUM_ENCIRCLE_RED, GameConstant.SSQ, 5,
            SocialEncircleKillConstant.SOCIAL_ENCIRCLE_MAX_COUNT) {
        @Override
        String getAchieveName(Integer recordType) {
            return SsqEncircleAchievementEnum.getNameByType(recordType);
        }

        @Override
        public SocialAchievement[] getSocialAchievement() {
            return SsqEncircleAchievementEnum.values();
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
                Integer socialRightCount = 0;
                if (userSocial.get("SOCIAL_RIGHT_COUNT") != null) {
                    socialRightCount = Integer.valueOf(userSocial.get("SOCIAL_RIGHT_COUNT").toString());
                }
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
    }, SSQ_KILL_NUM_RED(SocialEncircleKillConstant.SOCIAL_OPERATE_NUM_KILL_RED, GameConstant.SSQ, 3,
            SocialEncircleKillConstant.SOCIAL_KILL_NUM_MAX_COUNT) {
        @Override
        String getAchieveName(Integer recordType) {
            return SsqKillNumAchievementEnum.getNameByType(recordType);
        }

        @Override
        public SocialAchievement[] getSocialAchievement() {
            return SsqKillNumAchievementEnum.values();
        }

        @Override
        public Map<String, List<AchievementVo>> makeAchievementVo(List<Map> userRecentSocial, List<UserSocialRecord>
                userRecentAchieves) {
            Set<String> periodIds = new HashSet<>();
            Map<String, List<AchievementVo>> result = new HashMap<>();
            List<AchievementVo> killAchievements = new ArrayList<>();
            List<AchievementVo> killRecentAchieves = new ArrayList<>();

            Map<Integer, Map<String, Object>> killTypeMap = new HashMap<>();
            for (Map userSocial : userRecentSocial) {//PERIOD_ID, SOCIAL_CODE_TYPE, SOCIAL_COUNT, SOCIAL_RIGHT_COUNT
                if (periodIds.size() > getRecentAchieveCount()) {
                    break;
                }
                String periodId = userSocial.get("PERIOD_ID").toString();
                if (Integer.valueOf(periodId) < Integer.valueOf(SocialEncircleKillConstant
                        .SOCIAL_ENCIRCLE_BEGIN_PERIODID_SSQ)) {
                    continue;
                }
                Map<String, Object> tempRecentSocial = null;
                Integer socialCount = Integer.valueOf(userSocial.get("SOCIAL_COUNT").toString());
                Integer socialRightCount = Integer.valueOf(userSocial.get("SOCIAL_RIGHT_COUNT").toString());
                if (killTypeMap.containsKey(socialCount)) {
                    tempRecentSocial = killTypeMap.get(socialCount);
                    Integer takpartTimes = Integer.valueOf(tempRecentSocial.get("takpartTimes").toString());
                    Integer rightTimes = Integer.valueOf(tempRecentSocial.get("rightTimes").toString());
                    takpartTimes++;
                    rightTimes = rightTimes + socialRightCount;
                    tempRecentSocial.put("takpartTimes", takpartTimes);
                    tempRecentSocial.put("rightTimes", rightTimes);
                } else {
                    tempRecentSocial = new HashMap<>();
                    tempRecentSocial.put("achieveName", "杀" + socialCount + "码:");
                    tempRecentSocial.put("ifHighLight", 0);
                    tempRecentSocial.put("takpartTimes", 1);
                    tempRecentSocial.put("rightTimes", socialRightCount);
                }
                killTypeMap.put(socialCount, tempRecentSocial);
                periodIds.add(periodId);
            }
            //转化为List<AchievementVo>
            for (Map<String, Object> periodMap : killTypeMap.values()) {
                String desc = periodMap.get("takpartTimes") + "次全中(" + periodMap.get("rightTimes") + ")次";
                AchievementVo achievementVo = new AchievementVo(periodMap.get("achieveName").toString(), desc.trim(),
                        0);

                killRecentAchieves.add(achievementVo);
            }
            //转化encircleAchievements
            for (UserSocialRecord userAcheve : userRecentAchieves) {
                Integer ifHighLight = 0;
                if (userAcheve.getMaxContinueTimes() > 0) {
                    ifHighLight = 1;
                }
                for (SocialAchievement acNull : getSocialAchievement()) {
                    if (acNull.getAchievementType().equals(userAcheve.getRecordType())) {
                        AchievementVo achievementVo = new AchievementVo(getAchieveName(userAcheve.getRecordType()),
                                userAcheve.getMaxContinueTimes() + "连中", ifHighLight);
                        if (achievementVo != null) {
                            killAchievements.add(achievementVo);
                        }
                    }
                }
            }
            //成就初始化
            if (killAchievements.size() == 0) {
                for (SocialAchievement acNull : getSocialAchievement()) {
                    AchievementVo achievementVo = new AchievementVo(acNull.getAchievementName(), 0 + "连中", 0);
                    killAchievements.add(achievementVo);
                }
            }

            result.put("killRecentAchieves", killRecentAchieves);
            result.put("killAchievements", killAchievements);
            return result;
        }
    }, SSQ_KILL_NUM_RED_NEW(SocialEncircleKillConstant.SOCIAL_OPERATE_NUM_KILL_RED, GameConstant.SSQ, 3,
            SocialEncircleKillConstant.SOCIAL_KILL_NUM_MAX_COUNT) {
        @Override
        String getAchieveName(Integer recordType) {
            return SsqKillNumAchievementEnumNew.getNameByType(recordType);
        }

        @Override
        public SocialAchievement[] getSocialAchievement() {
            return SsqKillNumAchievementEnumNew.values();
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

    SsqAchievementEnum(Integer socialType, String gameEn, Integer recentAchieveCount, Integer maxSocialTimes) {
        this.socialType = socialType;
        this.gameEn = gameEn;
        this.recentAchieveCount = recentAchieveCount;
        this.maxSocialTimes = maxSocialTimes;
    }

    public static SsqAchievementEnum getAchievementEnumBySocialType(Integer socialType) {
        for (SsqAchievementEnum ac : SsqAchievementEnum.values()) {
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
