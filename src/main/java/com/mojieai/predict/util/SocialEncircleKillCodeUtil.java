package com.mojieai.predict.util;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.entity.po.*;
import com.mojieai.predict.entity.vo.EncircleVo;
import com.mojieai.predict.entity.vo.UserLoginVo;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

public class SocialEncircleKillCodeUtil {
    private static Logger log = LogConstant.commonLog;

    public static String[] removeStrOfNumArr(String[] strArr) {
        Set<String> res = new HashSet<>();
        for (String str : strArr) {
            if (CommonUtil.isNumeric(str)) {
                res.add(str);
            }
        }
        Object[] objectArr = res.toArray();
        String[] resArr = res.toArray(new String[objectArr.length]);
        return resArr;
    }

    public static List<EncircleVo> getEncircleRule(long gameId, Integer encircleType) {
        List<EncircleVo> result = new ArrayList<>();
        try {
            String encircleRule = ActivityIniCache.getActivityIniValue(ActivityIniConstant.SOCIAL_ENCIRCLE_RULE +
                    gameId + CommonConstant.COMMON_COLON_STR + encircleType);
            List<HashMap> tempRes = JSONObject.parseObject(encircleRule, ArrayList.class);
            for (Map temp : tempRes) {
                EncircleVo encircleVo = new EncircleVo();
                encircleVo.setEncircleCount(Integer.valueOf(temp.get("encircleCount").toString()));
                encircleVo.setEncircleCountName(temp.get("encircleCountName").toString());
                List<Integer> killNumCountList = (List<Integer>) temp.get("killNumCounts");
                Integer[] killNumCounts = new Integer[killNumCountList.size()];
                for (int i = 0; i < killNumCountList.size(); i++) {
                    killNumCounts[i] = (killNumCountList.get(i));
                }
                encircleVo.setKillNumCounts(killNumCounts);
                result.add(encircleVo);
            }
        } catch (Exception e) {
            log.error("getEncircleRule????????????????????????", e);
        }
        return result;
    }

    /* ???????????????????????? ???????????????*/
    public static List<Map> getEncircleCount(long gameId, Integer encircleType) {
        String encircleRule = ActivityIniCache.getActivityIniValue(ActivityIniConstant.SOCIAL_ENCIRCLE_RULE +
                gameId + CommonConstant.COMMON_COLON_STR + encircleType);
        if (StringUtils.isBlank(encircleRule)) {
            return null;
        }
        List<Map> result = new ArrayList<>();
        List<Integer> res = new ArrayList<>();
        List<HashMap> tempRes = JSONObject.parseObject(encircleRule, ArrayList.class);
        for (Map temp : tempRes) {
            if (temp.get("encircleCount") != null) {
                res.add(Integer.valueOf(temp.get("encircleCount").toString()));
            }
        }
        res = res.stream().sorted().collect(Collectors.toList());
        for (Integer num : res) {
            Map temp = new HashMap();
            temp.put("encircleNum", num);
            temp.put("encircleNumName", "???" + num + "???");
            result.add(temp);
        }
        return result;
    }

    public static Integer getMaxKillNumLevel(String killNumsRule) {
        Integer result = 0;
        if (StringUtils.isNotBlank(killNumsRule)) {
            String[] killNums = killNumsRule.split(CommonConstant.COMMA_SPLIT_STR);
            Arrays.sort(killNums);
            result = Integer.valueOf(killNums[killNums.length - 1]);
        }
        return result;
    }

    public static Integer getEncircleAwardLevelByRightCount(Integer encircleCount, Integer rightCount, Map<String,
            Integer> socialAwardLevel) {
        Integer result = 0;
        String awardKey = encircleCount + CommonConstant.COMMON_COLON_STR + rightCount;
        if (socialAwardLevel.containsKey(awardKey)) {
            result = socialAwardLevel.get(awardKey);
        }
        return result;
    }

    /**
     * 3.2??????????????????
     *
     * @return
     */
    public static Integer getScoreByRightCount(Integer socialCount, Integer rightCount, Map<String, Integer>
            socialAwardLevel) {
        Integer result = 0;
        String awardKey = socialCount + CommonConstant.COMMON_COLON_STR + rightCount;
        if (socialAwardLevel.containsKey(awardKey)) {
            result = socialAwardLevel.get(awardKey);
        }
        return result;
    }

    /**
     * 3.1???????????????????????????
     *
     * @param killCount
     * @param rank
     * @param socialAwardLevel
     * @return
     */
    public static Integer getKillNumAwardLevelByRank(Integer killCount, Integer rank, Map<String, Integer>
            socialAwardLevel) {
        Integer result = 0;
        String awardKey = killCount + CommonConstant.COMMON_COLON_STR + rank;
        if (!socialAwardLevel.containsKey(awardKey)) {
            awardKey = killCount + CommonConstant.COMMON_COLON_STR + 0;
        }
        result = socialAwardLevel.get(awardKey) == null ? 0 : socialAwardLevel.get(awardKey);
        return result;
    }

