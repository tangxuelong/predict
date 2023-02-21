package com.mojieai.predict.service.predict;

import com.alibaba.fastjson.JSON;
import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.cache.IniCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.PredictRedBallDao;
import com.mojieai.predict.dao.PredictUserRecordsDao;
import com.mojieai.predict.entity.po.*;
import com.mojieai.predict.entity.vo.UserLoginVo;
import com.mojieai.predict.enums.GameEnum;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.service.LoginService;
import com.mojieai.predict.service.PredictNumService;
import com.mojieai.predict.service.VipMemberService;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.PredictUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by tangxuelong on 2018/1/10.
 */
@Service
public class Fc3dPredictInfo extends PredictInfo {

    @Autowired
    private PredictNumService predictNumService;
    @Autowired
    private LoginService loginService;
    @Autowired
    private PredictUserRecordsDao predictUserRecordsDao;
    @Autowired
    private PredictRedBallDao predictRedBallDao;
    @Autowired
    private VipMemberService vipMemberService;

    @Override
    public Map<String, Object> generatePredictNums(GamePeriod period) {
        return null;
    }

    @Override
    public void killBluePredict(Long gameId, String periodId, PredictSchedule predictScheduleDirty) {

    }

    @Override
    public String getShowText(String gameEn) {
        return null;
    }

