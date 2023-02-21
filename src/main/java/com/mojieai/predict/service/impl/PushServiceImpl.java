package com.mojieai.predict.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.cache.IniCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.*;
import com.mojieai.predict.entity.dto.PushDto;
import com.mojieai.predict.entity.po.*;
import com.mojieai.predict.enums.CommonStatusEnum;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.DingTalkRobotService;
import com.mojieai.predict.service.LoginService;
import com.mojieai.predict.service.PushService;
import com.mojieai.predict.service.UserInfoService;
import com.mojieai.predict.service.game.AbstractGame;
import com.mojieai.predict.service.game.GameFactory;
import com.mojieai.predict.thread.AliyunPushTask;
import com.mojieai.predict.thread.PushTask;
import com.mojieai.predict.thread.ThreadPool;
import com.mojieai.predict.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Created by tangxuelong on 2017/7/20.
 */
@Service
public class PushServiceImpl implements PushService {
    @Autowired
    private RedisService redisService;
    @Autowired
    private PushScheduleDao pushScheduleDao;
    @Autowired
    private UserDeviceInfoDao userDeviceInfoDao;
    @Autowired
    private LoginService loginService;
    @Autowired
    private DingTalkRobotService dingTalkRobotService;
    @Autowired
    private PushTriggerDao pushTriggerDao;
    @Autowired
    private PushService pushService;
    @Autowired
    private SocialUserFansDao socialUserFansDao;
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private UserInfoDao userInfoDao;

    private static final Logger log = LogConstant.commonLog;

    @Override
    public void pushToSingle(PushDto pushDto, String clientId) {
        List<String> list = new ArrayList<>();
        list.add(clientId);
        pushToListPart(list, pushDto, String.valueOf(CommonStatusEnum.NO.getStatus()));
    }

    @Override
    public void pushToList(PushDto pushDto, GamePeriod period) {
        log.info("start push periodId:", period.getPeriodId());
        /* 通过快照List拿到最后一个key,设置当前推送key缓存*/
        String pushUserListKey = redisService.kryoGet(RedisConstant.getPushingKey(period.getGameId(), period
                .getPeriodId()), String.class);
        if (pushUserListKey == null) {
            List<String> list = redisService.kryoLRange(RedisConstant.getPushKeyList(period.getGameId()),
                    -1L, -1L, String.class);
            if (null == list) {
                throw new BusinessException("get push user error : getPushKeyList is null");
            }
            pushUserListKey = list.get(0);
            redisService.kryoSetEx(RedisConstant.getPushingKey(period.getGameId(), period.getPeriodId()),
                    RedisConstant.EXPIRE_TIME_SECOND_ONE_HOUR, pushUserListKey);
        }
        Integer count = redisService.kryoZCard(pushUserListKey).intValue();
        int i = 0;
        Integer pushedCount = redisService.kryoGet(RedisConstant.getPushedCountKey(period.getGameId(), period
                        .getPeriodId()),
                Integer.class);
        if (null != pushedCount && pushedCount > 0) {
            /* 获取已经发送的条数*/
            i = pushedCount;
        }
        int eachPollCount = ActivityIniCache.getActivityIniIntValue(ActivityIniConstant.PUSH_EACH_POLL_COUNT, 10000);
        for (; i < count + eachPollCount; i += eachPollCount) {
            /* 发送条数记录 */
            redisService.kryoSetEx(RedisConstant.getPushedCountKey(period.getGameId(), period.getPeriodId()),
                    RedisConstant.EXPIRE_TIME_SECOND_ONE_HOUR, i + eachPollCount);
            List<String> clientIdList = redisService.kryoZRangeByScoreGet(pushUserListKey, 0L, Long.MAX_VALUE, i, i +
                            eachPollCount,
                    String.class);
            if (clientIdList.size() > 0) {
                pushToListPart(clientIdList, pushDto, String.valueOf(CommonStatusEnum.NO.getStatus()));
            }
        }
        /* 推送结束更新schedule*/
        pushScheduleDao.updatePushSchedule(period.getGameId(), period.getPeriodId(), "IF_WINNING_NUMBER_PUSH",
                "IF_WINNING_NUMBER_PUSH_TIME");
        redisService.kryoSet(RedisConstant.getPushFlagKey(period.getGameId(), period.getPeriodId()),
                CommonStatusEnum.YES.getStatus());
        String markdown = "#### 推送服务 \n" + "> 推送结束啦\n" + "> ###### " + DateUtil.formatNowTime
                (15) + "发布 \n";
        List<String> at = new ArrayList<>();
        dingTalkRobotService.sendMassageToAll("推送服务", markdown, at);
        log.info("end push periodId:", period.getPeriodId());
    }