    public static String getEncircleTimeShow(String encircleTime) {
        String result = "";
        if (StringUtils.isBlank(encircleTime)) {
            return result;
        }

        boolean res = DateUtil.ifIsToday(DateUtil.formatString(encircleTime, 7));
        boolean todayYear = ifIsCurrYear(encircleTime);
        String[] timeArr = encircleTime.split(CommonConstant.SPACE_SPLIT_STR);
        String[] dateArr = timeArr[0].split(CommonConstant.COMMON_DASH_STR);
        String[] time = timeArr[1].split(CommonConstant.COMMON_COLON_STR);
        if (res) {
            result = "?????? " + time[0] + CommonConstant.COMMON_COLON_STR + time[1];
        } else if (todayYear) {
            result = dateArr[1] + "-" + dateArr[2] + CommonConstant.SPACE_SPLIT_STR + time[0] + CommonConstant
                    .COMMON_COLON_STR + time[1];
        } else {
            result = DateUtil.formatTime(DateUtil.formatString(encircleTime, 7), "yyyy-MM-dd HH:mm");
        }
        return result;
    }

    //??????????????????????????????????????????list
    public static Map<String, List<IndexUserSocialCode>> splitUserSocialDataByType(List<IndexUserSocialCode>
                                                                                           indexUserSocialCodes) {
        Map<String, List<IndexUserSocialCode>> result = new HashMap<>();
        for (IndexUserSocialCode temp : indexUserSocialCodes) {
            String key = getUserSocialIndexMapName(temp.getGameId(), temp.getSocialCodeType(), temp.getSocialCount());
            if (result.containsKey(key)) {
                result.get(key).add(temp);
            } else {
                List<IndexUserSocialCode> tempList = new ArrayList<>();
                tempList.add(temp);
                result.put(key, tempList);
            }
        }
        return result;
    }

    public static String getUserSocialIndexMapName(Long gameId, Integer socialType, Integer socialCount) {
        return new StringBuffer(SocialEncircleKillConstant.INDEX_SOCIAL_SPLIT_PREFIX).append(gameId).append
                (CommonConstant.COMMON_SPLIT_STR).append(socialType).append(CommonConstant.COMMON_SPLIT_STR).append
                (socialCount).toString();
    }

    public static void generateNewRecord(String openAwardPeriodId, List<IndexUserSocialCode> userAllSocial,
                                         UserSocialRecord lastUserSocialRecord, Integer rightCount) {
        lastUserSocialRecord.setRecordId(null);
        //?????????????????????????????????????????????????????????????????????
        if (userAllSocial == null || userAllSocial.size() <= 0) {
            lastUserSocialRecord.setPeriodId(openAwardPeriodId);
            return;
        }
        Integer total = lastUserSocialRecord.getTotalCount() == null ? 0 : lastUserSocialRecord.getTotalCount();
        Integer oldTotal = total;
        Integer maxContinue = lastUserSocialRecord.getMaxContinueTimes() == null ? 0 : lastUserSocialRecord
                .getMaxContinueTimes();
        Integer currentContinue = lastUserSocialRecord.getCurrentContinueTimes() == null ? 0 : lastUserSocialRecord
                .getCurrentContinueTimes();
        for (IndexUserSocialCode indexUserSocialCode : userAllSocial) {
            if (indexUserSocialCode.getSocialRightCount() == rightCount) {
                total++;
                currentContinue++;
            }
        }
        //?????????????????????
        if (oldTotal == total) {
            currentContinue = 0;
        }
        if (currentContinue > maxContinue) {
            maxContinue = currentContinue;
        }
        lastUserSocialRecord.setTotalCount(total);
        lastUserSocialRecord.setMaxContinueTimes(maxContinue);
        lastUserSocialRecord.setCurrentContinueTimes(currentContinue);
        lastUserSocialRecord.setPeriodId(openAwardPeriodId);
    }