    @Override
    public Map<String, Object> getPredictNumber(Long userId, Long gameId, String timeSpan, Integer predictType) {
        Map<String, Object> resultMap = new HashMap<>();
        // 预测期次
        GamePeriod lastOpenPeriod = PeriodRedis.getLastOpenPeriodByGameId(gameId);
        GamePeriod predictPeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(gameId, lastOpenPeriod.getPeriodId());
        resultMap.put("predictPeriod", predictPeriod.getPeriodId());
        resultMap.put("ifRunOutFlag", 0);
        // 用户次数校验
        Integer userMaxNums = predictNumService.getUserPredictMaxNums(gameId, predictPeriod.getPeriodId(), userId);
        String userUseNumsStr = redisService.get(RedisConstant.userUseNums(gameId, predictPeriod.getPeriodId(),
                userId));
        if (StringUtils.isBlank(userUseNumsStr)) {
            userUseNumsStr = "0";
        }
        Integer userUseNums = Integer.valueOf(userUseNumsStr);
        Integer leftTimes = (userMaxNums - userUseNums) < 0 ? 0 : (userMaxNums - userUseNums);
        String leftTimesStr = "剩<font color=\"#FF5050\">" + leftTimes + "</font>次";
        if (leftTimes == 0) {
            if (!vipMemberService.checkUserIsVip(userId, VipMemberConstant.VIP_MEMBER_TYPE_DIGIT)) {
                resultMap.put("leadLoginFlag", PredictConstant.PREDICT_LEAD_LOGIN_FLAG_VIP);
                leftTimesStr = "获取更多";
            }
            resultMap.put("ifRunOutFlag", 1);
            resultMap.put("leftTimes", leftTimesStr);
            resultMap.put("ifRunOutMsg", "本期智慧次数已用完！");
            return resultMap;
        }
        Integer lastLeftTimes = leftTimes;
        userUseNums = redisService.incr(RedisConstant.userUseNums(gameId, predictPeriod.getPeriodId(), userId))
                .intValue();
        leftTimes = (userMaxNums - userUseNums) < 0 ? 0 : (userMaxNums - userUseNums);
        // 福彩3d 获取预测号码
        // 获取到开奖号码
        String redisKey = RedisConstant.predictNumbers(gameId, predictPeriod.getPeriodId());
        List<String> predictNumbers = redisService.kryoGet(redisKey, ArrayList.class);
        //String countFlagKey = RedisConstant.predictNumbersCountFlag(gameId, predictPeriod.getPeriodId());
        List<String> userGetNumber = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            //Integer count = redisService.incr(countFlagKey).intValue();
            // 1 为空 1002为结束
//            if (count == 1 || count >= 729) {
//                // 初始化数据
//                predictNumbers = productPredictNumbers(gameId);
//                if (count == 729) {
//                    redisService.set(countFlagKey, "1");
//                }
//            }
            if (null == predictNumbers || predictNumbers.size() <= 0) {
                predictNumbers = productPredictNumbers(gameId);
                //redisService.set(countFlagKey, "1");
            }
            String number = predictNumbers.remove(0);
            userGetNumber.add(number);
            String recordsId = String.valueOf(userId) + String.valueOf(gameId) + String.valueOf(userUseNums) + String
                    .valueOf(i) + predictPeriod.getPeriodId();
            // 用户领取记录
            PredictUserRecords predictUserRecords = new PredictUserRecords(recordsId, gameId, predictPeriod.getPeriodId
                    (), userId, number, timeSpan, predictType);
            predictUserRecordsDao.insert(predictUserRecords);
        }
        redisService.kryoSetEx(redisKey, (int) DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(), predictPeriod
                .getAwardTime()) + (60 * 60), predictNumbers);
        // 用户预测记录表
        resultMap.put("userGetNumber", userGetNumber);
        // 用户预测title
        // 剩余次数
        if (!vipMemberService.checkUserIsVip(userId, VipMemberConstant.VIP_MEMBER_TYPE_DIGIT) && lastLeftTimes == 0) {
            resultMap.put("leadLoginFlag", PredictConstant.PREDICT_LEAD_LOGIN_FLAG_VIP);
        }
        if (!vipMemberService.checkUserIsVip(userId, VipMemberConstant.VIP_MEMBER_TYPE_DIGIT) && leftTimes == 0) {
            leftTimesStr = "获取更多";
        }
        resultMap.put("leftTimes", leftTimesStr);
        resultMap.put("openAwardFlag", 1);
        resultMap.put("openAwardAlterMsg", PredictUtil.getShowTextCal(GameCache.getGame(gameId).getGameEn(),
                "本期官方投注已截止，预测结果仅供参考", ""));
        return resultMap;
    }

    @Override
    public Map<String, Object> predictNumbersIndex(Long userId, Long gameId) {
        // 预测的期次
        Map<String, Object> resultMap = new HashMap<>();
        // 预测期次
        GamePeriod lastOpenPeriod = PeriodRedis.getLastOpenPeriodByGameId(gameId);
        GamePeriod predictPeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(gameId, lastOpenPeriod.getPeriodId());
        resultMap.put("predictPeriod", predictPeriod.getPeriodId());
        Integer leftTimes = CommonConstant.FREE_PREDICT_MAX_TIMS;

        // 如果没有登录，次数为最大次数
        if (null != userId) {
            Integer userMaxNums = predictNumService.getUserPredictMaxNums(gameId, predictPeriod.getPeriodId(), userId);
            String userUseNumsStr = redisService.get(RedisConstant.userUseNums(gameId, predictPeriod.getPeriodId(),
                    userId));
            if (StringUtils.isBlank(userUseNumsStr)) {
                userUseNumsStr = "0";
            }
            Integer userUseNums = Integer.valueOf(userUseNumsStr);
            leftTimes = (userMaxNums - userUseNums) < 0 ? 0 : (userMaxNums - userUseNums);
            String txt = (userUseNums == 0) ? "共" : "剩";
            String leftTimesStr = txt + "<font color=\"#FF5050\">" + leftTimes + "</font>次";
            if (leftTimes == 0 && !vipMemberService.checkUserIsVip(userId, VipMemberConstant.VIP_MEMBER_TYPE_DIGIT)) {
                leftTimesStr = "获取更多";
            }
            resultMap.put("leftTimes", leftTimesStr);
        } else {
            resultMap.put("leftTimes", "共<font color=\"#FF5050\">" + leftTimes + "</font>次");
        }
        resultMap.put("predictBehandBtnMsg", "长按获取中奖号码");

        // 当前期次如果有试机号
        Map<String, Object> remark = JSON.parseObject(predictPeriod.getRemark());
        String winningNumber = lastOpenPeriod.getWinningNumbers();
        Boolean isHaveWinningNumber = Boolean.TRUE;
        String awardPeriodId = lastOpenPeriod.getPeriodId();
        String testNum = JSON.parseObject(lastOpenPeriod.getRemark()).getString("testNum");
        if (null != remark && StringUtils.isNotBlank(String.valueOf(remark.get("testNum")))) {
            isHaveWinningNumber = Boolean.FALSE;
            winningNumber = "等待开奖";
            testNum = String.valueOf(remark.get("testNum"));
            awardPeriodId = predictPeriod.getPeriodId();
        }
        resultMap.put("isHaveWinningNumber", isHaveWinningNumber);
        resultMap.put("awardPeriodId", awardPeriodId);
        resultMap.put("testNum", testNum);
        resultMap.put("winningNumber", winningNumber);
        // 跑马灯
        List<String> marquee = redisService.kryoGet(RedisConstant.getMarqueeIndex(gameId), ArrayList.class);
        if (null == marquee || marquee.size() <= 0) {
            log.error("跑马灯空了！！！速速查看");
            marquee = new ArrayList<>();
            marquee.add("敬请期待！");
        }
        resultMap.put("marquee", marquee);

        // 首页更多预测
        try {
            Map<String, Object> predictMore = (Map<String, Object>) JSON.parseObject(ActivityIniCache
                    .getActivityIniValue(ActivityIniConstant
                            .getIndexPredictMoreKey(GameCache.getGame(gameId).getGameEn())));
            List<Map<String, Object>> predictSorts = (List<Map<String, Object>>) predictMore.get("predictSorts");
            resultMap.put("predictSorts", predictSorts);
        } catch (Exception e) {
            log.error("福彩3d更多预测Ini parse error please check now!", e);
            throw new BusinessException("ini parse error", e);
        }
        return resultMap;
    }

    /* build 首页预测号码*/
    @Override
    public List<String> productPredictNumbers(Long gameId) {
        // 当前期次有了试机号才是当前期预测期次
        GamePeriod lastOpenPeriod = PeriodRedis.getLastOpenPeriodByGameId(gameId);
        GamePeriod predictPeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(gameId, lastOpenPeriod.getPeriodId());
        // 只有当预测期次预测号码集合为空时，才进行新的预测
        String redisKey = RedisConstant.predictNumbers(gameId, predictPeriod.getPeriodId());
        List<String> predictNumbers = redisService.kryoGet(redisKey, ArrayList.class);
        if (null != predictNumbers && predictNumbers.size() > 1) {
            log.error("福彩3d预测号码失败，当前期已经预测且并未被用户领取完毕，不需要重新预测！");
            return predictNumbers;
        }
        // 生成号码 放入缓存
        if (null == predictNumbers) {
            predictNumbers = new ArrayList<>();
        }
        for (int i = 0; i < 1000; i++) {
            // 去掉定位杀1码
            String[] positionKillThreeArr = new String[3];
            PredictRedBall predictRedBall = predictRedBallDao.getPredictRedBall(gameId, predictPeriod.getPeriodId(),
                    PredictConstant.POSITION_KILL_THREE);
            if (null != predictRedBall) {
                positionKillThreeArr = predictRedBall.getNumStr().split(CommonConstant.SPACE_SPLIT_STR);
            }

            StringBuffer sb = new StringBuffer();
            if (String.valueOf(i).length() <= 1) {
                // 考虑个位
                if (String.valueOf(i).contains(positionKillThreeArr[2])) {
                    continue;
                }
                sb.append(String.valueOf(0)).append(CommonConstant.SPACE_SPLIT_STR).append(String.valueOf(0)).append
                        (CommonConstant.SPACE_SPLIT_STR).append(String.valueOf(i));
            } else if (String.valueOf(i).length() <= 2) {
                // 考虑个位 十位
                if (String.valueOf(i).substring(1, 2).contains(positionKillThreeArr[2])) {
                    continue;
                }
                if (String.valueOf(i).substring(0, 1).contains(positionKillThreeArr[1])) {
                    continue;
                }
                sb.append(String.valueOf(0)).append(CommonConstant.SPACE_SPLIT_STR).append(String.valueOf(i)
                        .substring(0, 1)).append(CommonConstant.SPACE_SPLIT_STR).append(String.valueOf(i)
                        .substring(1, 2));
            } else {
                // 考虑个位 十位 百位
                if (String.valueOf(i).substring(2, 3).contains(positionKillThreeArr[2])) {
                    continue;
                }
                if (String.valueOf(i).substring(1, 2).contains(positionKillThreeArr[1])) {
                    continue;
                }
                if (String.valueOf(i).substring(0, 1).contains(positionKillThreeArr[0])) {
                    continue;
                }
                sb.append(String.valueOf(i).substring(0, 1)).append(CommonConstant.SPACE_SPLIT_STR).append(String
                        .valueOf(i).substring(1, 2)).append(CommonConstant.SPACE_SPLIT_STR).append(String.valueOf(i)
                        .substring(2, 3));
            }
            predictNumbers.add(sb.toString());
        }
        /* 洗牌*/
        Collections.shuffle(predictNumbers);
        redisService.kryoSetEx(redisKey, (int) DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(), predictPeriod
                .getAwardTime()) + (60 * 60), predictNumbers);
        return predictNumbers;
    }

    /* 计算中奖号码 产生走马灯*/
    @Override
    public void analysisPredictNumbers(Long gameId, String periodId) {
        List<PredictUserRecords> predictUserRecordsList = predictUserRecordsDao.getUnAwardPredictRecords(gameId,
                periodId);
        GamePeriod period = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, periodId);
        String winningNumber = period.getWinningNumbers();
        // 没有开奖
        if (StringUtils.isBlank(winningNumber)) {
            return;
        }
        List<String> marquee = redisService.kryoGet(RedisConstant.getMarqueeIndex(gameId), ArrayList.class);
        if (null == marquee || marquee.size() <= 0) {
            marquee = new ArrayList<>();
        }
        Collections.reverse(marquee);
        if (null != predictUserRecordsList && predictUserRecordsList.size() > 0) {
            for (PredictUserRecords predictUserRecords : predictUserRecordsList) {
                Integer isAward = 0;
                // 防止重复放入跑马灯
                if (predictUserRecords.getIsAward() == 0) {
                    // 只计算命中
                    if (winningNumber.trim().equals(predictUserRecords.getNumStr().trim())) {
                        isAward = 1;
                        // 如果命中，加入到首页跑马灯缓存
                        UserLoginVo userLoginVo = loginService.getUserLoginVo(predictUserRecords.getUserId());
                        String marqueeStr = userLoginVo.getNickName() + "在" + predictUserRecords.getPeriodId()
                                + "期预测中喜中1040元!";
                        marquee.add(marqueeStr);
                    }
                    predictUserRecordsDao.updateNumStr(predictUserRecords.getRecordId(), predictUserRecords.getNumStr(),
                            isAward);
                }
            }
        }
        Collections.reverse(marquee);// 最新的在最前
        List<String> newMarquee = marquee;
        if (marquee.size() > 99) {
            newMarquee = marquee.subList(0, 99);
        }
        redisService.kryoSet(RedisConstant.getMarqueeIndex(gameId), newMarquee);
    }

    /* 跑马灯历史记录*/
    @Override
    public Map<String, Object> predictNumbersHistory(Long gameId, String periodId) {
        Map<String, Object> resultMap = new HashMap<>();

        //100 条 100期
        Integer count = 0;
        // 当前开奖期
        GamePeriod period = PeriodRedis.getLastOpenPeriodByGameId(gameId);
        GamePeriod nextPeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(gameId, period.getPeriodId());
        if (periodId != null) {
            period = PeriodRedis.getNextPeriodByGameIdAndPeriodId(gameId, periodId);
        }

        List<Object> periodResult = redisService.kryoGet(RedisConstant.FC3D_HISTORY + nextPeriod.getPeriodId(),
                ArrayList
                        .class);

        Boolean isHaveNextPage = redisService.kryoGet(RedisConstant.FC3D_HISTORY + "isHaveNextPage" + nextPeriod
                .getPeriodId(), Boolean.class);

        String periodIdRes = redisService.kryoGet(RedisConstant.FC3D_HISTORY + "periodIdRes" + nextPeriod.getPeriodId(),
                String.class);

        if (null == periodResult || periodResult.size() <= 0) {
            periodResult = new ArrayList<>();
            List<PredictUserRecords> predictUserRecordsList = predictUserRecordsDao.getAwardPredictRecords(gameId,
                    period.getPeriodId());
            Map<String, Object> currentPeriodResult = new HashMap<>();
            List<Object> predictList = new ArrayList<>();
            setPeriodRecords(predictUserRecordsList, period, currentPeriodResult, predictList, periodResult);
            count += predictUserRecordsList.size();

            if (count < 100) {
                for (int i = 0; i < 100; i++) {
                    if (count >= 100) {
                        break;
                    }
                    period = PeriodRedis.getLastPeriodByGameIdAndPeriodId(gameId, period.getPeriodId());
                    predictUserRecordsList = predictUserRecordsDao.getAwardPredictRecords(gameId, period
                            .getPeriodId());
                    count += predictUserRecordsList.size();
                    currentPeriodResult = new HashMap<>();
                    predictList = new ArrayList<>();
                    setPeriodRecords(predictUserRecordsList, period, currentPeriodResult, predictList, periodResult);
                    if (i < 99) {
                        isHaveNextPage = Boolean.FALSE;
                    }
                    periodIdRes = period.getPeriodId();
                }
            }
            if (StringUtils.isBlank(periodId)) {
                redisService.kryoSetEx(RedisConstant.FC3D_HISTORY + nextPeriod.getPeriodId(), ((int) DateUtil.getDiffSeconds
                        (DateUtil.getCurrentTimestamp(), nextPeriod.getAwardTime())) + 6000, periodResult);

                redisService.kryoSetEx(RedisConstant.FC3D_HISTORY + "isHaveNextPage" + nextPeriod.getPeriodId(), ((int)
                                DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(), nextPeriod.getAwardTime())) +
                                6000,
                        isHaveNextPage);

                redisService.kryoSetEx(RedisConstant.FC3D_HISTORY + "periodIdRes" + nextPeriod.getPeriodId(), ((int)
                                DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(), nextPeriod.getAwardTime())) + 6000,
                        periodIdRes);

            }
        }

        resultMap.put("isHaveNextPage", isHaveNextPage);
        resultMap.put("periodId", periodIdRes);
        resultMap.put("predictHistory", periodResult);
        return resultMap;
    }

    private void setPeriodRecords(List<PredictUserRecords> predictUserRecordsList, GamePeriod period, Map<String,
            Object> currentPeriodResult, List<Object> predictList, List<Object> periodResult) {
        if (null != predictUserRecordsList && predictUserRecordsList.size() > 0) {
            currentPeriodResult.put("period", period.getPeriodId() + "期开奖：");
            currentPeriodResult.put("winningNumber", "<font color=\"#FF5050\">" + period.getWinningNumbers() +
                    "</font>");
            for (PredictUserRecords predictUserRecords : predictUserRecordsList) {
                Map<String, Object> predict = getPredictUserRecordVo(predictUserRecords);
                predictList.add(predict);
            }
            currentPeriodResult.put("predictList", predictList);
            periodResult.add(currentPeriodResult);
        }
    }

    private Map<String, Object> getPredictUserRecordVo(PredictUserRecords predictUserRecords) {
        Map<String, Object> awardRecords = new HashMap<>();
        UserLoginVo userLoginVo = loginService.getUserLoginVo(predictUserRecords.getUserId());
        awardRecords.put("userId", String.valueOf(userLoginVo.getUserId()));
        awardRecords.put("nickName", userLoginVo.getNickName());
        awardRecords.put("headImgUrl", userLoginVo.getHeadImgUrl());
        awardRecords.put("predictType", predictUserRecords.getPredictType());
        awardRecords.put("predictTypeUrl", predictUserRecords.getPredictType() == 0 ? "https://ohduoklem.qnssl" +
                ".com/changan" + ".png" : "");
        awardRecords.put("predictNumber", predictUserRecords.getNumStr());
        awardRecords.put("timeSpan", predictUserRecords.getTimeSpan());
        awardRecords.put("award", "1040元");
        Boolean isVip = vipMemberService.checkUserIsVip(predictUserRecords.getUserId(), VipMemberConstant
                .VIP_MEMBER_TYPE_DIGIT);
        awardRecords.put("isVip", isVip);
        return awardRecords;
    }

    // 预测号码 定位杀三码
    public void productPositionKillThree(Long gameId, String periodId) {
        /* 检查predict redis*/
        Game game = GameCache.getGame(gameId);
        GamePeriod predictPeriod = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, periodId);
        String redisFlag = redisService.kryoGet(RedisConstant.getPositionKillThreeFlag(gameId, periodId), String.class);
        if (StringUtils.isNotBlank(redisFlag)) {
            return;
        }

        /* 更新上期中奖*/
        GamePeriod lastPeriod = PeriodRedis.getLastPeriodByGameIdAndPeriodId(gameId, periodId);

        String redisKey = RedisConstant.getPredictTypeKey(game.getGameEn(), PredictConstant.POSITION_KILL_THREE);
        Map<String, String> predictNumbers = redisService.kryoGet(redisKey, TreeMap.class);

        /* 如果缓存为空，从数据库取*/
        if (null == predictNumbers) {
            predictNumbers = new TreeMap<>(Comparator.reverseOrder());
            /* 取100期，可以从新构建缓存*/
            List<PredictRedBall> predictBalls = predictRedBallDao.getPredictRedBalls(gameId,
                    PredictConstant.POSITION_KILL_THREE, 100);
            if (null != predictBalls) {
                for (PredictRedBall predictRedBall : predictBalls) {
                    predictNumbers.put(predictRedBall.getPeriodId(), predictRedBall.getNumStr());
                }
            }
        }

        /* 如果缓存没有，数据库也没有 就不用计算上一期*/
        if (null != predictNumbers) {
            String lastPredictNumber = predictNumbers.get(lastPeriod.getPeriodId());
            if (StringUtils.isNotBlank(lastPredictNumber)) {
                String redNumber = lastPeriod.getWinningNumbers();
                String predictRedNumber = lastPredictNumber;
                if (!predictRedNumber.contains(CommonConstant.COMMON_STAR_STR)) {
                    // 定位计算中了几个号
                    String[] pre = predictRedNumber.split(CommonConstant.SPACE_SPLIT_STR);
                    String[] red = redNumber.split(CommonConstant.SPACE_SPLIT_STR);
                    predictRedNumber = "";
                    for (int i = 0; i < 3; i++) {
                        if (pre[i].equals(red[i])) {
                            predictRedNumber += CommonConstant.COMMON_STAR_STR + pre[i] + CommonConstant
                                    .SPACE_SPLIT_STR;
                        } else {
                            predictRedNumber += pre[i] + CommonConstant.SPACE_SPLIT_STR;
                        }
                    }
                }
                lastPredictNumber = predictRedNumber.trim();
                /* 更新开奖数据到数据数据库*/
                predictRedBallDao.updateNumStrByGameIdPeriodId(gameId, lastPeriod.getPeriodId(), lastPredictNumber,
                        PredictConstant.POSITION_KILL_THREE);
                predictNumbers.put(lastPeriod.getPeriodId(), lastPredictNumber);
            }
        }
        /* 预测定位杀三码*//* 检查是否预测*/
        PredictRedBall predictRedBall = predictRedBallDao.getPredictRedBall(gameId, predictPeriod.getPeriodId(),
                PredictConstant.POSITION_KILL_THREE);
        if (null == predictRedBall) {
            // 红球
            StringBuffer redSb = new StringBuffer(game.getGameEn()).append(predictPeriod.getPeriodId()).append(IniCache
                    .getIniValue(IniConstant.RANDOM_CODE, CommonConstant.RANDOM_CODE));
            List<String> redList = new ArrayList<>();
            redList.addAll(Arrays.asList(GameEnum.getGameEnumById(game.getGameId()).getRedBalls()));
            redList.addAll(Arrays.asList(GameEnum.getGameEnumById(game.getGameId()).getRedBalls()));
            redList.addAll(Arrays.asList(GameEnum.getGameEnumById(game.getGameId()).getRedBalls()));
            Collections.shuffle(redList, new Random(new Long((long) redSb.toString().hashCode())));
            String predictNumber = redList.get(0) + CommonConstant.SPACE_SPLIT_STR + redList.get(1) + CommonConstant
                    .SPACE_SPLIT_STR + redList.get(2);

            /* 插入数据库*/
            predictRedBall = new PredictRedBall(gameId, predictPeriod.getPeriodId(), PredictConstant
                    .POSITION_KILL_THREE, predictNumber);
            predictRedBallDao.insert(predictRedBall);
            predictNumbers.put(predictPeriod.getPeriodId(), predictNumber);
        }

        // 更新预测期次redis /* 更新开奖期次缓存*/
        redisService.kryoSet(redisKey, predictNumbers);

        redisService.expire(redisKey, (int) DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(),
                DateUtil.getIntervalSeconds(predictPeriod.getAwardTime(), 3600)));

        /* 更新predict redis*/
        Integer second = 1;
        if ((int) DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(), DateUtil.getIntervalSeconds(predictPeriod
                .getAwardTime(), 3600)) > 0) {
            second = (int) DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(), DateUtil.getIntervalSeconds
                    (predictPeriod.getAwardTime(), 3600));
        }
        redisService.kryoSetEx(RedisConstant.getPositionKillThreeFlag(gameId, periodId), second, "1");
    }

    // 预测号码 3胆码
    public void productThreeDanCode(Long gameId, String periodId) {
        /* 检查predict redis*/
        Game game = GameCache.getGame(gameId);
        GamePeriod predictPeriod = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, periodId);
        String redisFlag = redisService.kryoGet(RedisConstant.getThreeDanCodeFlag(gameId, periodId), String.class);
        if (StringUtils.isNotBlank(redisFlag)) {
            return;
        }

        /* 更新上期中奖*/
        GamePeriod lastPeriod = PeriodRedis.getLastPeriodByGameIdAndPeriodId(gameId, periodId);

        String redisKey = RedisConstant.getPredictTypeKey(game.getGameEn(), PredictConstant.THREE_KILL_CODE);
        Map<String, String> predictNumbers = redisService.kryoGet(redisKey, TreeMap.class);

        /* 如果缓存为空，从数据库取*/
        if (null == predictNumbers) {
            predictNumbers = new TreeMap<>(Comparator.reverseOrder());
            /* 取100期，可以从新构建缓存*/
            List<PredictRedBall> predictBalls = predictRedBallDao.getPredictRedBalls(gameId,
                    PredictConstant.THREE_KILL_CODE, 100);
            if (null != predictBalls) {
                for (PredictRedBall predictRedBall : predictBalls) {
                    predictNumbers.put(predictRedBall.getPeriodId(), predictRedBall.getNumStr());
                }
            }
        }

        /* 如果缓存没有，数据库也没有 就不用计算上一期*/
        if (null != predictNumbers) {
            String lastPredictNumber = predictNumbers.get(lastPeriod.getPeriodId());
            if (StringUtils.isNotBlank(lastPredictNumber)) {
                String redNumber = lastPeriod.getWinningNumbers();
                String predictRedNumber = lastPredictNumber;
                if (!predictRedNumber.contains(CommonConstant.COMMON_STAR_STR)) {
                    // 计算中了几个号
                    String[] pre = predictRedNumber.split(CommonConstant.SPACE_SPLIT_STR);
                    for (int i = 0; i < 3; i++) {
                        if (redNumber.contains(pre[i])) {
                            predictRedNumber = predictRedNumber.replaceAll(pre[i], CommonConstant.COMMON_STAR_STR +
                                    pre[i]);
                        }
                    }
                }
                lastPredictNumber = predictRedNumber;
                /* 更新开奖数据到数据数据库*/
                predictRedBallDao.updateNumStrByGameIdPeriodId(gameId, lastPeriod.getPeriodId(), lastPredictNumber,
                        PredictConstant.THREE_KILL_CODE);
                predictNumbers.put(lastPeriod.getPeriodId(), lastPredictNumber);
            }
        }
        /* 预测定位杀三码*//* 检查是否预测*/
        PredictRedBall predictRedBall = predictRedBallDao.getPredictRedBall(gameId, predictPeriod.getPeriodId(),
                PredictConstant.THREE_KILL_CODE);
        if (null == predictRedBall) {
            // 红球
            StringBuffer redSb = new StringBuffer(game.getGameEn()).append(predictPeriod.getPeriodId()).append(IniCache
                    .getIniValue(IniConstant.RANDOM_CODE, CommonConstant.RANDOM_CODE));
            List<String> redList = Arrays.asList(GameEnum.getGameEnumById(game.getGameId()).getRedBalls());
            Collections.shuffle(redList, new Random(new Long((long) redSb.toString().hashCode())));
            List<String> redListNum = new ArrayList<>();
            redListNum.add(redList.get(0));
            redListNum.add(redList.get(1));
            redListNum.add(redList.get(2));
            Collections.sort(redListNum);
            String predictNumber = redListNum.get(0) + CommonConstant.SPACE_SPLIT_STR + redListNum.get(1) +
                    CommonConstant.SPACE_SPLIT_STR + redListNum.get(2);

            /* 插入数据库*/
            predictRedBall = new PredictRedBall(gameId, predictPeriod.getPeriodId(), PredictConstant
                    .THREE_KILL_CODE, predictNumber);
            predictRedBallDao.insert(predictRedBall);
            predictNumbers.put(predictPeriod.getPeriodId(), predictNumber);
        }

        // 更新预测期次redis /* 更新开奖期次缓存*/
        redisService.kryoSet(redisKey, predictNumbers);

        redisService.expire(redisKey, (int) DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(),
                DateUtil.getIntervalSeconds(predictPeriod.getAwardTime(), 3600)));

        /* 更新predict redis*/
        Integer second = 1;
        if ((int) DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(), DateUtil.getIntervalSeconds(predictPeriod
                .getAwardTime(), 3600)) > 0) {
            second = (int) DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(), DateUtil.getIntervalSeconds
                    (predictPeriod.getAwardTime(), 3600));
        }
        redisService.kryoSetEx(RedisConstant.getThreeDanCodeFlag(gameId, periodId), second, "1");
    }

    @Override
    public void productPredict() {
        Game game = GameCache.getGame(GameConstant.FC3D);
        GamePeriod currentPeriod = PeriodRedis.getCurrentPeriod(game.getGameId());
        GamePeriod lastPeriod = PeriodRedis.getLastPeriodByGameIdAndPeriodId(game.getGameId(), currentPeriod
                .getPeriodId());
        String fcPredictFlag = redisService.kryoGet(RedisConstant.getFcPredictFlag(game.getGameId(), currentPeriod
                .getPeriodId()), String.class);
        if (StringUtils.isNotBlank(fcPredictFlag)) {
            return;
        }

        // 上期开奖之后
        if (StringUtils.isNotBlank(lastPeriod.getWinningNumbers())) {
            redisService.kryoSetEx(RedisConstant.getFcPredictFlag(game.getGameId(), currentPeriod
                    .getPeriodId()), (int) DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(), DateUtil
                    .getIntervalSeconds(currentPeriod.getAwardTime(), 3600)), "1");
            /* 定位杀3码*/
            productPositionKillThree(game.getGameId(), currentPeriod.getPeriodId());
            /* 三胆码*/
            productThreeDanCode(game.getGameId(), currentPeriod.getPeriodId());
            /* 上期算奖*/
            analysisPredictNumbers(game.getGameId(), lastPeriod.getPeriodId());
        }
    }

    @Override
    public Map<String, Object> positionKill(Long gameId) {
        Map<String, Object> resultMap = new HashMap<>();
        List<Object> predictList = new ArrayList<>();
        Map<String, String> predictNumbers = getPredictNumber(gameId, PredictConstant.POSITION_KILL_THREE);
        for (Map.Entry entry : predictNumbers.entrySet()) {
            Map<String, Object> predict = new HashMap<>();
            predict.put("period", String.valueOf(entry.getKey()) + "期");
            predict.put("predictNumber", entry.getValue());
            GamePeriod period = PeriodRedis.getPeriodByGameIdAndPeriodDb(gameId, String.valueOf(entry.getKey()));
            predict.put("winningNumber", StringUtils.isNotBlank(period.getWinningNumbers()) ? period.getWinningNumbers
                    () : "--");
            Integer count = 0;
            for (String num : String.valueOf(entry.getValue()).split(CommonConstant.SPACE_SPLIT_STR)) {
                if (!num.contains(CommonConstant.COMMON_STAR_STR)) {
                    count += 1;
                }
            }
            predict.put("count", count);
            predictList.add(predict);
        }
        resultMap.put("positionKill", predictList);
        resultMap.put("showText", PredictUtil.getShowTextCal(GameCache.getGame(gameId).getGameEn(), PredictConstant
                .POSITION_KILL_CODE_EXPIRE_MSG, PredictConstant.POSITION_KILL_CODE_MSG));
        return resultMap;
    }

    @Override
    public Map<String, Object> threeDanCode(Long gameId) {
        Map<String, Object> resultMap = new HashMap<>();
        List<Object> predictList = new ArrayList<>();
        Map<String, String> predictNumbers = getPredictNumber(gameId, PredictConstant.THREE_KILL_CODE);
        for (Map.Entry entry : predictNumbers.entrySet()) {
            Map<String, Object> predict = new HashMap<>();
            predict.put("period", String.valueOf(entry.getKey()) + "期");
            predict.put("predictNumber", entry.getValue());
            GamePeriod period = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, String.valueOf(entry.getKey()));
            predict.put("winningNumber", StringUtils.isNotBlank(period.getWinningNumbers()) ? period.getWinningNumbers
                    () : "--");
            Integer count = 0;
            for (String num : String.valueOf(entry.getValue()).split(CommonConstant.SPACE_SPLIT_STR)) {
                if (num.contains(CommonConstant.COMMON_STAR_STR)) {
                    count += 1;
                }
            }
            predict.put("count", count);
            predictList.add(predict);
        }
        resultMap.put("threeDanCode", predictList);
        resultMap.put("showText", PredictUtil.getShowTextCal(GameCache.getGame(gameId).getGameEn(), PredictConstant
                .THREE_DAN_CODE_EXPIRE_MSG, PredictConstant.THREE_DAN_CODE_MSG));
        return resultMap;
    }

    private Map<String, String> getPredictNumber(Long gameId, Integer predictType) {
        String redisKey = RedisConstant.getPredictTypeKey(GameCache.getGame(gameId).getGameEn(), predictType);
        Map<String, String> predictNumbers = redisService.kryoGet(redisKey, TreeMap.class);
        if (null == predictNumbers) {
            predictNumbers = new TreeMap<>(Comparator.reverseOrder());
            List<PredictRedBall> predictRedBallList = predictRedBallDao.getPredictRedBalls(GameCache.getGame
                    (gameId).getGameId(), predictType, 100);

            for (PredictRedBall predictRedBall : predictRedBallList) {
                predictNumbers.put(predictRedBall.getPeriodId(), predictRedBall.getNumStr());
            }

            /* 设置缓存*/
            redisService.kryoSet(redisKey, predictNumbers);
            /* 保持缓存可以刷新*/
            GamePeriod period = PeriodRedis.getCurrentPeriod(GameCache.getGame(gameId).getGameId());
            redisService.expire(redisKey, (int) DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(),
                    DateUtil.getIntervalSeconds(period.getAwardTime(), 3600)));
        }
        return predictNumbers;
    }
}

