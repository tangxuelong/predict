package com.mojieai.predict.service.impl;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.*;
import com.mojieai.predict.entity.bo.GoldTask;
import com.mojieai.predict.entity.bo.PaginationList;
import com.mojieai.predict.entity.dto.PushDto;
import com.mojieai.predict.entity.po.*;
import com.mojieai.predict.entity.vo.EncircleKillNumVo;
import com.mojieai.predict.entity.vo.UserLoginVo;
import com.mojieai.predict.enums.CronEnum;
import com.mojieai.predict.enums.GoldCoinTaskEnum;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.*;
import com.mojieai.predict.service.beanself.BeanSelfAware;
import com.mojieai.predict.service.game.GameFactory;
import com.mojieai.predict.thread.AliyunPushTask;
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
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ExecutorService;

@Service
public class SocialKillCodeServiceImpl implements SocialKillCodeService, BeanSelfAware {
    private static final Logger log = CronEnum.PERIOD.getLogger();

    @Autowired
    private KillCodeIdSequenceDao killCodeIdSequenceDao;
    @Autowired
    private LoginService loginService;
    @Autowired
    private SocialKillCodeDao socialKillCodeDao;
    @Autowired
    private SocialEncircleCodeDao socialEncircleCodeDao;
    @Autowired
    private IndexUserSocialCodeDao indexUserSocialCodeDao;
    @Autowired
    private RedisService redisService;
    @Autowired
    private SocialEncircleCodeService socialEncircleCodeService;
    @Autowired
    private SocialService socialService;
    @Autowired
    private PredictNumService predictNumService;
    @Autowired
    private SocialStatisticService socialStatisticService;
    @Autowired
    private VipMemberService vipMemberService;
    @Autowired
    private UserSocialTaskAwardService userSocialTaskAwardService;
    @Autowired
    private UserSocialTaskAwardDao userSocialTaskAwardDao;
    @Autowired
    private UserTitleService userTitleService;
    @Autowired
    private SocialResonanceService socialResonanceService;

    private SocialKillCodeService self;

    @Override
    public Long generateKillCodeId() {
        String timePrefix = DateUtil.formatDate(new Date(), DateUtil.DATE_FORMAT_YYMMDDHH);
        long seq = killCodeIdSequenceDao.getKillCodeIdSequence();
        Long killCodeId = Long.parseLong(timePrefix + CommonUtil.formatSequence(seq));
        return killCodeId;
    }