    private void aliPushToDeviceList(PushDto pushDto, GamePeriod period) {
        log.info("start alipush periodId:", period.getPeriodId());
        /* 通过快照List拿到最后一个key,设置当前推送key缓存*/
        String pushUserListKey = redisService.kryoGet(RedisConstant.getAliPushingKey(period.getGameId(), period
                .getPeriodId()), String.class);
        if (pushUserListKey == null) {
            List<String> list = redisService.kryoLRange(RedisConstant.getAliPushKeyList(period.getGameId()),
                    -1L, -1L, String.class);
            if (null == list) {
                throw new BusinessException("get push user error : getPushKeyList is null");
            }
            pushUserListKey = list.get(0);
            redisService.kryoSetEx(RedisConstant.getAliPushingKey(period.getGameId(), period.getPeriodId()),
                    RedisConstant.EXPIRE_TIME_SECOND_ONE_HOUR, pushUserListKey);
        }
        Integer count = redisService.kryoZCard(pushUserListKey).intValue();
        int i = 0;
        Integer pushedCount = redisService.kryoGet(RedisConstant.getAliPushedCountKey(period.getGameId(), period
                .getPeriodId()), Integer.class);
        if (null != pushedCount && pushedCount > 0) {
            /* 获取已经发送的条数*/
            i = pushedCount;
        }
        int eachPollCount = ActivityIniCache.getActivityIniIntValue(ActivityIniConstant.PUSH_EACH_POLL_COUNT, 10000);
        for (; i < count + eachPollCount; i += eachPollCount) {
            /* 发送条数记录 */
            redisService.kryoSetEx(RedisConstant.getAliPushedCountKey(period.getGameId(), period.getPeriodId()),
                    RedisConstant.EXPIRE_TIME_SECOND_ONE_HOUR, i + eachPollCount);
            List<String> clientIdList = redisService.kryoZRangeByScoreGet(pushUserListKey, 0L, Long.MAX_VALUE, i, i +
                    eachPollCount, String.class);
            if (clientIdList.size() > 0) {
                pushToListPart(clientIdList, pushDto, String.valueOf(CommonStatusEnum.YES.getStatus()));
            }
        }
    }

    @Override
    public void pushToList(PushDto pushDto) {
        /* 获取最新的推送名单*/
        // 接口调用全量推送

    }

    /* 遍历clientList 每100条调用个推一次推送 多线程处理*/
    @Override
    public void pushToListPart(List<String> clientIdList, PushDto pushDto, String pushType) {
        ExecutorService exec = ThreadPool.getInstance().getPushExec();
        CompletionService<Integer> push = new ExecutorCompletionService<>(exec);
        try {
            if (clientIdList.size() > 0) {
                int index = 1;
                int threadCount = 0;
                List<String> groupClientIdList = new ArrayList<>();
                StringBuffer sb = new StringBuffer();
                for (String clientId : clientIdList) {
                    if (pushType.equals(String.valueOf(CommonStatusEnum.YES.getStatus()))) {
                        sb.append(clientId);
                        if ((index % 100) != 0 && index != clientIdList.size()) {
                            sb.append(CommonConstant.COMMA_SPLIT_STR);
                        }
                    }
                    groupClientIdList.add(clientId);
                    // 每一百条一次推送
                    if ((index % 100) == 0 || index == clientIdList.size()) {
                        /* 推送开关*/
                        if (CommonStatusEnum.YES.getStatus() == ActivityIniCache.getActivityIniIntValue
                                (ActivityIniConstant.PUSH_SWITCH, 1)) {
                            if (!exec.isShutdown()) {
                                if (pushType.equals(String.valueOf(CommonStatusEnum.YES.getStatus()))) {
                                    push.submit(new AliyunPushTask(pushDto, "DEVICE", sb.toString(), "default"));
                                }
                                if (pushType.equals(String.valueOf(CommonStatusEnum.NO.getStatus()))) {
                                    push.submit(new PushTask(pushDto, groupClientIdList));
                                }
                            }
                            threadCount++;
                        }
                        groupClientIdList = new ArrayList<>();
                        sb = new StringBuffer();
                    }
                    index++;
                }

                // for循环次数=线程个数
                for (int i = 0; i < threadCount; i++) {
                    Future f = push.take();
                    //log.error(f.get());
                }

            } else {
                log.error("push message failed pushClientList is empty");
            }
        } catch (Exception e) {
            throw new BusinessException("push message to app is error e.message is" + e.getMessage());
        }
    }

