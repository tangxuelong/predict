package com.mojieai.predict.service.impl;

import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.*;
import com.mojieai.predict.entity.bo.DetailMatchInfo;
import com.mojieai.predict.entity.po.*;
import com.mojieai.predict.entity.vo.ResultVo;
import com.mojieai.predict.enums.CommonStatusEnum;
import com.mojieai.predict.enums.FootballCalculateResultEnum;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.*;
import com.mojieai.predict.service.beanself.BeanSelfAware;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.SportsUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by tangxuelong on 2017/11/7.
 */
@Service
public class VipProgramServiceImpl implements VipProgramService, BeanSelfAware {

    @Autowired
    private ThirdHttpService thirdHttpService;
    @Autowired
    private VipProgramDao vipProgramDao;
    @Autowired
    private VipMemberService vipMemberService;
    @Autowired
    private UserVipProgramDateTimeDao userVipProgramDateTimeDao;
    @Autowired
    private UserVipProgramDateService userVipProgramDateService;
    @Autowired
    private UserVipProgramService userVipProgramService;
    @Autowired
    private MatchScheduleDao matchScheduleDao;
    @Autowired
    private RedisService redisService;

    private VipProgramService self;

    @Override
    public ResultVo productVipProgram(Integer awardNum, Integer recommendNum, String price, String programInfo) {
        ResultVo resultVo = new ResultVo(ResultConstant.SUCCESS, "");
        // 创建program
        VipProgram vipProgram = new VipProgram(awardNum, recommendNum, price, programInfo);

        Timestamp currentTimestamp = DateUtil.getCurrentTimestamp();
        Timestamp endTime = null;
        List<String> matchIds = new ArrayList<>();
        // programInfo 比赛ID:类型$选项&选项,比赛ID:类型$选项&选项
        for (String eachMatchRecommend : programInfo.split(CommonConstant.COMMA_SPLIT_STR)) {
            String matchId = eachMatchRecommend.split(CommonConstant.COMMON_COLON_STR)[0];
            List<DetailMatchInfo> detailMatchInfos = thirdHttpService.getMatchListByMatchIds(matchId);
            Timestamp matchTime = detailMatchInfos.get(0).getEndTime();
            if (DateUtil.compareDate(matchTime, currentTimestamp)) {
                resultVo.setCode(ResultConstant.ERROR);
                resultVo.setMsg("包含已开打的比赛。matchId:" + matchId);
                return resultVo;
            }
            if (endTime == null || DateUtil.compareDate(matchTime, endTime)) {
                endTime = matchTime;
            }
            matchIds.add(matchId);
        }
        vipProgram.setEndTime(endTime);
        try {
            if (vipProgramDao.insert(vipProgram) > 0) {
                for (String matchId : matchIds) {
                    matchScheduleDao.updateMatchStatus(Integer.valueOf(matchId), CommonConstant.LOTTERY_CODE_FOOTBALL, 0,
                            "IF_VIP_PROGRAM", "VIP_PROGRAM_TIME");
                }
            }
        } catch (DuplicateKeyException e) {
        }
        return resultVo;
    }

