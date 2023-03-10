package com.mojieai.predict.service.impl;

import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.cache.SocialLevelIntegralCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.*;
import com.mojieai.predict.entity.bo.GoldTask;
import com.mojieai.predict.entity.bo.SocialKillNumFilter;
import com.mojieai.predict.entity.bo.UserEncircleInfo;
import com.mojieai.predict.entity.po.*;
import com.mojieai.predict.entity.vo.*;
import com.mojieai.predict.enums.CronEnum;
import com.mojieai.predict.enums.GoldCoinTaskEnum;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.*;
import com.mojieai.predict.service.beanself.BeanSelfAware;
import com.mojieai.predict.thread.SocialTaskAwardTask;
import com.mojieai.predict.thread.StatisticTask;
import com.mojieai.predict.thread.ThreadPool;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.SocialEncircleKillCodeUtil;
import com.mojieai.predict.util.TrendUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutorService;

@Service
public class SocialEncircleCodeServiceImpl implements SocialEncircleCodeService, BeanSelfAware {
    private static final Logger log = CronEnum.PERIOD.getLogger();

    @Autowired
    private SocialEncircleCodeDao socialEncircleCodeDao;
    @Autowired
    private EncircleCodeIdSequenceDao encircleCodeSequenceDao;
    @Autowired
    private IndexUserSocialCodeDao indexUserSocialCodeDao;
    @Autowired
    private LoginService loginService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private SocialKillCodeDao socialKillCodeDao;
    @Autowired
    private SocialService socialService;
    @Autowired
    private SocialEncircleCodeService socialEncircleCodeService;
    @Autowired
    private SocialStatisticService socialStatisticService;
    @Autowired
    private VipMemberService vipMemberService;
    @Autowired
    private UserSocialTaskAwardService userSocialTaskAwardService;
    @Autowired
    private UserSocialTaskAwardDao userSocialTaskAwardDao;
    @Autowired
    private SocialKillCodeService socialKillCodeService;
    @Autowired
    private UserTitleService userTitleService;
    @Autowired
    private UserSocialRecordService userSocialRecordService;
    @Autowired
    private UserSocialIntegralService userSocialIntegralService;
    @Autowired
    private SocialResonanceService socialResonanceService;

    @Autowired
    private PredictNumService predictNumService;
    private SocialEncircleCodeService self;


    @Override
    public Long generateEncircleCodeId() {
        String timePrefix = DateUtil.formatDate(new Date(), DateUtil.DATE_FORMAT_YYMMDDHH);
        long seq = encircleCodeSequenceDao.getEncircleCodeIdSequence();
        Long encircleCodeId = Long.parseLong(timePrefix + CommonUtil.formatSequence(seq));
        return encircleCodeId;
    }

    @Override
    public Map<String, Object> addEncircleCode(long gameId, String periodId, long userId, String encircleNums,
                                               Integer encircleCount, String killCounts, String versionCode,
                                               String clientIp, Integer clientId) {
        Map<String, Object> result = new HashMap<>();
        //1.??????Social
        SocialEncircle socialEncircle = packageEncirclePo(gameId, periodId, encircleNums, encircleCount, killCounts,
                userId);
        //2.??????redisList
        String encircleTempListKey = RedisConstant.getSocialUserEncircleTempListKey(gameId);
        Long insertRedisRes = redisService.kryoRPush(encircleTempListKey, socialEncircle);
        //3.???????????????
        if (insertRedisRes == null || insertRedisRes <= 0L) {
            log.error(encircleTempListKey + "??????push???tempList");
            result.put("successFlag", SocialEncircleKillConstant.SOCIAL_ADD_ENCIRCLE_ERROR_FLAG);
            result.put("title", SocialEncircleKillConstant.SOCIAL_ADD_ENCIRCLE_ERROR_TITLE);
            result.put("msg", SocialEncircleKillConstant.SOCIAL_ADD_ENCIRCLE_MSG_REENCIRCLE_ERR);
            return result;
        }
        //4.check ?????????????????????????????????
        Integer taskType = Integer.valueOf(GoldTask.TASK_TYPE_SOCIAL_ENCIRCLE_SSQ);
        if (GameCache.getGame(gameId).getGameEn().equals(GameConstant.DLT)) {
            taskType = Integer.valueOf(GoldTask.TASK_TYPE_SOCIAL_ENCIRCLE_DLT);
        }
        UserSocialTaskAward taskAward = userSocialTaskAwardService.initUserSocialTask(gameId, periodId, userId,
                taskType);
        int insertRes = self.addEncircleNumAndIndexUserSocial(socialEncircle, taskAward.getTaskId());
        if (insertRes > 0) {
            //3.??????????????????
            String isDistributeFlag = RedisConstant.getUserDistributeFlag(gameId, periodId);
            if (null == redisService.kryoZRank(isDistributeFlag, userId)) {
                redisService.kryoZAddSet(isDistributeFlag, System.currentTimeMillis(), userId);
                GamePeriod nextPeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(gameId, periodId);
                int expireSeconds = (int) DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(), nextPeriod
                        .getAwardTime()) + (60 * 60 * 24);
                redisService.expire(isDistributeFlag, expireSeconds);
                predictNumService.updateUserPredictMaxNums(gameId, nextPeriod.getPeriodId(), userId, 1);
            }
            Map<String, Integer> socialKillAwardLevel = socialService.getAwardLevelMap(gameId, CommonConstant
                    .RED_BALL_TYPE, CommonConstant.SOCIAL_CODE_TYPE_KILL);
            //4.??????????????????????????????
            saveUserEncircleInfo2Redis(socialEncircle, socialKillAwardLevel);
            result.put("successFlag", SocialEncircleKillConstant.SOCIAL_ADD_ENCIRCLE_SUCC_FLAG);
            if (Integer.valueOf(versionCode) < CommonConstant.VERSION_CODE_2_3) {
                result.put("title", SocialEncircleKillConstant.SOCIAL_ADD_ENCIRCLE_SUCC_TITLE);
                result.put("msg", SocialEncircleKillConstant.SOCIAL_ADD_ENCIRCLE_MSG_SUCC);
            } else {
                result.put("msg", SocialEncircleKillConstant.SOCIAL_ADD_ENCIRCLE_SUCC_TITLE);
            }
            //5.?????????????????????
            notifyRobotKillNum(socialEncircle);
            //6.?????????????????????????????????
            try {
                ExecutorService exec = ThreadPool.getInstance().getStatisticSocialExec();
                StatisticTask task = new StatisticTask(SocialEncircleKillConstant
                        .SOCIAL_BIG_DATA_HOT_ENCIRCLE_NUMBERS, socialStatisticService, socialEncircle, null);
                exec.submit(task);
            } catch (Exception e) {
                log.warn(socialEncircle.getEncircleCodeId() + "?????????????????????????????????", e);
            }
            //7.????????????????????????
            ExecutorService taskExec = ThreadPool.getInstance().getUserSocialTaskExec();
            SocialTaskAwardTask task = new SocialTaskAwardTask(gameId, periodId, userId, taskType, clientIp, clientId,
                    userSocialTaskAwardService);
            taskExec.submit(task);
            //8.????????????
            socialResonanceService.livingBuildEncircleResonance(socialEncircle);
        }