    public static Map<String, Object> packageMyKillNumList(long gameId, List<SocialKillCode>
            samePeriodSocialKillCode, Integer periodStatus, Long lookUpUserId, RedisService redisService, Map<String,
            Integer> killNumAwardLevel, boolean isMe, String versionCode, boolean lookUpUserIsVip) {
        Map<String, Object> result = new HashMap<>();
        if (samePeriodSocialKillCode == null || samePeriodSocialKillCode.size() <= 0) {
            return null;
        }
        Integer version = Integer.valueOf(versionCode);

        List<Map<String, Object>> killNums = new ArrayList<>();
        Map<String, Object> tempMap = null;
        for (SocialKillCode socialKillCode : samePeriodSocialKillCode) {
            tempMap = new HashMap<>();

            //???????????????????????????
            boolean hasTake = false;
            String preKillNumDesc = "";
            if (periodStatus == SocialEncircleKillConstant.SOCIAL_ENCIRCLE_STATUS_ENABLE) {
                String concurrentUserKillLockKey = RedisConstant.getConcurrentUserKillLockKey(gameId, socialKillCode
                        .getEncircleCodeId(), lookUpUserId);
                if (redisService.isKeyExist(concurrentUserKillLockKey)) {
                    hasTake = true;
                }
                if (!hasTake && !isMe && version <= CommonConstant.VERSION_CODE_2_3) {
                    preKillNumDesc = "???";
                }
            }
            String killNumFront = preKillNumDesc + "???" + socialKillCode.getKillNums() + "???";
            String title = preKillNumDesc + "???" + socialKillCode.getKillNums() + "???";
            String title2 = "";
            String killNumBack = "";
            Integer codeType = SocialEncircleKillConstant.SOCIAL_PERSON_KILL_LIST_TEXT_TYPE;
            if (periodStatus.equals(SocialEncircleKillConstant.SOCIAL_ENCIRCLE_STATUS_ENABLE)) {
                if (hasTake || isMe || lookUpUserIsVip) {
                    if (version <= CommonConstant.VERSION_CODE_2_3) {
                        killNumFront = killNumFront + "???";
                    }
                    killNumBack = socialKillCode.getUserKillCode();
                    codeType = SocialEncircleKillConstant.SOCIAL_PERSON_KILL_LIST_CODE_TYPE;
                } else {
                    String tempStr = "(?????????????????????)";
                    title2 = "(?????????????????????)";
                    killNumBack = getKillNumSymbol(socialKillCode.getKillNums());
                    if (version <= CommonConstant.VERSION_CODE_2_3) {
                        tempStr = "??????????????????";
                        killNumBack = "";
                    }
                    if (version <= CommonConstant.VERSION_CODE_3_2) {
                        killNumFront = killNumFront + tempStr;
                    }
                }
            } else {
                if (version <= CommonConstant.VERSION_CODE_2_3) {
                    killNumFront = killNumFront + "???";
                }
                killNumBack = socialKillCode.getUserKillCode();
                codeType = SocialEncircleKillConstant.SOCIAL_PERSON_KILL_LIST_CODE_TYPE;
            }
            //????????????????????????
            String killNumAwardAdMsg = "????????????:" + getScoreByRightCount(socialKillCode.getKillNums(),
                    socialKillCode.getKillNums(), killNumAwardLevel) + "";
            String killNumAwardAdMsgNew = "????????????:" + getScoreByRightCount(socialKillCode.getKillNums(),
                    socialKillCode.getKillNums(), killNumAwardLevel) + "";
            //??????????????????
            int killNumAwardStatus = 0;
            String killNumBtnMsg = "?????????";
            String killNumBtnMsgNew = "?????????";
            if (socialKillCode.getIsDistribute() != null && socialKillCode.getIsDistribute() == 1) {
                if (socialKillCode.getRightNums() < socialKillCode.getKillNums()) {
                    killNumAwardStatus = 2;
                    killNumBtnMsg = "??????";
                    killNumBtnMsgNew = "<font color=\"#FF5050\">???" + socialKillCode.getKillNums() + "???" +
                            socialKillCode.getRightNums() + "</font>";
                } else {
                    killNumAwardStatus = 1;
                    killNumBtnMsg = "??????";
                    killNumBtnMsgNew = "<font color=\"#FF5050\">???" + socialKillCode.getKillNums() + "???" +
                            socialKillCode.getRightNums() + "</font>";
                }
                String star = "";
                String end = "";
                if (socialKillCode.getUserAwardScore() > 0) {
                    star = CommonConstant.COMMON_BRACKET_LEFT;
                    end = CommonConstant.COMMON_BRACKET_RIGHT;
                }
                killNumAwardAdMsg = "??????:" + star + socialKillCode.getUserAwardScore() + end + "??????";
                killNumAwardAdMsgNew = "??????:<font color=\"#FF5050\">" + socialKillCode.getUserAwardScore() + "</font>??????";
            }
            if (StringUtils.isBlank(killNumBack)) {
                killNumBack = " ";
            }
            tempMap.put("numType", codeType);
            tempMap.put("title", title);
            tempMap.put("title2", title2);
            tempMap.put("killNumFront", killNumFront);
            tempMap.put("killNumBack", killNumBack);
            tempMap.put("killNumAwardAdMsg", killNumAwardAdMsg);
            tempMap.put("killNumAwardAdMsgNew", killNumAwardAdMsgNew);
            tempMap.put("killNumAwardStatus", killNumAwardStatus);
            tempMap.put("killNumBtnMsg", killNumBtnMsg);
            tempMap.put("killNumBtnMsgNew", killNumBtnMsgNew);
            tempMap.put("encircleCodeId", socialKillCode.getEncircleCodeId());
            tempMap.put("socialTime", getEncircleTimeShow(DateUtil.formatTime(socialKillCode.getCreateTime())));
            tempMap.put("socialTimeSort", socialKillCode.getCreateTime());
            tempMap.put("periodId", socialKillCode.getPeriodId());
            killNums.add(tempMap);
        }

        result.put("killNumDetails", killNums);
        result.put("periodId", samePeriodSocialKillCode.get(0).getPeriodId());
        result.put("periodName", GameConstant.PERIOD_NAME_MAP.get(GameCache.getGame(gameId).getGameEn()) +
                samePeriodSocialKillCode.get(0).getPeriodId() + "???");
        return result;
    }