    @Override
    public Map<String, Object> getVipProgramList(Long userId) {
        // 会员专区返回信息 1.是不是会员
        Map<String, Object> resultMap = new HashMap<>();
        Boolean isVip = vipMemberService.checkUserIsVip(userId, VipMemberConstant.VIP_MEMBER_TYPE_SPORTS);
        resultMap.put("isVip", isVip);
        Integer privilegeStatus = userVipProgramDateService.getUserVipProgramPrivilegeStatus(userId);

        List<Object> vipProgramList = new ArrayList<>();
        List<VipProgram> vipPrograms = vipProgramDao.getVipProgramNotStart();
        for (VipProgram vipProgram : vipPrograms) {
            Map<String, Object> vipP = new HashMap<>();
            vipP.put("vipProgramId", vipProgram.getProgramId());
            vipP.put("iconImg", vipProgram.getIconImg());
            vipP.put("priceDesc", CommonUtil.removeZeroAfterPoint(CommonUtil.convertFen2Yuan(vipProgram.getPrice())
                    .toString()) + CommonConstant.CASH_MONETARY_UNIT_YUAN + "解锁一场");
            vipP.put("vipUnlockAd", "会员免费查看一场");
            vipP.put("privilegeStatus", privilegeStatus);
            vipP.put("awardNum", CommonUtil.divide(vipProgram.getAwardNum() + "", "10", 1));
            vipP.put("recommendNum", CommonUtil.divide(vipProgram.getRecommendNum() + "", "10", 1).toString());
            vipP.put("endTime", "截止时间" + DateUtil.formatTime(vipProgram.getEndTime(), "yyyy-MM-dd HH:mm"));
            List<Map<String, Object>> recommendInfos = new ArrayList<>();
            if (userVipProgramService.checkUserPurchaseVipProgram(userId, vipProgram.getProgramId())) {
                recommendInfos = getVipProgramRecommend(vipProgram.getProgramInfo(), true);
            }
            vipP.put("recommendInfos", recommendInfos);
            vipP.put("programStatus", userVipProgramService.checkUserPurchaseVipProgram(userId, vipProgram
                    .getProgramId()) ? 1 : 0);
            vipP.put("leadVipTxt", "开通会员免费看");
            vipProgramList.add(vipP);
        }
        resultMap.put("vipProgramList", vipProgramList);
        // 次数
        int freeTimes = 0;
        if (isVip) {
            freeTimes = 1;
            // 获取用户免费次数
            String dateId = DateUtil.getCurrentDay(DateUtil.DATE_FORMAT_YYYYMMDD);
            UserVipProgramDateTime dateTimes = userVipProgramDateTimeDao.getUserVipProgramTimes(userId, dateId, false);
            if (dateTimes != null && dateTimes.getUseTimes() != null) {
                freeTimes = freeTimes - dateTimes.getUseTimes();
            }
        }
        resultMap.put("freeTimes", freeTimes);
        resultMap.put("recentRightCount", getRecentRightCount());
        return resultMap;
    }

    @Override
    public Map<String, Object> getRedVipProgram(Long lastDate) {
        Map<String, Object> result = new HashMap<>();
        if (lastDate == null) {
            lastDate = Long.MAX_VALUE;
        }
        List<Map<String, Object>> programs = redisService.kryoZRevRangeByScoreGet(RedisConstant.getRedVipProgramKey()
                , Long.MIN_VALUE, lastDate, 0, 11, HashMap.class);
        if (programs == null || programs.size() == 0) {
            rebuildRedVipProgramRedis();
        }

        Boolean hasNext = Boolean.FALSE;
        if (programs != null) {
            if (programs.size() > 10) {
                hasNext = Boolean.TRUE;
                programs = programs.subList(0, 10);
                lastDate = Long.valueOf(programs.get(programs.size() - 1).get("dateIndex").toString());
            }
            for (Map<String, Object> program : programs) {
                program.put("rewardIndex", CommonUtil.divide(program.get("rewardIndex") + "", "10", 1));
                program.put("recommendIndex", CommonUtil.divide(program.get("recommendIndex") + "", "10", 1));
            }
        }

        result.put("programs", programs);
        result.put("hasNext", hasNext);
        result.put("lastIndex", lastDate);
        return result;
    }