    @Override
    public Map<String, Object> addKillCode(long gameId, GamePeriod gamePeriod, long userId, Long encircleId, String
            userKillCode, String clientIp, Integer clientId) {
        Map<String, Object> result = new HashMap<>();
        result.put("successFlag", IniConstant.COMPATIBLE_SIGN_YES);

        //1.检验圈号存在性
        SocialEncircle socialEncircle = socialEncircleCodeDao.getSocialEncircleByEncircleId(gameId, gamePeriod
                .getPeriodId(), encircleId);
        if (socialEncircle == null) {
            result.put("successFlag", IniConstant.COMPATIBLE_SIGN_NO);
            result.put("errorMsg", "圈号不存在");
            return result;
        }
        //2.检验杀号是否在圈号范围
        if (socialEncircle.getUserEncircleCode() == null) {
            log.error(gamePeriod.getPeriodId() + "期围号id：" + encircleId + ",没有围号内容请确认");
            result.put("successFlag", IniConstant.COMPATIBLE_SIGN_NO);
            result.put("errorMsg", "圈号不存在");
            return result;
        }
        String[] killNumArr = SocialEncircleKillCodeUtil.removeStrOfNumArr(userKillCode.split(CommonConstant
                .COMMA_SPLIT_STR));
        if (Integer.valueOf(socialEncircle.getKillNums()) < (killNumArr.length)) {
            result.put("successFlag", IniConstant.COMPATIBLE_SIGN_NO);
            result.put("errorMsg", "杀号个数错误");
            return result;
        }
        for (String killNum : killNumArr) {
            if (!socialEncircle.getUserEncircleCode().contains(killNum)) {
                result.put("successFlag", IniConstant.COMPATIBLE_SIGN_NO);
                result.put("errorMsg", "所杀号码不在圈号范围");
                return result;
            }
        }
        //3.查找user_rank
        try {
            String encircleUserKillRankKey = RedisConstant.getEncircleUserKillRankKey(gameId, encircleId);
            Long userRank = redisService.incr(encircleUserKillRankKey);
            log.info("userId userRank is " + userRank);
            Integer taskType = Integer.valueOf(GoldTask.TASK_TYPE_SOCIAL_KILL_SSQ);
            if (GameCache.getGame(gameId).getGameEn().equals(GameConstant.DLT)) {
                taskType = Integer.valueOf(GoldTask.TASK_TYPE_SOCIAL_KILL_DLT);
            }
            UserSocialTaskAward taskAward = userSocialTaskAwardService.initUserSocialTask(gameId, gamePeriod
                    .getPeriodId(), userId, taskType);
            //4.保存更新
            SocialKillCode socialKillCode = self.saveUserKillNumTransaction(gameId, gamePeriod.getPeriodId(), userId,
                    encircleId, userKillCode, killNumArr.length, result, userRank, taskAward.getTaskId());
            //5.通知刷新杀号列表
            noticeUpdateKillNumList(socialEncircle);
            //6.加杀号使用次数
            String userKillNumTotalTimesKey = RedisConstant.getUserKillNumTotalTimesKey(gameId, gamePeriod
                    .getPeriodId(), userId);
            redisService.incr(userKillNumTotalTimesKey);
            if (redisService.ttl(userKillNumTotalTimesKey) == -1L) {
                int expireTime = TrendUtil.getExprieSecond(gamePeriod.getAwardTime(), 3600);
                redisService.expire(userKillNumTotalTimesKey, expireTime);
            }
            String isDistributeFlag = RedisConstant.getUserDistributeKillFlag(gameId, gamePeriod.getPeriodId());
            if (null == redisService.kryoZRank(isDistributeFlag, userId)) {
                redisService.kryoZAddSet(isDistributeFlag, System.currentTimeMillis(), userId);
                GamePeriod nextPeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(gameId, gamePeriod.getPeriodId());
                int expireSeconds = (int) DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(), nextPeriod
                        .getAwardTime()) + (60 * 60 * 24);
                redisService.expire(isDistributeFlag, expireSeconds);
                predictNumService.updateUserPredictMaxNums(gameId, nextPeriod.getPeriodId(), userId, 1);
            }
            //7.多线程做附属任务
            multiThreadTask(socialKillCode, clientIp, clientId);
            //8
            socialResonanceService.livingBuildKillResonance(socialKillCode);
        } catch (DuplicateKeyException e) {
            log.error("出现重复杀号保存杀号异常，用户已经参与过杀号，请核实缓存。encircleID:" + encircleId + "userId" + userId);
            String concurrentUserKillLockKey = RedisConstant.getConcurrentUserKillLockKey(gameId, encircleId, userId);
            redisService.kryoSetNx(concurrentUserKillLockKey, 1);
            result.put("successFlag", IniConstant.COMPATIBLE_SIGN_NO);
            result.put("errorMsg", "您已经参与过该次杀号");
        }
        /* 杀号推送*/
        /* 给自己杀号 不推送*/
        if (userId != socialEncircle.getUserId()) {
            Game game = GameCache.getGame(gameId);
            UserLoginVo userLoginVo = loginService.getUserLoginVo(userId);
            String text = "彩友“" + userLoginVo.getNickName() + "”已帮您杀号 >>";
            String url = "";
            Map<String, String> content = new HashMap<>();
            content.put("pushUrl", "mjlottery://mjnative?page=encircleNumDetail&periodId=" +
                    gamePeriod.getPeriodId() + "&encircleCodeId=" + encircleId + "&gameEn=" + game.getGameEn());
            content.put("killNumPushText", text);
            PushDto pushDto = new PushDto(CommonConstant.APP_TITLE, text, url, content);
            AliyunPushTask pushTask = new AliyunPushTask(pushDto, "ACCOUNT", String.valueOf(socialEncircle.getUserId
                    ()), "default");
            ThreadPool.getInstance().getPushExec().submit(pushTask);
        }
        return result;
    }

    private void multiThreadTask(SocialKillCode socialKillCode, String clientIp, Integer clientId) {
        try {
            //统计杀号大数据
            ExecutorService exec = ThreadPool.getInstance().getStatisticSocialExec();
            StatisticTask task = new StatisticTask(SocialEncircleKillConstant.SOCIAL_BIG_DATA_HOT_KILL_NUMBERS,
                    socialStatisticService, null, socialKillCode);
            exec.submit(task);
        } catch (Exception e) {
            log.warn(socialKillCode.getKillCodeId() + "统计社区杀号大数据异常", e);
        }
        try {
            Integer taskType = Integer.valueOf(GoldTask.TASK_TYPE_SOCIAL_KILL_SSQ);
            if (GameCache.getGame(socialKillCode.getGameId()).getGameEn().equals(GameConstant.DLT)) {
                taskType = Integer.valueOf(GoldTask.TASK_TYPE_SOCIAL_KILL_DLT);
            }
            ExecutorService exec = ThreadPool.getInstance().getUserSocialTaskExec();
            SocialTaskAwardTask task = new SocialTaskAwardTask(socialKillCode.getGameId(), socialKillCode.getPeriodId
                    (), socialKillCode.getUserId(), taskType, clientIp, clientId, userSocialTaskAwardService);
            exec.submit(task);
        } catch (Exception e) {

        }
    }

    @Transactional
    @Override
    public SocialKillCode saveUserKillNumTransaction(long gameId, String periodId, long userId, Long encircleId, String
            userKillCode, Integer killNumCount, Map<String, Object> result, Long userRank, String taskId) {
        //1.锁住圈号任务
        UserSocialTaskAward userSocialTaskAward = userSocialTaskAwardDao.getUserSocialTaskAwardById(taskId, userId,
                true);
        SocialKillCode socialKillCode = new SocialKillCode();
        socialKillCode.setCodeType(SocialEncircleKillConstant.SOCIAL_KILL_CODE_TYPE_RED);
        socialKillCode.setEncircleCodeId(encircleId);
        socialKillCode.setGameId(gameId);
        socialKillCode.setKillCodeId(generateKillCodeId());
        socialKillCode.setPeriodId(periodId);
        socialKillCode.setKillNums(killNumCount);
        socialKillCode.setUserId(userId);
        socialKillCode.setUserKillCode(userKillCode);
        socialKillCode.setUserRank(userRank.intValue());
        //2.添加杀号
        int insertRes = socialKillCodeDao.insert(socialKillCode);
        if (insertRes > 0) {
            IndexUserSocialCode indexUserSocialCode = new IndexUserSocialCode();
            indexUserSocialCode.setGameId(gameId);
            indexUserSocialCode.setPeriodId(periodId);
            indexUserSocialCode.setSocialCodeId(socialKillCode.getKillCodeId());
            indexUserSocialCode.setSocialCodeType(SocialEncircleKillConstant.SOCIAL_OPERATE_NUM_KILL_RED);
            indexUserSocialCode.setUserId(userId);
            indexUserSocialCode.setSocialCount(socialKillCode.getKillNums());
            indexUserSocialCodeDao.insert(indexUserSocialCode);
            socialEncircleCodeDao.updateUserRankByencircleId(gameId, periodId, encircleId, userRank.intValue());
            result.put("msg", "杀号成功");
        }
        //3.添加奖励
        Integer maxTimes = null;
        Integer times = userSocialTaskAward.getTaskTimes() == null ? 0 : userSocialTaskAward.getTaskTimes();
        String taskType = GoldTask.TASK_TYPE_SOCIAL_KILL_SSQ;
        if (GameCache.getGame(gameId).getGameEn().equals(GameConstant.DLT)) {
            taskType = GoldTask.TASK_TYPE_SOCIAL_KILL_DLT;
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
                userSocialTaskAwardDao.updateTaskTimesById(taskId, userId, times, isAward);
            } else if (times.equals(maxTimes)) {
                userSocialTaskAwardDao.updateTaskIsAward(taskId, userId, SocialEncircleKillConstant
                        .SOCIAL_TASK_IS_AWARD_WAIT, SocialEncircleKillConstant.SOCIAL_TASK_IS_AWARD_INIT);
            }
        }
        return socialKillCode;
    }

    @Override
    public Map<String, Object> getMyKillNumsV2_3(long gameId, Long userId, Long lookUpUserId, Integer lastIndex,
                                                 String versionCode) {
        boolean isMe = false;
        boolean kiHasNext = false;
        boolean lookUpUserIsVip = vipMemberService.checkUserIsVip(lookUpUserId, VipMemberConstant.VIP_MEMBER_TYPE_DIGIT);
        Integer kiPeriodSizeEncircle = SocialEncircleKillConstant.SOCIAL_MY_KILL_NUM_LIST_PERIOD_SIZE;
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> killNums = new ArrayList<>();

        if (userId.equals(lookUpUserId)) {
            isMe = true;
        }
        if (!isMe) {
            lastIndex = null;
            kiPeriodSizeEncircle = SocialEncircleKillConstant.SOCIAL_MY_ENCIRCLE_LIST_PERIOD_SIZE;
        }
        Map<String, Integer> socialKillAwardLevel = socialService.getAwardLevelMap(gameId, CommonConstant.RED_BALL_TYPE,
                CommonConstant.SOCIAL_CODE_TYPE_KILL);

        //1.获取杀号信息
        List<IndexUserSocialCode> killIndexs = indexUserSocialCodeDao.getUserPartTakePeriodId(kiPeriodSizeEncircle,
                SocialEncircleKillConstant.SOCIAL_OPERATE_NUM_KILL_RED, userId, lastIndex);
        int countKi = 0;
        for (IndexUserSocialCode killIndex : killIndexs) {
            if (killIndexs.size() == kiPeriodSizeEncircle && killIndexs.size() - 2 == countKi) {
                break;
            }
            List<SocialKillCode> socialKillCodes = socialKillCodeDao.getKillNumsByCondition(gameId, killIndex
                    .getPeriodId(), null, userId);
            Integer periodStatus = socialEncircleCodeService.getPeriodEncircleStatus(gameId, killIndex.getPeriodId());
            Map<String, Object> killNumList = SocialEncircleKillCodeUtil.packageMyKillNumList(gameId,
                    socialKillCodes, periodStatus, lookUpUserId, redisService, socialKillAwardLevel, isMe,
                    versionCode, lookUpUserIsVip);
            killNums.add(killNumList);
            countKi++;
        }

        if (killIndexs.size() == kiPeriodSizeEncircle) {
            kiHasNext = true;
        }
        if (killIndexs.size() > 0) {
            lastIndex = killIndexs.get(killIndexs.size() - 1).getIndexId();
            if (killIndexs.size() == SocialEncircleKillConstant.SOCIAL_MY_KILL_NUM_LIST_PERIOD_SIZE) {
                lastIndex = killIndexs.get(killIndexs.size() - 2).getIndexId();
            }
        }

        if (!isMe) {
            kiHasNext = false;
        }

        result.put("killNums", killNums);
        result.put("userId", userId);
        result.put("isMe", isMe);
        result.put("hasNext", kiHasNext);
        result.put("lastIndex", lastIndex);
        return result;
    }

    @Override
    public Map<String, Object> getKillNumsInfoByEncircleId(long gameId, String periodId, Long encircleCodeId, Long
            userId, Integer page, Map<String, Integer> socialKillAwardLevel, Integer periodEncircleStatus, Integer
                                                                   killNumDetaillType, Long encircleUserId, String
                                                                   versionCode) {
        Boolean hasNext = Boolean.FALSE;
        Boolean noShowFlag = Boolean.FALSE;
        Map<String, Object> result = new HashMap<>();
        Integer isDistribute = SocialEncircleKillConstant.SOCIAL_ENCIRCLE_KILL_IS_DISTRIBUTE_NO;
        //杀号奖励派发后按照积分排序
        String gameDistributeFlag = redisService.kryoGet(RedisConstant.getDisTributeFlag(gameId), String.class);
        if (StringUtils.isNotBlank(gameDistributeFlag)) {
            GamePeriod disTributePeriod = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, gameDistributeFlag);
            GamePeriod period = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, periodId);
            if (!DateUtil.compareDate(disTributePeriod.getEndTime(), period.getEndTime())) {
                isDistribute = SocialEncircleKillConstant.SOCIAL_ENCIRCLE_KILL_IS_DISTRIBUTE_YES;
            }
        }
        PaginationList<SocialKillCode> socialKillCodes = socialKillCodeDao.getKillNumsByEncircleIdByPage(gameId,
                periodId, encircleCodeId, null, page, isDistribute);
        SocialKillCode socialKillCode = socialKillCodeDao.getKillNumsByEncircleIdAndUserId(gameId, periodId,
                encircleCodeId, userId);
        boolean takePart = false;//todo 优化
        if (socialKillCode != null && socialKillCode.getEncircleCodeId() != null) {
            takePart = true;
            noShowFlag = true;
        }
        //自己可以看自己的
        if (userId.equals(encircleUserId)) {
            takePart = true;
            noShowFlag = true;
        }
        //vip可以看
        boolean visitorIsVip = vipMemberService.checkUserIsVip(userId, VipMemberConstant.VIP_MEMBER_TYPE_DIGIT);
        String unlockKillNumMsg = "会员免费查看所有杀号";
        if (visitorIsVip) {
            takePart = true;
            unlockKillNumMsg = "会员你好，已解锁所有杀号";
        }
        if (!periodEncircleStatus.equals(SocialEncircleKillConstant.SOCIAL_ENCIRCLE_STATUS_ENABLE) || userId.equals
                (encircleUserId) || socialKillCodes == null || socialKillCodes.size() == 0 || noShowFlag) {
            unlockKillNumMsg = "";
        }

        List<EncircleKillNumVo> encircleKillNumVos = convertSocialKillCodes2Vo(socialKillCodes, takePart,
                socialKillAwardLevel, killNumDetaillType, periodEncircleStatus, userId, versionCode);

        if (encircleKillNumVos.size() > 0) {
            Integer currentPage = socialKillCodes.getPaginationInfo().getCurrentPage();
            Integer totalPage = socialKillCodes.getPaginationInfo().getTotalPage();
            if (currentPage < totalPage) {
                hasNext = Boolean.TRUE;
            }
            page = socialKillCodes.getPaginationInfo().getCurrentPage();
        }
        //奖励
        String awardColumnName = "奖励(积分)";
        String awardColumnNameNew = "奖励:";
        String awardColumnNameNewFF = "";
        String awardColumnNameNewFB = "";
        if (periodEncircleStatus != SocialEncircleKillConstant.SOCIAL_ENCIRCLE_STATUS_END) {
            awardColumnName = "预计奖励(积分)";
            awardColumnNameNew = "预计奖励:";
            awardColumnNameNewFF = "<font color=\"#FF5050\">";
            awardColumnNameNewFB = "</font>";
        }
        for (EncircleKillNumVo encircleKillNumVo : encircleKillNumVos) {
            encircleKillNumVo.setScoreMsg(awardColumnNameNew + awardColumnNameNewFF + encircleKillNumVo.getRewardScore()
                    + awardColumnNameNewFB + "积分");
        }

        String titleText = "(参与杀号后可见)";

        if (takePart || periodEncircleStatus != SocialEncircleKillConstant.SOCIAL_ENCIRCLE_STATUS_ENABLE ||
                killNumDetaillType == SocialEncircleKillConstant.SOCIAL_MYENCIRCLE_LIST_TYPE_ENCIRCLE) {
            titleText = "";
        }

        result.put("ifReward", 0);
        result.put("hasNext", hasNext);
        result.put("page", page);
        result.put("datas", encircleKillNumVos);
        result.put("visitorIsVip", visitorIsVip);
        result.put("awardColumnName", awardColumnName);
        result.put("unlockKillNumMsg", unlockKillNumMsg);
        result.put("leadKillNumAdMsg", "暂无杀号");
        result.put("titleText", titleText);
        return result;
    }

    @Override
    public Map<String, Object> getKillNumDetailByEncircleId(long gameId, String periodId, Long encircleId, Long
            killNumUserId, Integer page, Integer killNumDetaillType, String versionCode) {
        String encircleNum = "";
        Integer partakeCount = 0;
        String killNumAdMsg = "";
        Integer killNumEnable = 1;
        String killNumEnableMsg = "";
        String encircleAwardMsg = "";
        String killNumBtnMsg = "确定";
        String encircleTime = "";
        String rewardScoreInfo = "";
        String rewardScoreInfo_3_2 = "";
        String encircleUserName = "";
        String encircleUserHeadImg = "";
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> userKillNums = new HashMap<>();
        Integer killNumStatus = SocialEncircleKillConstant.SOCIAL_KILL_NUM_STATUS_INI;
        //杀号奖级Map
        Map<String, Integer> socialKillAwardLevel = socialService.getAwardLevelMap(gameId, CommonConstant
                .RED_BALL_TYPE, CommonConstant.SOCIAL_CODE_TYPE_KILL);
        //1.获取圈号信息
        SocialEncircle socialEncircle = socialEncircleCodeDao.getSocialEncircleByEncircleId(gameId, periodId,
                encircleId);
        if (socialEncircle == null) {
            log.error("圈号id:" + encircleId + "不存在,gameId" + gameId + "期次ID" + periodId);
            result.put("msg", "圈号不存在");
            return result;
        }
        //2.如果2.3版本之后，围号列表进入的杀号人不等于圈号人，自动跳转杀号详情
        if (Integer.valueOf(versionCode) >= CommonConstant.VERSION_CODE_2_3 && killNumDetaillType.equals
                (SocialEncircleKillConstant.SOCIAL_MYENCIRCLE_LIST_TYPE_ENCIRCLE) && !killNumUserId.equals
                (socialEncircle.getUserId())) {
            killNumDetaillType = SocialEncircleKillConstant.SOCIAL_MYENCIRCLE_LIST_TYPE_KILL;
        }
        Integer periodEncircleStatus = socialEncircleCodeService.getPeriodEncircleStatus(gameId, socialEncircle
                .getPeriodId());

        encircleNum = socialEncircle.getUserEncircleCode();
        //4.1 版本更新用户没参数杀号直接看围号详情
        SocialKillCode socialKillCode = socialKillCodeDao.getKillNumsByEncircleIdAndUserId(gameId, periodId,
                encircleId, killNumUserId);
        if (Integer.valueOf(versionCode) >= CommonConstant.VERSION_CODE_4_1 && periodEncircleStatus.equals
                (SocialEncircleKillConstant.SOCIAL_ENCIRCLE_STATUS_END)) {
            killNumDetaillType = SocialEncircleKillConstant.SOCIAL_MYENCIRCLE_LIST_TYPE_ENCIRCLE;
        }
        //3.区分杀号和围号列表（目前只有两种）
        if (killNumDetaillType == SocialEncircleKillConstant.SOCIAL_MYENCIRCLE_LIST_TYPE_KILL) {
            //2.1圈号文案展示
            encircleNum = encircleNum.replaceAll(CommonConstant.COMMON_ESCAPE_STR + CommonConstant.COMMON_STAR_STR,
                    CommonConstant.SPACE_NULL_STR);
            //2.1依据杀号人杀号改变围号的展示
//            SocialKillCode socialKillCode = socialKillCodeDao.getKillNumsByEncircleIdAndUserId(gameId, periodId,
//                    encircleId, killNumUserId);
            if (socialKillCode != null && socialKillCode.getKillCodeId() != null) {
                String userKillCode = socialKillCode.getUserKillCode();
                String[] killCodeArr = userKillCode.replace(CommonConstant.COMMON_STAR_STR, CommonConstant
                        .SPACE_NULL_STR).split(CommonConstant.COMMA_SPLIT_STR);
                for (String killCode : killCodeArr) {
                    encircleNum = encircleNum.replace(killCode, CommonConstant.COMMON_STAR_STR + killCode);
                }
            }
            //2.2围号上面文案展示
            killNumAdMsg = "最多可杀" + socialEncircle.getKillNums() + "个，点击要杀的号";
            //2.3依据期次状态展示文案
            if (periodEncircleStatus == SocialEncircleKillConstant.SOCIAL_ENCIRCLE_STATUS_ENABLE) {
                //2.3.1杀号号码可不可点
                String userKillNumTotalTimesKey = RedisConstant.getUserKillNumTotalTimesKey(gameId, periodId,
                        killNumUserId);
                Integer userKillTimes = StringUtils.isBlank(redisService.get(userKillNumTotalTimesKey)) ? 0 : Integer
                        .valueOf(redisService.get(userKillNumTotalTimesKey));
                if (userKillTimes >= SocialEncircleKillConstant.SOCIAL_KILL_NUM_MAX_COUNT) {
                    killNumEnable = 0;
                    killNumEnableMsg = "本期10次杀号已用完";
                }
                //2.3.2btn下面奖励文案
                Integer userRank = socialEncircle.getFollowKillNums() == null ? 0 : socialEncircle.getFollowKillNums();
                Integer maxKill = SocialEncircleKillCodeUtil.getMaxKillNumLevel(socialEncircle.getKillNums());
                Integer score = SocialEncircleKillCodeUtil.getScoreByRightCount(maxKill, maxKill, socialKillAwardLevel);
                if (score == null) {
                    score = 0;
                }
                rewardScoreInfo = "我的预计奖励" + score + "积分";
                Map<String, Integer> awardLevelMap = socialService.getAwardLevelMap(gameId, CommonConstant
                        .RED_BALL_TYPE, CommonConstant.SOCIAL_CODE_TYPE_KILL);
                rewardScoreInfo_3_2 = "最高奖励" + awardLevelMap.get(String.valueOf(socialEncircle.getKillNums()) +
                        CommonConstant.COMMON_COLON_STR + String.valueOf(socialEncircle.getKillNums())) + "积分";
                if (socialKillCode != null && socialKillCode.getKillCodeId() != null) {
                    rewardScoreInfo = "";
                    killNumBtnMsg = "已参与";
                    killNumStatus = SocialEncircleKillConstant.SOCIAL_KILL_NUM_STATUS_TAKEPART;
                    rewardScoreInfo_3_2 = "";
                }
            } else if (periodEncircleStatus == SocialEncircleKillConstant.SOCIAL_ENCIRCLE_STATUS_OPEN_AWARD) {
                //2.3.1杀号号码可不可点
                killNumEnable = 0;
                killNumEnableMsg = "本期已结束";
                //2.3.2btn下面奖励文案
                rewardScoreInfo = "杀号已在20:00截止，等待开奖";
                rewardScoreInfo_3_2 = rewardScoreInfo;
                killNumBtnMsg = "已结束";
                killNumStatus = SocialEncircleKillConstant.SOCIAL_KILL_NUM_STATUS_END;
                if (socialKillCode != null && socialKillCode.getKillCodeId() != null) {
                    killNumBtnMsg = "已参与";
                    killNumStatus = SocialEncircleKillConstant.SOCIAL_KILL_NUM_STATUS_TAKEPART;
                }
            } else if (periodEncircleStatus == SocialEncircleKillConstant.SOCIAL_ENCIRCLE_STATUS_END) {
                //2.3.1杀号号码可不可点
                killNumEnable = 0;
                killNumEnableMsg = "本期已结束";
                //2.3.2btn下面奖励文案
                rewardScoreInfo = "";
                rewardScoreInfo_3_2 = "";
                killNumBtnMsg = "已结束";
                killNumStatus = SocialEncircleKillConstant.SOCIAL_KILL_NUM_STATUS_END;
                if (socialKillCode != null && socialKillCode.getKillCodeId() != null) {
                    Integer score = socialKillCode.getUserAwardScore() == null ? 0 : socialKillCode.getUserAwardScore();
                    //rewardScoreInfo = "获得积分：" + score;
//                    killNumStatus = SocialEncircleKillConstant.SOCIAL_KILL_NUM_STATUS_NOT_RIGHT;
//                    killNumBtnMsg = "未中，0积分";
//                    if (score > 0) {
//                        killNumBtnMsg = "全中，" + String.valueOf(score) + "积分";
//                        killNumStatus = SocialEncircleKillConstant.SOCIAL_KILL_NUM_STATUS_ALL_RIGHT;
//                    }
                    killNumBtnMsg = "杀" + socialKillCode.getKillNums() + "中" + socialKillCode.getRightNums() + "，" +
                            String.valueOf(score) + "积分";
                    killNumStatus = SocialEncircleKillConstant.SOCIAL_KILL_NUM_STATUS_ALL_RIGHT;
                }
                killNumAdMsg = "围" + socialEncircle.getEncircleNums() + "中" + socialEncircle.getRightNums();
            } else if (periodEncircleStatus == SocialEncircleKillConstant.SOCIAL_ENCIRCLE_STATUS_FUTURE) {//暂时没有未来期次

            }
        } else {
            //2.1围号文案展示
            if (Integer.valueOf(versionCode) < CommonConstant.VERSION_CODE_2_3) {
                encircleNum = encircleNum.replaceAll(CommonConstant.COMMON_ESCAPE_STR + CommonConstant
                        .COMMON_STAR_STR, CommonConstant.SPACE_NULL_STR);
            } else {
                String[] encircleNumArr = encircleNum.split(CommonConstant.COMMA_SPLIT_STR);
                StringBuilder encircleNumSb = new StringBuilder();
                for (String num : encircleNumArr) {
                    if (encircleNumSb.length() > 0) {
                        encircleNumSb.append(CommonConstant.COMMA_SPLIT_STR);
                    }
                    if (periodEncircleStatus != SocialEncircleKillConstant.SOCIAL_ENCIRCLE_STATUS_ENABLE) {
                        if (num.contains(CommonConstant.COMMON_STAR_STR)) {
                            num = num.replaceAll(CommonConstant.COMMON_ESCAPE_STR + CommonConstant.COMMON_STAR_STR,
                                    CommonConstant.SPACE_NULL_STR);
                        } else {
                            num = num.replace(num, CommonConstant.POUND_SPLIT_STR + num);
                        }
                    }
                    encircleNumSb.append(num);
                }
                encircleNum = encircleNumSb.toString();
            }
            //2.2围号上面文案展示
            killNumAdMsg = "围" + socialEncircle.getEncircleNums() + "码，求杀" + socialEncircle.getKillNums() + "个";
            if (socialEncircle.getIsDistribute() != null && socialEncircle.getIsDistribute() == 1) {
                encircleAwardMsg = "围" + socialEncircle.getEncircleNums() + "中" + socialEncircle.getRightNums();
            }
            //2.3杀号号码可不可点
            killNumEnable = 0;
        }

        /* 可直接获取的信息*/
        //圈号人基础信息
        Long userId = socialEncircle.getUserId();
        UserLoginVo userLoginVo = loginService.getUserLoginVo(userId);
        if (userLoginVo != null) {
            encircleUserName = userLoginVo.getNickName();
            encircleUserHeadImg = userLoginVo.getHeadImgUrl();
        }
        partakeCount = socialEncircle.getFollowKillNums() == null ? partakeCount : socialEncircle.getFollowKillNums();
        encircleTime = SocialEncircleKillCodeUtil.getEncircleTimeShow(DateUtil.formatTime(socialEncircle
                .getCreateTime()));
        //围号下的杀号列表
        userKillNums = getKillNumsInfoByEncircleId(gameId, periodId, encircleId, killNumUserId, page,
                socialKillAwardLevel, periodEncircleStatus, killNumDetaillType, userId, versionCode);
        result.putAll(userKillNums);
        //检查vip
        boolean isVip = vipMemberService.checkUserIsVip(userId, VipMemberConstant.VIP_MEMBER_TYPE_DIGIT);
        List<String> godList = userTitleService.getUserGodList(gameId, userId, versionCode);

        result.put("encircleTime", encircleTime);
        result.put("encircleNum", encircleNum);
        result.put("killNumStatus", killNumStatus);
        result.put("periodId", periodId);
        result.put("encircleUserIsVip", isVip);
        result.put("encircleCodeId", encircleId);
        result.put("partakeCount", partakeCount);
        result.put("killNumAdMsg", killNumAdMsg);
        result.put("encircleUserName", encircleUserName);
        result.put("encircleUserHeadImg", encircleUserHeadImg);
        result.put("encircleAwardMsg", encircleAwardMsg);
        result.put("rewardScoreInfo", rewardScoreInfo);
        result.put("scoreDefault", rewardScoreInfo_3_2);
        result.put("killNumBtnMsg", killNumBtnMsg);
        result.put("killNumCount", socialEncircle.getKillNums());
        result.put("killNumEnable", killNumEnable);
        result.put("killNumEnableMsg", killNumEnableMsg);
        result.put("encircleUserId", userId);
        result.put("encircleUserIdStr", String.valueOf(userId));
        result.put("godList", godList);
        return result;
    }

    private List<EncircleKillNumVo> convertSocialKillCodes2Vo(PaginationList<SocialKillCode> socialKillCodes, Boolean
            takePartFlag, Map<String, Integer> socialKillAwardLevel, Integer killNumDetaillType, Integer
                                                                      periodEncircleStatus, Long userId, String
                                                                      versionCode) {
        List<EncircleKillNumVo> encircleKillNumVos = new ArrayList<>();
        if (socialKillCodes == null || socialKillCodes.getList().size() <= 0) {
            return encircleKillNumVos;
        }
        Integer index = 0;
        for (SocialKillCode socialKillCode : socialKillCodes.getList()) {
            EncircleKillNumVo encircleKillNumVo = new EncircleKillNumVo();
            String userKillCode = "杀号后可见";
            String userKillCodeNew_3_2 = "";
            String nums[] = socialKillCode.getUserKillCode().split(CommonConstant.COMMA_SPLIT_STR);
            for (int i = 0; i < nums.length; i++) {
                userKillCodeNew_3_2 += CommonConstant.COMMON_STAR_STR + CommonConstant.COMMON_QUESTION_STR;
                if (i < nums.length - 1) {
                    userKillCodeNew_3_2 += CommonConstant.COMMA_SPLIT_STR;
                }
            }

            Integer numType = SocialEncircleKillConstant.SOCIAL_PERSON_KILL_LIST_TEXT_TYPE;
            if (takePartFlag || periodEncircleStatus != SocialEncircleKillConstant.SOCIAL_ENCIRCLE_STATUS_ENABLE ||
                    killNumDetaillType == SocialEncircleKillConstant.SOCIAL_MYENCIRCLE_LIST_TYPE_ENCIRCLE) {
                userKillCode = socialKillCode.getUserKillCode();
                userKillCodeNew_3_2 = socialKillCode.getUserKillCode();
                numType = SocialEncircleKillConstant.SOCIAL_PERSON_KILL_LIST_CODE_TYPE;
            }
            encircleKillNumVo.setUserKillCode(userKillCode);
            encircleKillNumVo.setUserKillCode_3_2(userKillCodeNew_3_2.trim());
            encircleKillNumVo.setNumType(numType);
            UserLoginVo userLoginVo = loginService.getUserLoginVo(socialKillCode.getUserId());
            encircleKillNumVo.setHeadImg(userLoginVo.getHeadImgUrl());
            encircleKillNumVo.setUserName(userLoginVo.getNickName());
            encircleKillNumVo.setKillUserId(userLoginVo.getUserId());
            Integer isMe = 0;
            if (userId.equals(userLoginVo.getUserId())) {
                isMe = 1;
            }
            encircleKillNumVo.setIsMe(isMe);
            Integer score = 0;
            String key = null;
            if (socialKillCode.getUserAwardScore() != null) {
                score = socialKillCode.getUserAwardScore();
            } else if (socialKillCode.getRightNums() != null) {
                key = socialKillCode.getKillNums() + CommonConstant.COMMON_COLON_STR + socialKillCode.getRightNums();
            } else {
                key = socialKillCode.getKillNums() + CommonConstant.COMMON_COLON_STR + socialKillCode.getKillNums();
            }
            if (StringUtils.isNotBlank(key) && socialKillAwardLevel.containsKey(key)) {
                score = socialKillAwardLevel.get(key);
            }
            encircleKillNumVo.setRewardScore(score + CommonConstant.SPACE_NULL_STR);
            encircleKillNumVo.setVip(vipMemberService.checkUserIsVip(socialKillCode.getUserId(), VipMemberConstant
                    .VIP_MEMBER_TYPE_DIGIT));
            // 大神
            List<String> godList = userTitleService.getUserGodList(socialKillCode.getGameId(), socialKillCode
                    .getUserId(), versionCode);
            encircleKillNumVo.setGodList(godList);

            // 最牛杀号
            if (index == 0) {
                SocialEncircle socialEncircle = socialEncircleCodeDao.getSocialEncircleByEncircleId(socialKillCode
                        .getGameId(), socialKillCode.getPeriodId(), socialKillCode.getEncircleCodeId());
                Boolean isBestKill = GameFactory.getInstance().getGameBean(GameCache.getGame(socialKillCode.getGameId())
                        .getGameEn()).judgeIsBestKillNum(Integer.valueOf(socialEncircle.getKillNums()), socialKillCode
                        .getKillNums(), socialKillCode.getRightNums());
                encircleKillNumVo.setIsBestKill(isBestKill);
            }
            index++;

            encircleKillNumVos.add(encircleKillNumVo);
        }
        return encircleKillNumVos;
    }

    private void noticeUpdateKillNumList(SocialEncircle socialEncircle) {
        String userKillTempList = RedisConstant.getSocialUserKillTempListKey(socialEncircle.getGameId());
        redisService.kryoRPush(userKillTempList, socialEncircle);
    }

    @Override
    public void setSelf(Object proxyBean) {
        self = (SocialKillCodeService) proxyBean;
    }
}