    @Override
    public void winningNumberPush() {
        List<Long> gameIdList = new ArrayList<>(GameCache.getAllGameMap().keySet());
        for (Long gameId : gameIdList) {
            try {
                Game game = GameCache.getGame(gameId);
                if (null == game) {
                    log.error("winningNumberPush get game is null");
                    throw new BusinessException("winningNumberPush getgame is null");
                }
                /* 大盘彩*/
                if (game.getGameType().equals(Game.GAME_TYPE_COMMON)) {
                    gameWinningNumberPush(game);
                }
            } catch (Exception e) {
                // // 报警 继续下一个彩种推送
                log.error("push winingNumber error gameId is " + gameId, e);
            }
        }
    }

    /* 十五分钟执行一次*/
    @Override
    public void copyPushUsers() {
        List<Long> gameIdList = new ArrayList<>(GameCache.getAllGameMap().keySet());
        for (Long gameId : gameIdList) {
            Game game = GameCache.getGame(gameId);
            if (null == game) {
                log.error("copyPushUsers get game is null");
                throw new BusinessException("copyPushUsers get game is null");
            }
            // 大盘彩
            if (game.getGameType().equals(Game.GAME_TYPE_COMMON)) {

                // 个推
                // 存储快照
                String key = RedisConstant.getPushPhotoKey(gameId);
                // 添加一个空的set
                redisService.kryoZAddSet(RedisConstant.getDefaultClientSet(), System.currentTimeMillis(),
                        "33159e3120076f45ee4a06db852d8de5");
                //合并
                redisService.kryoZUnionStore(key, RedisConstant.getDefaultClientSet(), RedisConstant
                        .getPushClientList(game.getGameId()));
                // 设置过期时间 过期时间一个小时
                redisService.expire(key, RedisConstant.EXPIRE_TIME_SECOND_TWO_HOUR);
                // 存储 keyList
                redisService.kryoRPush(RedisConstant.getPushKeyList(gameId), key);
                redisService.kryoLtrim(RedisConstant.getPushKeyList(gameId), -9, -1);


                // aliyun
                String aliKey = RedisConstant.getAliPushPhotoKey(gameId);
                // 添加一个空的set
                redisService.kryoZAddSet(RedisConstant.getAliDefaultClientSet(), System.currentTimeMillis(),
                        "");
                //合并
                redisService.kryoZUnionStore(aliKey, RedisConstant.getAliDefaultClientSet(), RedisConstant
                        .getAliPushClientList(game.getGameId()));
                // 设置过期时间 过期时间一个小时
                redisService.expire(aliKey, RedisConstant.EXPIRE_TIME_SECOND_TWO_HOUR);
                // 存储 keyList
                redisService.kryoRPush(RedisConstant.getAliPushKeyList(gameId), aliKey);
                redisService.kryoLtrim(RedisConstant.getAliPushKeyList(gameId), -9, -1);
            }
        }
    }