    @Override
    public void rebuildRedVipProgramRedis() {
        List<VipProgram> vipPrograms = vipProgramDao.getVipProgramByStatus(null, null, CommonConstant
                .VIP_PROGRAM_STATUS_RED);
        if (vipPrograms == null || vipPrograms.size() <= 0) {
            return;
        }
        TreeMap<Integer, Map<String, Object>> treeProgram = new TreeMap<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2 - o1;
            }
        });

        redisService.del(RedisConstant.getRedVipProgramKey());

        for (VipProgram tempProgram : vipPrograms) {
            Integer date = Integer.valueOf(DateUtil.formatTime(tempProgram.getCreateTime(), "yyyyMMdd"));

            Map<String, Object> temp = null;
            if (treeProgram.containsKey(date)) {
                temp = treeProgram.get(date);
            }
            treeProgram.put(date, packageNewVipProgram2Map(tempProgram, temp));
        }

        for (Integer key : treeProgram.keySet()) {
            redisService.kryoZAddSet(RedisConstant.getRedVipProgramKey(), Long.valueOf(key), treeProgram.get(key));
        }
    }

    @Override
    public Boolean saveRedVipProgram2Redis(VipProgram vipProgram) {
        if (vipProgram == null || !vipProgram.getStatus().equals(CommonConstant.VIP_PROGRAM_STATUS_RED)) {
            return Boolean.FALSE;
        }
        Integer date = Integer.valueOf(DateUtil.formatTime(vipProgram.getCreateTime(), "yyyyMMdd"));
        List<Map<String, Object>> programList = redisService.kryoZRevRangeByScoreGet(RedisConstant.getRedVipProgramKey()
                , Long.MIN_VALUE, Long.valueOf(date), 0, 1, HashMap.class);
        if (programList != null && programList.size() > 0) {
            Map<String, Object> programMap = programList.get(0);
            if (Integer.valueOf(programMap.get("dateIndex").toString()).equals(date)) {
                programMap = programList.get(0);
            }
            Map<String, Object> newProgramMap = packageNewVipProgram2Map(vipProgram, programMap);
            redisService.kryoZRem(RedisConstant.getRedVipProgramKey(), programMap);
            redisService.kryoZAddSet(RedisConstant.getRedVipProgramKey(), Long.valueOf(date), newProgramMap);
        }

        return Boolean.FALSE;
    }

    public Map<String, Object> packageNewVipProgram2Map(VipProgram vipProgram, Map<String, Object> oldVipProgramMap) {
        Map<String, Object> programMap = new HashMap<>();
        List<Map<String, Object>> programData = new ArrayList<>();
        if (oldVipProgramMap != null && !oldVipProgramMap.isEmpty()) {
            programMap.putAll(oldVipProgramMap);
            programData = (List<Map<String, Object>>) programMap.get("programData");
            Integer awardNum = Integer.valueOf(programMap.get("rewardIndex").toString());
            Integer recommendNum = Integer.valueOf(programMap.get("recommendIndex").toString());
            awardNum = awardNum > vipProgram.getAwardNum() ? awardNum : vipProgram.getAwardNum();
            recommendNum = recommendNum > vipProgram.getRecommendNum() ? recommendNum : vipProgram.getRecommendNum();
            programMap.put("rewardIndex", awardNum);
            programMap.put("recommendIndex", recommendNum);
        } else {
            programMap.put("dateTime", DateUtil.formatTime(vipProgram.getCreateTime(), "yyyy-MM-dd"));
            programMap.put("playTypeName", "推荐玩法：2串1");
            programMap.put("programCount", 0);
            programMap.put("rewardIndex", vipProgram.getAwardNum());
            programMap.put("recommendIndex", vipProgram.getRecommendNum());
            programMap.put("programStatus", 1);
            programMap.put("programData", programData);
        }

        List<Map<String, Object>> redVipProgramInfo = getVipProgramRecommend(vipProgram.getProgramInfo(), false);
        if (redVipProgramInfo != null && redVipProgramInfo.size() > 0) {
            Map<String, Object> redVipProgramInfoMap = new HashMap<>();
            redVipProgramInfoMap.put("programInfo", redVipProgramInfo);
            programData.add(redVipProgramInfoMap);
        }
        programMap.put("programCount", programData.size());
        programMap.put("dateIndex", Integer.valueOf(DateUtil.formatTime(vipProgram.getCreateTime(), "yyyyMMdd")));
        return programMap;
    }

    @Override
    public void vipProgramOpenPrizeTiming() {
        List<MatchSchedule> matchSchedules = matchScheduleDao.getVipProgramMatch();
        if (matchSchedules == null || matchSchedules.size() == 0) {
            return;
        }
        Set<String> calculateMatchIds = new HashSet<>();
        for (MatchSchedule temp : matchSchedules) {
            MatchSchedule matchSchedule = matchScheduleDao.getMatchScheduleByPk(temp.getMatchId(), temp.getLotteryCode
                    ());
            if (matchSchedule.getIfEnd() != 2 || matchSchedule.getIfVipProgram().equals(CommonStatusEnum.YES
                    .getStatus())) {
                continue;
            }
            DetailMatchInfo matchInfo = thirdHttpService.getMatchMapByMatchId(temp.getMatchId());
            if (calculateVipProgram(matchInfo)) {
                calculateMatchIds.add(matchInfo.getMatchId());
            }
        }
        if (calculateMatchIds.size() > 0) {
            for (String matchId : calculateMatchIds) {
                matchScheduleDao.updateMatchStatus(Integer.valueOf(matchId), SportsProgramConstant
                        .LOTTERY_LOTTERY_CODE_FOOTBALL, "IF_VIP_PROGRAM", "VIP_PROGRAM_TIME");
            }
        }
    }

    private Boolean calculateVipProgram(DetailMatchInfo matchInfo) {
        List<VipProgram> vipPrograms = vipProgramDao.getNotCalculateMatchVipProgram(matchInfo.getMatchId());
        if (vipPrograms == null || vipPrograms.size() == 0) {
            return Boolean.FALSE;
        }

        Boolean result = Boolean.TRUE;
        for (VipProgram vipProgram : vipPrograms) {
            if (StringUtils.isBlank(vipProgram.getProgramInfo())) {
                continue;
            }
            String calculateProgramInfo = getCalculateProgramInfo(vipProgram.getProgramInfo(), matchInfo);
            Integer matchCount = vipProgram.getProgramInfo().split(",").length;
            Boolean res = self.updateVipProgramStatusAfterCalculate(vipProgram.getProgramId(), calculateProgramInfo,
                    matchCount);
            if (!res) {
                result = Boolean.FALSE;
            }
            int hitOption = calculateProgramInfo.split(CommonConstant.COMMON_ESCAPE_STR + CommonConstant
                    .COMMON_STAR_STR).length - 1;
            if (res && hitOption == calculateProgramInfo.split(CommonConstant.COMMA_SPLIT_STR).length) {
                vipProgram = vipProgramDao.getVipProgramByProgramId(vipProgram.getProgramId(), false);
                saveRedVipProgram2Redis(vipProgram);
            }
        }
        return result;
    }

    @Transactional
    @Override
    public Boolean updateVipProgramStatusAfterCalculate(String programId, String calculateProgramInfo, Integer
            matchCount) {
        VipProgram vipProgram = vipProgramDao.getVipProgramByProgramId(programId, true);
        if (vipProgram.getStatus().equals(CommonConstant.VIP_PROGRAM_STATUS_OPEN_AWARD) || vipProgram.getStatus()
                .equals(CommonConstant.VIP_PROGRAM_STATUS_RED)) {
            return Boolean.TRUE;
        }
        vipProgram.setProgramInfo(calculateProgramInfo);
        Integer oldMatchCount = vipProgram.getCalMatchCount();
        Integer newMatchCount = oldMatchCount + 1;
        if (newMatchCount > matchCount) {
            return Boolean.FALSE;
        }
        Integer isRight = CommonConstant.VIP_PROGRAM_IS_RIGHT_PART_OPEN_AWARD;
        Integer status = CommonConstant.VIP_PROGRAM_STATUS_PARTY;
        if (newMatchCount.equals(matchCount)) {
            isRight = CommonConstant.VIP_PROGRAM_IS_RIGHT_LOSE;
            status = CommonConstant.VIP_PROGRAM_STATUS_OPEN_AWARD;
            Integer rightCount = calculateProgramInfo.split("\\*").length;
            if (rightCount > 1) {
                isRight = CommonConstant.VIP_PROGRAM_IS_RIGHT_RIGHT;
                if (rightCount > matchCount) {
                    status = CommonConstant.VIP_PROGRAM_STATUS_RED;
                }
            }
        }
        Integer updateRes = vipProgramDao.updateVipProgramStatus(vipProgram.getProgramId(), calculateProgramInfo,
                isRight, vipProgram.getIsRight(), status, vipProgram.getStatus(), newMatchCount, oldMatchCount);
        if (updateRes > 0) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    public String getCalculateProgramInfo(String originProgramInfo, DetailMatchInfo matchInfo) {
        String result = originProgramInfo;
        StringBuilder programInfoSb = new StringBuilder();
        String[] programInfoArr = originProgramInfo.split(CommonConstant.COMMA_SPLIT_STR);
        for (int i = 0; i < programInfoArr.length; i++) {
            String matchProgramInfo = programInfoArr[i];
            if (!matchProgramInfo.contains(matchInfo.getMatchId())) {
                programInfoSb.append(matchProgramInfo);
                if (i == 0) {
                    programInfoSb.append(CommonConstant.COMMA_SPLIT_STR);
                }
                continue;
            }
            //711753:0$3@1
            String[] matchOptionArr = matchProgramInfo.split(CommonConstant.COMMON_COLON_STR);
            String[] playTypeOptionsArr = matchOptionArr[1].split(CommonConstant.COMMON_ESCAPE_STR + CommonConstant
                    .COMMON_DOLLAR_STR);
            if (playTypeOptionsArr[0].contains(CommonConstant.COMMON_STAR_STR)) {
                playTypeOptionsArr[0] = playTypeOptionsArr[0].replaceAll("\\*", CommonConstant.SPACE_NULL_STR);
            }
            Integer playType = Integer.valueOf(playTypeOptionsArr[0]);
            Integer hitOption = FootballCalculateResultEnum.getEnum(playType).getHitOption(matchInfo.getHostScore() +
                    "", matchInfo.getAwayScore() + "", matchInfo.getHandicap(playType));

            String options = playTypeOptionsArr[1];
            if (playTypeOptionsArr[1].contains(hitOption + "")) {
                if (playTypeOptionsArr[1].contains(CommonConstant.COMMON_STAR_STR)) {
                    playTypeOptionsArr[1] = playTypeOptionsArr[1].replace(hitOption + "", "");
                }
                options = playTypeOptionsArr[1].replace(hitOption + "", "*" + hitOption);
            }
            result = matchOptionArr[0] + ":" + playTypeOptionsArr[0] + CommonConstant.COMMON_DOLLAR_STR + options;
            programInfoSb.append(result);
            if (i == 0) {
                programInfoSb.append(CommonConstant.COMMA_SPLIT_STR);
            }
        }
        result = programInfoSb.toString();
        return result;
    }

    //711753:0$3@1,711777:1$1@0
    private List<Map<String, Object>> getVipProgramRecommend(String programInfo, boolean oddBracket) {
        if (StringUtils.isBlank(programInfo)) {
            return null;
        }
        List<Map<String, Object>> result = new ArrayList<>();
        String[] matchOptions = programInfo.split(",");
        for (String matchOption : matchOptions) {
            //711753:0$3@1
            String[] matchOptionArr = matchOption.split(":");
            String matchId = matchOptionArr[0];
            //0$3@1
            String playTypeOptions = matchOptionArr[1];
            String[] playTypeOptionArr = playTypeOptions.split("\\$");
            Integer playType = Integer.valueOf(playTypeOptionArr[0]);
            String options = playTypeOptionArr[1];

            String[] optionArr = options.split("@");

            DetailMatchInfo matchInfo = thirdHttpService.getMatchMapByMatchId(Integer.valueOf(matchId));
            String score = " vs ";
            if (matchInfo.getHostScore() != null && matchInfo.getAwayScore() != null) {
                score = " " + matchInfo.getHostScore() + ":" + matchInfo.getAwayScore();
            }

            Map<String, Object> temp = new HashMap<>();
            temp.put("matchName", matchInfo.getHostName() + score + matchInfo.getAwayName());
            temp.put("matchSn", matchInfo.getMatchDate() + " " + matchInfo.getMatchName());
            temp.put("oddAd", assembleVipProgramOption2ShowStr(optionArr, matchInfo, playType, oddBracket));
            result.add(temp);
        }
        return result;
    }

    private String assembleVipProgramOption2ShowStr(String[] optionArr, DetailMatchInfo matchInfo, Integer playType,
                                                    Boolean oddBracket) {
        if (optionArr == null || optionArr.length == 0) {
            return "";
        }
        StringBuilder optionsSb = new StringBuilder();
        for (int i = 0; i < optionArr.length; i++) {
            String option = optionArr[i];
            Boolean hitFlag = Boolean.FALSE;
            if (option.contains(CommonConstant.COMMON_STAR_STR)) {
                hitFlag = Boolean.TRUE;
                option = option.replaceAll(CommonConstant.COMMON_ESCAPE_STR + CommonConstant.COMMON_STAR_STR, "");
            }
            String odds = matchInfo.getItemOdd(playType, option);
            if (hitFlag) {
                optionsSb.append("<font color='" + CommonConstant.COMMON_COLOR_RED + "'>");
            }
            optionsSb.append(SportsUtils.getItemCn(SportsProgramConstant.LOTTERY_LOTTERY_CODE_FOOTBALL, playType, option));
            if (oddBracket) {
                optionsSb.append("(").append(odds).append(")");
            } else {
                optionsSb.append(odds);
            }

            if (hitFlag) {
                optionsSb.append("</font>");
            }
            if (i < optionArr.length - 1) {
                optionsSb.append("/");
            }
        }
        return optionsSb.toString();
    }

    private Integer getRecentRightCount() {
        Timestamp endTime = DateUtil.getCurrentTimestamp();
        Timestamp beginTime = DateUtil.getIntervalDays(endTime, -7);
        List<VipProgram> programs = vipProgramDao.getVipProgramByStatus(beginTime, endTime, CommonConstant
                .VIP_PROGRAM_STATUS_RED);
        if (programs == null || programs.size() == 0) {
            return 0;
        }
        return programs.size();
    }

    @Override
    public void setSelf(Object proxyBean) {
        this.self = (VipProgramService) proxyBean;
    }
}