        return result;
    }

    @Override
    public Map<String, Object> getMyEncircle(long gameId, long userId, Integer page, Integer socialCodeType, Integer
            lastIndexId) {
        Boolean hasNext = Boolean.FALSE;
        Map<String, Object> result = new HashMap<>();
        Set<EncirclePeriodsVo> encirclePeriodsVos = new TreeSet<>();

        List<IndexUserSocialCode> indexUserSocialCodes = indexUserSocialCodeDao.getUserPartTakePeriodId
                (SocialEncircleKillConstant.SOCIAL_MY_KILL_NUM_LIST_PERIOD_SIZE, socialCodeType, userId,
                        lastIndexId);

//        PaginationList<IndexUserSocialCode> indexUserSocialCodePage = indexUserSocialCodeDao
//                .getIndexUserSocialCodeByGameIdAndUserIdByPage(gameId, userId, page, socialCodeType,
//                        SocialEncircleKillConstant.SOCIAL_MY_KILL_NUM_LIST_PAGE_SIZE);
        Integer lastIndex = 0;
        if (indexUserSocialCodes != null) {
            GamePeriod currentPeriod = PeriodRedis.getCurrentPeriod(gameId);
            Map<String, EncirclePeriodsVo> tempResult = packageEncirclePeriodsVo(gameId, currentPeriod,
                    indexUserSocialCodes, userId, socialCodeType);
            //2.??????????????????
            for (EncirclePeriodsVo tempEntry : tempResult.values()) {
                encirclePeriodsVos.add(tempEntry);
            }
            if (indexUserSocialCodes.size() >= SocialEncircleKillConstant.SOCIAL_MY_KILL_NUM_LIST_PERIOD_SIZE) {
                hasNext = true;
                lastIndex = indexUserSocialCodes.get(indexUserSocialCodes.size() - 2).getIndexId();
            }
        }
        result.put("page", page);
        result.put("hasNext", hasNext);
        result.put("datas", encirclePeriodsVos);
        result.put("lastIndex", lastIndex);
        return result;
    }

    @Override
    public Map<String, Object> getEncircleIndex(long gameId, Integer encircleType, Long userId) {
        String errorMsg = "";
        Integer enableEncircleFalg = 1;
        Map<String, Object> result = new HashMap<>();
        String lastAwardPeriodKey = RedisConstant.getDisTributeFlag(gameId);
        String lastOpenPeriodId = redisService.kryoGet(lastAwardPeriodKey, String.class);
        GamePeriod gamePeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(gameId, lastOpenPeriodId);
        if (gamePeriod == null) {
            log.error(lastAwardPeriodKey + "????????????????????????");
            gamePeriod = PeriodRedis.getCurrentPeriod(gameId);
        }
        List<EncircleVo> encircleNums = SocialEncircleKillCodeUtil.getEncircleRule(gameId, encircleType);
        Integer periodStatus = getPeriodEncircleStatus(gameId, gamePeriod.getPeriodId());
        if (periodStatus != SocialEncircleKillConstant.SOCIAL_ENCIRCLE_STATUS_ENABLE) {
            enableEncircleFalg = 0;
            errorMsg = SocialEncircleKillConstant.SOCIAL_ADD_ENCIRCLE_MSG_PERIOD_ERR;
        }
        String userEncircleCountKey = RedisConstant.getEncircleTimesKey(gameId, gamePeriod.getPeriodId(),
                encircleType, userId);

        String encircleTimes = redisService.get(userEncircleCountKey);
        if (CommonUtil.isNumeric(encircleTimes) && Long.valueOf(encircleTimes) >= SocialEncircleKillConstant
                .SOCIAL_ENCIRCLE_MAX_COUNT) {
            enableEncircleFalg = 0;
            errorMsg = SocialEncircleKillConstant.SOCIAL_ADD_ENCIRCLE_MSG_PERIOD_UESED_ERR;
        }

        result.put("periodId", gamePeriod.getPeriodId());
        result.put("periodName", GameConstant.PERIOD_NAME_MAP.get(GameCache.getGame(gameId).getGameEn()) + gamePeriod
                .getPeriodId() + "???");
        result.put("totalEncircleTimes", 2);
        result.put("encircleTimes", encircleTimes);
        result.put("encircleFlag", enableEncircleFalg);
        result.put("errorMsg", errorMsg);
        result.put("encircleInfo", "*??????????????????????????????2???");
        result.put("encircleNums", encircleNums);
        result.put("encircleAdMsg", "???????????????????????????");

        return result;
    }

    /**
     * ????????????(2.1???????????????)
     * <p>
     * ????????????3???
     *
     * @param gameId
     * @param page
     * @param partakeCounts
     * @param encircleCounts
     * @param killNumCount
     * @param killNumUserId
     * @return
     */
    @Override
    public Map<String, Object> getKillNumList(long gameId, Integer page, Integer partakeCounts, String
            encircleCounts, String killNumCount, Long killNumUserId, String encircleListType) {
        Boolean hasNext = Boolean.FALSE;
        Map<String, Object> result = new HashMap<>();
        Set<EncirclePeriodsVo> newEncirclePeriodsVos = new TreeSet<>();

        //??????????????????
        SocialKillNumFilter socialKillNumFilter = new SocialKillNumFilter();
        socialKillNumFilter.setEncircleCount(encircleCounts);
        socialKillNumFilter.setKillNumCount(killNumCount);
        socialKillNumFilter.setPartakeCount(partakeCounts);

        //1.?????????????????????
        newEncirclePeriodsVos = getKillNumListByRedis(gameId, page, SocialEncircleKillConstant
                .SOCIAL_KILL_NUM_LIST_PAGE_SIZE, killNumUserId, socialKillNumFilter);
        //2.??????
        int totalCount = 0;
        for (EncirclePeriodsVo encirclePeriodsVo : newEncirclePeriodsVos) {
            String key = RedisConstant.getPeriodEncircleListKey(gameId, encirclePeriodsVo.getPeriodId(),
                    SocialEncircleKillConstant.SOCIAL_OPERATE_NUM_ENCIRCLE_RED);
            totalCount = totalCount + redisService.kryoZCount(key, Long.MIN_VALUE, Long.MAX_VALUE).intValue();
        }
        if (socialKillNumFilter.getFilterNoDataFlag() == 0 && totalCount > page * SocialEncircleKillConstant
                .SOCIAL_KILL_NUM_LIST_PAGE_SIZE) {
            hasNext = Boolean.TRUE;
        }

        result.put("page", page);
        result.put("hasNext", hasNext);
        result.put("datas", newEncirclePeriodsVos);
        result.put("filterNoDataFlag", socialKillNumFilter.getFilterNoDataFlag());
        return result;
    }

    @Override
    public Map<String, Object> getKillNumListByPeriodId(long gameId, Integer lastIndex, String periodId, Long userId,
                                                        SocialKillNumFilter socialKillNumFilter, String versionCode) {
        //??????????????????id??????????????????
        if (StringUtils.isBlank(periodId)) {
            GamePeriod gamePeriod = PeriodRedis.getCurrentPeriod(gameId);
            Integer periodStatus = getPeriodEncircleStatus(gameId, gamePeriod.getPeriodId());
            if (periodStatus == SocialEncircleKillConstant.SOCIAL_ENCIRCLE_STATUS_FUTURE) {
                gamePeriod = PeriodRedis.getLastPeriodByGameIdAndPeriodId(gameId, gamePeriod.getPeriodId());
            }
            periodId = gamePeriod.getPeriodId();
        }
        //
        if (lastIndex == null) {
            lastIndex = 0;
        }

        Map<String, Object> killNumList = getKillNumListFromRedisByPeriodId(gameId, periodId, lastIndex,
                SocialEncircleKillConstant.SOCIAL_KILL_NUM_LIST_PAGE_SIZE, userId, socialKillNumFilter, versionCode);
        return killNumList;
    }

    //???????????????????????????????????????
    @Override
    public void checkSocialKillNumList() {
        for (Game game : GameCache.getAllGameMap().values()) {
            if (game.getGameType() == Game.GAME_TYPE_COMMON) {
                //??????????????????????????? ???????????????
                /*if (game.getGameEn().equals(GameConstant.DLT)) {
                    continue;
                }*/
                String userEncircleTempList = RedisConstant.getSocialUserEncircleTempListKey(game.getGameId());
                if (redisService.llen(userEncircleTempList) == 0L) {
                    continue;
                }

                Map<String, Integer> socialAwardLevel = socialService.getAwardLevelMap(game.getGameId(),
                        CommonConstant
                                .RED_BALL_TYPE, CommonConstant.SOCIAL_CODE_TYPE_KILL);

                while (redisService.llen(userEncircleTempList) > 0L) {
                    try {
                        SocialEncircle socialEncircle = redisService.kryoLindex(userEncircleTempList, 0,
                                SocialEncircle
                                        .class);
                        String socialEncircleListPeriodKey = RedisConstant.getPeriodEncircleListKey(socialEncircle
                                .getGameId(), socialEncircle.getPeriodId(), SocialEncircleKillConstant
                                .SOCIAL_OPERATE_NUM_ENCIRCLE_RED);
                        //1.???????????????check
                        SocialEncircle socialEncircleDb = socialEncircleCodeDao.getSocialEncircleByEncircleId
                                (socialEncircle.getGameId(), socialEncircle.getPeriodId(), socialEncircle
                                        .getEncircleCodeId());
                        if (socialEncircleDb == null) {
                            redisService.kryoLPop(userEncircleTempList, SocialEncircle.class);
                            break;
                        }
                        //2.????????????
                        MyEncircleVo myEncircleVo = packageMyEncircleVo(socialEncircleDb, socialAwardLevel);
                        boolean res = redisService.kryoZAddSet(socialEncircleListPeriodKey, socialEncircleDb
                                .getCreateTime().getTime(), myEncircleVo);
                        if (res) {
                            redisService.kryoLPop(userEncircleTempList, SocialEncircle.class);
                        }
                        //??????????????????
                        if (redisService.ttl(socialEncircleListPeriodKey) == -1L) {
                            redisService.expire(socialEncircleListPeriodKey, 2592000);//??????????????????30???
                        }
                    } catch (Exception e) {
                        log.error("?????????????????????????????????", e);
                        break;
                    }
                }
            }
        }
    }

    //???????????????????????????????????????
    @Override
    public void updateKilledEncircleList() {
        //????????????
        for (Game game : GameCache.getAllGameMap().values()) {
            if (game.getGameType() == Game.GAME_TYPE_COMMON) {
                String userKillTempList = RedisConstant.getSocialUserKillTempListKey(game.getGameId());
                if (redisService.llen(userKillTempList) == 0L) {
                    continue;
                }
                long timeBegin = System.currentTimeMillis();
                String periodId = "";

                Map<String, Integer> socialAwardLevel = socialService.getAwardLevelMap(game.getGameId(),
                        CommonConstant
                                .RED_BALL_TYPE, CommonConstant.SOCIAL_CODE_TYPE_KILL);

                int count = 0;
                while (redisService.llen(userKillTempList) > 0L) {
                    count++;
                    try {
                        SocialEncircle socialEncircle = redisService.kryoLindex(userKillTempList, 0, SocialEncircle
                                .class);
                        periodId = socialEncircle.getPeriodId();
                        String socialEncircleListPeriodKey = RedisConstant.getPeriodEncircleListKey(socialEncircle
                                .getGameId(), socialEncircle.getPeriodId(), SocialEncircleKillConstant
                                .SOCIAL_OPERATE_NUM_ENCIRCLE_RED);
                        //1.???????????????check
                        SocialEncircle socialEncircleDb = socialEncircleCodeDao.getSocialEncircleByEncircleId
                                (socialEncircle.getGameId(), socialEncircle.getPeriodId(), socialEncircle
                                        .getEncircleCodeId());
                        if (socialEncircleDb == null) {
                            redisService.kryoLPop(userKillTempList, SocialEncircle.class);
                            break;
                        }
                        //2.????????????
                        MyEncircleVo myEncircleVo = packageMyEncircleVo(socialEncircleDb, socialAwardLevel);

                        List<MyEncircleVo> myEncircleVos = redisService.kryoZRevRangeByScoreGet
                                (socialEncircleListPeriodKey, socialEncircleDb.getCreateTime().getTime() - 3L,
                                        socialEncircleDb.getCreateTime().getTime() + 3L, MyEncircleVo.class);
                        if (myEncircleVo == null || myEncircleVos.size() == 0) {
                            redisService.kryoLPop(userKillTempList, SocialEncircle.class);
                            continue;
                        }
                        for (MyEncircleVo myEncircleVo1 : myEncircleVos) {
                            if (myEncircleVo1.getEncircleCodeId().equals(socialEncircleDb.getEncircleCodeId() +
                                    "")) {
                                redisService.kryoZRem(socialEncircleListPeriodKey, myEncircleVo1);
                                boolean res = redisService.kryoZAddSet(socialEncircleListPeriodKey, socialEncircleDb
                                        .getCreateTime().getTime(), myEncircleVo);
                                if (res) {
                                    redisService.kryoLPop(userKillTempList, SocialEncircle.class);
                                    count = 0;
                                }
                                break;
                            }
                        }
                        if (count > 100) {
                            log.error("update killNum list over times 100 please check");
                        }
                        //3.????????????userEncircleInfo
                        saveUserEncircleInfo2Redis(socialEncircle, socialAwardLevel);
                    } catch (Exception e) {
                        log.error("?????????????????????????????????", e);
                        break;
                    }
                }
                long timeEnd = System.currentTimeMillis();
                //???????????? todo  ??????????????????
                if (StringUtils.isNotBlank(periodId)) {
                    socialEncircleCodeService.rebuildPeriodHotEncircle(game.getGameId(), periodId);
                    periodId = "";
                }
                long timeHot = System.currentTimeMillis();
            }
        }
    }

    @Override
    public Integer getPeriodEncircleStatus(long gameId, String periodId) {
        String lastAwardPeriodKey = RedisConstant.getDisTributeFlag(gameId);
        String lastOpenPeriodId = redisService.kryoGet(lastAwardPeriodKey, String.class);
        if (StringUtils.isBlank(lastOpenPeriodId)) {
            log.error("??????????????????????????????flag??????????????????.key:" + lastAwardPeriodKey);
            return SocialEncircleKillConstant.SOCIAL_ENCIRCLE_STATUS_END;
        }
        if (Integer.valueOf(periodId) <= Integer.valueOf(lastOpenPeriodId)) {
            return SocialEncircleKillConstant.SOCIAL_ENCIRCLE_STATUS_END;
        }
        GamePeriod currentPeriod = PeriodRedis.getCurrentPeriod(gameId);
        if (periodId.equals(currentPeriod.getPeriodId())) {
            GamePeriod lastPeriod = PeriodRedis.getLastPeriodByGameIdAndPeriodId(gameId, currentPeriod.getPeriodId());
            if (lastPeriod.getPeriodId().equals(lastOpenPeriodId)) {
                return SocialEncircleKillConstant.SOCIAL_ENCIRCLE_STATUS_ENABLE;
            }
            return SocialEncircleKillConstant.SOCIAL_ENCIRCLE_STATUS_FUTURE;

        } else {
            return SocialEncircleKillConstant.SOCIAL_ENCIRCLE_STATUS_OPEN_AWARD;
        }
    }

    //??????????????????????????????????????????????????????
    @Override
    public void updateKillNumList(long gameId, String periodId) {
        String[] winArr = null;
        try {
            String userPeriodKillNumListKey = RedisConstant.getPeriodEncircleListKey(gameId, periodId,
                    SocialEncircleKillConstant.SOCIAL_OPERATE_NUM_ENCIRCLE_RED);

            List<MyEncircleVo> myEncircleVos = redisService.kryoZRevRange(userPeriodKillNumListKey, 0, Long
                    .MAX_VALUE, MyEncircleVo.class);
            GamePeriod gamePeriod = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, periodId);
            if (StringUtils.isNotBlank(gamePeriod.getWinningNumbers())) {
                winArr = gamePeriod.getWinningNumbers().split(CommonConstant.COMMON_COLON_STR)[0].split
                        (CommonConstant.SPACE_SPLIT_STR);
            }

            //??????????????????zscan?????????
            if (myEncircleVos != null && myEncircleVos.size() > 0) {
                for (MyEncircleVo myEncircleVo : myEncircleVos) {
                    Long res = redisService.kryoZRem(userPeriodKillNumListKey, myEncircleVo);
                    if (res > 0L) {
                        calculateMyEncircleVo(myEncircleVo, userPeriodKillNumListKey, winArr);
                    }
                }
            }

        } catch (Exception e) {
            log.error("????????????????????????gameId:" + gameId + "periodId:" + periodId, e);
        }
    }

    //??????????????????true

    private boolean filterMyEncircleVo(MyEncircleVo myEncircleVo, SocialKillNumFilter socialKillNumFilter) {

        if (socialKillNumFilter == null) {
            return true;
        }

        if (socialKillNumFilter.getEncircleCount() != null) {
            String[] allEncircle = SocialEncircleKillCodeUtil.removeStrOfNumArr(socialKillNumFilter.getEncircleCount()
                    .split(CommonConstant.COMMA_SPLIT_STR));
            if (allEncircle == null || allEncircle.length <= 0) {
                return true;
            }
            List<String> allEncircleList = Arrays.asList(allEncircle);
            List<String> partEncircleList = Arrays.asList(new String[]{myEncircleVo.getEncircleCount() + ""});
            List<String> fileterEncircleList = new ArrayList<>();
            List<String> requireKillList = new ArrayList<>();
            fileterEncircleList.addAll(allEncircleList);
            requireKillList.addAll(partEncircleList);
            fileterEncircleList.retainAll(requireKillList);
            if (fileterEncircleList == null || fileterEncircleList.size() == 0) {
                return false;
            }
        }
        if (socialKillNumFilter.getKillNumCount() != null) {
            String[] filterArr = SocialEncircleKillCodeUtil.removeStrOfNumArr(socialKillNumFilter.getKillNumCount()
                    .split(CommonConstant.COMMA_SPLIT_STR));
            String[] requireKillArr = SocialEncircleKillCodeUtil.removeStrOfNumArr(myEncircleVo.getEncircleKillCount()
                    .split(CommonConstant.COMMA_SPLIT_STR));
            if (filterArr == null || filterArr.length == 0) {
                return true;
            }
            List<String> fileterKillListTemp = Arrays.asList(filterArr);
            List<String> requireKillListTemp = Arrays.asList(requireKillArr);
            List<String> fileterKillList = new ArrayList<>();
            List<String> requireKillList = new ArrayList<>();
            fileterKillList.addAll(fileterKillListTemp);
            requireKillList.addAll(requireKillListTemp);
            fileterKillList.retainAll(requireKillList);
            if (fileterKillList == null || fileterKillList.size() == 0) {
                return false;
            }
        }
        if (socialKillNumFilter.getPartakeCount() != null && socialKillNumFilter.getPartakeCount() != myEncircleVo
                .getPartakeCount()) {
            return false;
        }
        return true;
    }

    private Map<String, EncirclePeriodsVo> packageEncirclePeriodsVo(long gameId, GamePeriod
            currentPeriod, List<IndexUserSocialCode> indexUserSocialCodeList, Long killNumUserId, Integer
                                                                            socialCodeType) {
        Map<String, EncirclePeriodsVo> tempResult = new HashMap<>();

        Map<String, Integer> socialEncircleAwardLevel = socialService.getAwardLevelMap(gameId, CommonConstant
                .RED_BALL_TYPE, CommonConstant.SOCIAL_CODE_TYPE_ENCIRCLE);
        Map<String, Integer> socialKillAwardLevel = socialService.getAwardLevelMap(gameId, CommonConstant
                .RED_BALL_TYPE, CommonConstant.SOCIAL_CODE_TYPE_KILL);

        //????????????????????????????????????????????????????????????????????????????????????????????????
        int endIndex = indexUserSocialCodeList.size();
        if (indexUserSocialCodeList.size() >= SocialEncircleKillConstant.SOCIAL_MY_KILL_NUM_LIST_PERIOD_SIZE) {
            endIndex = indexUserSocialCodeList.size() - 1;
        }
        for (int i = 0; i < endIndex; i++) {
            IndexUserSocialCode temp = indexUserSocialCodeList.get(i);
            if (!tempResult.containsKey(temp.getPeriodId())) {
                EncirclePeriodsVo encirclePeriodsVo = new EncirclePeriodsVo();
                tempResult.put(temp.getPeriodId(), encirclePeriodsVo);
                encirclePeriodsVo.setGameId(gameId);
                encirclePeriodsVo.setPeriodId(temp.getPeriodId());
                encirclePeriodsVo.setPeriodName(GameConstant.PERIOD_NAME_MAP.get(GameCache.getGame(gameId).getGameEn
                        ()) + temp.getPeriodId() + "???");
                encirclePeriodsVo.setAdMsg("???????????????????????????????????????");
                encirclePeriodsVo.setFilterEncircleBtnStatus(1);
                encirclePeriodsVo.setFilterEncircleBtnAdMsg("??????");
                encirclePeriodsVo.setLeadEncircleAdMsg("????????????,");
                encirclePeriodsVo.setLeadEncircleBackAdMsg("");
            }
            EncirclePeriodsVo encirclePeriodsVo = tempResult.get(temp.getPeriodId());
            List<SocialEncircle> socialEncircles = new ArrayList<>();
            if (socialCodeType == SocialEncircleKillConstant.SOCIAL_OPERATE_NUM_KILL_RED) {
                List<SocialKillCode> socialKillCodes = socialKillCodeDao.getKillNumsByCondition(gameId, temp
                        .getPeriodId(), null, killNumUserId);
                for (SocialKillCode socialKillCode : socialKillCodes) {
                    SocialEncircle socialEncircle = socialEncircleCodeDao.getSocialEncircleByEncircleId(gameId, temp
                            .getPeriodId(), socialKillCode.getEncircleCodeId());
                    if (socialEncircle != null) {
                        socialEncircles.add(socialEncircle);
                    }
                }
            } else {
                socialEncircles = socialEncircleCodeDao.getSocialEncircleByCondition(gameId, temp.getPeriodId(), null,
                        killNumUserId, SocialEncircleKillConstant.SOCIAL_OPERATE_NUM_ENCIRCLE_RED, null);
            }

            for (SocialEncircle socialEncircle : socialEncircles) {
                MyEncircleVo myEncircleVo = new MyEncircleVo();
                myEncircleVo.setEncircleCodeId(socialEncircle.getEncircleCodeId() + "");
                myEncircleVo.setEncircleCount(socialEncircle.getEncircleNums());
                myEncircleVo.setEncircleName("?????????" + socialEncircle.getEncircleNums() + "???");
                myEncircleVo.setEncircleTimeBak(DateUtil.formatTime(socialEncircle.getCreateTime()));
                myEncircleVo.setEncircleTime(SocialEncircleKillCodeUtil.getEncircleTimeShow(myEncircleVo
                        .getEncircleTimeBak()));
                int partakeCount = socialEncircle.getFollowKillNums() == null ? 0 : socialEncircle.getFollowKillNums();
                myEncircleVo.setPartakeCount(partakeCount);
                myEncircleVo.setEncircleNum(socialEncircle.getUserEncircleCode());
                myEncircleVo.setPeriodId(socialEncircle.getPeriodId());
                myEncircleVo.setEncircleDesc("????????????" + socialEncircle.getEncircleNums() + "??????????????????" + socialEncircle
                        .getKillNums() + "??????");
                myEncircleVo.setEncircleKillCount(socialEncircle.getKillNums());
                UserLoginVo userLoginVo = loginService.getUserLoginVo(socialEncircle.getUserId());
                myEncircleVo.setEncircleUserName(userLoginVo.getNickName());
                myEncircleVo.setEncircleHeadImg(userLoginVo.getHeadImgUrl());
                myEncircleVo.setEncircleUserId(userLoginVo.getUserId());
                myEncircleVo.setKillNumBtnAdMsg("?????????");
                //??????????????????
                Integer maxKill = SocialEncircleKillCodeUtil.getMaxKillNumLevel(socialEncircle.getKillNums());
                Integer killNumAward = SocialEncircleKillCodeUtil.getScoreByRightCount(maxKill, maxKill,
                        socialKillAwardLevel);
                myEncircleVo.setKillNumAwardAdMsg("???????????????" + killNumAward + "??????");
                //??????????????????
                String encirleRightAwardInfo = "";
                Integer rightAward = 0;
                if (socialEncircle.getRightNums() != null) {
                    rightAward = SocialEncircleKillCodeUtil.getEncircleAwardLevelByRightCount(socialEncircle
                            .getEncircleNums(), socialEncircle.getRightNums(), socialEncircleAwardLevel);
                }
                if (socialEncircle.getIsDistribute() != null && socialEncircle.getIsDistribute() == 1) {
                    encirleRightAwardInfo = "??????" + socialEncircle.getRightNums() + "??????" + rightAward + "??????";
                }

                myEncircleVo.setEncircleAwardAdMsg(encirleRightAwardInfo);

                //????????????????????????
                int killAwardStatus = 0;
                Integer encircleStatus = getPeriodEncircleStatus(socialEncircle.getGameId(), socialEncircle
                        .getPeriodId());
                SocialKillCode socialKillCode = socialKillCodeDao.getKillNumsByEncircleIdAndUserId(gameId,
                        myEncircleVo.getPeriodId(), Long.valueOf(myEncircleVo.getEncircleCodeId()), killNumUserId);
                if (encircleStatus == SocialEncircleKillConstant.SOCIAL_ENCIRCLE_STATUS_ENABLE) {
                    killAwardStatus = SocialEncircleKillConstant.SOCIAL_KILL_NUM_AWARD_STATUS_WAIT_OPEN;
                    if (socialKillCode != null) {
                        myEncircleVo.setKillNumBtnAdMsg("?????????");
                    }
                    myEncircleVo.setMyKillNumAwardFrontAdMsg("???????????????");
                    myEncircleVo.setMyKillNumAwardBackAdMsg(killNumAward + "??????");
                } else if (encircleStatus == SocialEncircleKillConstant.SOCIAL_ENCIRCLE_STATUS_OPEN_AWARD) {
                    killAwardStatus = SocialEncircleKillConstant.SOCIAL_KILL_NUM_AWARD_STATUS_WAIT_OPEN;
                    myEncircleVo.setKillNumBtnAdMsg("?????????");
                    myEncircleVo.setMyKillNumAwardFrontAdMsg("???????????????");
                    myEncircleVo.setMyKillNumAwardBackAdMsg(killNumAward + "??????");
                } else {
                    if (killNumUserId != null) {
                        if (socialKillCode.getRightNums() != null && socialEncircle.getKillNums().contains
                                (socialKillCode.getRightNums() + "")) {
                            killAwardStatus = SocialEncircleKillConstant.SOCIAL_KILL_NUM_AWARD_STATUS_WIN;
                        } else {
                            killAwardStatus = SocialEncircleKillConstant.SOCIAL_KILL_NUM_AWARD_STATUS_LOSS;
                        }
                    }

                    Integer rightKillNumAward = socialKillCode.getUserAwardScore();
                    myEncircleVo.setMyKillNumAwardFrontAdMsg("???????????????");
                    myEncircleVo.setMyKillNumAwardBackAdMsg(rightKillNumAward + "??????");
                }
                myEncircleVo.setKillNumAwardStatus(killAwardStatus);
                encirclePeriodsVo.addMyEncircleVo(myEncircleVo);

            }
        }
        return tempResult;
    }

    @Transactional
    public Integer addEncircleNumAndIndexUserSocial(SocialEncircle socialEncircle, String taskId) {
        log.info("transaction begin save encircleNum" + socialEncircle.getUserId());
        Integer insertRes = 0;
        try {
            //1.??????????????????
            UserSocialTaskAward userSocialTaskAward = userSocialTaskAwardDao.getUserSocialTaskAwardById(taskId,
                    socialEncircle.getUserId(), true);
            //2.????????????
            socialEncircleCodeDao.insert(socialEncircle);
            IndexUserSocialCode indexUserSocialCode = new IndexUserSocialCode();
            indexUserSocialCode.setGameId(socialEncircle.getGameId());
            indexUserSocialCode.setPeriodId(socialEncircle.getPeriodId());
            indexUserSocialCode.setSocialCodeId(socialEncircle.getEncircleCodeId());
            indexUserSocialCode.setSocialCodeType(SocialEncircleKillConstant.SOCIAL_OPERATE_NUM_ENCIRCLE_RED);
            indexUserSocialCode.setUserId(socialEncircle.getUserId());
            indexUserSocialCode.setSocialCount(socialEncircle.getEncircleNums());
            //3.??????index??????
            insertRes = indexUserSocialCodeDao.insert(indexUserSocialCode);
            //4.??????????????????
            Integer maxTimes = null;
            Integer times = userSocialTaskAward.getTaskTimes() == null ? 0 : userSocialTaskAward.getTaskTimes();
            String taskType = GoldTask.TASK_TYPE_SOCIAL_ENCIRCLE_SSQ;
            if (GameCache.getGame(socialEncircle.getGameId()).getGameEn().equals(GameConstant.DLT)) {
                taskType = GoldTask.TASK_TYPE_SOCIAL_ENCIRCLE_DLT;
            }
            GoldCoinTaskEnum gcte = GoldCoinTaskEnum.getGoldCoinTaskEnumByType(taskType);
            if (gcte != null && gcte.getGoldTask() != null) {
                maxTimes = gcte.getGoldTask().getTaskTimes();
            }

            if (maxTimes != null) {
                if (times < maxTimes) {
                    times += 1;
                    Integer isAward = null;
                    if (times.equals(maxTimes)) {
                        isAward = SocialEncircleKillConstant.SOCIAL_TASK_IS_AWARD_WAIT;
                    }
                    userSocialTaskAwardDao.updateTaskTimesById(taskId, socialEncircle.getUserId(), times, isAward);
                } else if (times.equals(maxTimes)) {
                    userSocialTaskAwardDao.updateTaskIsAward(taskId, socialEncircle.getUserId(),
                            SocialEncircleKillConstant.SOCIAL_TASK_IS_AWARD_WAIT, SocialEncircleKillConstant
                                    .SOCIAL_TASK_IS_AWARD_INIT);
                }
            }
        } catch (Exception e) {
            log.error("????????????????????????,userId:" + socialEncircle.getUserId(), e);
            throw new BusinessException("????????????");
        } finally {
            return insertRes;
        }
    }

    @Override
    public SocialEncircle getSocialEncircleByEncircleId(long gameId, String periodId, Long encircleId) {
        return socialEncircleCodeDao.getSocialEncircleByEncircleId(gameId, periodId, encircleId);
    }

    @Override
    public void reCalculateIndexUserSocialRightNums(long gameId, String beginPeriod, String endPeriod) {
        Integer begin = Integer.valueOf(beginPeriod);
        Integer end = Integer.valueOf(endPeriod);
        while (begin <= end) {
            try {
                String periodId = begin + "";
                if (begin < 10001) {
                    periodId = "0" + begin;
                }
                List<SocialEncircle> socialEncircles = socialEncircleCodeDao.getSocialEncircleByCondition(gameId,
                        periodId, null, null, SocialEncircleKillConstant.SOCIAL_OPERATE_NUM_ENCIRCLE_RED, null);
                for (SocialEncircle temp : socialEncircles) {
                    indexUserSocialCodeDao.updateRightNums(gameId, temp.getUserId(), periodId, 0, temp
                            .getEncircleCodeId(), temp.getRightNums(), temp.getEncircleNums());
                }
                List<SocialKillCode> socialKillCodes = socialKillCodeDao.getKillNumsByCondition(gameId, periodId,
                        null, null);
                for (SocialKillCode temp : socialKillCodes) {
                    Integer awardNum = 0;
                    if (temp.getRightNums() == temp.getKillNums()) {
                        awardNum = 1;
                    }
                    indexUserSocialCodeDao.updateRightNums(gameId, temp.getUserId(), periodId, 1, temp
                            .getKillCodeId(), awardNum, temp.getKillNums());
                }
                GamePeriod gamePeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(gameId, periodId);
                begin = Integer.valueOf(gamePeriod.getPeriodId());
            } catch (Exception e) {
                continue;
            }
        }
    }

    @Override
    public Map<String, Object> getMyEncirclesV2_3(long gameId, Long userId, Long lookUpUserId, Integer lastIndex) {
        boolean isMe = false;
        boolean enHasNext = false;
        Map<String, Object> result = new HashMap<>();
        Integer enPeriodSizeEncircle = SocialEncircleKillConstant.SOCIAL_MY_KILL_NUM_LIST_PERIOD_SIZE;
        List<Map<String, Object>> encircles = new ArrayList<>();
        if (userId.equals(lookUpUserId)) {
            isMe = true;
        }
        if (!isMe) {
            lastIndex = null;
            enPeriodSizeEncircle = SocialEncircleKillConstant.SOCIAL_OTHER_KILL_NUM_LIST_PERIOD_SIZE;
        }

        //1.??????????????????
        List<IndexUserSocialCode> encircleIndexs = indexUserSocialCodeDao.getUserPartTakePeriodId(enPeriodSizeEncircle,
                SocialEncircleKillConstant.SOCIAL_OPERATE_NUM_ENCIRCLE_RED, userId, lastIndex);
        int countEn = 0;
        for (IndexUserSocialCode encircleIndex : encircleIndexs) {
            if (encircleIndexs.size() == enPeriodSizeEncircle && encircleIndexs.size() - 2 == countEn) {
                break;
            }
            List<SocialEncircle> socialEncircles = socialEncircleCodeDao.getSocialEncircleByCondition(gameId,
                    encircleIndex.getPeriodId(), null, userId, SocialEncircleKillConstant
                            .SOCIAL_OPERATE_NUM_ENCIRCLE_RED, null);
            Map<String, Object> myEncircleList = SocialEncircleKillCodeUtil.packageMyEncircleList(gameId,
                    socialEncircles);
            encircles.add(myEncircleList);
            countEn++;
        }

        if (encircleIndexs.size() == enPeriodSizeEncircle) {
            enHasNext = true;
        }
        if (encircleIndexs.size() > 0) {
            lastIndex = encircleIndexs.get(encircleIndexs.size() - 1).getIndexId();
            if (encircleIndexs.size() == SocialEncircleKillConstant.SOCIAL_MY_KILL_NUM_LIST_PERIOD_SIZE) {
                lastIndex = encircleIndexs.get(encircleIndexs.size() - 2).getIndexId();
            }
        }
        if (!isMe) {
            enHasNext = false;
        }

        result.put("encircles", encircles);
        result.put("hasNext", enHasNext);
        result.put("userId", userId);
        result.put("isMe", isMe);
        result.put("encircleLastIndex", lastIndex);
        return result;
    }

    @Override
    public List<UserEncircleInfo> getFollowsCurrentEncircleList(GamePeriod gamePeriod, List<SocialUserFollow>
            follows, Long userId) {
        if (follows == null || follows.size() <= 0) {
            return null;
        }
        Integer periodStatus = getPeriodEncircleStatus(gamePeriod.getGameId(), gamePeriod.getPeriodId());
        if (periodStatus != SocialEncircleKillConstant.SOCIAL_ENCIRCLE_STATUS_ENABLE) {
            return null;
        }
        List<UserEncircleInfo> result = new ArrayList<>();
        Map<String, Integer> socialKillAwardLevel = socialService.getAwardLevelMap(gamePeriod.getGameId(),
                CommonConstant.RED_BALL_TYPE, CommonConstant.SOCIAL_CODE_TYPE_KILL);

        for (SocialUserFollow socialUserFollow : follows) {
            String key = RedisConstant.getUserCurrentEncircleVo(gamePeriod.getGameId(), gamePeriod.getPeriodId(),
                    SocialEncircleKillConstant.SOCIAL_OPERATE_NUM_ENCIRCLE_RED, socialUserFollow.getFollowUserId());
            List<UserEncircleInfo> temp = redisService.kryoGet(key, ArrayList.class);
            if (temp == null || temp.size() <= 0) {
                //?????????????????????
                List<SocialEncircle> socialEncircles = socialEncircleCodeDao.getSocialEncircleByCondition(gamePeriod
                                .getGameId(), gamePeriod.getPeriodId(), null, socialUserFollow.getFollowUserId(),
                        SocialEncircleKillConstant.SOCIAL_OPERATE_NUM_ENCIRCLE_RED, null);
                if (socialEncircles != null && socialEncircles.size() > 0) {
                    for (SocialEncircle socialEncircle : socialEncircles) {
                        temp = saveUserEncircleInfo2Redis(socialEncircle, socialKillAwardLevel);
                    }
                }
            }
            if (temp != null && temp.size() > 0) {
                packageEncircleInfo(gamePeriod.getGameId(), temp, userId);
                result.addAll(temp);
            }
        }
        return result;
    }

    @Override
    public void packageEncircleInfo(long gameId, List<UserEncircleInfo> userEncircleInfos, Long userId) {

        for (UserEncircleInfo userEncircleInfo : userEncircleInfos) {
            UserLoginVo userLoginVo = loginService.getUserLoginVo(userEncircleInfo.getEncircleUserId());
            userEncircleInfo.setEncircleUserName(userLoginVo.getNickName());
            userEncircleInfo.setEncircleHeadImg(userLoginVo.getHeadImgUrl());
            String concurrentUserKillLockKey = RedisConstant.getConcurrentUserKillLockKey(gameId, Long.valueOf
                    (userEncircleInfo.getEncircleCodeId()), userId);
            Integer status = getPeriodEncircleStatus(gameId, userEncircleInfo.getPeriodId());
            if (status == SocialEncircleKillConstant.SOCIAL_ENCIRCLE_STATUS_ENABLE) {
                userEncircleInfo.setKillNumStatus(SocialEncircleKillConstant.SOCIAL_KILL_NUM_STATUS_INI);
            }
            if (redisService.isKeyByteExist(concurrentUserKillLockKey)) {
                userEncircleInfo.setKillNumBtnAdMsg("?????????");
                userEncircleInfo.setKillNumStatus(SocialEncircleKillConstant.SOCIAL_KILL_NUM_STATUS_TAKEPART);
            }
            userEncircleInfo.setEncircleTime(SocialEncircleKillCodeUtil.getEncircleTimeShow(DateUtil.formatTime
                    (userEncircleInfo.getEncircleTimeBak())));
            userEncircleInfo.setVip(vipMemberService.checkUserIsVip(userEncircleInfo.getEncircleUserId(),
                    VipMemberConstant.VIP_MEMBER_TYPE_DIGIT));
        }
    }

    private String addStar2Balls(String balls) {
        if (StringUtils.isBlank(balls)) {
            return balls;
        }
        balls = balls.replaceAll(CommonConstant.COMMA_SPLIT_STR, CommonConstant.COMMA_SPLIT_STR + CommonConstant
                .COMMON_STAR_STR);
        balls = CommonConstant.COMMON_STAR_STR + balls;
        return balls;
    }

    // type 0 ?????? 1 ?????? 2 ??????
    @Override
    public Map<String, Object> userSocialRecords(long gameId, Long userId, Long lookUpUserId, String versionCode,
                                                 Integer type, Boolean enHasNext, Boolean killHasNext, Integer
                                                         enLastIndex, Integer killLastIndex) {

        Map<String, Object> userSocialRecords = new HashMap<>();
        if (null == enHasNext) {
            enHasNext = Boolean.TRUE;
        }
        if (null == killHasNext) {
            killHasNext = Boolean.TRUE;
        }
        userSocialRecords.put("userId", userId + "");

        Boolean allHasNext = Boolean.FALSE;

        String lastFiveText = "";

        Boolean isMe = Boolean.FALSE;
        List<Map<String, Object>> userSocialList = new ArrayList<>();
        if (enHasNext) {
            // ????????????
            Map<String, Object> userEncircles = getMyEncirclesV2_3(gameId, userId, lookUpUserId, enLastIndex);
            for (Map<String, Object> en : (List<Map<String, Object>>) userEncircles.get("encircles")) {
                if (null == en) {
                    continue;
                }
                Map<String, Object> enNew = new HashMap<>();
                enNew.put("periodId", en.get("periodId"));
                List<Map<String, Object>> detailList = new ArrayList<>();
                for (Map<String, Object> detail : (List<Map<String, Object>>) en.get("encircleDetails")) {
                    if (null == detail) {
                        continue;
                    }
                    Map<String, Object> detailNew = new HashMap<>();
                    detailNew.put("itemName", detail.get("encircleName"));//??????
                    detailNew.put("partakeCount", detail.get("partakeCount"));//????????????
                    detailNew.put("socialTime", detail.get("socialTime"));//??????
                    detailNew.put("socialTimeSort", detail.get("socialTimeSort"));//??????
                    detailNew.put("awardAdMsg", detail.get("encircleAwardHtmlMsg"));//????????????
                    detailNew.put("encircleCodeId", detail.get("encircleCodeId"));//??????ID
                    detailNew.put("socialNum", detail.get("encircleNum"));//??????
                    detailNew.put("periodId", detail.get("periodId"));//??????
                    detailNew.put("socialBtnName", detail.get("encircleNumBtnMsg"));//??????
                    detailNew.put("socialTypeFlag", 0);//??????
                    detailNew.put("title2", "");//??????
                    detailList.add(detailNew);

                    GamePeriod gamePeriod = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, en.get("periodId")
                            .toString());
                    if (StringUtils.isBlank(gamePeriod.getWinningNumbers())) {
                        for (Map encircle : detailList) {
                            String nums = encircle.get("socialNum").toString();
                            if (!nums.contains(CommonConstant.COMMON_STAR_STR)) {
                                encircle.put("socialNum", addStar2Balls(nums));
                            }
                        }
                    }

                }
                enNew.put("details", detailList);
                enNew.put("periodName", en.get("periodName"));
                userSocialList.add(enNew);
            }
            enHasNext = (Boolean) userEncircles.get("enHasNext");
            isMe = (Boolean) userEncircles.get("isMe");
            if (userEncircles != null && userEncircles.containsKey("encircleLastIndex") && userEncircles.get
                    ("encircleLastIndex") != null) {
                enLastIndex = Integer.valueOf(userEncircles.get("encircleLastIndex").toString());
            }
        }
        if (killHasNext) {
            // ????????????
            Map<String, Object> userKills = socialKillCodeService.getMyKillNumsV2_3(gameId, userId, lookUpUserId,
                    killLastIndex, versionCode);
            for (Map<String, Object> en : (List<Map<String, Object>>) userKills.get("killNums")) {
                if (null == en) {
                    continue;
                }
                Map<String, Object> enNew = new HashMap<>();
                enNew.put("periodId", en.get("periodId"));
                enNew.put("periodName", en.get("periodName"));
                List<Map<String, Object>> detailList = new ArrayList<>();
                for (Map<String, Object> detail : (List<Map<String, Object>>) en.get("killNumDetails")) {
                    if (null == detail) {
                        continue;
                    }
                    Map<String, Object> detailNew = new HashMap<>();

                    detailNew.put("itemName", detail.get("killNumFront"));//??????
                    SocialEncircle socialEncircle = socialEncircleCodeDao.getSocialEncircleByEncircleId(gameId, en.get
                            ("periodId").toString(), Long.parseLong(detail.get("encircleCodeId").toString()));

                    Integer follows = 0;
                    if (socialEncircle != null && socialEncircle.getFollowKillNums() != null) {
                        follows = socialEncircle.getFollowKillNums();
                    }
                    detailNew.put("partakeCount", follows);//????????????
                    detailNew.put("socialTime", detail.get("socialTime"));//??????
                    detailNew.put("socialTimeSort", detail.get("socialTimeSort"));//??????
                    detailNew.put("awardAdMsg", detail.get("killNumAwardAdMsgNew"));//????????????
                    detailNew.put("encircleCodeId", detail.get("encircleCodeId"));//??????ID
                    detailNew.put("socialNum", detail.get("killNumBack"));//??????
                    detailNew.put("periodId", detail.get("periodId"));//??????
                    detailNew.put("socialBtnName", detail.get("killNumBtnMsgNew"));//??????
                    detailNew.put("socialTypeFlag", 1);//??????
                    detailNew.put("title2", detail.get("title2"));//??????
                    detailList.add(detailNew);
                    enNew.put("details", detailList);
                    GamePeriod gamePeriod = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, en.get("periodId")
                            .toString());
                    if (StringUtils.isBlank(gamePeriod.getWinningNumbers())) {
                        for (Map encircle : detailList) {
                            String nums = encircle.get("socialNum").toString();
                            if (!nums.contains(CommonConstant.COMMON_STAR_STR) && !nums.contains(CommonConstant
                                    .COMMON_QUESTION_STR)) {
                                encircle.put("socialNum", addStar2Balls(nums));
                            }
                        }
                    }
                }
                if (true) { //?????????type =0 ?????????????????????
                    // ??????????????? ???????????????????????????
                    Boolean flag = Boolean.TRUE;
                    for (int i = 0; i < userSocialList.size(); i++) {
                        Map<String, Object> userEn = userSocialList.get(i);
                        if (userEn.get("periodId").equals(enNew.get("periodId"))) {
                            List<Map<String, Object>> detailsEn = (List<Map<String, Object>>) userEn.get("details");
                            detailsEn.addAll(detailList);
                            flag = Boolean.FALSE;
                        }
                    }
                    if (flag || userSocialList.size() <= 0) {
                        userSocialList.add(enNew);
                    }
                }
            }
            killHasNext = (Boolean) userKills.get("hasNext");
            isMe = (Boolean) userKills.get("isMe");
            if (userKills.get("lastIndex") != null) {
                killLastIndex = Integer.valueOf(userKills.get("lastIndex").toString());
            }
        }
        if (null == enHasNext) {
            enHasNext = Boolean.FALSE;
        }
        if (null == killHasNext) {
            killHasNext = Boolean.FALSE;
        }
        if (enHasNext || killHasNext) {
            allHasNext = Boolean.TRUE;
        }


        // ????????????????????? ?????? ??????
        for (Map<String, Object> socialCode : userSocialList) {
            List<Map<String, Object>> detailList = (List<Map<String, Object>>) socialCode.get("details");
            Collections.sort(detailList, Comparator.comparing(p -> ((Timestamp) p.get
                    ("socialTimeSort"))));
            Collections.reverse(detailList);
            socialCode.put("details", detailList);
        }
        // ????????????
        Collections.sort(userSocialList, Comparator.comparing(p -> Integer.valueOf(p.get("periodId").toString())));


        // ?????????????????? ?????????5??????
        if (!lookUpUserId.equals(userId) && userSocialList.size() > 5) {
            userSocialList.subList(0, 5);
        }

        // ???????????????????????????????????????
        if (type == 1 || type == 2) {
            int count = 0;
            for (int i = 0; i < userSocialList.size() + count; i++) {
                Map<String, Object> socialCode = (Map<String, Object>) userSocialList.get(i - count);
                List<Map<String, Object>> detailList = (List<Map<String, Object>>) socialCode.get("details");
                List<Map<String, Object>> detailListResult = new ArrayList<>();
                for (Map<String, Object> detail : detailList) {
                    // ????????????
                    if (type == 1 && Integer.valueOf(detail.get("socialTypeFlag").toString()) == 0) {
                        detailListResult.add(detail);
                    }
                    // ????????????
                    if (type == 2 && Integer.valueOf(detail.get("socialTypeFlag").toString()) == 1) {
                        detailListResult.add(detail);
                    }
                }
                socialCode.put("details", detailListResult);
                if (detailListResult.size() == 0) {
                    userSocialList.remove(i - count);
                    count++;
                }
            }
        }
        Collections.reverse(userSocialList);

        if (userSocialList.size() <= 0) {
            allHasNext = Boolean.FALSE;
            if (type == 0) {
                lastFiveText = "????????????";
            }
            if (type == 1) {
                lastFiveText = "??????????????????";
            }
            if (type == 2) {
                lastFiveText = "??????????????????";
            }
            if (!lookUpUserId.equals(userId)) {
                if (type == 0) {
                    lastFiveText = "?????????5???????????????";
                }
                if (type == 1) {
                    lastFiveText = "?????????5???????????????";
                }
                if (type == 2) {
                    lastFiveText = "?????????5???????????????";
                }
            }
        } else {
            if (!lookUpUserId.equals(userId)) {
                lastFiveText = "????????????5?????????";
            }
        }

        userSocialRecords.put("userSocialList", userSocialList);
        userSocialRecords.put("enHasNext", enHasNext);
        userSocialRecords.put("killHasNext", killHasNext);
        userSocialRecords.put("enLastIndex", enLastIndex);
        userSocialRecords.put("killLastIndex", killLastIndex);
        userSocialRecords.put("allHasNext", allHasNext);
        userSocialRecords.put("isMe", isMe);
        userSocialRecords.put("lastFiveText", lastFiveText);

        //getUserTitleDetail
        //3.3??????????????????????????????
        if (StringUtils.isNotBlank(versionCode) && Integer.valueOf(versionCode) >= CommonConstant.VERSION_CODE_3_3) {
            Map<String, Object> userTitleAndLevelMap = getUserTitleAndLevel(gameId, userId);
            userSocialRecords.putAll(userTitleAndLevelMap);
        } else {
            Map<String, Object> userTitleDetail = userTitleService.getUserTitleDetail(gameId, userId);
            userTitleDetail.computeIfAbsent("godKillTimes", k -> "0???");
            userTitleDetail.computeIfAbsent("godEncircleTimes", k -> "0???");
            if (!userTitleDetail.get("godKillTimes").toString().contains("???")) {
                userTitleDetail.put("godKillTimes", userTitleDetail.get("godKillTimes").toString() + "???");
            }
            if (!userTitleDetail.get("godEncircleTimes").toString().contains("???")) {
                userTitleDetail.put("godEncircleTimes", userTitleDetail.get("godEncircleTimes").toString() + "???");
            }
            userSocialRecords.putAll(userTitleDetail);
            Map<String, List<AchievementVo>> userAchieveMap = userSocialRecordService.getUserAchievementVo(gameId,
                    userId);
            List<AchievementVo> encircleAchievements = userAchieveMap.get("encircleAchievements");

            List<AchievementVo> killAchievements = userAchieveMap.get("killAchievementsNew");

            // ????????????????????????
            List<Map<String, Object>> achieves = new ArrayList<>();
            for (AchievementVo achievementVo : encircleAchievements) {
                if (achievementVo.getIfHighLight() == 1) {
                    Map<String, Object> achieve = new HashMap<>();
                    achieve.put("achieveName", achievementVo.getAchieveName());
                    achieve.put("achieveDesc", achievementVo.getAchieveDesc());
                    achieve.put("ifHighLight", achievementVo.getIfHighLight());
                    achieve.put("socialType", "??????");
                    achieves.add(achieve);
                }
            }
            for (AchievementVo achievementVo : killAchievements) {
                if (achievementVo.getIfHighLight() == 1) {
                    Map<String, Object> achieve = new HashMap<>();
                    achieve.put("achieveName", achievementVo.getAchieveName());
                    achieve.put("achieveDesc", achievementVo.getAchieveDesc());
                    achieve.put("ifHighLight", achievementVo.getIfHighLight());
                    achieve.put("socialType", "??????");
                    achieves.add(achieve);
                }
            }
            for (AchievementVo achievementVo : encircleAchievements) {
                if (achievementVo.getIfHighLight() == 0) {
                    Map<String, Object> achieve = new HashMap<>();
                    achieve.put("achieveName", achievementVo.getAchieveName());
                    achieve.put("achieveDesc", achievementVo.getAchieveDesc());
                    achieve.put("ifHighLight", achievementVo.getIfHighLight());
                    achieve.put("socialType", "??????");
                    achieves.add(achieve);
                }
            }
            userSocialRecords.put("encircleAchievements", achieves.subList(0, 3));
        }
        return userSocialRecords;
    }

    @Override
    public void rebuildKillNumListRedis(Long gameId, Integer count, String periodId) {
        GamePeriod currentPeriod = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, periodId);
        if (StringUtils.isBlank(periodId)) {
            currentPeriod = PeriodRedis.getCurrentPeriod(gameId);
        }
        Map<String, Integer> socialAwardLevel = socialService.getAwardLevelMap(gameId, CommonConstant.RED_BALL_TYPE,
                CommonConstant.SOCIAL_CODE_TYPE_KILL);
        for (int i = 0; i < count; i++) {
            log.info("==========begin rebuild ===============" + currentPeriod.getPeriodId());
            try {
                List<SocialEncircle> socialEncircles = socialEncircleCodeDao.getSocialEncircleByPeriodId(gameId,
                        currentPeriod.getPeriodId());
                if (socialEncircles != null && socialEncircles.size() > 0) {
                    String socialEncircleListPeriodKey = RedisConstant.getPeriodEncircleListKey(gameId, currentPeriod
                            .getPeriodId(), SocialEncircleKillConstant.SOCIAL_OPERATE_NUM_ENCIRCLE_RED);
                    redisService.del(socialEncircleListPeriodKey);
                    for (SocialEncircle socialEncircle : socialEncircles) {
                        MyEncircleVo myEncircleVo = packageMyEncircleVo(socialEncircle, socialAwardLevel);
                        redisService.kryoZAddSet(socialEncircleListPeriodKey, socialEncircle.getCreateTime().getTime(),
                                myEncircleVo);
                    }
                }
            } catch (Exception e) {
                log.error("??????????????????????????????", e);
                break;
            }
            currentPeriod = PeriodRedis.getLastPeriodByGameIdAndPeriodId(gameId, currentPeriod.getPeriodId());
            log.info("==========end rebuild ===============" + currentPeriod.getPeriodId());
        }
    }

    @Override
    public List<MyEncircleVo> rebuildPeriodHotEncircle(long gameId, String periodId) {
        List<MyEncircleVo> myEncircleVos = new ArrayList<>();
        List<SocialEncircle> hotEncircles = socialEncircleCodeDao.getPeriodHotEncircle(gameId, periodId,
                SocialEncircleKillConstant.SOCIAL_HOT_ENCIRCLE_TYPE, SocialEncircleKillConstant
                        .SOCIAL_OPERATE_NUM_ENCIRCLE_RED);

        String key = RedisConstant.getHotEncircleListKey(gameId, periodId, SocialEncircleKillConstant
                .SOCIAL_OPERATE_NUM_ENCIRCLE_RED);
        redisService.del(key);
        if (hotEncircles != null && hotEncircles.size() > 0) {
            Map<String, Integer> socialAwardLevel = socialService.getAwardLevelMap(gameId, CommonConstant.RED_BALL_TYPE,
                    CommonConstant.SOCIAL_CODE_TYPE_KILL);
            for (SocialEncircle encircle : hotEncircles) {
                MyEncircleVo myEncircleVo = packageMyEncircleVo(encircle, socialAwardLevel);
                if (myEncircleVo != null) {
                    myEncircleVos.add(myEncircleVo);
                }
            }
            if (myEncircleVos.size() > 0) {
                GamePeriod gamePeriod = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, periodId);

                int expireTime = TrendUtil.getExprieSecond(gamePeriod.getAwardTime(), 3600);
                redisService.kryoSetEx(key, expireTime, myEncircleVos);
            }
            return myEncircleVos;
        }

        return myEncircleVos;
    }

    @Override
    public MyEncircleVo packageMyEncircleVo(SocialEncircle socialEncircle, Map<String, Integer> socialAwardLevel) {
        Integer periodStatus = getPeriodEncircleStatus(socialEncircle.getGameId(), socialEncircle.getPeriodId());

        MyEncircleVo myEncircleVo = new MyEncircleVo();
        myEncircleVo.setPeriodId(socialEncircle.getPeriodId());
        myEncircleVo.setIsHot(socialEncircle.getIsHot());
        myEncircleVo.setEncircleCodeId(socialEncircle.getEncircleCodeId() + "");
        myEncircleVo.setEncircleCount(socialEncircle.getEncircleNums());
        myEncircleVo.setEncircleName("?????????" + socialEncircle.getEncircleNums() + "???");
        myEncircleVo.setEncircleTimeBak(DateUtil.formatTime(socialEncircle.getCreateTime()));
        myEncircleVo.setEncircleTime(SocialEncircleKillCodeUtil.getEncircleTimeShow(DateUtil.formatTime
                (socialEncircle.getCreateTime())));
        int partakeCount = socialEncircle.getFollowKillNums() == null ? 0 : socialEncircle.getFollowKillNums();
        myEncircleVo.setPartakeCount(partakeCount);
        myEncircleVo.setEncircleNum(socialEncircle.getUserEncircleCode());
        myEncircleVo.setPeriodId(socialEncircle.getPeriodId());
        myEncircleVo.setEncircleDesc("???" + "?????????" + socialEncircle.getEncircleNums() + "??????????????????" + socialEncircle
                .getKillNums() + "??????");
        myEncircleVo.setEncircleAwardAdMsg("");//??????????????????
        myEncircleVo.setKillNumAwardAdMsg("");//????????????
        myEncircleVo.setEncircleKillCount(socialEncircle.getKillNums());

        UserLoginVo userLoginVo = loginService.getUserLoginVo(socialEncircle.getUserId());
        myEncircleVo.setEncircleUserName(userLoginVo.getNickName());
        myEncircleVo.setEncircleHeadImg(userLoginVo.getHeadImgUrl());
        myEncircleVo.setEncircleUserId(userLoginVo.getUserId());

//        Integer killListAwardMsg = SocialEncircleKillCodeUtil.getMaxEncircleAward(socialEncircle.getKillNums(),
//                socialEncircle.getEncircleNums(), socialAwardLevel);

        Integer maxKill = SocialEncircleKillCodeUtil.getMaxKillNumLevel(socialEncircle.getKillNums());
        Integer killNumAward = SocialEncircleKillCodeUtil.getScoreByRightCount(maxKill, maxKill, socialAwardLevel);

        myEncircleVo.setKillListAwardAdMsg("???????????????" + killNumAward + "??????");
        myEncircleVo.setKillNumBtnAdMsg("?????????");
        //???????????????????????????status
        if (periodStatus == SocialEncircleKillConstant.SOCIAL_ENCIRCLE_STATUS_ENABLE) {
            myEncircleVo.setKillNumAwardStatus(0);
            myEncircleVo.setKillNumBtnAdMsg("?????????");
        } else if (periodStatus == SocialEncircleKillConstant.SOCIAL_ENCIRCLE_STATUS_OPEN_AWARD) {
            myEncircleVo.setKillNumAwardStatus(1);
            myEncircleVo.setKillNumStatus(2);
        } else {
            myEncircleVo.setKillNumAwardStatus(1);
            myEncircleVo.setKillNumStatus(2);
        }

        return myEncircleVo;
    }

    @Override
    public Map<String, Object> getSocialBigData(long gameId) {
        Map<String, Object> result = new HashMap<>();
        boolean isEnd = false;
        long countDownSecond = 0;
        GamePeriod currentPeriod = PeriodRedis.getAwardCurrentPeriod(gameId);

        isEnd = SocialEncircleKillCodeUtil.getSocialIsEnd(gameId);
        countDownSecond = SocialEncircleKillCodeUtil.getCountDownSecound(gameId, isEnd);

        String socialBigDataKey = RedisConstant.getSocialBigDataKey(gameId, currentPeriod.getPeriodId());
        List<SocialBigDataVo> socialBigDataVos = redisService.kryoGet(socialBigDataKey, ArrayList.class);
        if (socialBigDataVos == null) {
            socialBigDataVos = socialStatisticService.rebuildSocialStatistic(gameId, currentPeriod.getPeriodId());
        }
        String statisticDate = "";
        String statisticHour = "";
        if (socialBigDataVos != null && socialBigDataVos.size() > 0) {
            Timestamp statisticAll = null;
            if (isEnd) {
                statisticAll = SocialEncircleKillCodeUtil.getPeriodLastStatisticTime(gameId);
            } else {
                statisticAll = SocialEncircleKillCodeUtil.getOneTimeStatisticTime(gameId, null, DateUtil
                        .getCurrentTimestamp());
            }

            String statisticAllStr = DateUtil.formatTime(statisticAll, "MM-dd HH:mm");
            String[] statisticAllArr = statisticAllStr.split(CommonConstant.SPACE_SPLIT_STR);
            if (statisticAllArr != null && statisticAllArr.length >= 2) {
                statisticDate = statisticAllArr[0];
                statisticHour = statisticAllArr[1];
            }
        }

        String dataUpdateAd = "(???6??????????????????)";
        if (isEnd) {
            dataUpdateAd = "???????????????";
            if (socialBigDataVos == null || socialBigDataVos.size() == 0) {
                countDownSecond = SocialEncircleKillCodeUtil.getCountDownSecound(gameId, false);
            }
        }

        String dataPrepareMsg = "????????????????????????";
        Map<String, Object> dataInstruction = SocialEncircleKillCodeUtil.getBigDataInstruction();

        result.put("isEnd", isEnd);
        result.put("periodId", currentPeriod.getPeriodId());
        result.put("dataUpdateAd", dataUpdateAd);
        result.put("dataPrepareMsg", dataPrepareMsg);
        result.put("countDownSecond", countDownSecond);
        result.put("socialData", socialBigDataVos);
        result.put("dataInstruction", dataInstruction);
        result.put("statisticDate", statisticDate);
        result.put("statisticHour", statisticHour);
        return result;
    }

    private SocialEncircle packageEncirclePo(Long gameId, String periodId, String encircleNums, Integer
            encircleCount, String killCounts, Long encircleUserId) {
        SocialEncircle socialEncircle = new SocialEncircle();
        socialEncircle.setPeriodId(periodId);
        socialEncircle.setCodeType(SocialEncircleKillConstant.ENCIRCLE_CODE_TYPE_RED);
        socialEncircle.setEncircleCodeId(generateEncircleCodeId());
        socialEncircle.setGameId(gameId);
        socialEncircle.setUserEncircleCode(encircleNums);
        socialEncircle.setEncircleNums(encircleCount);
        socialEncircle.setKillNums(killCounts);
        socialEncircle.setUserId(encircleUserId);
        return socialEncircle;
    }

    //???????????????1.??????????????????sortedSet??????total?????????
    private Set<EncirclePeriodsVo> getKillNumListByRedis(long gameId, int page, int totalCount, Long userId,
                                                         SocialKillNumFilter socialKillNumFilter) {
        Set<EncirclePeriodsVo> encirclePeriodsVos = new TreeSet<>();
        GamePeriod gamePeriod = PeriodRedis.getCurrentPeriod(gameId);
        //?????????????????????key
        String socialEncircleListPeriodKey = RedisConstant.getPeriodEncircleListKey(gameId, gamePeriod.getPeriodId(),
                SocialEncircleKillConstant.SOCIAL_OPERATE_NUM_ENCIRCLE_RED);
        //????????????????????????????????????????????????????????????
        Long currentPeriodDataCount = redisService.kryoZCount(socialEncircleListPeriodKey, Long.MIN_VALUE, Long
                .MAX_VALUE);
        int resCount = totalCount;
        Long begin = (long) (page - 1) * totalCount;
        if (begin < currentPeriodDataCount || currentPeriodDataCount == 0L) {
            EncirclePeriodsVo killNumVo = getKillNumListByPeriodId(gameId, gamePeriod.getPeriodId(), begin, Long
                    .valueOf(totalCount), userId, socialKillNumFilter);
            //???????????????????????????
            if (killNumVo != null && killNumVo.getEncircles() != null && killNumVo.getEncircles().size() == 0 &&
                    socialKillNumFilter.ifNeedFilter()) {
                encirclePeriodsVos.add(killNumVo);
                socialKillNumFilter.setFilterNoDataFlag(1);
                return encirclePeriodsVos;
            }
            //??????resCount
            if (killNumVo != null) {
                encirclePeriodsVos.add(killNumVo);
                resCount = totalCount - killNumVo.getEncircles().size();
            }
            //??????sortedSet??????begin
            begin = 0L;
        } else {
            begin = begin - currentPeriodDataCount;
        }

        String tempPeriodId = gamePeriod.getPeriodId();
        //???????????????????????????
        while (resCount > 0) {
            GamePeriod tempPeriod = PeriodRedis.getLastPeriodByGameIdAndPeriodId(gameId, tempPeriodId);
            socialEncircleListPeriodKey = RedisConstant.getPeriodEncircleListKey(gameId, tempPeriod.getPeriodId(),
                    SocialEncircleKillConstant.SOCIAL_OPERATE_NUM_ENCIRCLE_RED);
            Long periodDataCount = redisService.kryoZCount(socialEncircleListPeriodKey, Long.MIN_VALUE, Long
                    .MAX_VALUE) == null ? 0 : redisService.kryoZCount(socialEncircleListPeriodKey, Long.MIN_VALUE,
                    Long.MAX_VALUE);
            //????????????????????????????????????????????????????????????
            if (begin >= periodDataCount) {
                begin = begin - periodDataCount;
                tempPeriodId = tempPeriod.getPeriodId();
                //????????????????????????0????????????????????????
                if (periodDataCount == 0) {
                    log.info("???????????????" + tempPeriod + "????????????????????????");
                    break;
                }
                continue;
            }
            EncirclePeriodsVo encirclePeriodVo = getKillNumListByPeriodId(gameId, tempPeriod.getPeriodId(),
                    begin, Long.valueOf(resCount), userId, socialKillNumFilter);
            tempPeriodId = tempPeriod.getPeriodId();
            if (encirclePeriodVo == null || encirclePeriodVo.getEncircles() == null || encirclePeriodVo.getEncircles()
                    .size() <= 0) {
                log.error(tempPeriod.getPeriodId() + "???????????????sortedSet?????????");
                break;
            }
            resCount = resCount - encirclePeriodVo.getEncircles().size();
            encirclePeriodsVos.add(encirclePeriodVo);
        }
        return encirclePeriodsVos;
    }

    //???????????????2.??????????????????SortedSet????????????????????????????????????

    private EncirclePeriodsVo getKillNumListByPeriodId(long gameId, String periodId, Long begin, Long count, Long
            userId, SocialKillNumFilter socialKillNumFilter) {
        String socialEncircleListPeriodKey = RedisConstant.getPeriodEncircleListKey(gameId, periodId,
                SocialEncircleKillConstant.SOCIAL_OPERATE_NUM_ENCIRCLE_RED);
        EncirclePeriodsVo encirclePeriodsVo = new EncirclePeriodsVo();
        if (begin == null) {
            begin = 0L;
        }
        long end = begin + count - 1;
        List<MyEncircleVo> myEncircleVos = redisService.kryoZRevRange(socialEncircleListPeriodKey, begin, end,
                MyEncircleVo.class);

        Integer status = getPeriodEncircleStatus(gameId, periodId);

        if (status == SocialEncircleKillConstant.SOCIAL_ENCIRCLE_STATUS_FUTURE) {
            return null;
        }

        if (begin > 1 && (myEncircleVos == null || myEncircleVos.size() == 0)) {
            return null;
        }
        myEncircleVos = partakeEncircleVoStatusDeal(gameId, userId, myEncircleVos, false, status,
                socialKillNumFilter, null);

        encirclePeriodsVo.setGameId(gameId);
        encirclePeriodsVo.setPeriodId(periodId);
        encirclePeriodsVo.setPeriodName(GameConstant.PERIOD_NAME_MAP.get(GameCache.getGame(gameId).getGameEn()) +
                periodId + "???");
        encirclePeriodsVo.setAdMsg("???????????????????????????????????????");
        encirclePeriodsVo.setLeadEncircleAdMsg("????????????,");
        encirclePeriodsVo.setLeadEncircleBackAdMsg("");
        encirclePeriodsVo.addArrayListMyEncircleVo(myEncircleVos);

        if (status == SocialEncircleKillConstant.SOCIAL_ENCIRCLE_STATUS_ENABLE) {
            encirclePeriodsVo.setFilterEncircleBtnStatus(1);
            encirclePeriodsVo.setFilterEncircleBtnAdMsg("??????");
        } else if (status == SocialEncircleKillConstant.SOCIAL_ENCIRCLE_STATUS_OPEN_AWARD) {
            encirclePeriodsVo.setFilterEncircleBtnStatus(0);
            encirclePeriodsVo.setFilterEncircleBtnAdMsg("?????????????????????");
        } else {
            encirclePeriodsVo.setFilterEncircleBtnStatus(0);
            encirclePeriodsVo.setFilterEncircleBtnAdMsg("?????????");
        }

        return encirclePeriodsVo;
    }

    private List<MyEncircleVo> partakeEncircleVoStatusDeal(long gameId, Long userId, List<MyEncircleVo>
            myEncircleVos, boolean ifAllFilter, Integer periodIdStatus, SocialKillNumFilter socialKillNumFilter,
                                                           String versionCode) {
        if (versionCode == null) {
            versionCode = CommonConstant.NO_ENCRYPT_VERSION + "";
        }
        if (myEncircleVos == null || myEncircleVos.size() <= 0) {
            return myEncircleVos;
        }
        List<MyEncircleVo> myEncircleResult = new ArrayList<>();
        for (MyEncircleVo myEncircleVo : myEncircleVos) {
            if (periodIdStatus == SocialEncircleKillConstant.SOCIAL_ENCIRCLE_STATUS_ENABLE) {
                String concurrentUserKillLockKey = RedisConstant.getConcurrentUserKillLockKey(gameId, Long.valueOf
                        (myEncircleVo.getEncircleCodeId()), userId);
                myEncircleVo.setKillNumStatus(SocialEncircleKillConstant.SOCIAL_KILL_NUM_STATUS_INI);
                if (redisService.isKeyByteExist(concurrentUserKillLockKey)) {
                    myEncircleVo.setKillNumBtnAdMsg("?????????");
                    myEncircleVo.setKillNumStatus(SocialEncircleKillConstant.SOCIAL_KILL_NUM_STATUS_TAKEPART);
                }
                if (filterMyEncircleVo(myEncircleVo, socialKillNumFilter)) {
                    //2.3??????????????????????????????
                    if (Integer.valueOf(versionCode) >= CommonConstant.VERSION_CODE_2_3 && myEncircleVo.getIsHot()
                            .equals(SocialEncircleKillConstant.SOCIAL_GENERATE_ENCIRCLE_TYPE)) {
                        myEncircleResult.add(myEncircleVo);
                    } else if (Integer.valueOf(versionCode) < CommonConstant.VERSION_CODE_2_3) {
                        myEncircleResult.add(myEncircleVo);
                    }
                }
            } else {
                myEncircleVo.setIsHot(SocialEncircleKillConstant.SOCIAL_GENERATE_ENCIRCLE_TYPE);
                if (periodIdStatus == SocialEncircleKillConstant.SOCIAL_ENCIRCLE_STATUS_OPEN_AWARD) {
                    myEncircleVo.setKillNumBtnAdMsg("?????????");
                }
                if (periodIdStatus == SocialEncircleKillConstant.SOCIAL_ENCIRCLE_STATUS_END && StringUtils.isNotBlank
                        (versionCode)) {
                    if (Integer.valueOf(versionCode) >= CommonConstant.VERSION_CODE_2_3) {
                        StringBuffer bunAdMsg = new StringBuffer();
                        String[] encircleArr = myEncircleVo.getEncircleNum().split(CommonConstant
                                .COMMON_ESCAPE_STR + CommonConstant.COMMON_STAR_STR);
                        Integer hitNum = encircleArr == null ? 0 : (encircleArr.length - 1);
                        if (hitNum > 0) {
                            bunAdMsg.append(CommonConstant.COMMON_STAR_STR);
                        }
                        bunAdMsg.append("???" + myEncircleVo.getEncircleCount() + "???" + hitNum);
                        myEncircleVo.setKillNumBtnAdMsg(bunAdMsg.toString());
                    }
                }
                myEncircleVo.setKillNumStatus(SocialEncircleKillConstant.SOCIAL_KILL_NUM_STATUS_END);
                if (ifAllFilter) {//???????????????2.2?????????????????????
                    if (filterMyEncircleVo(myEncircleVo, socialKillNumFilter)) {
                        myEncircleResult.add(myEncircleVo);
                    }
                } else {
                    myEncircleResult.add(myEncircleVo);
                }

            }
            UserLoginVo userLoginVo = loginService.getUserLoginVo(myEncircleVo.getEncircleUserId());
            myEncircleVo.setEncircleUserName(userLoginVo.getNickName());
            myEncircleVo.setEncircleHeadImg(userLoginVo.getHeadImgUrl());
            myEncircleVo.setEncircleUserId(userLoginVo.getUserId());
            myEncircleVo.setEncircleTime(SocialEncircleKillCodeUtil.getEncircleTimeShow(myEncircleVo
                    .getEncircleTimeBak()));
            if (Integer.valueOf(versionCode) >= CommonConstant.VERSION_CODE_2_3) {
                String desc = myEncircleVo.getEncircleDesc().replaceAll(CommonConstant.COMMON_LEFT_STR_CN,
                        CommonConstant.SPACE_NULL_STR).replaceAll(CommonConstant.COMMON_RIGHT_STR_CN, CommonConstant
                        .SPACE_NULL_STR);
                myEncircleVo.setEncircleDesc(desc);
            }
            //vip??????
            myEncircleVo.setVip(vipMemberService.checkUserIsVip(myEncircleVo.getEncircleUserId(), VipMemberConstant
                    .VIP_MEMBER_TYPE_DIGIT));
        }
        return myEncircleResult;
    }

    private void calculateMyEncircleVo(MyEncircleVo myEncircleVo, String userPeriodKillNumList, String[] winNumArr) {
        if (winNumArr != null && winNumArr.length > 0 && StringUtils.isNotBlank(myEncircleVo.getEncircleNum())) {
            String encircleNum = myEncircleVo.getEncircleNum();
            if (!encircleNum.contains(CommonConstant.COMMON_STAR_STR)) {
                for (String winNum : winNumArr) {
                    encircleNum = encircleNum.replaceAll(winNum, CommonConstant.COMMON_ESCAPE_STR +
                            CommonConstant.COMMON_STAR_STR + winNum);
                }
            }
            myEncircleVo.setEncircleNum(encircleNum);
        }
        myEncircleVo.setKillNumBtnAdMsg("?????????");
        myEncircleVo.setKillNumAwardStatus(SocialEncircleKillConstant.SOCIAL_ENCIRCLE_STATUS_END);
        myEncircleVo.setKillNumStatus(SocialEncircleKillConstant.SOCIAL_KILL_NUM_STATUS_END);
        redisService.kryoZAddSet(userPeriodKillNumList, DateUtil.formatString(myEncircleVo.getEncircleTimeBak(),
                2).getTime(), myEncircleVo);
    }

    private Map<String, Object> getKillNumListFromRedisByPeriodId(long gameId, String periodId, Integer lastIndex,
                                                                  Integer socialKillNumListPageSize, Long userId,
                                                                  SocialKillNumFilter socialKillNumFilter, String
                                                                          versionCode) {
        boolean hasNext = Boolean.TRUE;
        Map<String, Object> result = new HashMap<>();
        List<MyEncircleVo> myEncircleVos = new ArrayList<>();
        //??????????????????????????????
        Integer periodStatus = getPeriodEncircleStatus(gameId, periodId);
        if (periodStatus == SocialEncircleKillConstant.SOCIAL_ENCIRCLE_STATUS_FUTURE) {
            return null;
        }
        //????????????begin???end index
        Long begin = Long.valueOf(lastIndex);
        Long end = (long) lastIndex + socialKillNumListPageSize - 1;
        boolean noMore = Boolean.FALSE;
        //??????
        while (myEncircleVos.size() < socialKillNumListPageSize) {
            List<MyEncircleVo> tempEncircles = getMyEncircleVos(gameId, periodId, begin, end);
            if (tempEncircles == null || tempEncircles.size() == 0) {
                hasNext = Boolean.FALSE;
                break;
            }
            if (tempEncircles.size() < socialKillNumListPageSize) {
                noMore = true;
                hasNext = Boolean.FALSE;
                end = begin + tempEncircles.size();
            }
            //???????????????
            tempEncircles = partakeEncircleVoStatusDeal(gameId, userId, tempEncircles, true, periodStatus,
                    socialKillNumFilter, versionCode);
            myEncircleVos.addAll(tempEncircles);
            if (myEncircleVos.size() >= socialKillNumListPageSize || noMore) {
                break;
            }
            begin = end + 1;
            end = end + socialKillNumListPageSize;
        }
        result = packageKillNumList(gameId, periodId, userId, periodStatus, myEncircleVos);

        //??????????????????
        if (myEncircleVos.size() == 0) {
            if (socialKillNumFilter.ifNeedFilter()) {
                result.put("leadEncircleAdMsg", "??????????????????????????????????????????");
                result.put("adMsg", "?????????????????????");
            } else {
                result.put("leadEncircleAdMsg", "???????????????");
                result.put("adMsg", "");
            }
            if (periodStatus != SocialEncircleKillConstant.SOCIAL_ENCIRCLE_STATUS_ENABLE) {
                result.put("leadEncircleBackAdMsg", "");
            }
        }
        //?????????2.3??????????????????
        if (Integer.valueOf(versionCode) >= CommonConstant.VERSION_CODE_2_3 && periodStatus ==
                SocialEncircleKillConstant.SOCIAL_ENCIRCLE_STATUS_ENABLE && lastIndex == 0) {
            List<MyEncircleVo> hotMyEncircleVos = getHotEncircleVoByPeriodId(gameId, periodId, userId);
            if (hotMyEncircleVos != null && hotMyEncircleVos.size() > 0) {
                hotMyEncircleVos.addAll(myEncircleVos);
                result.put("encircles", hotMyEncircleVos);
            }
        }

        result.put("lastIndex", end + 1);
        result.put("hasNext", hasNext);
        result.remove("gameId");
        result.remove("periodName");
        result.remove("filterEncircleBtnStatus");
        result.put("followEncircleCount", "");
        return result;
    }

    private List<MyEncircleVo> getMyEncircleVos(long gameId, String periodId, Long begin, Long end) {
        List<MyEncircleVo> myEncircleVos = new ArrayList<>();
        String socialEncircleListPeriodKey = RedisConstant.getPeriodEncircleListKey(gameId, periodId,
                SocialEncircleKillConstant.SOCIAL_OPERATE_NUM_ENCIRCLE_RED);

        Long periodDataCount = redisService.kryoZCount(socialEncircleListPeriodKey, Long.MIN_VALUE, Long.MAX_VALUE)
                == null ? 0 : redisService.kryoZCount(socialEncircleListPeriodKey, Long.MIN_VALUE, Long.MAX_VALUE);
        if (begin >= periodDataCount) {
            return null;
        }
        //?????????????????????????????????
        if (periodDataCount <= 0 && begin == 0) {
            rebuildKillNumListRedis(gameId, 1, periodId);
            periodDataCount = redisService.kryoZCount(socialEncircleListPeriodKey, Long.MIN_VALUE, Long.MAX_VALUE)
                    == null ? 0 : redisService.kryoZCount(socialEncircleListPeriodKey, Long.MIN_VALUE, Long.MAX_VALUE);
            if (periodDataCount <= 0) {
                return null;
            }
        }
        if (periodDataCount <= end) {
            end = periodDataCount - 1;
        }

        myEncircleVos = redisService.kryoZRevRange(socialEncircleListPeriodKey, begin, end, MyEncircleVo.class);
        return myEncircleVos;
    }

    private Map<String, Object> packageKillNumList(long gameId, String periodId, Long userId, Integer periodStatus,
                                                   List<MyEncircleVo> myEncircleVos) {
        Map<String, Object> result = new HashMap<>();

        result.put("gameId", gameId);
        result.put("periodId", periodId);
        result.put("periodName", GameConstant.PERIOD_NAME_MAP.get(GameCache.getGame(gameId).getGameEn()) + periodId +
                "???");
        result.put("adMsg", "???????????????????????????????????????");
        result.put("leadEncircleAdMsg", "????????????,");
        result.put("leadEncircleBackAdMsg", "");
        result.put("encircles", myEncircleVos);
        result.put("filterEncircleBtnStatus", 1);
        result.put("filterEncircleBtnAdMsg", "??????");
        //?????????
        if (periodStatus == SocialEncircleKillConstant.SOCIAL_ENCIRCLE_STATUS_ENABLE || periodStatus ==
                SocialEncircleKillConstant.SOCIAL_ENCIRCLE_STATUS_FUTURE || periodStatus ==
                SocialEncircleKillConstant.SOCIAL_ENCIRCLE_STATUS_OPEN_AWARD) {
            result.put("futurePeriodId", "");
        } else {
            GamePeriod nextPeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(gameId, periodId);
            result.put("futurePeriodId", nextPeriod.getPeriodId());
        }
        //?????????
        String beginPeriod = SocialEncircleKillConstant.SOCIAL_ENCIRCLE_BEGIN_PERIODID_SSQ;
        if (GameCache.getGame(gameId).getGameEn().equals(GameConstant.DLT)) {
            beginPeriod = SocialEncircleKillConstant.SOCIAL_ENCIRCLE_BEGIN_PERIODID_DLT;
        }
        GamePeriod lastPeriod = PeriodRedis.getLastPeriodByGameIdAndPeriodId(gameId, periodId);
        if (!lastPeriod.getPeriodId().equals(beginPeriod)) {
            result.put("lastPeriodId", lastPeriod.getPeriodId());
        } else {
            result.put("lastPeriodId", "");
        }

//      if (periodStatus == SocialEncircleKillConstant.SOCIAL_ENCIRCLE_STATUS_OPEN_AWARD) {
//          result.put("filterEncircleBtnStatus", 0);
//          result.put("filterEncircleBtnAdMsg", "?????????????????????");

        return result;
    }

    //?????????????????????????????????????????????
    @Override
    public List<UserEncircleInfo> saveUserEncircleInfo2Redis(SocialEncircle socialEncircle, Map<String, Integer>
            socialKillAwardLevel) {
        UserEncircleInfo userEncircleInfo = convertSocialEncircle2EncircleInfo(socialEncircle, socialKillAwardLevel);

        String userEncircleInfoKey = RedisConstant.getUserCurrentEncircleVo(socialEncircle.getGameId(),
                socialEncircle.getPeriodId(), SocialEncircleKillConstant.SOCIAL_OPERATE_NUM_ENCIRCLE_RED,
                socialEncircle.getUserId());
        List<UserEncircleInfo> userEncircleInfos = redisService.kryoGet(userEncircleInfoKey, ArrayList.class);
        if (userEncircleInfos == null) {
            userEncircleInfos = new ArrayList<>();
        }
        if (!userEncircleInfos.contains(userEncircleInfo)) {
            List<UserEncircleInfo> disableUserInfos = new ArrayList<>();
            for (UserEncircleInfo temp : userEncircleInfos) {
                if (temp.getEncircleCodeId().equals(userEncircleInfo.getEncircleCodeId())) {
                    disableUserInfos.add(temp);
                }
            }
            if (disableUserInfos.size() > 0) {
                userEncircleInfos.removeAll(disableUserInfos);
            }
            userEncircleInfos.add(userEncircleInfo);
        }

        GamePeriod gamePeriod = PeriodRedis.getPeriodByGameIdAndPeriod(socialEncircle.getGameId(), socialEncircle
                .getPeriodId());
        int expireTime = TrendUtil.getExprieSecond(gamePeriod.getAwardTime(), 3600);
        redisService.kryoSetEx(userEncircleInfoKey, expireTime, userEncircleInfos);
        return userEncircleInfos;
    }

    @Override
    public List<SocialEncircle> getAllEncircle(long gameId, String periodId) {
        return socialEncircleCodeDao.getSocialEncircleByPeriodId(gameId, periodId);
    }

    @Override
    public UserEncircleInfo convertSocialEncircle2EncircleInfo(SocialEncircle socialEncircle, Map<String, Integer>
            socialKillAwardLevel) {
        UserEncircleInfo userEncircleInfo = new UserEncircleInfo();
        userEncircleInfo.setEncircleCodeId(socialEncircle.getEncircleCodeId() + "");
        userEncircleInfo.setEncircleCount(socialEncircle.getEncircleNums());
        userEncircleInfo.setEncircleDesc("???" + socialEncircle.getEncircleNums() + "????????????" + socialEncircle.getKillNums
                () + "???");
        UserLoginVo userLoginVo = loginService.getUserLoginVo(socialEncircle.getUserId());
        userEncircleInfo.setEncircleHeadImg(userLoginVo.getHeadImgUrl());
        userEncircleInfo.setEncircleKillCount(Integer.valueOf(socialEncircle.getKillNums()));
        userEncircleInfo.setEncircleNum(socialEncircle.getUserEncircleCode());
        Timestamp createTime = DateUtil.getCurrentTimestamp();
        if (socialEncircle.getCreateTime() != null) {
            createTime = socialEncircle.getCreateTime();
        }
        userEncircleInfo.setEncircleTime(SocialEncircleKillCodeUtil.getEncircleTimeShow(DateUtil.formatTime
                (createTime)));
        userEncircleInfo.setEncircleTimeBak(createTime);
        userEncircleInfo.setEncircleUserId(socialEncircle.getUserId());
        userEncircleInfo.setEncircleUserName(userLoginVo.getNickName());
        Integer maxKill = SocialEncircleKillCodeUtil.getMaxKillNumLevel(socialEncircle.getKillNums());
        Integer killNumAward = SocialEncircleKillCodeUtil.getScoreByRightCount(maxKill, maxKill, socialKillAwardLevel);
        userEncircleInfo.setKillListAwardAdMsg("???????????????" + killNumAward + "??????");
        userEncircleInfo.setKillNumStatus(SocialEncircleKillConstant.SOCIAL_KILL_NUM_STATUS_END);
        userEncircleInfo.setPartakeCount(socialEncircle.getFollowKillNums());
        userEncircleInfo.setPeriodId(socialEncircle.getPeriodId());
        Integer periodStatus = getPeriodEncircleStatus(socialEncircle.getGameId(), socialEncircle.getPeriodId());
        String killNumBtnAdMsg = "?????????";
        if (periodStatus == SocialEncircleKillConstant.SOCIAL_ENCIRCLE_STATUS_END) {
            killNumBtnAdMsg = "*???" + socialEncircle.getEncircleNums() + "???" + socialEncircle.getRightNums();

        }
        userEncircleInfo.setKillNumBtnAdMsg(killNumBtnAdMsg);
        return userEncircleInfo;
    }

    @Override
    public Integer updateHotEncircleType(Long gameId, String periodId, Long encircleId, Integer isHot) {
        return socialEncircleCodeDao.setEncircleIsHot(gameId, periodId, encircleId, isHot);
    }

    private List<MyEncircleVo> getHotEncircleVoByPeriodId(long gameId, String periodId, Long userId) {
        String key = RedisConstant.getHotEncircleListKey(gameId, periodId, SocialEncircleKillConstant
                .SOCIAL_OPERATE_NUM_ENCIRCLE_RED);
        List<MyEncircleVo> hotEncircles = redisService.kryoGet(key, ArrayList.class);
        //todo ????????????????????????redis
        if (hotEncircles != null) {
            for (MyEncircleVo myEncircleVo : hotEncircles) {
                UserLoginVo userLoginVo = loginService.getUserLoginVo(myEncircleVo.getEncircleUserId());
                myEncircleVo.setEncircleHeadImg(userLoginVo.getHeadImgUrl());
                myEncircleVo.setEncircleUserName(userLoginVo.getNickName());
                myEncircleVo.setKillNumStatus(0);
                String concurrentUserKillLockKey = RedisConstant.getConcurrentUserKillLockKey(gameId, Long.valueOf
                        (myEncircleVo.getEncircleCodeId()), userId);
                if (redisService.isKeyByteExist(concurrentUserKillLockKey)) {
                    myEncircleVo.setKillNumBtnAdMsg("?????????");
                    myEncircleVo.setKillNumStatus(SocialEncircleKillConstant.SOCIAL_KILL_NUM_STATUS_TAKEPART);
                    String encircleDesc = myEncircleVo.getEncircleDesc().replaceAll(CommonConstant.COMMON_ESCAPE_STR
                            + CommonConstant.COMMON_LEFT_STR_CN, CommonConstant.SPACE_NULL_STR).replaceAll
                            (CommonConstant.COMMON_ESCAPE_STR + CommonConstant.COMMON_RIGHT_STR_CN, CommonConstant
                                    .SPACE_NULL_STR);
                    myEncircleVo.setEncircleDesc(encircleDesc);
                    myEncircleVo.setKillNumStatus(1);
                }
                myEncircleVo.setVip(vipMemberService.checkUserIsVip(myEncircleVo.getEncircleUserId(),
                        VipMemberConstant.VIP_MEMBER_TYPE_DIGIT));
                myEncircleVo.setKillNumStatus(0);
            }
        }
        return hotEncircles;
    }

    private void notifyRobotKillNum(SocialEncircle socialEncircle) {
        try {
            int sendToRobot = new Random().nextInt(100) + 1;
            int manualControlPercent = ActivityIniCache.getActivityIniIntValue(ActivityIniConstant
                    .USER_ENCIRCLE_SEND_TO_ROBOT_PERCENT, 5);
            /* ??????????????????????????????*/
            if (!SocialEncircleKillCodeUtil.isNormalTime(DateUtil.getCurrentTimestamp())) {
                manualControlPercent = 200;
            }
            if (sendToRobot <= manualControlPercent) {
                String robotKillEncircleKey = RedisConstant.getRobotKillEncircleKey(socialEncircle.getGameId(),
                        SocialEncircleKillConstant.ENCIRCLE_CODE_TYPE_RED);
                redisService.kryoRPush(robotKillEncircleKey, socialEncircle);
            }
        } catch (Exception e) {
            log.error("notifyRobotKillNum ??????", e);
        }
    }

    private Map<String, Object> getUserTitleAndLevel(long gameId, Long userId) {
        Map<String, Object> result = new HashMap<>();
        //1.????????????????????????
        String userIntegralKey = RedisConstant.getUserIntegralKey(gameId, userId);
        UserSocialIntegralVo userSocialIntegralVo = redisService.kryoGet(userIntegralKey, UserSocialIntegralVo.class);
        if (userSocialIntegralVo == null) {
            userSocialIntegralVo = userSocialIntegralService.refreshUserIntegralRedis(gameId, userId);
        }
        String levelName = "";
        String userScore = "";
        String nextLevelScore = "";
        Double percent = 0d;
        String color = "#8AD765";
        Map<String, Object> socialLevel = new HashMap<>();
        if (userSocialIntegralVo != null) {
            levelName = "LV." + userSocialIntegralVo.getSocialLevel() + userSocialIntegralVo.getLevelName();
            userScore = userSocialIntegralVo.getIntegral();
            nextLevelScore = userSocialIntegralVo.getUpgradeIntegral();
            if (StringUtils.isNotBlank(nextLevelScore) && Integer.valueOf(nextLevelScore) != 0) {
                percent = Double.valueOf(userScore) / Double.valueOf(nextLevelScore) * 100;
            }
            color = SocialLevelIntegralCache.getColorByLevel(userSocialIntegralVo.getSocialLevel());
        }
        socialLevel.put("levelName", levelName);
        socialLevel.put("userScore", userScore);
        socialLevel.put("percent", percent.intValue());
        socialLevel.put("nextLevelScore", nextLevelScore);
        socialLevel.put("color", color);
        //2.??????????????????
        List<Map<String, Object>> title = new ArrayList<>();
        //2.1????????????
        Map<String, Object> userTitle = userTitleService.getUserTitleDetail(gameId, userId);
        if (userTitle != null) {
            boolean isGodKill = (boolean) userTitle.get("isGodKill");
            boolean isGodEncircle = (boolean) userTitle.get("isGodEncircle");
            Integer godKillTimes = (Integer) userTitle.get("godKillTimes");
            Integer godEncircleTimes = (Integer) userTitle.get("godEncircleTimes");
            Integer killIfLight = isGodKill ? 1 : 0;
            Integer encircleIfLight = isGodEncircle ? 1 : 0;

            Map<String, Object> godKill = new HashMap();
            godKill.put("title", "");
            godKill.put("socialType", SocialEncircleKillConstant.USER_TITLE_IMG_TYPE_GOD_KILL);
            godKill.put("socialName", "??????");
            godKill.put("titleDesc", godKillTimes + "???");
            godKill.put("ifLight", killIfLight);
            godKill.put("ifLight", killIfLight);
            title.add(godKill);

            Map<String, Object> encircleKill = new HashMap();
            encircleKill.put("title", "");
            encircleKill.put("socialType", SocialEncircleKillConstant.USER_TITLE_IMG_TYPE_GOD_ENCIRCLE);
            encircleKill.put("socialName", "??????");
            encircleKill.put("titleDesc", godEncircleTimes + "???");
            encircleKill.put("ifLight", encircleIfLight);
            title.add(encircleKill);
        }
        //2.2????????????
        Map<String, List<AchievementVo>> userAchieveMap = userSocialRecordService.getUserAchievementVo(gameId, userId);
        List<AchievementVo> encircleAchievements = userAchieveMap.get("encircleAchievements");
        List<AchievementVo> killAchievements = userAchieveMap.get("killAchievementsNew");

        // ????????????????????????
        for (AchievementVo achievementVo : encircleAchievements) {

            Map<String, Object> achieve = new HashMap<>();
            achieve.put("title", achievementVo.getAchieveName());
            achieve.put("socialType", SocialEncircleKillConstant.USER_TITLE_IMG_TYPE_ACHIEVE_ENCIRCLE);
            achieve.put("socialName", "??????");
            achieve.put("titleDesc", achievementVo.getAchieveDesc());
            achieve.put("ifLight", achievementVo.getIfHighLight());
            title.add(achieve);
        }
        for (AchievementVo achievementVo : killAchievements) {
            Map<String, Object> achieve = new HashMap<>();
            achieve.put("title", achievementVo.getAchieveName());
            achieve.put("socialType", SocialEncircleKillConstant.USER_TITLE_IMG_TYPE_ACHIEVE_KILL);
            achieve.put("socialName", "??????");
            achieve.put("titleDesc", achievementVo.getAchieveDesc());
            achieve.put("ifLight", achievementVo.getIfHighLight());
            title.add(achieve);
        }
        Collections.sort(title, (n1, n2) -> Integer.valueOf(n2.get("ifLight").toString()).compareTo(Integer.valueOf
                (n1.get("ifLight").toString())));
        List<Map<String, Object>> userTitles = null;
        if (title != null && title.size() >= 3) {
            userTitles = title.subList(0, 3);
        }
        result.put("socialLevel", socialLevel);
        result.put("userTitles", userTitles);
        return result;
    }

    @Override
    public void setSelf(Object proxyBean) {
        self = (SocialEncircleCodeService) proxyBean;
    }
}