    @Override
    public Boolean checkPush(String clientId, Long gameId) {
        if (null != redisService.kryoZRank(RedisConstant.getPushClientList(gameId), clientId)) {
            return Boolean.TRUE;
        }
        if (null != redisService.kryoZRank(RedisConstant.getAliPushClientList(gameId), clientId)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public void userOperate(String deviceId, String gameEn, String type) {
        // null判断 try catch
        try {
            String deviceIdConvert = loginService.generateDeviceId(deviceId);
            UserDeviceInfo userDeviceInfo = userDeviceInfoDao.getUserDeviceInfoByDeviceId(deviceIdConvert);
            if (null == userDeviceInfo) {
                throw new BusinessException("user operate push type is error :userDeviceInfo is null");
            }
            String pushGameEnStr = userDeviceInfo.getPushGameEns();
            StringBuffer pushGameEnsSb;
            Game game = GameCache.getGame(gameEn);
            // 开启推送
            if (null != type && new Integer(type) == CommonStatusEnum.YES.getStatus()) {
                // getPushGameEns 为空
                pushGameEnsSb = new StringBuffer(pushGameEnStr);
                if (StringUtils.isNotBlank(pushGameEnStr)) {
                    pushGameEnsSb.append(CommonConstant.COMMA_SPLIT_STR);
                }
                pushGameEnsSb.append(gameEn);
                // 更新缓存
                if (null != userDeviceInfo.getPushType() && userDeviceInfo.getPushType().equals(1)) {
                    redisService.kryoZAddSet(RedisConstant.getAliPushClientList(game.getGameId()), System
                            .currentTimeMillis(), userDeviceInfo.getClientId());
                } else {
                    redisService.kryoZAddSet(RedisConstant.getPushClientList(game.getGameId()), System
                            .currentTimeMillis(), userDeviceInfo.getClientId());
                }

            } else {
                // 取消推送
                pushGameEnsSb = new StringBuffer();
                if (StringUtils.isNotBlank(pushGameEnStr)) {
                    String[] pushGameEns = pushGameEnStr.split(CommonConstant.COMMA_SPLIT_STR);
                    int index = 0;
                    for (String pushGameEn : pushGameEns) {
                        if (!pushGameEn.equals(gameEn)) {
                            index++;
                            // 最后的逗号
                            pushGameEnsSb.append(pushGameEn);
                            if (index != pushGameEns.length - 1) {
                                pushGameEnsSb.append(CommonConstant.COMMA_SPLIT_STR);
                            }
                        }
                    }
                }
                // 更新缓存
                if (null != userDeviceInfo.getPushType() && userDeviceInfo.getPushType().equals(1)) {
                    redisService.kryoZRem(RedisConstant.getAliPushClientList(game.getGameId()), userDeviceInfo
                            .getClientId());
                } else {
                    redisService.kryoZRem(RedisConstant.getPushClientList(game.getGameId()), userDeviceInfo
                            .getClientId());
                }
            }
            userDeviceInfo.setPushGameEns(pushGameEnsSb.toString());
            // 更新数据库
            userDeviceInfoDao.update(userDeviceInfo);
        } catch (Exception e) {
            throw new BusinessException("user operate push type is error :" + e.getMessage());
        }
    }

    @Override
    public void rebuildClientIdList() {
        try {
            int index = 0;
            for (int i = 0; i < 100; i++) {
                // 获取所有的clientId
                List<UserDeviceInfo> userDeviceInfos = userDeviceInfoDao.getAllUserDeviceInfoByShardType(String
                        .valueOf(i));
                if (null != userDeviceInfos && userDeviceInfos.size() > 0) {
                    for (UserDeviceInfo userDeviceInfo : userDeviceInfos) {
                        if (userDeviceInfo.getUserId() != null) {
                            UserInfo userInfo = userInfoDao.getUserInfo(userDeviceInfo.getUserId());
                            if (userInfo != null && StringUtils.isNotBlank(userInfo.getRemark())) {
                                Map<String, Object> remarkMap = JSONObject.parseObject(userInfo.getRemark(), HashMap
                                        .class);
                                if (remarkMap.containsKey("lotteryType") && Integer.valueOf(remarkMap.get
                                        ("lotteryType").toString()).equals(CommonConstant.LOTTERY_TYPE_SPORTS)) {
                                    continue;
                                }
                            }
                        }
                        if (null == userDeviceInfo.getPushGameEns()) {
                            continue;
                        }
                        String[] pushGameEns = userDeviceInfo.getPushGameEns().split(CommonConstant.COMMA_SPLIT_STR);
                        for (String pushGameEn : pushGameEns) {
                            if (StringUtils.isNotBlank(pushGameEn)) {
                                Game game = GameCache.getGame(pushGameEn);
                                if (StringUtils.isNotBlank(userDeviceInfo.getClientId())) {
                                    index++;
                                    if (null == userDeviceInfo.getPushType() || !userDeviceInfo.getPushType().equals
                                            (1)) {
                                        redisService.kryoZAddSet(RedisConstant.getPushClientList(game.getGameId()),
                                                System.currentTimeMillis() + i, userDeviceInfo.getClientId());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error(">>>>>>>>>" + e.getMessage());
        }

    }

    private void gameWinningNumberPush(Game game) {
        Long gameId = game.getGameId();
        /* 获取(AwardTime)上一期的期次信息*/
        List<GamePeriod> openPeriodList = PeriodRedis.getLastAwardPeriodByGameId(gameId);
        GamePeriod openingPeriod = openPeriodList.get(0);
        if (null == openingPeriod) {
            log.error("get currentPeriod is null when push check gameId is" + gameId);
            throw new BusinessException("get currentPeriod is null when push check gameId:" + gameId);
        }
        /* 当前时间在awardTime范围内 */
        Timestamp start = DateUtil.getIntervalMinutes(openingPeriod.getAwardTime(), -180);
        Timestamp end = DateUtil.getIntervalMinutes(openingPeriod.getAwardTime(), 180);
        Timestamp now = DateUtil.getCurrentTimestamp();
        if (!DateUtil.isBetween(now, start, end)) {
            return;
        }
        /* 检查redis推送标识位*/
        String gamePushFlag = redisService.kryoGet(RedisConstant.getPushFlagKey(openingPeriod.getGameId(),
                openingPeriod.getPeriodId()), String.class);
        if (StringUtils.isNotBlank(gamePushFlag)) {
            return;
        }
        /* 检查period中是否有开奖号码*/
        if (StringUtils.isBlank(openingPeriod.getWinningNumbers())) {
            return;
        }
        /* 检查预测schedule中是否已经推送*/
        PushSchedule pushSchedule = pushScheduleDao.getPushSchedule(gameId, openingPeriod.getPeriodId());
        if (pushSchedule == null) {
            pushSchedule = new PushSchedule(gameId, openingPeriod.getPeriodId(), DateUtil
                    .getCurrentTimestamp());
            pushScheduleDao.insert(pushSchedule);
        }
        if (pushSchedule.getIfWinningNumberPush() == CommonStatusEnum.YES.getStatus()) {
            redisService.kryoSet(RedisConstant.getPushFlagKey(openingPeriod.getGameId(), openingPeriod.getPeriodId()),
                    String.valueOf(CommonStatusEnum.YES.getStatus()));
            return;
        }
        Map<String, String> pushContent = new HashMap<>();
        pushContent.put("title", CommonConstant.getWinningNumberTxt(openingPeriod.getPeriodId(), game
                .getGameName()));
        AbstractGame abstractGame = GameFactory.getInstance().getGameBean(gameId);
        pushContent.put("winNum", openingPeriod.getWinningNumbers());
        pushContent.put("type", "1");
        pushContent.put("pushUrl", abstractGame.getWinningNumberPushUrl());
        pushContent.put("text", "");
        PushDto pushDto = new PushDto(CommonConstant.APP_TITLE, CommonConstant.getWinningNumberTxt(openingPeriod
                .getPeriodId(), game.getGameName()) + openingPeriod.getWinningNumbers().replace(CommonConstant
                        .COMMON_COLON_STR,
                CommonConstant.COMMON_VERTICAL_STR), game.getGameEn(), pushContent);

        /* 阿里推送*/
        String aliPushFlagKey = RedisConstant.getAliPushFlagKey(openingPeriod.getGameId(), openingPeriod.getPeriodId());
        String aliPushFlag = redisService.kryoGet(aliPushFlagKey, String.class);

        if (IniCache.getIniValue(IniConstant.ALIYUN_PUSH_WINNING_SWITCH, "0").equals("1")) {
            if (StringUtils.isBlank(aliPushFlag)) {
                try {
                    GamePeriod currentPeriod = PeriodRedis.getCurrentPeriod(gameId);
                    redisService.kryoSetEx(aliPushFlagKey, (int) DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(),
                            currentPeriod.getAwardTime()), String.valueOf(CommonStatusEnum.YES.getStatus()));
                    aliPushToDeviceList(pushDto, openingPeriod);
                    String markdown = "#### 推送服务 \n" + "> 阿里推送结束\n" + "> ###### " + DateUtil.formatNowTime
                            (15) + "发布 \n";
                    List<String> at = new ArrayList<>();
                    dingTalkRobotService.sendMassageToAll("推送服务", markdown, at);
                } catch (Exception e) {
                    log.error("alipush exception" + e.getStackTrace());
                }
            }
        }

        /* 个推推送*/
        pushToList(pushDto, openingPeriod);
    }

    @Override
    public void createPushTask(PushTrigger pushTrigger) {
        pushTriggerDao.insert(pushTrigger);
    }

    @Override
    public void triggerPush() {
        List<PushTrigger> pushTriggerList = pushTriggerDao.getAllNeedPushRecords();
        for (PushTrigger pushTrigger : pushTriggerList) {
            // 推送标志位
            if (pushTrigger.getIsPushed() == 0) {
                // 时间
                if (DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(), pushTrigger.getPushTime()) < 60) {
                    pushTrigger.setIsPushed(1);
                    pushTriggerDao.update(pushTrigger);
                    // 全员推送
                    if (pushTrigger.getPushType().equals(0)) {
                        Map<String, String> content = new HashMap<>();
                        content.put("pushUrl", pushTrigger.getPushUrl());
                        PushDto pushDto = new PushDto(pushTrigger.getPushTitle(), pushTrigger.getPushText(),
                                "", content);
                        AliyunPushTask pushTask = new AliyunPushTask(pushDto, "ALL", "", "");
                        ThreadPool.getInstance().getPushExec().submit(pushTask);
                    }
                    // 部分推送
                    if (pushTrigger.getPushType().equals(1)) {
                        // 手机号推送
                        String[] targetValues = pushTrigger.getPushTarget().split(CommonConstant.COMMA_SPLIT_STR);
                        if (targetValues[0].length() < 12) {
                            for (String mobileSingle : targetValues) {
                                Long userId = loginService.getUserId(mobileSingle);
                                String url = "";
                                Map<String, String> content = new HashMap<>();
                                content.put("pushUrl", pushTrigger.getPushUrl());
                                PushDto pushDto = new PushDto(pushTrigger.getPushTitle(), pushTrigger.getPushText(),
                                        url, content);
                                AliyunPushTask pushTask = new AliyunPushTask(pushDto, "ACCOUNT", String.valueOf
                                        (userId), "");
                                ThreadPool.getInstance().getPushExec().submit(pushTask);
                            }
                        } else {// clientId推送
                            String url = "";
                            Map<String, String> content = new HashMap<>();
                            content.put("pushUrl", pushTrigger.getPushUrl());
                            PushDto pushDto = new PushDto(pushTrigger.getPushTitle(), pushTrigger.getPushText(), url,
                                    content);
                            pushService.pushToListPart(Arrays.asList(targetValues), pushDto, "1");
                        }
                    }
                    if (pushTrigger.getPushType().equals(2)) {
                        if (StringUtils.isBlank(pushTrigger.getPushTarget())) {
                            log.error("推送异常：" + pushTrigger.toString());
                            continue;
                        }
                        Integer pushUserType = Integer.valueOf(pushTrigger.getPushTarget());
                        pushByUserType(pushUserType, pushTrigger);
                    }
                }
            }
        }
    }

    private void pushByUserType(Integer pushUserType, PushTrigger pushTrigger) {
        String url = "";
        Map<String, String> content = new HashMap<>();
        content.put("pushUrl", pushTrigger.getPushUrl());
        PushDto pushDto = new PushDto(pushTrigger.getPushTitle(), pushTrigger.getPushText(),
                url, content);
        // 查出所有用户
        List<UserInfo> userInfos = userInfoDao.geAllUserInfos();
        if (null != userInfos && userInfos.size() > 0) {
            for (UserInfo userInfo : userInfos) {
                if (pushUserType.equals(getUserType(userInfo))) {
                    AliyunPushTask pushTask = new AliyunPushTask(pushDto, "ACCOUNT", String.valueOf
                            (userInfo.getUserId()), "");
                    ThreadPool.getInstance().getPushExec().submit(pushTask);
                }
            }
        }
    }

    @Override
    public Integer godPredictPush(Long userId, Integer pushType, PushDto pushDto) {
        Integer fanType = CommonConstant.SOCIAL_FOLLOW_FANS_TYPE_DIGIT;
        if (pushType.equals(CommonConstant.PUSH_CENTER_NOTICE_TYPE_FOOTBALL_GOLD)) {
            fanType = CommonConstant.SOCIAL_FOLLOW_FANS_TYPE_SPORT;
        }
        List<Long> userIds = socialUserFansDao.getUserFansUserId(userId, fanType);
        for (Long tempUserId : userIds) {
            if (userInfoService.checkUserReceivePush(tempUserId, pushType)) {
                AliyunPushTask pushTask = new AliyunPushTask(pushDto, "ACCOUNT", String.valueOf(tempUserId), "");
                ThreadPool.getInstance().getPushExec().submit(pushTask);
            }
        }

        return 1;
    }

    private Integer getUserType(UserInfo userInfo) {
        Integer userType = 2;
        if (StringUtils.isBlank(userInfo.getRemark())) {
            return userType;
        }
        Map<String, Object> remarkMap = JSONObject.parseObject(userInfo.getRemark(), HashMap.class);
        if (!remarkMap.containsKey("lotteryType")) {
            return userType;
        }
        return Integer.valueOf(remarkMap.get("lotteryType").toString());
    }
}