    private static String getKillNumSymbol(Integer killNums) {
        if (killNums <= 0) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < killNums; i++) {
            sb.append(CommonConstant.COMMON_STAR_STR + CommonConstant.COMMON_QUESTION_STR);
            if (i < killNums - 1) {
                sb.append(CommonConstant.COMMA_SPLIT_STR);
            }
        }
        return sb.toString();
    }

    public static Map<String, Object> packageMyEncircleList(long gameId, List<SocialEncircle>
            samePeriodSocialEncircles) {
        Map<String, Object> result = new HashMap<>();
        if (samePeriodSocialEncircles == null || samePeriodSocialEncircles.size() <= 0) {
            return null;
        }

        List<Map<String, Object>> encircles = new ArrayList<>();
        Map<String, Object> tempMap = null;
        for (SocialEncircle socialEncircle : samePeriodSocialEncircles) {
            tempMap = new HashMap<>();
            String encircleAwardAdMsg = "";
            String encircleNumBtnMsg = "?????????";
            String encircleAwardHtmlMsg = "";

            if (socialEncircle.getIsDistribute() != null && socialEncircle.getIsDistribute() == 1 && socialEncircle
                    .getUserAwardScore() != null) {
                encircleAwardAdMsg = "(??????" + socialEncircle.getRightNums() + "??????" + socialEncircle.getUserAwardScore()
                        + "??????)";
                encircleNumBtnMsg = "<font color=\"#FF5050\">???" + socialEncircle.getEncircleNums() + "???" +
                        socialEncircle.getRightNums() + "</font>";
                encircleAwardHtmlMsg = "??????:<font color=\"#FF5050\">" + socialEncircle.getUserAwardScore() + "</font>??????";
            }
            int partakeCount = socialEncircle.getFollowKillNums() == null ? 0 : socialEncircle.getFollowKillNums();
            tempMap.put("encircleAwardAdMsg", encircleAwardAdMsg);
            tempMap.put("encircleNum", socialEncircle.getUserEncircleCode());
            tempMap.put("partakeCount", partakeCount);
            tempMap.put("encircleCodeId", socialEncircle.getEncircleCodeId());
            tempMap.put("encircleName", "???" + socialEncircle.getEncircleNums() + "???");
            tempMap.put("socialTime", getEncircleTimeShow(DateUtil.formatTime(socialEncircle.getCreateTime())));
            tempMap.put("socialTimeSort", socialEncircle.getCreateTime());
            tempMap.put("periodId", socialEncircle.getPeriodId());
            tempMap.put("encircleNumBtnMsg", encircleNumBtnMsg);
            tempMap.put("encircleAwardHtmlMsg", encircleAwardHtmlMsg);
            encircles.add(tempMap);
        }

        result.put("encircleDetails", encircles);
        result.put("periodId", samePeriodSocialEncircles.get(0).getPeriodId());
        result.put("periodName", GameConstant.PERIOD_NAME_MAP.get(GameCache.getGame(gameId).getGameEn()) +
                samePeriodSocialEncircles.get(0).getPeriodId() + "???");
        return result;
    }

    public static SocialClassicEncircle convertSocialEncircle2ClassicPo(SocialEncircle socialEncircle) {
        SocialClassicEncircle socialClassicEncircle = new SocialClassicEncircle();
        socialClassicEncircle.setEncircleCodeId(socialEncircle.getEncircleCodeId());
        socialClassicEncircle.setCodeType(socialEncircle.getCodeType());
        socialClassicEncircle.setEncircleNums(socialEncircle.getEncircleNums());
        socialClassicEncircle.setFollowKillNums(socialEncircle.getFollowKillNums());
        socialClassicEncircle.setGameId(socialEncircle.getGameId());
        socialClassicEncircle.setKillNums(socialEncircle.getKillNums());
        socialClassicEncircle.setPeriodId(socialEncircle.getPeriodId());
        socialClassicEncircle.setRightNums(socialEncircle.getRightNums());
        socialClassicEncircle.setUserAwardScore(socialEncircle.getUserAwardScore());
        socialClassicEncircle.setUserEncircleCode(socialEncircle.getUserEncircleCode());
        socialClassicEncircle.setUserId(socialEncircle.getUserId());
        socialClassicEncircle.setCreateTime(socialEncircle.getCreateTime());
        return socialClassicEncircle;
    }

    public static boolean ifIsCurrYear(String time) {
        String year = getCurrYear();
        if (time.contains(year)) {
            return true;
        }
        return false;
    }

    public static String getCurrYear() {
        Calendar date = Calendar.getInstance();
        return String.valueOf(date.get(Calendar.YEAR));
    }

    //????????????????????????????????????????????????????????????
    public static boolean getSocialIsEnd(long gameId) {
        boolean res = false;

        Timestamp currentTime = DateUtil.getCurrentTimestamp();
        Timestamp awardTime = PeriodRedis.getAwardCurrentPeriod(gameId).getAwardTime();
        String currentEndTimeStr = DateUtil.formatTime(awardTime, "yyyy-MM-dd") + " 18:00:00";
        Timestamp currentEndTime = DateUtil.formatToTimestamp(currentEndTimeStr, "yyyy-MM-dd HH:mm:ss");

        if (DateUtil.compareDate(currentEndTime, currentTime)) {
            res = true;
        }
        return res;
    }

    //?????????????????????????????????????????????????????????
    public static Long getCountDownSecound(long gameId, boolean isEnd) {
        Long res = 0L;
        Timestamp currentTime = DateUtil.getCurrentTimestamp();
        if (isEnd) {
            res = getAwardTimesTorrow6(currentTime, gameId);
            return res;
        }
        Timestamp statisticTime = getOneTimeStatisticTime(gameId, null, currentTime);
        if (statisticTime == null) {
            return res;
        }
        res = DateUtil.getDiffSeconds(currentTime, statisticTime);
        return res;
    }

    private static Long getAwardTimesTorrow6(Timestamp currentTime, long gameId) {
        GamePeriod currentAwardPeriod = PeriodRedis.getAwardCurrentPeriod(gameId);
        String currStatisticEndTimeStr = DateUtil.formatTime(currentAwardPeriod.getAwardTime(), "yyyy-MM-dd") + " " +
                "06:00:00";
        Timestamp currStatisticEndTime = DateUtil.formatToTimestamp(currStatisticEndTimeStr, "yyyy-MM-dd HH:mm:ss");
        return DateUtil.getDiffSeconds(currentTime, currStatisticEndTime);
    }

    /* ??????????????????????????????????????? ???????????????null*/
    public static Timestamp getOneTimeStatisticTime(long gameId, String timeType, Timestamp dateTime) {
        if (timeType == null) {
            timeType = SocialEncircleKillConstant.SOCIAL_BIG_DATA_STATISTIC_CURRENT_TIME;
        }
        if (dateTime == null) {
            dateTime = DateUtil.getCurrentTimestamp();
        }

        GamePeriod currentAwardPeriod = PeriodRedis.getAwardCurrentPeriod(gameId);
        GamePeriod lastAwardPeriod = PeriodRedis.getLastPeriodByGameIdAndPeriodId(gameId, currentAwardPeriod
                .getPeriodId());
        Timestamp currentAwardTime = currentAwardPeriod.getAwardTime();
        Timestamp lastAwardTime = lastAwardPeriod.getAwardTime();
        Timestamp lastAwardTorrowTime = DateUtil.getIntervalDays(lastAwardTime, 1);


        String currStatisticEndTimeStr = DateUtil.formatTime(currentAwardTime, "yyyy-MM-dd") + " 18:00:00";
        String currStatisticBeginTimeStr = DateUtil.formatTime(lastAwardTorrowTime, "yyyy-MM-dd") + " 06:00:00";

        Timestamp currStatisticEndTime = DateUtil.formatToTimestamp(currStatisticEndTimeStr, "yyyy-MM-dd HH:mm:ss");
        Timestamp currStatisticBeginTime = DateUtil.formatToTimestamp(currStatisticBeginTimeStr, "yyyy-MM-dd " +
                "HH:mm:ss");

        //1.??????dateTime??????????????????????????????null
        if (DateUtil.compareDate(currStatisticEndTime, dateTime)) {
            if (timeType.equals(SocialEncircleKillConstant.SOCIAL_BIG_DATA_STATISTIC_BEFOR_TIME)) {
                return currStatisticEndTime;
            }
            return null;
        }
        //2.??????????????????????????????????????????
        if (DateUtil.compareDate(dateTime, currStatisticBeginTime)) {
            if (timeType.equals(SocialEncircleKillConstant.SOCIAL_BIG_DATA_STATISTIC_BEFOR_TIME)) {
                return null;
            }
            return currStatisticBeginTime;
        }
        //3.
        Timestamp dateTimeTorrow = DateUtil.getIntervalDays(dateTime, 1);
        Timestamp dateTimeYesterDay = DateUtil.getIntervalDays(dateTime, -1);

        String statisticYesTimeStr0 = DateUtil.formatTime(dateTimeYesterDay, "yyyy-MM-dd") + " 00:00:00";
        String statisticTimeStr6 = DateUtil.formatTime(dateTime, "yyyy-MM-dd") + " 06:00:00";
        String statisticTimeStr12 = DateUtil.formatTime(dateTime, "yyyy-MM-dd") + " 12:00:00";
        String statisticTimeStr18 = DateUtil.formatTime(dateTime, "yyyy-MM-dd") + " 18:00:00";
        String statisticTimeStr0 = DateUtil.formatTime(dateTimeTorrow, "yyyy-MM-dd") + " 00:00:00";

        Timestamp statisticYesTime0 = DateUtil.formatToTimestamp(statisticYesTimeStr0, "yyyy-MM-dd HH:mm:ss");
        Timestamp statisticTime6 = DateUtil.formatToTimestamp(statisticTimeStr6, "yyyy-MM-dd HH:mm:ss");
        Timestamp statisticTime12 = DateUtil.formatToTimestamp(statisticTimeStr12, "yyyy-MM-dd HH:mm:ss");
        Timestamp statisticTime18 = DateUtil.formatToTimestamp(statisticTimeStr18, "yyyy-MM-dd HH:mm:ss");
        Timestamp statisticTime0 = DateUtil.formatToTimestamp(statisticTimeStr0, "yyyy-MM-dd HH:mm:ss");

        Timestamp beforeStatisticTime = null;
        Timestamp currentStatisticTime = null;
        if (DateUtil.compareDate(dateTime, statisticTime6)) {
            beforeStatisticTime = statisticYesTime0;
            currentStatisticTime = statisticTime6;
        } else if (DateUtil.compareDate(dateTime, statisticTime12)) {
            beforeStatisticTime = statisticTime6;
            currentStatisticTime = statisticTime12;
        } else if (DateUtil.compareDate(dateTime, statisticTime18)) {
            beforeStatisticTime = statisticTime12;
            currentStatisticTime = statisticTime18;
        } else {
            beforeStatisticTime = statisticTime18;
            currentStatisticTime = statisticTime0;
        }

        if (timeType.equals(SocialEncircleKillConstant.SOCIAL_BIG_DATA_STATISTIC_BEFOR_TIME)) {
            return beforeStatisticTime;
        }
        return currentStatisticTime;
    }

    public static Timestamp getStatisticTime(long gameId, String timeType, Timestamp dateTime) {

        Timestamp currentTime = DateUtil.getCurrentTimestamp();
        if (dateTime != null) {
            currentTime = dateTime;
        }

        Timestamp awardTime = PeriodRedis.getAwardCurrentPeriod(gameId).getAwardTime();
        Timestamp awardTrrowTime = DateUtil.getIntervalDays(awardTime, 1);

        String statisticTimeStr0 = DateUtil.formatTime(awardTime, "yyyy-MM-dd") + " 00:00:00";
        String statisticTimeStr6 = DateUtil.formatTime(awardTime, "yyyy-MM-dd") + " 06:00:00";
        String statisticTimeStr12 = DateUtil.formatTime(awardTime, "yyyy-MM-dd") + " 12:00:00";
        String statisticTimeStr18 = DateUtil.formatTime(awardTime, "yyyy-MM-dd") + " 18:00:00";
        String statisticTorrowTimeStr0 = DateUtil.formatTime(awardTrrowTime, "yyyy-MM-dd") + " 00:00:00";
        String statisticTorrowTimeStr6 = DateUtil.formatTime(awardTrrowTime, "yyyy-MM-dd") + " 06:00:00";

        Timestamp statisticBeforeTime = null;
        Timestamp statisticTime = null;
        Timestamp statisticAfterTime = null;

        Timestamp statisticTime0 = DateUtil.formatToTimestamp(statisticTimeStr0, "yyyy-MM-dd HH:mm:ss");
        Timestamp statisticTime6 = DateUtil.formatToTimestamp(statisticTimeStr6, "yyyy-MM-dd HH:mm:ss");
        Timestamp statisticTime12 = DateUtil.formatToTimestamp(statisticTimeStr12, "yyyy-MM-dd HH:mm:ss");
        Timestamp statisticTime18 = DateUtil.formatToTimestamp(statisticTimeStr18, "yyyy-MM-dd HH:mm:ss");
        Timestamp statisticTorrowTime0 = DateUtil.formatToTimestamp(statisticTorrowTimeStr0, "yyyy-MM-dd HH:mm:ss");
        Timestamp statisticTorrowTime6 = DateUtil.formatToTimestamp(statisticTorrowTimeStr6, "yyyy-MM-dd HH:mm:ss");

        if (DateUtil.compareDate(currentTime, statisticTime6)) {
            statisticBeforeTime = statisticTime0;
            statisticTime = statisticTime6;
            statisticAfterTime = statisticTime12;
        } else if (DateUtil.compareDate(currentTime, statisticTime12)) {
            statisticBeforeTime = statisticTime6;
            statisticTime = statisticTime12;
            statisticAfterTime = statisticTime18;
        } else if (DateUtil.compareDate(currentTime, statisticTime18)) {
            statisticBeforeTime = statisticTime12;
            statisticTime = statisticTime18;
            statisticAfterTime = statisticTorrowTime0;
        } else {
            statisticBeforeTime = statisticTime18;
            statisticTime = statisticTorrowTime0;
            statisticAfterTime = statisticTorrowTime6;
        }

        if (timeType.equals(SocialEncircleKillConstant.SOCIAL_BIG_DATA_STATISTIC_BEFOR_TIME)) {
            return statisticBeforeTime;
        } else if (timeType.equals(SocialEncircleKillConstant.SOCIAL_BIG_DATA_STATISTIC_CURRENT_TIME)) {
            return statisticTime;
        } else {
            return statisticAfterTime;
        }
    }

    public static String getHotStatisticCode(String socialData, int dataCount) {
        StringBuffer result = new StringBuffer();
        try {
            Map<String, Integer> codeMap = JSONObject.parseObject(socialData, HashMap.class);
            if (codeMap != null) {
                List<Map.Entry<String, Integer>> listMap = codeMap.entrySet().stream().sorted(Map.Entry.<String,
                        Integer>comparingByValue().reversed()).collect(Collectors.toList());
                int count = 0;
                if (codeMap.size() < dataCount) {
                    dataCount = codeMap.size();
                }
                for (Map.Entry<String, Integer> entry : listMap) {
                    if (count >= dataCount) {
                        break;
                    }
                    result.append(entry.getKey());
                    if (count < dataCount - 1) {
                        result.append(CommonConstant.COMMA_SPLIT_STR);
                    }
                    count++;
                }
            }
        } catch (Exception e) {
            return "";
        }
        return result.toString();
    }

    /* ????????????????????????????????????*/
    public static Integer getEncircleMinKillCount(String killNums) {
        String[] requireNum = killNums.split(CommonConstant.COMMA_SPLIT_STR);
        Integer min = 0;
        for (String num : requireNum) {
            if (Integer.valueOf(num) > min) {
                min = Integer.valueOf(num);
            }
        }
        return min;
    }

    public static boolean isNormalTime(Timestamp date) {
        String day = DateUtil.getDate(date, "yyyy-MM-dd");
        Timestamp begin = DateUtil.formatString(day + " 06:00:00", "yyyy-MM-dd HH:mm:ss");
        Timestamp end = DateUtil.formatString(day + " 23:00:00", "yyyy-MM-dd HH:mm:ss");
        if (DateUtil.compareDate(begin, date) && DateUtil.compareDate(date, end)) {
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        getHotStatisticCode("{\"03\":\"23\",\"01\":\"234\",\"06\":\"2\"}", 2);
    }

    public static Timestamp getPeriodLastStatisticTime(long gameId) {
        GamePeriod currentAwardPeriod = PeriodRedis.getAwardCurrentPeriod(gameId);
        Timestamp currentAwardTime = currentAwardPeriod.getAwardTime();
        String currStatisticEndTimeStr = DateUtil.formatTime(currentAwardTime, "yyyy-MM-dd") + " 18:00:00";
        return DateUtil.formatToTimestamp(currStatisticEndTimeStr, "yyyy-MM-dd HH:mm:ss");
    }

//    public static String getTaskTypeCn(Integer taskType) {
//        if (taskType.equals(SocialEncircleKillConstant.SOCIAL_TASK_AWARD_TYPE_ENCIRCLE)) {
//            return "??????";
//        } else if (taskType.equals(SocialEncircleKillConstant.SOCIAL_TASK_AWARD_TYPE_KILL)) {
//            return "??????";
//        }
//        return "";
//    }

    public static String getSocialResonanceBestNumColor(int numIndex) {
        if (numIndex < 2) {
            return "#FF5050";
        }
        if (numIndex < 4) {
            return "#FF813B";
        }
        if (numIndex < 6) {
            return "#FFBB3D";
        }
        return "";
    }

    /* ??????????????????????????????*/
    public static Map<String, Integer> getPredictNumsMap(String rankType) {
        String rankPredictNums = null;
        if (rankType.equals(CommonConstant.SOCIAL_RANK_TYPE_PERIOD)) {
            rankPredictNums = ActivityIniCache.getActivityIniValue(ActivityIniConstant
                    .SOCIAL_PERIOD_RANK_PREDICT_NUMS);
        }
        if (rankType.equals(CommonConstant.SOCIAL_RANK_TYPE_WEEK)) {
            rankPredictNums = ActivityIniCache.getActivityIniValue(ActivityIniConstant
                    .SOCIAL_WEEK_RANK_PREDICT_NUMS);
        }
        if (rankType.equals(CommonConstant.SOCIAL_RANK_TYPE_MONTH)) {
            rankPredictNums = ActivityIniCache.getActivityIniValue(ActivityIniConstant
                    .SOCIAL_MONTH_RANK_PREDICT_NUMS);
        }

        Map<String, Integer> predictNumsMap = (Map<String, Integer>) JSONObject.parse(rankPredictNums);
        return predictNumsMap;
    }

    public static Integer getOnePeriodUserRank(long gameId, String periodId, String rankType, String socialType,
                                               Long userId, RedisService redisService) {
        Integer userRank = null;
        String redisRankKey = getRedisRankKey(gameId, periodId, rankType, socialType);
        if (userId != null) {
            Long currentUserRankL = redisService.kryoZRank(redisRankKey, userId);
            if (null != currentUserRankL) {
                Double currentUserScore = redisService.kryoZScore(redisRankKey, userId);

                if (socialType.equals(CommonConstant.SOCIAL_CODE_TYPE_KILL)) {
                    List<Long> userList = redisService.kryoZRangeByScoreGet(redisRankKey, currentUserScore.longValue
                            (), currentUserScore.longValue(), Long.class);

                    if (null != userList && userList.size() > 1) {
                        List<Long> userIdsL = redisService.kryoZRange(redisRankKey, currentUserRankL - userList.size
                                (), currentUserRankL - 1, Long.class);
                        int count = 0;
                        for (int i = userIdsL.size() - 1; i >= 0; i--) {
                            Double sameScoreUser = redisService.kryoZScore(redisRankKey, userIdsL.get(i));
                            if (!sameScoreUser.equals(currentUserScore)) {
                                currentUserRankL = currentUserRankL - count;
                                break;
                            }
                            count++;
                        }
                    }
                }
                userRank = currentUserRankL.intValue() + 1;
            }
        }
        return userRank;
    }

    /* ?????????*/
    public static String getRedisRankKey(Long gameId, String periodId, String rankType, String socialType) {
        if (rankType.equals(CommonConstant.SOCIAL_RANK_TYPE_PERIOD)) {
            if (socialType.equals(CommonConstant.SOCIAL_CODE_TYPE_ENCIRCLE)) {
                return RedisConstant.getEncirclePeriodRank(gameId, periodId);
            }
            if (socialType.equals(CommonConstant.SOCIAL_CODE_TYPE_KILL)) {
                return RedisConstant.getKillPeriodRank(gameId, periodId);
            }
        }
        if (rankType.equals(CommonConstant.SOCIAL_RANK_TYPE_WEEK)) {
            GamePeriod period = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, periodId);
            if (socialType.equals(CommonConstant.SOCIAL_CODE_TYPE_ENCIRCLE)) {

                return RedisConstant.getEncircleWeekRank(gameId, CommonUtil.getWeekIdByDate(period.getAwardTime()));
            }
            if (socialType.equals(CommonConstant.SOCIAL_CODE_TYPE_KILL)) {
                return RedisConstant.getKillWeekRank(gameId, CommonUtil.getWeekIdByDate(period.getAwardTime()));
            }
        }
        if (rankType.equals(CommonConstant.SOCIAL_RANK_TYPE_MONTH)) {
            GamePeriod period = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, periodId);
            if (socialType.equals(CommonConstant.SOCIAL_CODE_TYPE_ENCIRCLE)) {
                return RedisConstant.getEncircleMonthRank(gameId, CommonUtil.getMonthIdByDate(period.getAwardTime()));
            }
            if (socialType.equals(CommonConstant.SOCIAL_CODE_TYPE_KILL)) {
                return RedisConstant.getKillMonthRank(gameId, CommonUtil.getMonthIdByDate(period.getAwardTime()));
            }
        }
        return null;
    }

    public static Integer getAwardTypeByAwardDate(int awardDate, Integer socialType) {
        if (socialType.equals(SocialEncircleKillConstant.SOCIAL_OPERATE_NUM_ENCIRCLE_RED)) {
            if (awardDate == UserTitleConstant.WEEK_RANK_TOP_20_AWARD) {
                return SocialEncircleKillConstant.ACHIEVE_POP_AWARD_TYPE_WEEK_ENCIRCLE;
            } else if (awardDate == UserTitleConstant.MONTH_RANK_TOP_10_AWARD) {
                return SocialEncircleKillConstant.ACHIEVE_POP_AWARD_TYPE_MONTH_ENCIRCLE;
            }
        } else if (socialType.equals(SocialEncircleKillConstant.SOCIAL_OPERATE_NUM_KILL_RED)) {
            if (awardDate == UserTitleConstant.WEEK_RANK_TOP_20_AWARD) {
                return SocialEncircleKillConstant.ACHIEVE_POP_AWARD_TYPE_WEEK_KILL;
            } else if (awardDate == UserTitleConstant.MONTH_RANK_TOP_10_AWARD) {
                return SocialEncircleKillConstant.ACHIEVE_POP_AWARD_TYPE_MONTH_KILL;
            }
        }
        return null;
    }

    public static Integer getUserSocialRank(Long userId, long gameId, String periodId, String rankType, String
            socialType, RedisService redisService) {
        Integer userRank = null;
        String rankKey = getRedisRankKey(gameId, periodId, rankType, socialType);
        if (rankType.equals(CommonConstant.SOCIAL_RANK_TYPE_PERIOD)) {
            userRank = getOnePeriodUserRank(gameId, periodId, rankType, socialType, userId, redisService);
        } else {
            Long rank = redisService.kryoZRank(rankKey, userId);
            userRank = rank == null ? null : rank.intValue();
        }
        return userRank;
    }

    public static String getSocialNameBySocialType(Integer socialType) {
        if (socialType.equals(SocialEncircleKillConstant.SOCIAL_OPERATE_NUM_ENCIRCLE_RED)) {
            return "??????";
        } else if (socialType.equals(SocialEncircleKillConstant.SOCIAL_OPERATE_NUM_KILL_RED)) {
            return "??????";
        }
        return "";
    }

    public static Map<String, Object> getBigDataInstruction() {
        Map<String, Object> result = new HashMap<>();
        List<Map> content = new ArrayList<>();
        result.put("title", "<font color=\"#333333\">???????????????:</font>");

        Map<String, Object> timeMap = new HashMap<>();
        timeMap.put("title", "<font color=\"#333333\">1?????????</font>");
        timeMap.put("content", "<font color=\"#999999\">??????????????????????????????????????????6:00??????????????????6???????????????????????????????????????????????????18:00?????????</font>");
        Map<String, Object> dataInfoMap = new HashMap<>();
        dataInfoMap.put("title", "<font color=\"#333333\">2???????????????</font>");
        dataInfoMap.put("content", "<font color=\"#999999\">??????????????????????????????????????????????????????????????????????????????VIP?????????</font>");
        Map<String, Object> voiceMap = new HashMap<>();
        voiceMap.put("title", "<font color=\"#333333\">3?????????</font>");
        voiceMap.put("content", "<font color=\"#999999\">????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????</font>");

        content.add(timeMap);
        content.add(dataInfoMap);
        content.add(voiceMap);
        result.put("content", content);
        return result;
    }
}
