package com.mojieai.predict.service.impl;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.cache.IniCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.*;
import com.mojieai.predict.entity.bo.DetailMatchInfo;
import com.mojieai.predict.entity.dto.HttpParamDto;
import com.mojieai.predict.entity.dto.PushDto;
import com.mojieai.predict.entity.po.*;
import com.mojieai.predict.entity.vo.ActivityProgramVo;
import com.mojieai.predict.entity.vo.ResultVo;
import com.mojieai.predict.entity.vo.UserLoginVo;
import com.mojieai.predict.enums.GameEnum;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.*;
import com.mojieai.predict.service.beanself.BeanSelfAware;
import com.mojieai.predict.thread.AliyunPushTask;
import com.mojieai.predict.thread.ThreadPool;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.HttpServiceUtils;
import com.mojieai.predict.util.Md5Util;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by tangxuelong on 2017/11/7.
 */
@Service
public class ActivityServiceImpl implements ActivityService, BeanSelfAware {
    @Autowired
    private RedisService redisService;
    @Autowired
    private PredictNumService predictNumService;
    @Autowired
    private LoginService loginService;
    @Autowired
    private ActivityInfoDao activityInfoDao;
    @Autowired
    private ActivityAwardLevelDao activityAwardLevelDao;
    @Autowired
    private ActivityDateUserInfoDao activityDateUserInfoDao;
    @Autowired
    private ActivityUserInfoDao activityUserInfoDao;
    @Autowired
    private ActivityUserLogDao activityUserLogDao;
    @Autowired
    private IndexUserSocialCodeDao indexUserSocialCodeDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private PayService payService;
    @Autowired
    private VipMemberService vipMemberService;
    @Autowired
    private FestivalQuestionDao festivalQuestionDao;
    @Autowired
    private FestivalAnswerDao festivalAnswerDao;
    @Autowired
    private SMSService smsService;
    @Autowired
    private UserSignService userSignService;
    @Autowired
    private ActivityProgramDao activityProgramDao;
    @Autowired
    private UserCouponService userCouponService;
    @Autowired
    private CouponConfigDao couponConfigDao;
    @Autowired
    private VipMemberDao vipMemberDao;
    @Autowired
    private MatchScheduleDao matchScheduleDao;
    @Autowired
    private ThirdHttpService thirdHttpService;
    @Autowired
    private DanguanProgramDao danguanProgramDao;
    @Autowired
    private DanguanProgramUserDao danguanProgramUserDao;
    @Autowired
    private DanguanProgramCardsDao danguanProgramCardsDao;
    @Autowired
    private DanguanProgramUserTimesDao danguanProgramUserTimesDao;
    @Autowired
    private UserAccountFlowDao userAccountFlowDao;

    private ActivityService self;

    protected Logger log = LogConstant.commonLog;


    @Override
    public void share(Long gameId, Long userId, String openId) {
        try {
            /* 检查用户是否已经给他人添加次数*/
            String redisKey = RedisConstant.getUserShareWx(gameId, userId);
            Long rank = redisService.kryoZRank(redisKey, openId);
            if (null != rank) {
                return;
            }
            /* 添加预测次数*/
            GamePeriod period = PeriodRedis.getCurrentPeriod(gameId);

            redisService.kryoZAddSet(redisKey, System.currentTimeMillis(), openId);
            redisService.expire(redisKey, (int) DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(), period
                    .getAwardTime()));

            /* 数据统计*/
            String redisDayKey = "SHARE_USER_RANK:" + DateUtil.getCurrentDay(DateUtil.DATE_FORMAT_YYYYMMDD);
            redisService.kryoZAddSet(redisDayKey, System.currentTimeMillis(), userId);
            redisService.expire(redisDayKey, 60 * 60 * 24 * 30);
            predictNumService.updateUserPredictMaxNums(gameId, period.getPeriodId(), userId, 1);

            // 大乐透
            Game game = GameCache.getGame(GameConstant.DLT);
            GamePeriod gamePeriod = PeriodRedis.getCurrentPeriod(game.getGameId());
            predictNumService.updateUserPredictMaxNums(gamePeriod.getGameId(), gamePeriod.getPeriodId(), userId, 1);
        } catch (Exception e) {
            log.error("share error openId" + openId, e);
            throw new BusinessException("share error", e);
        }
    }

    @Override
    public Integer shareResult(Long gameId, Long userId) {
        String redisKey = RedisConstant.getUserShareWx(gameId, userId);
        List<String> openIds = redisService.kryoZRange(redisKey, 0L, -1L, String.class);
        if (null == openIds) {
            return 0;
        }
        return openIds.size();
    }

    @Override
    public Map<String, String> getJsApiTicket(String pageUrl) {
        /* 获取access_token（有效期7200秒，开发者必须在自己的服务全局缓存access_token）*/
        String wx_access_token = redisService.kryoGet("wx_access_token", String.class);
        if (StringUtils.isBlank(wx_access_token)) {
            StringBuffer url = new StringBuffer();
            url.append("https://api.weixin.qq.com/cgi-bin/token?").append("grant_type=client_credential&appid=")
                    .append(CommonConstant.WX_APP_ID).append("&secret=").append(CommonConstant.WX_APP_SECRET);
            String result = HttpServiceUtils.sendHttpsPostRequest(url.toString(), "", HttpParamDto.DEFAULT_CHARSET);
            log.info("wx_access_token:" + result);
            wx_access_token = JSONObject.parseObject(result).getString("access_token");

            if (null != wx_access_token) {
                redisService.kryoSetEx("wx_access_token", 7200, wx_access_token);
            }
        }
        log.info("wx_access_token:" + wx_access_token);
        /* 用第一步拿到的access_token 采用http GET方式请求获得jsapi_ticket*/
        String jsApiTicket = redisService.kryoGet("wx_js_api_ticket", String.class);
        int activeTime = 0;
        if (StringUtils.isBlank(jsApiTicket)) {
            StringBuffer getTicketUrl = new StringBuffer();
            getTicketUrl.append("https://api.weixin.qq.com/cgi-bin/ticket/getticket?").append("access_token=").append
                    (wx_access_token).append("&type=jsapi");
            String getTicketResult = HttpServiceUtils.sendRequest(getTicketUrl.toString());
            if (StringUtils.isNotBlank(getTicketResult)) {
                Map ticketResult = JSONObject.parseObject(getTicketResult, HashMap.class);
                if (ticketResult.containsKey("ticket") && ticketResult.get("ticket") != null) {
                    jsApiTicket = String.valueOf(ticketResult.get("ticket"));
                }

                if (ticketResult.containsKey("expires_in") && ticketResult.get("expires_in") != null) {
                    activeTime = Integer.parseInt(ticketResult.get("expires_in").toString());
                }
            }
            if (StringUtils.isNotBlank(jsApiTicket)) {
                redisService.kryoSetEx("wx_js_api_ticket", activeTime, jsApiTicket);
            }
        }
        log.info("wx_access_token:" + jsApiTicket);

        Random random = new Random();
        String nonceStr = Md5Util.getMD5String(String.valueOf(random.nextInt(10000)).getBytes());
        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        String sign = "jsapi_ticket=" + jsApiTicket + "&noncestr=" + nonceStr + "&timestamp=" + timeStamp + "&url=" +
                pageUrl;
        log.info("wx_access_token:" + sign);
        String signature = getSha1(sign);
        log.info("wx_access_token:" + signature);
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("timestamp", timeStamp);
        resultMap.put("nonceStr", nonceStr);
        resultMap.put("signature", signature);
        resultMap.put("appId", CommonConstant.WX_APP_ID);
        return resultMap;
    }

    @Override
    public Map<String, Integer> shareUsersToday(Long gameId) {
        String redisDayKey = "SHARE_USER_RANK:" + DateUtil.getCurrentDay(DateUtil.DATE_FORMAT_YYYYMMDD);
        Map<String, Integer> shareUserRanks = new HashMap<>();
        List<Long> userIds = redisService.kryoZRange(redisDayKey, 0L, -1L, Long.class);
        if (userIds != null && userIds.size() > 0) {
            for (Long userId : userIds) {
                UserLoginVo userLoginVo = loginService.getUserLoginVo(userId);
                shareUserRanks.put(userLoginVo.getMobile(), shareResult(gameId, userId));
            }
        }
        return shareUserRanks;
    }

    @Override
    public ResultVo commonTakePartInActivity(Integer activityId, Long userId) {
        ResultVo resultVo = new ResultVo(ResultConstant.ERROR, "");
        if (!checkActivityIsEnabled(activityId)) {
            resultVo.setMsg("活动已结束");
            return resultVo;
        }
        if (checkUserTakepartActivity(userId, activityId, -1)) {
            resultVo.setMsg("您已参与活动");
            return resultVo;
        }
        ActivityUserInfo activityUserInfo = new ActivityUserInfo(activityId, userId, 1, null, DateUtil
                .getCurrentTimestamp(), DateUtil.getCurrentTimestamp());
        try {
            activityUserInfoDao.insert(activityUserInfo);
            resultVo.setCode(ResultConstant.SUCCESS);
        } catch (Exception e) {
            log.error(activityId + "：参加活动异常", e);
        }
        return resultVo;
    }

    @Override
    public Map<String, Object> drawLotteryInfo(Integer activityId, Long userId) {
        // 抽奖次数
        Map<String, Object> resultMap = new HashMap<>();
        String dateId = DateUtil.getCurrentDay(DateUtil.DATE_FORMAT_YYYYMMDD);
        ActivityDateUserInfo activityDateUserInfo = activityDateUserInfoDao.getUserTimesByDate(activityId, userId,
                dateId, Boolean.FALSE);
        if (null == activityDateUserInfo) {
            /* 当日抽奖次数初始化*/
            Integer defaultTimes = 1;

            String lastDateId = DateUtil.getYesterday(DateUtil.DATE_FORMAT_YYYYMMDD);
            ActivityDateUserInfo activityUserInfoYesterday = activityDateUserInfoDao.getUserTimesByDate(activityId,
                    userId, lastDateId, Boolean.FALSE);

            if (null != activityUserInfoYesterday && activityUserInfoYesterday.getTimes() > 1) {
                defaultTimes = activityUserInfoYesterday.getTimes();
            }
            activityDateUserInfo = new ActivityDateUserInfo(activityId, userId, dateId, defaultTimes, null, DateUtil
                    .getCurrentTimestamp(), DateUtil.getCurrentTimestamp());
            activityDateUserInfoDao.insert(activityDateUserInfo);
        }
        resultMap.put("leftTimes", activityDateUserInfo.getTimes());

        // 抽奖列表
        List<Map<String, Object>> levelIds = new ArrayList<>();
        List<ActivityUserLog> activityUserLogs = activityUserLogDao.getUserLog(activityId, userId);
        if (null != activityUserLogs) {
            for (ActivityUserLog activityUserLog : activityUserLogs) {
                Map<String, Object> levelList = new HashMap<>();
                levelList.put("levelId", activityUserLog.getLevelId());
                levelList.put("time", DateUtil.formatDate(activityUserLog.getCreateTime(), DateUtil.DATE_FORMAT_Y_M_D));
                levelIds.add(levelList);
            }
        }
        resultMap.put("levelIds", levelIds);

        //总次数
        ActivityUserInfo activityUserInfo = activityUserInfoDao.getUserTotalTimes(activityId, userId);
        if (null == activityUserInfo) {
            activityUserInfo = new ActivityUserInfo(activityId, userId, 0, null, DateUtil.getCurrentTimestamp(),
                    DateUtil.getCurrentTimestamp());
            activityUserInfoDao.insert(activityUserInfo);
        }
        resultMap.put("totalTimes", activityUserInfo.getTotalTimes());

        // 已经分享人数
        String redisKey = RedisConstant.getUserShareWxActivity(activityId, userId);
        Long shareNums = redisService.kryoZCard(redisKey);
        resultMap.put("shareNums", shareNums.intValue());

        // 当天参与次数
        Integer userDateTimes = 0;
        List<ActivityUserLog> activityUserLogList = activityUserLogDao.getDateUserLog(activityId, userId, dateId);
        if (null != activityUserLogs) {
            userDateTimes = activityUserLogList.size();
        }
        resultMap.put("userDateTimes", userDateTimes);

        resultMap.put("userId", userId);

        resultMap.put("isOver", Boolean.FALSE);
        ActivityInfo activityInfo = activityInfoDao.getActivityInfo(activityId);
        /* 检查活动是否已经失效或者结束*/
        if (null == activityInfo) {
            resultMap.put("isOver", Boolean.TRUE);
        }
        if (!DateUtil.isBetween(DateUtil.getCurrentTimestamp(), activityInfo.getStartTime(), activityInfo.getEndTime
                ())) {
            resultMap.put("isOver", Boolean.TRUE);
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> drawLottery(Integer activityId, Long userId) {
        /* 获取活动信息*/ // TODO: 2017/11/23 activity endTime 放到内存 
        Map<String, Object> awardResult = new HashMap<>();
        ActivityInfo activityInfo = activityInfoDao.getActivityInfo(activityId);
        /* 检查活动是否已经失效或者结束*/
        if (null == activityInfo) {
            return getActivityFinished(awardResult, "活动已经结束");
        }
        if (!DateUtil.isBetween(DateUtil.getCurrentTimestamp(), activityInfo.getStartTime(), activityInfo.getEndTime
                ())) {
            return getActivityFinished(awardResult, "活动已经结束");
        }

        /* 用户当日抽奖次数校验*/
        String dateId = DateUtil.getCurrentDay(DateUtil.DATE_FORMAT_YYYYMMDD);
        ActivityDateUserInfo activityDateUserInfo = activityDateUserInfoDao.getUserTimesByDate(activityId, userId,
                dateId, Boolean.FALSE);
        if (null == activityDateUserInfo) {
            /* 当日抽奖次数初始化*/
            Integer defaultTimes = 1;

            String lastDateId = DateUtil.getYesterday(DateUtil.DATE_FORMAT_YYYYMMDD);
            ActivityDateUserInfo activityUserInfoYesterday = activityDateUserInfoDao.getUserTimesByDate(activityId,
                    userId, lastDateId, Boolean.FALSE);

            if (null != activityUserInfoYesterday && activityUserInfoYesterday.getTimes() > 1) {
                defaultTimes = activityUserInfoYesterday.getTimes();
            }
            activityDateUserInfo = new ActivityDateUserInfo(activityId, userId, dateId, defaultTimes, null, DateUtil
                    .getCurrentTimestamp(), DateUtil.getCurrentTimestamp());
            activityDateUserInfoDao.insert(activityDateUserInfo);
        }
        if (activityDateUserInfo.getTimes() <= 0) {
            return getActivityFinished(awardResult, "今日没有可用次数");
        }
        ActivityUserInfo activityUserInfo = activityUserInfoDao.getUserTotalTimes(activityId, userId);
        if (null == activityUserInfo) {
            activityUserInfo = new ActivityUserInfo(activityId, userId, 0, null, DateUtil.getCurrentTimestamp(),
                    DateUtil.getCurrentTimestamp());
            activityUserInfoDao.insert(activityUserInfo);
        }

        /* 抽奖*/
        Random random = new Random();
        Integer awardValue = random.nextInt(100) + 1; // 1-100

        ActivityAwardLevel awardLevel = new ActivityAwardLevel();
        ActivityAwardLevel defaultAwardLevel = new ActivityAwardLevel();
        /* 按照levelId顺序*/
        List<ActivityAwardLevel> activityAwardLevels = activityAwardLevelDao.getAwardLevelByActivityId(activityId);

        for (ActivityAwardLevel activityAwardLevel : activityAwardLevels) {
            String remark = activityAwardLevel.getRemark();
            Map<String, Object> remarkMap = (Map<String, Object>) JSONObject.parse(remark);
            Map<String, Object> userInfoRemarkMap = (Map<String, Object>) JSONObject.parse(activityUserInfo.getRemark
                    ());
            /*  是否分享(用户) */
            if (null != userInfoRemarkMap && StringUtils.isNotBlank(userInfoRemarkMap.get("isShare").toString())) {
                String sharePercent = remarkMap.get("sharePercent").toString();
                if (StringUtils.isNotBlank(sharePercent) && Integer.valueOf(sharePercent) >= awardValue) {
                    awardLevel = activityAwardLevel;
                    break;
                }
            } else {
                if (Integer.valueOf(remarkMap.get("awardPercent").toString()) >= awardValue) {
                    awardLevel = activityAwardLevel;
                    break;
                }
            }
        }
        Boolean mustRight = Boolean.FALSE;
        for (ActivityAwardLevel activityAwardLevel : activityAwardLevels) {
            String remark = activityAwardLevel.getRemark();
            Map<String, Object> remarkMap = (Map<String, Object>) JSONObject.parse(remark);
            /* 3.抽奖10次必中奖级(优先)*/
            if (null != remarkMap && null != remarkMap.get("mustRight") && StringUtils.isNotBlank(remarkMap.get
                    ("mustRight").toString())) {
                Integer rightTimes = Integer.valueOf(remarkMap.get("mustRight").toString());
                if (activityUserInfo.getTotalTimes() > 0 && activityUserInfo.getTotalTimes() < 51 && (activityUserInfo
                        .getTotalTimes() + 1) % rightTimes == 0) {
                    awardLevel = activityAwardLevel;
                    mustRight = Boolean.TRUE;
                    break;
                }
            }
            //defaultAwardLevel
            if (null != remarkMap && null != remarkMap.get("defaultAwardLevel") && StringUtils.isNotBlank(remarkMap.get
                    ("defaultAwardLevel").toString())) {
                defaultAwardLevel = activityAwardLevel;
            }
        }
        if (!mustRight) {
            /* 奖品条件判断*/
            /* 如果奖品已经派发完毕，1. 奖品每日上限*/
            if (awardLevel.getDayLeftCount() < 0) {
                log.error("抽奖活动派发奖品异常");
            }
            /* 奖级初始化*/
            if (!DateUtil.formatDate(awardLevel.getLastAwardTime(), DateUtil.DATE_FORMAT_YYYYMMDD).equals(dateId)) {
                awardLevel.setDayLeftCount(awardLevel.getDayAwardCount());

                awardLevel.setUpdateTime(DateUtil.getCurrentTimestamp());
            }
            if (awardLevel.getDayLeftCount() <= 0) {
                /* 派送其他奖品*/
                awardLevel = defaultAwardLevel;
            }
            /* 2.同一个人一天只能获得一次奖级奖品*/
            List<ActivityUserLog> activityUserLogList = activityUserLogDao.getDateUserLog(activityId, userId, dateId);

            String awardLevelRemark = awardLevel.getRemark();
            Map<String, Object> remarkMap = (Map<String, Object>) JSONObject.parse(awardLevelRemark);

            if (null != remarkMap && null != remarkMap.get("dayUserLimit") && StringUtils.isNotBlank(remarkMap.get
                    ("dayUserLimit").toString()) && activityUserLogList != null) {
                int index = 0;
                for (ActivityUserLog activityUserLog : activityUserLogList) {
                    if (activityUserLog.getLevelId().equals(awardLevel.getLevelId())) {
                        index++;
                    }
                }
                if (index >= Integer.valueOf(remarkMap.get("dayUserLimit").toString())) {
                    awardLevel = defaultAwardLevel;
                }
            }
        }


        ActivityDateUserInfo activityDateUserInfoLock = activityDateUserInfoDao.getUserTimesByDate(activityId, userId,
                dateId, Boolean.TRUE);
        if (activityDateUserInfoLock.getTimes() <= 0) {
            return getActivityFinished(awardResult, "今日没有可用次数");
        }
        self.updateActivityInfo(awardLevel, activityId, userId, activityDateUserInfo, activityUserInfo, dateId,
                mustRight);
        // 如果是智慧预测 直接+1
        if (awardLevel.getLevelId() == 3) {
            Game game = GameCache.getGame(GameConstant.SSQ);
            GamePeriod period = PeriodRedis.getCurrentPeriod(game.getGameId());
            predictNumService.updateUserPredictMaxNums(game.getGameId(), period.getPeriodId(), userId, 1);

            Game gameDlt = GameCache.getGame(GameConstant.DLT);
            GamePeriod periodDlt = PeriodRedis.getCurrentPeriod(gameDlt.getGameId());
            predictNumService.updateUserPredictMaxNums(gameDlt.getGameId(), periodDlt.getPeriodId(), userId, 1);
        }
        return getActivityAwarded(awardResult, awardLevel);
    }

    @Override
    public void drawLotteryShare(Integer activityId, Long userId, String openId) {
        try {
            /* 检查用户是否已经给他人添加次数*/
            String redisKey = RedisConstant.getUserShareWxActivity(activityId, userId);
            Long rank = redisService.kryoZRank(redisKey, openId);
            if (null != rank) {
                return;
            }
            redisService.kryoZAddSet(redisKey, System.currentTimeMillis(), openId);
            redisService.expire(redisKey, 60 * 60 * 24 * 30);

            /* 数据统计*/
            String redisDayKey = "ACTIVITY_SHARE_USER_RANK:" + DateUtil.getCurrentDay(DateUtil.DATE_FORMAT_YYYYMMDD);
            redisService.kryoZAddSet(redisDayKey, System.currentTimeMillis(), userId);
            redisService.expire(redisDayKey, 60 * 60 * 24 * 30);

            // 添加次数
            String dateId = DateUtil.getCurrentDay(DateUtil.DATE_FORMAT_YYYYMMDD);
            ActivityDateUserInfo activityDateUserInfo = activityDateUserInfoDao.getUserTimesByDate(activityId,
                    userId, dateId, Boolean.FALSE);
            activityDateUserInfo.setTimes(activityDateUserInfo.getTimes() + 1);
            activityDateUserInfo.setUpdateTime(DateUtil.getCurrentTimestamp());
            activityDateUserInfoDao.update(activityDateUserInfo);

            String text = "您的好友帮您增加了一次抽奖机会！";
            String url = "";
            Map<String, String> content = new HashMap<>();
            content.put("pushUrl", "mjLottery://mjNative?page=wap&url=https://predictdebug.mojieai" +
                    ".com/web/drawLottery/app.html");
            PushDto pushDto = new PushDto(CommonConstant.APP_TITLE, text, url, content);
            AliyunPushTask pushTask = new AliyunPushTask(pushDto, "ACCOUNT", String.valueOf(userId), "killPush");
            ThreadPool.getInstance().getPushExec().submit(pushTask);
        } catch (Exception e) {
            log.error("share error openId" + openId, e);
            throw new BusinessException("share error", e);
        }
    }

    @Override
    public void drawNumberShare(Integer activityId, Long userId, String openId) {
        try {
            /* 检查用户是否已经给他人添加次数*/
            GamePeriod period = PeriodRedis.getCurrentPeriod(GameCache.getGame(GameConstant.SSQ).getGameId());
            String redisKey = RedisConstant.getUserShareWxActivityPeriod(activityId, userId, period.getPeriodId());
            Long rank = redisService.kryoZRank(redisKey, openId);
            if (null != rank) {
                return;
            }
            // 查询是否是新用户
            User user = userDao.getUserByUserId(Long.parseLong(openId), Boolean.FALSE);
            if ((int) DateUtil.getDiffMinutes(user.getCreateTime(), DateUtil.getCurrentTimestamp()) > 10) {
                return;
            }
            redisService.kryoZAddSet(redisKey, System.currentTimeMillis(), openId);
            redisService.expire(redisKey, 60 * 60 * 24 * 30);

            /* 数据统计*/
            String redisDayKey = "ACTIVITY_SHARE_USER_RANK:" + period.getPeriodId();
            redisService.kryoZAddSet(redisDayKey, System.currentTimeMillis(), userId);
            redisService.expire(redisDayKey, 60 * 60 * 24 * 30);

            String text = "您的好友帮您增加了一次股份领取机会！";
            String url = "";
            Map<String, String> content = new HashMap<>();
            content.put("pushUrl", "mjlottery://mjnative?page=wap&url=https://predictapi.mojieai" +
                    ".com/web/drawnumberNew/app.html");
            PushDto pushDto = new PushDto(CommonConstant.APP_TITLE, text, url, content);
            AliyunPushTask pushTask = new AliyunPushTask(pushDto, "ACCOUNT", String.valueOf(userId), "killPush");
            ThreadPool.getInstance().getPushExec().submit(pushTask);
        } catch (Exception e) {
            log.error("share error openId" + openId, e);
            throw new BusinessException("share error", e);
        }
    }

    @Override
    public void drawLotteryShareOut(Integer activityId, Long userId) {
        ActivityUserInfo activityUserInfo = activityUserInfoDao.getUserTotalTimes(activityId, userId);
        if (null == activityUserInfo) {
            activityUserInfo = new ActivityUserInfo(activityId, userId, 0, null, DateUtil.getCurrentTimestamp(),
                    DateUtil.getCurrentTimestamp());
            activityUserInfoDao.insert(activityUserInfo);
        }
        String remark = activityUserInfo.getRemark();
        Map<String, Object> remarkMap = (Map<String, Object>) JSONObject.parse(remark);
        remarkMap.put("isShare", 1);
        activityUserInfo.setRemark(JSONUtils.toJSONString(remarkMap));
        activityUserInfoDao.update(activityUserInfo);
    }

    @Transactional
    public void updateActivityInfo(ActivityAwardLevel awardLevel, Integer activityId, Long userId, ActivityDateUserInfo
            activityDateUserInfo, ActivityUserInfo activityUserInfo, String dateId, Boolean mustRight) {
        if (!mustRight) {
            /* 更新活动奖级表*/
            awardLevel.setDayLeftCount(awardLevel.getDayLeftCount() - 1);
            awardLevel.setLastAwardTime(DateUtil.getCurrentTimestamp());
            awardLevel.setUpdateTime(DateUtil.getCurrentTimestamp());
            activityAwardLevelDao.update(awardLevel);
        }

        /* 更新用户奖级获取表*/
        ActivityUserLog currentUserLog = new ActivityUserLog(null, activityId, userId, awardLevel.getLevelId(), dateId,
                DateUtil.getCurrentTimestamp());
        activityUserLogDao.insert(currentUserLog);

        /* 更新用户奖级当日抽奖次数减少-1*/
        activityDateUserInfo.setTimes(activityDateUserInfo.getTimes() - 1);
        activityDateUserInfo.setUpdateTime(DateUtil.getCurrentTimestamp());
        activityDateUserInfoDao.update(activityDateUserInfo);

        /* 更新用户活动总次数*/
        activityUserInfo.setTotalTimes(activityUserInfo.getTotalTimes() + 1);
        activityUserInfo.setUpdateTime(DateUtil.getCurrentTimestamp());
        activityUserInfoDao.update(activityUserInfo);
    }

    private Map<String, Object> getActivityFinished(Map<String, Object> awardResult, String msg) {
        awardResult.put("awardResult", "");
        awardResult.put("awardStatus", -1);
        awardResult.put("awardMsg", msg);
        return awardResult;
    }

    private Map<String, Object> getSuccess(Map<String, Object> awardResult, String msg) {
        awardResult.put("awardResult", "");
        awardResult.put("awardStatus", 0);
        awardResult.put("awardMsg", msg);
        return awardResult;
    }

    private Map<String, Object> getActivityAwarded(Map<String, Object> awardResult, ActivityAwardLevel
            activityAwardLevel) {
        awardResult.put("awardResult", activityAwardLevel.getLevelId());
        awardResult.put("awardStatus", 1);
        awardResult.put("awardMsg", "恭喜您中奖了");
        return awardResult;
    }

    public static String getSha1(String str) {
        if (str == null || str.length() == 0) {
            return null;
        }
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        try {
            MessageDigest mdTemp = MessageDigest.getInstance("SHA1");
            mdTemp.update(str.getBytes("UTF-8"));

            byte[] md = mdTemp.digest();
            int j = md.length;
            char buf[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                buf[k++] = hexDigits[byte0 >>> 4 & 0xf];
                buf[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(buf);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void setSelf(Object proxyBean) {
        self = (ActivityService) proxyBean;
    }


    @Override
    public void rebuildLastDayTimes() {
        Integer activityId = 201711001;
        String dateId = "20171127";
        String LastDateId = "20171126";
        List<ActivityDateUserInfo> activityDateUserInfos = activityDateUserInfoDao.getUserByDate(activityId, dateId,
                Boolean.FALSE);

        for (ActivityDateUserInfo activityDateUserInfo : activityDateUserInfos) {
            ActivityDateUserInfo aduLastDay = activityDateUserInfoDao.getUserTimesByDate(activityId,
                    activityDateUserInfo.getUserId(), LastDateId, Boolean.FALSE);
            if (null != aduLastDay && aduLastDay.getTimes() > 1) {
                activityDateUserInfo.setTimes(activityDateUserInfo.getTimes() + (aduLastDay.getTimes() - 1));
                activityDateUserInfoDao.update(activityDateUserInfo);
            }
        }


    }

    @Override
    public Map<String, Object> drawNumberAward(Integer activityId, String periodId, Integer levelId, Integer
            awardAmount) {
        // 活动+期次+号码方案 的 奖金 存redis
        Map<String, Object> resultMap = new HashMap<>();
        String key = "drawNumberAward:" + String.valueOf(activityId) + periodId + String.valueOf(levelId);
        redisService.kryoSetEx(key, RedisConstant.EXPIRE_TIME_SECOND_THIRTY_DAY, awardAmount);
        Integer amount = redisService.kryoGet(key, Integer.class);
        resultMap.put("key", key);
        resultMap.put("amount", amount);
        return resultMap;
    }

    /* 排行榜*/
    private String getRedisRankKey(Long gameId, String periodId, String rankType, String socialType) {
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

                return RedisConstant.getEncircleWeekRank(gameId, getWeekIdByDate(period.getAwardTime()));
            }
            if (socialType.equals(CommonConstant.SOCIAL_CODE_TYPE_KILL)) {
                return RedisConstant.getKillWeekRank(gameId, getWeekIdByDate(period.getAwardTime()));
            }
        }
        if (rankType.equals(CommonConstant.SOCIAL_RANK_TYPE_MONTH)) {
            GamePeriod period = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, periodId);
            if (socialType.equals(CommonConstant.SOCIAL_CODE_TYPE_ENCIRCLE)) {
                return RedisConstant.getEncircleMonthRank(gameId, getMonthIdByDate(period.getAwardTime()));
            }
            if (socialType.equals(CommonConstant.SOCIAL_CODE_TYPE_KILL)) {
                return RedisConstant.getKillMonthRank(gameId, getMonthIdByDate(period.getAwardTime()));
            }
        }
        return null;
    }

    /* 获取当前周ID*/
    private String getCurrentWeekId() {
        Calendar current = Calendar.getInstance();
        return new StringBuffer().append(current.get(Calendar.YEAR)).append(DateUtil.getWeekOfYearOfCurrentDay() - 1)
                .toString();
    }

    /* 获取当前月ID*/
    private String getCurrentMonthId() {
        return new StringBuffer().append(DateUtil.getCurrentMonth()).toString();
    }

    /* 获取指定周ID*/
    private String getWeekIdByDate(Date date) {
        Calendar current = Calendar.getInstance();
        return new StringBuffer().append(current.get(Calendar.YEAR)).append(DateUtil.getWeekOfYear(date) - 1)
                .toString();
    }

    /* 获取指定月ID*/
    private String getMonthIdByDate(Date date) {
        return new StringBuffer().append(DateUtil.getMonth(new Timestamp(date.getTime()))).toString();
    }

    private Integer historyLevelMap(String periodLevel) {
        Map<String, Integer> historyLevelMap = new HashMap<>();
        historyLevelMap.put("20171531", 5);
        historyLevelMap.put("20171532", 5);
        historyLevelMap.put("20171533", 5);
        historyLevelMap.put("20171534", 50);
        historyLevelMap.put("20171535", 100);
        historyLevelMap.put("20171536", 100);
        historyLevelMap.put("20171537", 5);
        historyLevelMap.put("20171538", 5);
        historyLevelMap.put("20171539", 5);
        historyLevelMap.put("201715310", 50);
        historyLevelMap.put("201715311", 100);
        historyLevelMap.put("201715312", 100);

        historyLevelMap.put("20171541", 5);
        historyLevelMap.put("20171542", 5);
        historyLevelMap.put("20171543", 5);
        historyLevelMap.put("20171544", 30);
        historyLevelMap.put("20171545", 55);
        historyLevelMap.put("20171546", 30);
        historyLevelMap.put("20171547", 5);
        historyLevelMap.put("20171548", 5);
        historyLevelMap.put("20171549", 5);

        historyLevelMap.put("20180011", 5);
        historyLevelMap.put("20180012", 5);
        historyLevelMap.put("20180013", 5);
        historyLevelMap.put("20180014", 50);
        historyLevelMap.put("20180015", 100);
        historyLevelMap.put("20180016", 50);
        historyLevelMap.put("20180017", 5);
        historyLevelMap.put("20180018", 5);
        historyLevelMap.put("20180019", 5);

        historyLevelMap.put("20180021", 5);
        historyLevelMap.put("20180022", 5);
        historyLevelMap.put("20180023", 5);
        historyLevelMap.put("20180024", 50);
        historyLevelMap.put("20180025", 100);
        historyLevelMap.put("20180026", 50);
        historyLevelMap.put("20180027", 5);
        historyLevelMap.put("20180028", 5);
        historyLevelMap.put("20180029", 5);
        historyLevelMap.put("201800210", 100);
        historyLevelMap.put("201800211", 50);
        return historyLevelMap.get(periodLevel);
    }


    /* 分享送金币 送会员活动分享接口*/
    @Override
    @Transactional
    public Map<String, Object> newShare(Integer activityId, Long userId) {
        Map<String, Object> resultMap = new HashMap<>();
        // 当天的首次分享送2金币
        String dateId = DateUtil.getCurrentDay(DateUtil.DATE_FORMAT_YYYYMMDD);
        ActivityDateUserInfo activityDateUserInfo = activityDateUserInfoDao.getUserTimesByDate(activityId, userId,
                dateId, Boolean.TRUE);
        if (null != activityDateUserInfo) {
            return resultMap;
        }

        UserLoginVo userLoginVo = loginService.getUserLoginVo(userId);
        redisService.kryoZAddSet("ACTIVITY_MARQUEE", userId, "【" + userLoginVo.getNickName()
                + "】分享活动获得2金币");
        redisService.expire("ACTIVITY_MARQUEE", RedisConstant.EXPIRE_TIME_SECOND_THIRTY_DAY);

        // 插入记录，送金币
        activityDateUserInfo = new ActivityDateUserInfo(activityId, userId, dateId, 2, null, DateUtil
                .getCurrentTimestamp(), DateUtil.getCurrentTimestamp());
        activityDateUserInfoDao.insert(activityDateUserInfo);

        // 送金币
        resultMap = payService.fillAccount(userId, null, Long.parseLong(String.valueOf(2)), CommonConstant
                .PAY_TYPE_GOLD_COIN, null, Long.parseLong(String.valueOf(2)), "每日分享", "127.0.0.1", null);
        return resultMap;
    }

    /* 新用户注册，送会员*/
    @Override
    @Transactional
    public Map<String, Object> shareUserRegister(Integer activityId, Long fromUserId, Long userId, Integer vipType) {

        // 分享用户的总次数不能超过10次
        User fromUser = userDao.getUserByUserId(fromUserId, Boolean.TRUE);
        ActivityUserInfo activityUserInfo = activityUserInfoDao.getUserTotalTimes(activityId, fromUserId);

        if (null == activityUserInfo) {
            activityUserInfo = new ActivityUserInfo(activityId, fromUserId, 0, null, DateUtil.getCurrentTimestamp(),
                    DateUtil.getCurrentTimestamp());
            activityUserInfoDao.insert(activityUserInfo);
        }

        // 用户分享总次数小于10次，会员天数+1
        if (activityUserInfo.getTotalTimes() < 10) {
            vipMemberService.adminGiftVip(fromUserId, 1L, VipMemberConstant.VIP_SOURCE_TYPE_ADMIN, vipType);
            UserLoginVo userLoginVo = loginService.getUserLoginVo(fromUserId);
            // 添加一下跑马灯
            redisService.kryoZRem("ACTIVITY_MARQUEE", "【" + userLoginVo.getNickName() + "】分享活动获得" + (activityUserInfo
                    .getTotalTimes()) + "天VIP");
            redisService.kryoZAddSet("ACTIVITY_MARQUEE", fromUserId, "【" + userLoginVo.getNickName()
                    + "】分享活动获得" + (activityUserInfo.getTotalTimes() + 1) + "天VIP");
            redisService.expire("ACTIVITY_MARQUEE", RedisConstant.EXPIRE_TIME_SECOND_THIRTY_DAY);
        }
        // 注册用户总次数+1
        activityUserInfo.setTotalTimes(activityUserInfo.getTotalTimes() + 1);
        activityUserInfoDao.update(activityUserInfo);
        // 记录用户注册的人都是哪些
        ActivityUserLog activityUserLog = new ActivityUserLog(null, activityId, fromUserId, 0, String.valueOf(userId),
                DateUtil.getCurrentTimestamp());
        activityUserLogDao.insert(activityUserLog);

        // 给新用户发送奖励 3天vip
        vipMemberService.adminGiftVip(userId, 3L, VipMemberConstant.VIP_SOURCE_TYPE_ADMIN, vipType);

        return null;
    }

    // TODO: 2018/1/18  页面初始化统计数据
    @Override
    public Map<String, Object> shareUserIndex(Integer activityId, Long userId) {
        // 用户分享的个数
        Map<String, Object> resultMap = new HashMap<>();
        Integer shareNum = 0;
        Integer vipNum = 0;
        ActivityUserInfo activityUserInfo = activityUserInfoDao.getUserTotalTimes(activityId, userId);
        if (null != activityUserInfo) {
            shareNum = activityUserInfo.getTotalTimes();
            vipNum = shareNum;
            if (shareNum > 10) {
                vipNum = 10;
            }
        }
        resultMap.put("shareNum", shareNum);
        resultMap.put("vipNum", vipNum);
        resultMap.put("userId", userId);
        List<String> marquee = redisService.kryoZRange("ACTIVITY_MARQUEE", 0L, -1L, String.class);
        if (null == marquee || marquee.size() <= 0) {
            marquee = new ArrayList<>();
            marquee.add("敬请期待！");
        }
        resultMap.put("marquee", marquee);
        return resultMap;
    }


    // 春节活动接口
    // 首页
    @Override
    @Transactional
    public Map<String, Object> festivalIndex(Integer activityId, Long userId) {
        // 首页需要返回
        Map<String, Object> resultMap = new HashMap<>();
        // 1. 奖池 从缓存中获取每日的奖池
        String dateId = DateUtil.getCurrentDay(DateUtil.DATE_FORMAT_YYYYMMDD);
        String awardPool = redisService.get("Festival_AwardPool_" + dateId);
        if (StringUtils.isBlank(awardPool)) {
            // 读取数据库中的数据
            awardPool = "1000";
            redisService.set("Festival_AwardPool_" + dateId, "1000");
        }
        Integer awardPoolInt = Integer.valueOf(awardPool) + Integer.valueOf(ActivityIniCache.getActivityIniValue
                ("FESTIVAL_AWARD_POOL" + dateId, "0"));
        resultMap.put("awardPool", awardPoolInt);

        // 2. 倒计时 距离今天晚上8点还有多久
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 20);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        Date m20 = c.getTime();
        Long leftTime = DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(), new Timestamp(m20.getTime()));
        resultMap.put("leftTime", leftTime);

        // 3. 我的奖励
        List<Map<String, Object>> myAwardList = new ArrayList<>();
        BigDecimal total = new BigDecimal(0);

        if (null != userId) {
            List<ActivityDateUserInfo> activityDateUserInfos = activityDateUserInfoDao.getAllActivityUserInfo
                    (activityId, userId);
            if (null != activityDateUserInfos && activityDateUserInfos.size() > 0) {
                for (ActivityDateUserInfo activityDateUserInfo : activityDateUserInfos) {
                    if (StringUtils.isNotBlank(activityDateUserInfo.getRemark())) {
                        Map<String, Object> myAward = new HashMap<>();
                        myAward.put("dateId", activityDateUserInfo.getDateId().substring(0, 4) + "-" +
                                activityDateUserInfo
                                        .getDateId().substring(4, 6) + "-" + activityDateUserInfo.getDateId()
                                .substring(6, 8));
                        myAward.put("award", activityDateUserInfo.getRemark());
                        myAwardList.add(myAward);
                        total = total.add(new BigDecimal(activityDateUserInfo.getRemark()));
                    }
                }
            }
        }

        Collections.reverse(myAwardList);
        resultMap.put("myAwardList", myAwardList);
        resultMap.put("total", total.toString());

        // 4 .距离明天8点的时间
        Long tomorrowLeftTime = 0L;

        Calendar c2 = Calendar.getInstance();
        c2.add(Calendar.DATE, 1);
        c2.set(Calendar.HOUR_OF_DAY, 0);
        c2.set(Calendar.MINUTE, 0);
        c2.set(Calendar.SECOND, 0);
        Date m2 = c2.getTime();
        tomorrowLeftTime = DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(), new Timestamp(m2.getTime()));

        resultMap.put("tomorrowLeftTime", tomorrowLeftTime);

        // 活动校验
        Integer todayQuestionNum = 0;
        if (null != userId) {
            ActivityDateUserInfo activityDateUserInfo = activityDateUserInfoDao.getUserTimesByDate(activityId, userId,
                    dateId, Boolean.TRUE);
            if (null != activityDateUserInfo) {
                // 初始化
                todayQuestionNum = activityDateUserInfo.getTimes();
            } else {
                todayQuestionNum = 0;
            }
        }
        resultMap.put("todayQuestionNum", todayQuestionNum);

        return resultMap;
    }

    // 答题分奖金
    @Transactional
    @Override
    public Map<String, Object> questionAward(Integer activityId, Long userId) {
        Map<String, Object> resultMap = new HashMap<>();
        String dateId = DateUtil.getCurrentDay(DateUtil.DATE_FORMAT_YYYYMMDD);
        // 获取用户答题信息
        // 第十题 返回结束
        Boolean isOver = Boolean.FALSE;
        ActivityDateUserInfo activityDateUserInfo = activityDateUserInfoDao.getUserTimesByDate(activityId, userId,
                dateId, Boolean.TRUE);
        if (null == activityDateUserInfo) {
            // 初始化
            activityDateUserInfo = new ActivityDateUserInfo(activityId, userId, dateId, 0, null, DateUtil
                    .getCurrentTimestamp(), DateUtil.getCurrentTimestamp());
            activityDateUserInfoDao.insert(activityDateUserInfo);
        }
        if (activityDateUserInfo.getTimes() >= 9) { //最后一次
            isOver = Boolean.TRUE;
        }
        List<FestivalQuestion> festivalQuestionList = new ArrayList<>();
        // 返回 题目 答案
        if (activityDateUserInfo.getTimes() >= 0 && activityDateUserInfo.getTimes() < 3) {
            // 等级1
            festivalQuestionList = festivalQuestionDao.getQuestionByLevel(1);
        }
        if (activityDateUserInfo.getTimes() >= 3 && activityDateUserInfo.getTimes() < 7) {
            // 等级2
            festivalQuestionList = festivalQuestionDao.getQuestionByLevel(2);
        }
        if (activityDateUserInfo.getTimes() >= 7) {
            // 等级3
            festivalQuestionList = festivalQuestionDao.getQuestionByLevel(3);
        }
        // 今天的参与记录
        List<FestivalQuestion> chooseQuestionList = new ArrayList<>();
        List<ActivityUserLog> activityUserLogs = activityUserLogDao.getDateUserLog(activityId, userId, dateId);
        for (FestivalQuestion festivalQuestion : festivalQuestionList) {
            // 不重复
            Boolean isRepeat = Boolean.FALSE;
            for (ActivityUserLog activityUserLog : activityUserLogs) {
                if (String.valueOf(activityUserLog.getLevelId()).equals(festivalQuestion.getQuestionId())) {
                    isRepeat = Boolean.TRUE;
                }
            }
            if (!isRepeat) {
                chooseQuestionList.add(festivalQuestion);
            }
        }
        if (chooseQuestionList.size() == 0) {
            chooseQuestionList = festivalQuestionList;
        }
        if (chooseQuestionList.size() == 0) {
            chooseQuestionList = festivalQuestionDao.getQuestionByLevel(3);
        }
        try {
            Collections.shuffle(chooseQuestionList);
            FestivalQuestion chooseQuestion = chooseQuestionList.get(0);
            resultMap.put("Question", chooseQuestion.getQuestionText());
            List<FestivalAnswer> answers = festivalAnswerDao.getAnswerByQuestionId(chooseQuestion.getQuestionId());
            resultMap.put("answers", answers);
            resultMap.put("isOver", isOver);
            resultMap.put("questionId", chooseQuestion.getQuestionId());

            resultMap.put("todayQuestionNum", activityDateUserInfo.getTimes());


        } catch (Exception e) {

        }
        return resultMap;
    }

    @Transactional
    @Override
    public Map<String, Object> questionRightWithOutSign(Integer activityId, Long userId, String questionId) {
        FestivalQuestion festivalQuestion = festivalQuestionDao.getQuestionById(questionId);
        String dateId = DateUtil.getCurrentDay(DateUtil.DATE_FORMAT_YYYYMMDD);
        // 用户答题记录
        ActivityUserLog activityUserLog = new ActivityUserLog(null, activityId, userId, Integer.valueOf
                (festivalQuestion.getQuestionId()), dateId, DateUtil.getCurrentTimestamp());
        activityUserLogDao.insert(activityUserLog);

        ActivityDateUserInfo activityDateUserInfo = activityDateUserInfoDao.getUserTimesByDate(activityId, userId,
                dateId, Boolean.TRUE);
        // 用户答对次数记录
        activityDateUserInfo.setTimes(activityDateUserInfo.getTimes() + 1);
        activityDateUserInfoDao.update(activityDateUserInfo);
        // 奖池增加
        if (activityDateUserInfo.getTimes() == 1) {
            redisService.incr("Festival_AwardPool_" + dateId);
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("todayQuestionNum", activityDateUserInfo.getTimes());
        return resultMap;
    }

    // 用户首次答题错误，和首次答题超时调用
    @Transactional
    @Override
    public Map<String, Object> questionWrongWithOutSign(Integer activityId, Long userId, String questionId) {
        FestivalQuestion festivalQuestion = festivalQuestionDao.getQuestionById(questionId);
        String dateId = DateUtil.getCurrentDay(DateUtil.DATE_FORMAT_YYYYMMDD);
        // 用户答题记录
        ActivityUserLog activityUserLog = new ActivityUserLog(null, activityId, userId, Integer.valueOf
                (festivalQuestion.getQuestionId()), dateId, DateUtil.getCurrentTimestamp());
        activityUserLogDao.insert(activityUserLog);

        ActivityDateUserInfo activityDateUserInfo = activityDateUserInfoDao.getUserTimesByDate(activityId, userId,
                dateId, Boolean.TRUE);
        // 用户答对次数记录
        activityDateUserInfo.setTimes(-1);
        activityDateUserInfoDao.update(activityDateUserInfo);
        // 奖池增加
        if (activityDateUserInfo.getTimes() == 1 || activityDateUserInfo.getTimes() == -1) {
            redisService.incr("Festival_AwardPool_" + dateId);
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("todayQuestionNum", activityDateUserInfo.getTimes());
        return resultMap;
    }

    // 排行榜奖池
    @Override
    public Map<String, Object> rankWithOutSign(Integer activityId, Long selfUserId) {
        Map<String, Object> resultMap = new HashMap<>();
        String dateId = DateUtil.getCurrentDay(DateUtil.DATE_FORMAT_YYYYMMDD);
        // 奖池
        String awardPool = redisService.get("Festival_AwardPool_" + dateId);
        Integer awardPoolInt = Integer.valueOf(awardPool) + Integer.valueOf(ActivityIniCache.getActivityIniValue
                ("FESTIVAL_AWARD_POOL" + dateId, "0"));
        resultMap.put("awardPool", awardPoolInt);
        // 参与人数
        Integer count = 0;
        List<ActivityDateUserInfo> activityDateUserInfoList = activityDateUserInfoDao.getUserByDate(activityId,
                dateId, Boolean.FALSE);
        for (ActivityDateUserInfo activityDateUserInfo : activityDateUserInfoList) {
            // 如果中奖 平分派奖
            if (activityDateUserInfo.getTimes() == 10) {
                count++;
            }
        }
        String awardSelf = "0";
        // 自己中了多少钱
        if (null != selfUserId) {
            ActivityDateUserInfo activityDateUserInfo = activityDateUserInfoDao.getUserTimesByDate(activityId,
                    selfUserId, dateId, Boolean
                            .FALSE);
            if (null != activityDateUserInfo && StringUtils.isNotBlank(activityDateUserInfo.getRemark())) {
                awardSelf = activityDateUserInfo.getRemark();
            }
        }
        resultMap.put("awardSelf", awardSelf);

        resultMap.put("takeNum", count + Integer.valueOf(ActivityIniCache.getActivityIniValue
                ("FESTIVAL_AWARD_POOL" + dateId, "0")));

        // 排行榜
        List<Map<String, Object>> rankList = new ArrayList<>();
        List<Long> userIdList = redisService.kryoZRange("Festival_AwardPool_Rank" + dateId, Long.MIN_VALUE, Long
                .MAX_VALUE, Long.class);
        for (Long userId : userIdList) {
            Map<String, Object> userInfo = new HashMap<>();
            UserLoginVo userLoginVo = loginService.getUserLoginVo(userId);
            userInfo.put("headImgUrl", userLoginVo.getHeadImgUrl());
            userInfo.put("nickName", userLoginVo.getNickName());
            ActivityDateUserInfo activityDateUserInfo = activityDateUserInfoDao.getUserTimesByDate(activityId, userId,
                    dateId, Boolean.FALSE);
            userInfo.put("award", activityDateUserInfo.getRemark());
            userInfo.put("dateId", activityDateUserInfo.getDateId().substring(0, 4) + "-" +
                    activityDateUserInfo.getDateId().substring(4, 6) + "-" + activityDateUserInfo.getDateId()
                    .substring(6, 8));
            rankList.add(userInfo);
        }
        resultMap.put("rankList", rankList);
        if (rankList.size() > 20) {
            resultMap.put("rankList", rankList.subList(0, 20));
        }
        return resultMap;
    }

    // 排行榜算奖
    @Override
    public Map<String, Object> rankDistributeWithOutSign() {
        String dateId = DateUtil.getCurrentDay(DateUtil.DATE_FORMAT_YYYYMMDD);
        // 添加一些自己人的中奖数据
        String mobiles = "13004751699,13375392021,13659966528,13791018193,13898276787,13988741219,15091045984," +
                "15129764450,15184085887,15266049172,15306503931,15355913852,15836202227,15871268036,15970829432," +
                "18009081007,18042415784,18710667402";
        List<String> mobileArr = Arrays.asList(mobiles.split(CommonConstant.COMMA_SPLIT_STR));
        for (String m : mobileArr) {
            Long mUserId = loginService.getUserId(m);
            ActivityDateUserInfo activityDateUserInfo = activityDateUserInfoDao.getUserTimesByDate(201802002,
                    mUserId, dateId, Boolean.FALSE);
            if (null != activityDateUserInfo) {
                activityDateUserInfo.setTimes(10);
                activityDateUserInfoDao.update(activityDateUserInfo);
            } else {
                activityDateUserInfo = new ActivityDateUserInfo(201802002, mUserId, dateId, 0, null, DateUtil
                        .getCurrentTimestamp(), DateUtil.getCurrentTimestamp());
                activityDateUserInfoDao.insert(activityDateUserInfo);
            }
        }

        List<ActivityDateUserInfo> activityDateUserInfoList = activityDateUserInfoDao.getUserByDate(201802002,
                dateId, Boolean.FALSE);
        String awardPool = redisService.get("Festival_AwardPool_" + dateId);

        // 添加的中奖人数
        Integer awardPoolInt = Integer.valueOf(awardPool) + Integer.valueOf(ActivityIniCache.getActivityIniValue
                ("FESTIVAL_AWARD_POOL" + dateId, "0"));
        Integer count = 0;
        for (ActivityDateUserInfo activityDateUserInfo : activityDateUserInfoList) {
            // 如果中奖 平分派奖
            if (activityDateUserInfo.getTimes() == 10) {
                count++;
            }
        }
        // 分奖加上添加的中奖人数
        Integer each = awardPoolInt / (count + (Integer.valueOf(ActivityIniCache.getActivityIniValue
                ("FESTIVAL_AWARD_POOL" + dateId, "0"))));

        for (ActivityDateUserInfo activityDateUserInfo : activityDateUserInfoList) {
            // 如果中奖 平分派奖
            if (activityDateUserInfo.getTimes() == 10) {
                activityDateUserInfo.setRemark(String.valueOf(each));
                activityDateUserInfoDao.update(activityDateUserInfo);
                // 添加到排行榜
                redisService.kryoZAddSet("Festival_AwardPool_Rank" + dateId, System.currentTimeMillis(),
                        activityDateUserInfo.getUserId());
            }
        }
        return null;
    }

    @Override
    public Map<String, Object> clearTimesWithOutSign(Integer activityId, String mobile) {
        // 找到用户的ID
        Long userId = loginService.getUserId(mobile);
        String dateId = DateUtil.getCurrentDay(DateUtil.DATE_FORMAT_YYYYMMDD);
        ActivityDateUserInfo activityDateUserInfo = activityDateUserInfoDao.getUserTimesByDate(activityId, userId,
                dateId, Boolean.FALSE);
        activityDateUserInfo.setTimes(0);
        activityDateUserInfoDao.update(activityDateUserInfo);
        return null;
    }

    /*
     * 注册送5智慧币
     * */
    @Override
    public void registerGiveWisdomCoin(Long userId, Integer activityId) {
        ActivityUserInfo activityUserInfo = activityUserInfoDao.getUserTotalTimes(activityId, userId);
        if (null == activityUserInfo) {
            // 送金币
            payService.fillAccount(userId, String.valueOf(userId), Long.parseLong(String.valueOf(500)), CommonConstant
                    .PAY_TYPE_WISDOM_COIN, null, Long.parseLong(String.valueOf(500)), "新手送智慧币", "127.0.0.1", null);
            // 登记新用户购买会员享有优惠
            activityUserInfo = new ActivityUserInfo(activityId, userId, 0, null, DateUtil
                    .getCurrentTimestamp(), DateUtil.getCurrentTimestamp());
            activityUserInfoDao.insert(activityUserInfo);
        }
    }

    /*
     * 检查用户是否已经领取过智慧币
     * */

    @Override
    public Map<String, Object> checkIsGivenWisdomCoin(Long userId, Integer activityId) {
        Map<String, Object> resultMap = new HashMap<>();
        ActivityUserInfo activityUserInfo = activityUserInfoDao.getUserTotalTimes(activityId, userId);

        // 智慧币
        Boolean isGiven = Boolean.FALSE;
        if (null != activityUserInfo) {
            isGiven = Boolean.TRUE;
        }

        // 数字彩VIP
        Boolean isBuyVip = Boolean.FALSE;
        Boolean isBuyVip2 = Boolean.FALSE;

        if (null != activityUserInfo && activityUserInfo.getTotalTimes() == 2) {
            isBuyVip = Boolean.TRUE;
        }
        if (null != activityUserInfo && activityUserInfo.getTotalTimes() == 5) {
            isBuyVip2 = Boolean.TRUE;
        }
        if (null != activityUserInfo && activityUserInfo.getTotalTimes() == 7) {
            isBuyVip = Boolean.TRUE;
            isBuyVip2 = Boolean.TRUE;
        }

        Integer dateNumber = 0;
        if (null != activityUserInfo) {
            dateNumber = DateUtil.getDiffDays(activityUserInfo.getCreateTime(), DateUtil
                    .getCurrentTimestamp());
        }

        resultMap.put("IsGivenWisdomCoin", isGiven);
        resultMap.put("isBuyVip", isBuyVip);
        resultMap.put("isBuyVip2", isBuyVip2);
        resultMap.put("buyVipDate", dateNumber.intValue());
        return resultMap;
    }

    /*
     * 检查活动是否可用
     * */
    @Override
    public Boolean checkActivityIsEnabled(Integer activityId) {
        Boolean isEnabled = Boolean.TRUE;
        ActivityInfo activityInfo = activityInfoDao.getActivityInfo(activityId);
        /* 检查活动是否已经失效或者结束*/
        if (null == activityInfo) {
            return Boolean.FALSE;
        }
        if (!DateUtil.isBetween(DateUtil.getCurrentTimestamp(), activityInfo.getStartTime(), activityInfo.getEndTime
                ())) {
            isEnabled = Boolean.FALSE;
        }
        return isEnabled;
    }

    @Override
    public boolean checkUserTakepartActivity(Long userId, Integer activityId, Integer delayDay) {
        if (userId == null || activityId == null) {
            return false;
        }
        ActivityUserInfo activityUserInfo = activityUserInfoDao.getUserTotalTimes(activityId, userId);

        if (activityUserInfo != null) {
            Integer dateNumber = DateUtil.getDiffDays(activityUserInfo.getCreateTime(), DateUtil.getCurrentTimestamp());
            if ((delayDay != -1 && dateNumber > delayDay) || activityUserInfo.getTotalTimes() > 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void activitySmsPush() {
        // 找到活动的所有的用户
        List<ActivityUserInfo> activityUserInfos = activityUserInfoDao.getUsers(201803001);
        for (ActivityUserInfo activityUserInfo : activityUserInfos) {
            Integer dateCount = DateUtil.getDiffDays(activityUserInfo.getCreateTime(), DateUtil.getCurrentTimestamp());
            // 注册2天没有买会员的进行推送
            if (activityUserInfo.getTotalTimes() != 2 && dateCount >= 1 && dateCount < 4 && activityUserInfo
                    .getTotalTimes() != 3) {
                // 推送
                String text = "恭喜，您获得了尊享特权：“开通首月VIP仅10元”，这便宜一生仅一次，速来>>";
                String url = "";
                Map<String, String> content = new HashMap<>();
                content.put("pushUrl", "mjlottery://mjnative?page=wap&url=https://predictapi.mojieai.com/web/newuser/");
                content.put("killNumPushText", text);
                PushDto pushDto = new PushDto("现在开通首月VIP，仅10元！", text, url, content);
                AliyunPushTask pushTask = new AliyunPushTask(pushDto, "ACCOUNT", String.valueOf(activityUserInfo
                        .getUserId()),
                        "default");
                ThreadPool.getInstance().getPushExec().submit(pushTask);
                activityUserInfo.setTotalTimes(3);
                activityUserInfoDao.update(activityUserInfo);
            }

            // 会员资格最后一天推送
            if (activityUserInfo.getTotalTimes() != 2 && dateCount == 4 && activityUserInfo.getTotalTimes() != 4) {
                // 短信
                smsService.sendVerifyCodePushOnly(loginService.getUserLoginVo(activityUserInfo.getUserId()).getMobile
                        (), "您的“VIP首月仅10元”特权马上到期！点我10元开会员：https://predictapi.mojieai.com/99/", DateUtil
                        .getCurrentTimestamp());
                activityUserInfo.setTotalTimes(4);
                activityUserInfoDao.update(activityUserInfo);
            }
        }
    }

    /*
     * 领取方案活动 领取信息
     * */
    @Override
    public Map<String, Object> getActivityInfo(Integer activityId, Long userId) {
        Map<String, Object> resultMap = new HashMap<>();
        // 每个人的完成 签到和分享注册
        Integer isDoneSign = 0;
        // 双色球当前期次
        Game game = GameCache.getGame(GameConstant.SSQ);
//        GamePeriod currentPeriod = PeriodRedis.getCurrentPeriod(game.getGameId());
        GamePeriod currentPeriod = PeriodRedis.getPeriodByGameIdAndPeriod(game.getGameId(), "2018055");
        GamePeriod lastOpenPeriod = PeriodRedis.getLastPeriodByGameIdAndPeriodId(game.getGameId(), currentPeriod
                .getPeriodId());
        // 时间校验
        // endTime--九点 展示上一期的 状态为已经结束
        Boolean isOver = Boolean.FALSE;
        if ((int) DateUtil.getDiffSeconds(currentPeriod.getStartTime(), DateUtil.getCurrentTimestamp()) < (13 * 60 *
                60)) {
//            currentPeriod = lastOpenPeriod;
            // 已经结束
            isOver = Boolean.TRUE;
        }
        resultMap.put("isOver", isOver);

        // 查询次数
        Long shareNums = 0L;
        Integer leftTimes = 5;
        if (userId != null) {
            ActivityDateUserInfo activityDateUserInfo = activityDateUserInfoDao.getUserTimesByDate(activityId, userId,
                    currentPeriod.getPeriodId(), Boolean.FALSE);
            if (null == activityDateUserInfo) {
                /* 当期抽奖次数初始化*/
                Integer defaultTimes = 0;
                activityDateUserInfo = new ActivityDateUserInfo(activityId, userId, currentPeriod.getPeriodId(),
                        defaultTimes, null, DateUtil.getCurrentTimestamp(), DateUtil.getCurrentTimestamp());
                activityDateUserInfoDao.insert(activityDateUserInfo);
            }
            // 用户拥有的次数 - 用户当期已经使用的次数
            int userHaveTimes = 0;
            // 1. 签到
            if (userSignService.checkUserSign(userId, DateUtil.formatDate(new Date(), "yyyyMMdd"), CommonConstant
                    .USER_SIGN_TYPE_DAILY)) {
                isDoneSign = 1;
                userHaveTimes += 1;
            }
            // 2.分享次数
            String redisKey = RedisConstant.getUserShareWxActivityPeriod(activityId, userId, currentPeriod
                    .getPeriodId());
            shareNums = redisService.kryoZCard(redisKey);
            String lastRedisKey = RedisConstant.getUserShareWxActivityPeriod(activityId, userId, lastOpenPeriod
                    .getPeriodId());
            Long lastShareNums = redisService.kryoZCard(lastRedisKey);
            ActivityDateUserInfo lastUseNums = activityDateUserInfoDao.getUserTimesByDate(activityId, userId,
                    lastOpenPeriod.getPeriodId(), Boolean.FALSE);
            int lastNum = 0;
            if (null != lastUseNums && null != lastShareNums) {
                lastNum = lastShareNums.intValue() - lastUseNums.getTimes();
                if (lastNum < 0) {
                    lastNum = 0;
                }
            }
            if (shareNums > 3L) {
                shareNums = 3L;
            }
            userHaveTimes += shareNums.intValue() + lastNum;
            leftTimes = userHaveTimes - activityDateUserInfo.getTimes();
            if (leftTimes < 0) {
                leftTimes = 0;
            }
        }

        resultMap.put("leftTimes", leftTimes);
        resultMap.put("isDoneSign", isDoneSign);
        resultMap.put("shareNums", shareNums);

        // 领取的记录
        List<ActivityProgram> activityPrograms = activityProgramDao.getActivityPrograms(currentPeriod.getPeriodId());
        List<Object> records = new ArrayList<>();
        List<ActivityUserLog> activityUserLogs = null;
        if (userId != null) {
            activityUserLogs = activityUserLogDao.getUserLog(activityId, userId);
        }
        // 每个奖级和他剩余的次数
        List<ActivityProgramVo> activityProgramVos = new ArrayList<>();
        for (ActivityProgram activityProgram : activityPrograms) {
            ActivityProgramVo activityProgramVo = new ActivityProgramVo(activityProgram);
            // 添加是否是单式
            String redNumber = activityProgram.getLotteryNumber().split(CommonConstant.COMMON_COLON_STR)[0];
            String blueNumber = activityProgram.getLotteryNumber().split(CommonConstant.COMMON_COLON_STR)[1];
            if ((redNumber.split(CommonConstant.SPACE_SPLIT_STR).length + blueNumber.split(CommonConstant
                    .SPACE_SPLIT_STR).length) > 7) {
                activityProgramVo.setIsSingleFlag(0); //不是单式
            }
            Integer index = 0;
            Integer redIndex = 0;
            Integer blueIndex = 0;
            StringBuffer firstNumber = new StringBuffer();
            StringBuffer secondNumber = new StringBuffer();
            for (String number : redNumber.split(CommonConstant.SPACE_SPLIT_STR)) {
                if (index <= 6) {
                    firstNumber.append(number);
                    if (redIndex != redNumber.split(CommonConstant.SPACE_SPLIT_STR).length) {
                        firstNumber.append(CommonConstant.SPACE_SPLIT_STR);
                    }
                } else {
                    secondNumber.append(number);
                    if (redIndex != redNumber.split(CommonConstant.SPACE_SPLIT_STR).length) {
                        secondNumber.append(CommonConstant.SPACE_SPLIT_STR);
                    }
                }
                index++;
                redIndex++;

            }
            for (String number : blueNumber.split(CommonConstant.SPACE_SPLIT_STR)) {
                if (index <= 6) {
                    if (blueIndex == 0) {
                        firstNumber.append(CommonConstant.COMMON_COLON_STR);
                    }
                    firstNumber.append(number);
                    if (blueIndex != blueNumber.split(CommonConstant.SPACE_SPLIT_STR).length) {
                        firstNumber.append(CommonConstant.SPACE_SPLIT_STR);
                    }
                } else {
                    if (blueIndex == 0) {
                        secondNumber.append(CommonConstant.COMMON_COLON_STR);
                    }
                    secondNumber.append(number);
                    if (blueIndex != blueNumber.split(CommonConstant.SPACE_SPLIT_STR).length) {
                        secondNumber.append(CommonConstant.SPACE_SPLIT_STR);
                    }
                }
                index++;
                blueIndex++;
            }
            // 还未开始
            if (DateUtil.compareDate(DateUtil.getCurrentTimestamp(), activityProgramVo.getStartTime())) {
                activityProgramVo.setStatus(0);// 未开始
                activityProgramVo.setStatusText(DateUtil.getTodayTomorrowAndAfterTomorrow(activityProgramVo
                        .getStartTime()) + DateUtil.formatTime(activityProgramVo.getStartTime(), DateUtil
                        .DATE_FORMAT_HHMM));// 未开始
            } else {
                activityProgramVo.setStatus(1);// 立即领取
                activityProgramVo.setStatusText("立即领取");
            }
            if (activityProgramVo.getLeftCount() <= 0) {
                activityProgramVo.setStatus(2);// 已经领完
                activityProgramVo.setStatusText("已经领完");
            }
            if (isOver) {
                activityProgramVo.setStatus(3);// 已结束
                activityProgramVo.setStatusText("已结束");
            }

            if (userId != null) {
                for (ActivityUserLog activityUserLog : activityUserLogs) {
                    if (activityUserLog.getLevelId().equals(activityProgramVo.getProgramId())) {
                        activityProgramVo.setStatus(4);// 已经领取
                        activityProgramVo.setStatusText("已经领取");
                    }
                }
            }

            activityProgramVo.setFirstNumber(firstNumber.toString());
            activityProgramVo.setSecondNumber(secondNumber.toString());
            activityProgramVos.add(activityProgramVo);
        }

        resultMap.put("activityPrograms", activityProgramVos);

        // 我参与的
        List<Object> myHistory = new ArrayList<>();
        if (userId != null) {
            for (ActivityUserLog activityUserLog : activityUserLogs) {
                Map<String, Object> myHistoryMap = new HashMap<>();
                // 期次
                myHistoryMap.put("period", activityUserLog.getDateId() + "期");
                // 号码
                ActivityProgram activityProgram = activityProgramDao.getActivityProgramByProgramId(activityUserLog
                        .getLevelId(), Boolean.FALSE);
                myHistoryMap.put("activityProgram", activityProgram);
                // 状态 待开奖，已开奖
                Boolean isAward = Boolean.FALSE;
                String amountKey = "drawNumberAward:" + String.valueOf(activityId) + activityUserLog
                        .getDateId() + String.valueOf(activityUserLog.getLevelId());
                Integer awardAmount = redisService.kryoGet(amountKey, Integer.class);
                if (null != awardAmount && awardAmount >= 0) {
                    isAward = Boolean.TRUE;
                    myHistoryMap.put("awardAmount", awardAmount);
                }
                myHistoryMap.put("isAward", isAward);
                myHistory.add(myHistoryMap);
            }
        }

        resultMap.put("myHistory", myHistory);
        if (null != userId) {
            resultMap.put("userId", userId.toString());
        }

        // 下一期的时间
        GamePeriod nextPeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(game.getGameId(), currentPeriod
                .getPeriodId());
        String dateStr = DateUtil.getTodayTomorrowAndAfterTomorrow(DateUtil.getIntervalSeconds(nextPeriod
                .getStartTime(), 13 * 60 * 60));
        resultMap.put("dateStr", dateStr);

        // 是否开奖
        Boolean currentAward = Boolean.FALSE;
        if (null != currentPeriod.getOpenTime()) {
            currentAward = Boolean.TRUE;
        }
        resultMap.put("currentAward", currentAward);

        return resultMap;
    }

    @Override
    public void productProgram() {
        // 双色球
        Game game = GameCache.getGame(GameConstant.SSQ);
        GamePeriod currentPeriod = PeriodRedis.getCurrentPeriod(game.getGameId());

        // 第二天在开始
        if ((int) DateUtil.getDiffSeconds(currentPeriod.getStartTime(), DateUtil.getCurrentTimestamp()) < (12 * 60 *
                60)) {
            return;
        }
        // 五条方案
        // 每期结束定时生成方案
        /*单式：10股
        复式7+2：100股
        复式8+1：200股*/
        List<ActivityProgram> activityPrograms = activityProgramDao.getActivityPrograms(currentPeriod.getPeriodId());
        // 如果已经有方案了 直接返回
        if (activityPrograms.size() > 0) {
            return;
        }
        Map<String, Integer> programSettings = new HashMap<>();
        programSettings.put("6:1", 10);
        programSettings.put("7:2", 100);
        programSettings.put("8:1", 200);

        Integer index = 0;

        Calendar current = Calendar.getInstance();
        current.set(Calendar.MINUTE, 0);
        current.set(Calendar.SECOND, 0);

        current.set(Calendar.HOUR_OF_DAY, 10);
        Timestamp today10 = new Timestamp(current.getTime().getTime());

        current.set(Calendar.HOUR_OF_DAY, 22);
        Timestamp today22 = new Timestamp(current.getTime().getTime());


        current.add(Calendar.DATE, 1);
        current.set(Calendar.HOUR_OF_DAY, 10);
        Timestamp tomorrow10 = new Timestamp(current.getTime().getTime());
        current.set(Calendar.HOUR_OF_DAY, 22);
        Timestamp tomorrow22 = new Timestamp(current.getTime().getTime());

        current.add(Calendar.DATE, 1);
        current.set(Calendar.HOUR_OF_DAY, 10);
        Timestamp afterTomorrow10 = new Timestamp(current.getTime().getTime());
        current.set(Calendar.HOUR_OF_DAY, 22);
        Timestamp afterTomorrow22 = new Timestamp(current.getTime().getTime());

        for (Map.Entry entry : programSettings.entrySet()) {
            for (int n = 0; n < 2; n++) {
                Integer redNum = Integer.valueOf(entry.getKey().toString().split(CommonConstant
                        .COMMON_COLON_STR)[0]);
                Integer blueNum = Integer.valueOf(entry.getKey().toString().split(CommonConstant
                        .COMMON_COLON_STR)[1]);
                // 红球20
                StringBuffer redSb = new StringBuffer(game.getGameEn()).append(currentPeriod.getPeriodId()).append
                        (IniCache
                                .getIniValue(IniConstant.RANDOM_CODE, CommonConstant.RANDOM_CODE));
                List<String> redLists = Arrays.asList(GameEnum.getGameEnumById(game.getGameId()).getRedBalls());

                Collections.shuffle(redLists, new Random(new Long((long) redSb.toString().hashCode())));
                List<String> redList = new ArrayList<>(redLists);
                for (int i = 32; i > 29; i--) {
                    redList.remove(i);
                }
                // 篮球
                StringBuffer sb = new StringBuffer(game.getGameEn()).append(currentPeriod.getPeriodId()).append(IniCache
                        .getIniValue(IniConstant.RANDOM_CODE, CommonConstant.RANDOM_CODE));
                List<String> blueLists = Arrays.asList(GameEnum.getGameEnumById(game.getGameId()).getBlueBalls());

                Collections.shuffle(blueLists, new Random(new Long((long) sb.toString().hashCode())));

                List<String> blueList = new ArrayList<>(blueLists);
                for (int m = 0; m < 4; m++) {
                    blueList.remove(m);
                }
                // 生产一个方案
                List<String> newRedList = new ArrayList(redList);
                List<String> newBlueList = new ArrayList(blueList);
                StringBuffer stringBufferNumber = new StringBuffer();
                for (int j = 0; j < redNum; j++) {
                    if (j != 0) {
                        stringBufferNumber.append(CommonConstant.SPACE_SPLIT_STR);
                    }
                    Random random = new Random();
                    int randomNum = random.nextInt(newRedList.size());
                    stringBufferNumber.append(newRedList.remove(randomNum));
                }
                stringBufferNumber.append(CommonConstant.COMMON_COLON_STR);
                for (int p = 0; p < blueNum; p++) {
                    if (p != 0) {
                        stringBufferNumber.append(CommonConstant.SPACE_SPLIT_STR);
                    }
                    Random random = new Random();
                    int randomNum = random.nextInt(newBlueList.size());
                    stringBufferNumber.append(newBlueList.remove(randomNum));
                }
                // 每个方案的开始时间
                Timestamp startTime = DateUtil.getCurrentTimestamp();

                switch (index) {
                    case 0:
                        // 今天十点
                        startTime = today10;
                        break;
                    case 1:
                        // 明天十点
                        startTime = tomorrow10;
                        break;
                    case 2:
                        // 今天十点
                        startTime = today10;
                        break;
                    case 3:
                        // 明天十点
                        startTime = tomorrow10;
                        break;
                    case 4:
                        // 今天22点
                        startTime = today22;
                        break;
                    case 5:
                        // 明天十点
                        startTime = tomorrow10;
                        break;
                }
                // 如果今天是周五
                if (DateUtil.getTargetWeek(DateUtil.getCurrentTimestamp()).equals("周五")) {
                    switch (index) {
                        case 0:
                            // 今天十点
                            startTime = today10;
                            break;
                        case 1:
                            // 明天十点
                            startTime = afterTomorrow10;
                            break;
                        case 2:
                            // 今天十点
                            startTime = today10;
                            break;
                        case 3:
                            // 明天十点
                            startTime = afterTomorrow10;
                            break;
                        case 4:
                            // 今天22点
                            startTime = tomorrow10;
                            break;
                        case 5:
                            // 明天十点
                            startTime = afterTomorrow10;
                            break;
                    }
                }

                ActivityProgram activityProgram = new ActivityProgram(currentPeriod.getPeriodId(), stringBufferNumber
                        .toString(), entry.getKey().toString(), startTime, Integer.valueOf(entry.getValue().toString()),
                        Integer.valueOf(entry.getValue().toString()));
                activityProgramDao.insert(activityProgram);
                index++;
            }
        }
    }

    /*
     * 领取方案活动
     * 领取号码方法
     * */
    @Override
    public Map<String, Object> drawNumber(Integer activityId, Long userId, Integer programId) {
        Map<String, Object> awardResult = new HashMap<>();
        // 时间校验
        Game game = GameCache.getGame(GameConstant.SSQ);
        GamePeriod currentPeriod = PeriodRedis.getCurrentPeriod(game.getGameId());
        // TODO: 2018/5/4 检查方案是否可以领取 
        // 可以领取的时间为 endTime--九点 
        if ((int) DateUtil.getDiffSeconds(currentPeriod.getStartTime(), DateUtil.getCurrentTimestamp()) < (13 * 60 *
                60)) {
            return getActivityFinished(awardResult, "下期方案明天早9点开领");
        }
        // 如果有次数 插入号码
        ActivityInfo activityInfo = activityInfoDao.getActivityInfo(activityId);
        /* 检查活动是否已经失效或者结束*/
        if (null == activityInfo) {
            return getActivityFinished(awardResult, "活动已经结束");
        }
        if (!DateUtil.isBetween(DateUtil.getCurrentTimestamp(), activityInfo.getStartTime(), activityInfo.getEndTime
                ())) {
            return getActivityFinished(awardResult, "活动已经结束");
        }

        // 领取次数增加 号码发放
        return self.updateDrawNumber(activityId, programId, userId, currentPeriod, awardResult);
    }

    /*
     * 领取方案活动
     * 领取号码后的方法
     * */
    @Override
    @Transactional
    public Map<String, Object> updateDrawNumber(Integer activityId, Integer programId, Long userId, GamePeriod
            currentPeriod, Map<String, Object> awardResult) {
        ActivityProgram activityProgram = activityProgramDao.getActivityProgramByProgramId(programId, Boolean.TRUE);
        User user = userDao.getUserByUserId(userId, Boolean.TRUE);
        if (activityProgram.getLeftCount() <= 0) {
            return getActivityFinished(awardResult, "该股份已领完");
        }

        // 次数校验  // TODO: 2018/5/4 回头加上 
        /*Map<String, Object> leftTimes = getActivityInfo(activityId, userId);
        if (Integer.valueOf(leftTimes.get("leftTimes").toString()) <= 0) {
            return getActivityFinished(awardResult, "今日没有可用次数");
        }*/

        // 可以领取，并且拥有次数，是否已经领取校验
        List<ActivityUserLog> activityUserLogs = activityUserLogDao.getDateUserLog(activityId, userId, currentPeriod
                .getPeriodId());
        for (ActivityUserLog activityUserLog : activityUserLogs) {
            if (activityUserLog.getLevelId().equals(programId)) {
                return getActivityFinished(awardResult, "同一类型股份不可以重复领取");
            }
        }

        // 创建领取 记录log
        ActivityUserLog currentUserLog = new ActivityUserLog(null, activityId, userId, programId, currentPeriod
                .getPeriodId(), DateUtil.getCurrentTimestamp());
        activityUserLogDao.insert(currentUserLog);

        // 方案减少
        activityProgram.setLastAwardTime(DateUtil.getCurrentTimestamp());
        activityProgram.setLeftCount(activityProgram.getLeftCount() - 1);
        activityProgramDao.update(activityProgram);

        ActivityDateUserInfo activityDateUserInfo = activityDateUserInfoDao.getUserTimesByDate(activityId, userId,
                currentPeriod.getPeriodId(), Boolean.TRUE);

        Boolean insert = Boolean.FALSE;
        if (null == activityDateUserInfo) {
            activityDateUserInfo = new ActivityDateUserInfo(activityId, userId, currentPeriod.getPeriodId(), 0, null,
                    DateUtil.getCurrentTimestamp(), DateUtil.getCurrentTimestamp());
            insert = Boolean.TRUE;
        }

        // 用户领取的次数
        activityDateUserInfo.setTimes(activityDateUserInfo.getTimes() + 1);
        if (insert) {
            activityDateUserInfoDao.insert(activityDateUserInfo);
        } else {
            activityDateUserInfoDao.update(activityDateUserInfo);
        }


        return getSuccess(awardResult, "领取成功");
    }

    @Override
    public Map<String, Object> activityGiveCoupon2User(Long userId) {
        Map<String, Object> result = new HashMap<>();
        Integer activityId = 201806001;
        String status = ResultConstant.COUPON_DISTRIBUTE_FAIL_STATUS;
        String validDateDesc = "";
        if (!checkActivityIsEnabled(activityId) || checkUserTakepartActivity(userId, activityId, -1)) {
            validDateDesc = checkUserTakepartActivity(userId, activityId, -1) ? "已参与" : "";
            result.put("title", "领取失败");
            result.put("status", status);
            result.put("activityName", "足彩预测免单资格+1");
            result.put("validDateDesc", validDateDesc);
            return result;
        }
        ActivityUserInfo activityUserInfo = activityUserInfoDao.getUserTotalTimes(activityId, userId);
        if (null == activityUserInfo) {
            activityUserInfo = new ActivityUserInfo(activityId, userId, 0, null, DateUtil.getCurrentTimestamp(),
                    DateUtil.getCurrentTimestamp());
            activityUserInfoDao.insert(activityUserInfo);
        }

        ActivityInfo activityInfo = activityInfoDao.getActivityInfo(activityId);
        if (StringUtils.isNotBlank(activityInfo.getRemark())) {
            Map<String, Object> couponConfigIdsMap = JSONObject.parseObject(activityInfo.getRemark(), HashMap.class);
            String couponConfIds = couponConfigIdsMap.get("couponConfIds").toString();
            if (StringUtils.isNotBlank(couponConfIds)) {
                Set<String> statusSet = new HashSet<>();
                String[] couponConfIdArr = couponConfIds.split(CommonConstant.COMMA_SPLIT_STR);
                if (couponConfIdArr.length > 0) {
                    for (String couponConfId : couponConfIdArr) {
                        CouponConfig couponConfig = couponConfigDao.getCouponConfigById(Long.valueOf(couponConfId));
                        Map<String, Object> distributeRes = userCouponService.distributeCoupon2UserByConfig(userId,
                                activityId + couponConfId, DateUtil.getCurrentTimestamp(), couponConfig);
                        statusSet.add(distributeRes.get("status").toString());
                        if (StringUtils.isBlank(validDateDesc)) {
                            Timestamp beginTime = DateUtil.getCurrentTimestamp();
                            Timestamp endTime = DateUtil.getIntervalDays(beginTime, couponConfig.getValidDay());
                            validDateDesc = "(有效期" + DateUtil.formatTime(beginTime, DateUtil.DATE_FORMAT_M_D)
                                    + "-" + DateUtil.formatTime(endTime, DateUtil.DATE_FORMAT_M_D) + ")";
                        }
                    }
                }
                if (!statusSet.isEmpty() && !statusSet.contains(ResultConstant.COUPON_DISTRIBUTE_FAIL_STATUS)) {
                    activityUserInfo.setTotalTimes(1);
                    activityUserInfoDao.update(activityUserInfo);
                    status = ResultConstant.COUPON_DISTRIBUTE_SUCCESS_STATUS;
                }
            }
        }
        result.put("title", "领取成功");
        result.put("activityName", "足彩预测免单资格+1");
        result.put("status", status);
        result.put("validDateDesc", validDateDesc);
        return result;
    }

    /*
     * 会员到期短信推送提醒 // TODO: 2018/7/14
     * */
    @Override
    public void vipExpireSmsPush() {
        // 找到所有要过期的会员
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 1);
        Date time = cal.getTime();

        List<VipMember> vipMembers = vipMemberDao.getVipMemberByExpireDate(DateUtil.getEndOfOneDay(new Timestamp(time
                .getTime())));

        // 发送短信 区分足彩会员和数字彩会员
        for (VipMember vipMember : vipMembers) {
            // 数字彩会员
            if (vipMember.getVipType().equals(VipMemberConstant.VIP_MEMBER_TYPE_DIGIT)) {
                //
                smsService.sendVerifyCodePushOnly(loginService.getUserLoginVo(vipMember.getUserId()).getMobile
                        (), "", DateUtil
                        .getCurrentTimestamp());
            }
            // 足彩会员
            if (vipMember.getVipType().equals(VipMemberConstant.VIP_MEMBER_TYPE_SPORTS)) {
                smsService.sendVerifyCodePushOnly(loginService.getUserLoginVo(vipMember.getUserId()).getMobile
                        (), "", DateUtil
                        .getCurrentTimestamp());
            }
        }
    }

    /**
     * 单关方案 活动 start
     **/
    @Override
    public String productDanguanProgram(String matchId, String programInfo, Long price, Long vipPrice) {
        try {
            // 校验比赛信息
            DetailMatchInfo detailMatchInfo = thirdHttpService.getMatchMapByMatchId(Integer.valueOf(matchId));
            if (null == detailMatchInfo) {
                return "方案发布失败,没有这场比赛";
            }
            if (DateUtil.compareDate(detailMatchInfo.getEndTime(), DateUtil.getCurrentTimestamp())) {
                return "方案发布失败,比赛已经完场";
            }
            DanguanProgram danguanProgram = danguanProgramDao.getDuanguanProgram(matchId);
            if (null != danguanProgram) {
                return "方案发布失败,该比赛已经发布过方案";
            }
            // 拼接 programInfo
            String result = "";
            for (String playItem : programInfo.split("\\$")) {
                StringBuffer stringBuffer = new StringBuffer();
                for (String item : playItem.split(",")) {
                    if (CommonUtil.isNumeric(item)) {
                        // 总进球
                        if (!item.equals("7")) {
                            stringBuffer.append(item).append("球:");
                        } else {
                            stringBuffer.append(item).append("+:");
                        }
                        // 赔率
                        stringBuffer.append(detailMatchInfo.getItemOdd(5, item)).append(",");
                    } else {
                        // 选项 胜胜
                        stringBuffer.append(item).append(":");
                        // 赔率
                        stringBuffer.append(detailMatchInfo.getItemOddByName(6, item)).append(",");
                    }
                }
                // 去掉最后一个逗号
                if (StringUtils.isBlank(result)) {
                    // 如果index == 0 总进球 加上$
                    result += stringBuffer.substring(0, stringBuffer.length() - 1) + "$";
                } else {
                    result += stringBuffer.substring(0, stringBuffer.length() - 1);
                }
            }
            danguanProgram = new DanguanProgram(matchId, result, price, vipPrice);
            danguanProgramDao.insert(danguanProgram);
        } catch (Exception e) {
            log.info(e);
            return "方案发布失败";
        }
        return "方案发布成功";
    }

    @Override
    public List<Map<String, Object>> danguanProgramList(Long userId) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        List<DanguanProgram> danguanPrograms = danguanProgramDao.getNotAwardDuanguanProgram();
        if (null != danguanPrograms && danguanPrograms.size() > 0) {
            for (DanguanProgram danguanProgram : danguanPrograms) {
                // 比赛信息
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("matchId", danguanProgram.getMatchId());
                resultMap.put("price", danguanProgram.getPrice());
                resultMap.put("vipPrice", danguanProgram.getVipPrice());

                // 球队信息
                DetailMatchInfo detailMatchInfo = thirdHttpService.getMatchMapByMatchId(Integer.valueOf(danguanProgram
                        .getMatchId()));
                if (DateUtil.compareDate(detailMatchInfo.getEndTime(), DateUtil.getCurrentTimestamp())) {
                    continue;
                }
                resultMap.put("matchName", detailMatchInfo.getMatchName());
                resultMap.put("matchDate", detailMatchInfo.getMatchDate());
                resultMap.put("matchTime", detailMatchInfo.getMatchTime());
                resultMap.put("hostName", detailMatchInfo.getHostName());
                resultMap.put("awayName", detailMatchInfo.getAwayName());
                resultMap.put("hostImg", detailMatchInfo.getHostImg());
                resultMap.put("awayImg", detailMatchInfo.getAwayImg());
                resultMap.put("tag", detailMatchInfo.getTag());
                resultMap.put("memo", "danguanProgram:" + detailMatchInfo.getMatchId());

                // 用户是否购买
                DanguanProgramUser danguanProgramUser = null;
                if (null != userId) {
                    danguanProgramUser = danguanProgramUserDao.getDanguanProgramUserLog(userId,
                            danguanProgram.getMatchId());
                }

                if (null != danguanProgramUser) {
                    // 可以看见 方案信息
                    // 推荐信息 ，5球:4.5，1球:3.1$胜胜:2.5,负负:3.2
                    resultMap.put("programInfo", danguanProgram.getProgramInfo());
                    resultMap.put("isBuyed", 1);
                    resultMap.put("btnMsg", "");
                } else {
                    // 看不见
                    resultMap.put("programInfo", "");
                    resultMap.put("isBuyed", 0);
                    DanguanProgramUserTimes userTimes = danguanProgramUserTimesDao.getDanguanProgramUserTimes(userId,
                            Boolean.FALSE);
                    if (null != userTimes && userTimes.getLeftTimes() > 0) {
                        resultMap.put("btnMsg", "用特权查看");
                    } else {
                        resultMap.put("btnMsg", danguanProgram.getPrice() / 100 + "智慧币查看 (会员仅需" + danguanProgram
                                .getVipPrice() / 100 + ")");
                    }
                }

                resultList.add(resultMap);
            }
        }

        return resultList;
    }

    @Override
    public void getRightAwardNums(Map<String, Object> resultMap) {
        resultMap.put("rightNums", redisService.get("danguanProgramRightNum"));
        resultMap.put("awardNums", redisService.get("danguanProgramAwardNum"));
    }

    @Override
    public Integer userDanguanTimes(Long userId) {
        DanguanProgramUserTimes danguanProgramUserTimes = danguanProgramUserTimesDao.getDanguanProgramUserTimes
                (userId, Boolean.FALSE);
        if (danguanProgramUserTimes != null) {
            return danguanProgramUserTimes.getLeftTimes();
        }
        return 0;
    }

    @Override
    public Integer checkVip(Long userId) {
        if (vipMemberService.checkUserIsVip(userId, VipMemberConstant.VIP_MEMBER_TYPE_SPORTS)) {
            return 1;
        }
        return 0;
    }

    @Override
    public List<Map<String, Object>> danguanProgramHistory() {
        List<Map<String, Object>> resultList = new ArrayList<>();
        List<DanguanProgram> danguanPrograms = danguanProgramDao.getAwardDuanguanProgram();
        if (null != danguanPrograms && danguanPrograms.size() > 0) {
            for (DanguanProgram danguanProgram : danguanPrograms) {
                // 比赛信息
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("matchId", danguanProgram.getMatchId());
                resultMap.put("price", danguanProgram.getPrice());
                resultMap.put("vipPrice", danguanProgram.getVipPrice());

                // 球队信息
                DetailMatchInfo detailMatchInfo = thirdHttpService.getMatchMapByMatchId(Integer.valueOf(danguanProgram
                        .getMatchId()));
                resultMap.put("matchName", detailMatchInfo.getMatchName());
                resultMap.put("matchDate", detailMatchInfo.getMatchDate());
                resultMap.put("matchTime", detailMatchInfo.getMatchTime());
                resultMap.put("hostName", detailMatchInfo.getHostName());
                resultMap.put("awayName", detailMatchInfo.getAwayName());
                resultMap.put("hostImg", detailMatchInfo.getHostImg());
                resultMap.put("awayImg", detailMatchInfo.getAwayImg());
                resultMap.put("tag", detailMatchInfo.getTag());
                resultMap.put("hostScore", detailMatchInfo.getHostScore());
                resultMap.put("awayScore", detailMatchInfo.getAwayScore());

                // 如果命中
                Integer isRight = 0;
                String rightItem = "";
                // 推荐信息
                if (!StringUtils.isBlank(danguanProgram.getRightItem())) {
                    rightItem = danguanProgram.getRightItem();
                    isRight = 1;
                } else {
                    rightItem = "已完场";
                }
                resultMap.put("leftItem", danguanProgram.getProgramInfo());
                resultMap.put("isRight", isRight);
                resultMap.put("rightItem", rightItem);
                resultList.add(resultMap);
            }
        }

        return resultList;
    }

    // 售卖的卡列表信息
    @Override
    public List<Map<String, Object>> danguanProgramCards() {
        List<DanguanProgramCards> danguanProgramCards = danguanProgramCardsDao.getDanguanProgramCards();
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (DanguanProgramCards card : danguanProgramCards) {
            Map<String, Object> cardInfo = new HashMap<>();
            cardInfo.put("cardId", card.getCardId());
            cardInfo.put("cardName", card.getCardName());
            cardInfo.put("cardDesc", card.getCardDesc());
            cardInfo.put("originPrice", card.getOriginPrice());
            cardInfo.put("price", card.getPrice());
            cardInfo.put("memo", "danguanCard:" + card.getCardId());
            resultList.add(cardInfo);
        }

        return resultList;
    }

    // 特权购买方案
    @Override
    public Integer privilegeBuyProgram(Long userId, String matchId) {
        try {
            return self.transPrivilegeBuyProgram(userId, matchId);
        } catch (Exception e) {
            return -2;
        }
    }

    @Override
    @Transactional
    public Integer transPrivilegeBuyProgram(Long userId, String matchId) {
        // 校验用户特权
        DanguanProgramUserTimes userTimes = danguanProgramUserTimesDao.getDanguanProgramUserTimes(userId, Boolean.TRUE);
        if (null == userTimes || userTimes.getLeftTimes() == 0) {
            return -1;
        }
        // -1 解锁方案
        userTimes.setLeftTimes(userTimes.getLeftTimes() - 1);
        danguanProgramUserTimesDao.update(userTimes);
        DanguanProgramUser danguanProgramUser = new DanguanProgramUser(userId, matchId);
        danguanProgramUserDao.insert(danguanProgramUser);
        return 0;
    }

    @Override
    public Map<String, Object> buyDanguanCard(Long userId, Integer payChannelId, String memo, Integer bankId, Integer
            clientType) {
        // 购买卡
        Integer cardId = Integer.valueOf(memo.split(":")[1]);
        String payId = System.currentTimeMillis() + ":" + cardId;
        DanguanProgramCards danguanProgramCards = danguanProgramCardsDao.getDanguanProgramCardByCardId(cardId);

        if (payChannelId.equals(CommonConstant.WISDOM_COIN_CHANNEL_ID)) {
            // 智慧币
            Map<String, Object> payForToken = payService.payCreateFlow(userId, payId, danguanProgramCards.getPrice(),
                    CommonConstant.ACCOUNT_TYPE_WISDOM_COIN, payChannelId, danguanProgramCards.getPrice(),
                    "智慧币购买单关特权卡", null, null, null, CommonConstant.PAY_OPERATE_TYPE_DEC, null);

            // 支付成功 处理业务
            self.buyDuanguanCardBusiness(userId, danguanProgramCards.getTimes());
            payForToken.put("code", ResultConstant.SUCCESS);
            payForToken.put("msg", "支付成功");

            return payForToken;
        } else {
            // 现金
            Map<String, Object> payForToken = payService.payCreateFlow(userId, payId, danguanProgramCards.getPrice(),
                    CommonConstant.ACCOUNT_TYPE_CASH, payChannelId, danguanProgramCards.getPrice(),
                    "现金购买单关特权卡", "222.129.17.194", clientType, "activityServiceImpl.buyDuanguanCardBusinessCall",
                    CommonConstant.PAY_OPERATE_TYPE_DEC, bankId);
            return (Map<String, Object>) payForToken.get("payForToken");
        }
    }

    @Override
    @Transactional
    public void buyDuanguanCardBusiness(Long userId, Integer cardTimes) {
        // 卡次数
        DanguanProgramUserTimes danguanProgramUserTimes = danguanProgramUserTimesDao.getDanguanProgramUserTimes
                (userId, Boolean.TRUE);
        Boolean isInsert = Boolean.FALSE;
        if (null == danguanProgramUserTimes) {
            danguanProgramUserTimes = new DanguanProgramUserTimes(userId, 0);
            isInsert = Boolean.TRUE;
        }
        danguanProgramUserTimes.setLeftTimes(danguanProgramUserTimes.getLeftTimes() + cardTimes);
        if (isInsert) {
            danguanProgramUserTimesDao.insert(danguanProgramUserTimes);
        } else {
            danguanProgramUserTimesDao.update(danguanProgramUserTimes);
        }
    }

    @Override
    @Transactional
    public Boolean buyDuanguanCardBusinessCall(String cardId, String flowId) {
        Long userIdSuffix = Long.valueOf(flowId.substring(flowId.length() - 2, flowId.length
                ()));
        UserAccountFlow userAccountFlow = userAccountFlowDao.getUserFlowByShardType(flowId, userIdSuffix, Boolean.TRUE);

        //1.检验交易流水状态。是否已经支付或者未付款
        if (userAccountFlow == null || userAccountFlow.getStatus() != CommonConstant.PAY_STATUS_FINISH) {
            Integer status = userAccountFlow == null ? -1 : userAccountFlow.getStatus();
            log.error("流水id:" + flowId + " 异常." + "流水状态为:" + status);
            return Boolean.FALSE;
        }

        DanguanProgramCards danguanProgramCards = danguanProgramCardsDao.getDanguanProgramCardByCardId(Integer
                .valueOf(cardId.split(":")[1]));
        self.buyDuanguanCardBusiness(userAccountFlow.getUserId(), danguanProgramCards.getTimes());

        return Boolean.TRUE;

    }


    @Override
    public void updateDanguanProgramStatus() {
        // 所有未开始的方案
        List<DanguanProgram> danguanPrograms = danguanProgramDao.getNotAwardDuanguanProgram();
        if (null != danguanPrograms && danguanPrograms.size() > 0) {
            for (DanguanProgram danguanProgram : danguanPrograms) {
                // 如果方案结束 更新状态
                MatchSchedule matchSchedule = matchScheduleDao.getMatchScheduleByPk(Integer.valueOf(danguanProgram
                        .getMatchId()), CommonConstant.LOTTERY_CODE_FOOTBALL);
                if (null == matchSchedule) {
                    log.error("matchSchedule is null" + danguanProgram.getMatchId());
                    continue;
                }
                if (matchSchedule.getIfEnd() != null && matchSchedule.getIfEnd() == 2) {
                    // 获取比赛结果 更新信息
                    DetailMatchInfo detailMatchInfo = thirdHttpService.getMatchMapByMatchId(matchSchedule.getMatchId());
                    if (null == detailMatchInfo) {
                        log.error("detailMatchInfo is null" + danguanProgram.getMatchId());
                        continue;
                    }
                    // 进球
                    Integer rightBallNums = detailMatchInfo.getHostScore() + detailMatchInfo.getAwayScore();

                    // 总进球结果
                    String allBallResult = rightBallNums + "球";
                    // 半全场结果
                    String halfAllResult = "";
                    // 上半场
                    log.info("halfScore::::" + detailMatchInfo.getHalfScore());
                    if (null == detailMatchInfo.getHalfScore()) {
                        log.error(detailMatchInfo.getMatchId() + "半场比分没有");
                        continue;
                    }
                    String[] halfScore = detailMatchInfo.getHalfScore().replace("半场", "").split(":");
                    log.info("halfScore::::" + halfScore);
                    if (Integer.valueOf(halfScore[0]) > Integer.valueOf(halfScore[1])) {
                        halfAllResult += "胜";
                    }
                    if (Integer.valueOf(halfScore[0]) < Integer.valueOf(halfScore[1])) {
                        halfAllResult += "负";
                    }
                    if (Integer.valueOf(halfScore[0]).equals(Integer.valueOf(halfScore[1]))) {
                        halfAllResult += "平";
                    }
                    if (detailMatchInfo.getHostScore() > detailMatchInfo.getAwayScore()) {
                        halfAllResult += "胜";
                    }
                    if (detailMatchInfo.getHostScore() < detailMatchInfo.getAwayScore()) {
                        halfAllResult += "负";
                    }
                    if (detailMatchInfo.getHostScore().equals(detailMatchInfo.getAwayScore())) {
                        halfAllResult += "平";
                    }

                    log.info("halfScore::::" + halfAllResult);
                    String programRightItem = "";
                    // 遍历推荐
                    for (String playRecommend : danguanProgram.getProgramInfo().split("\\$")) {
                        for (String item : playRecommend.split(",")) {
                            if (item.indexOf(allBallResult) > -1 || item.indexOf(halfAllResult) > -1) {
                                programRightItem += item + ",";
                            }
                        }
                    }
                    log.info("halfScore::::" + programRightItem);
                    if (!StringUtils.isBlank(programRightItem)) {
                        programRightItem = programRightItem.substring(0, programRightItem.length() - 1);
                    }
                    log.info("halfScore::::" + programRightItem);
                    danguanProgram.setRightItem(programRightItem);
                    danguanProgram.setIsAwarded(1);
                    danguanProgramDao.update(danguanProgram);
                }
            }
            // 近期命中率统计规则：近七天命中的比赛场次数/近七天推荐的比赛场次数（只要中1个就算命中）
            // 近期收益率统计规则：近七天命中的选项赔率之和/近七天推荐的比赛场次数
            // 查询到最近七天的比赛

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(DateUtil.getBeginOfToday());
            calendar.add(Calendar.DATE, -7);
            List<DanguanProgram> danguanProgramList = danguanProgramDao.getDanguanProgramListByLimitDate(new
                    Timestamp(calendar.getTime().getTime()));
            log.info("halfScore::::size" + danguanProgramList.size());
            Integer allRightNums = 0; // 近七天命中的比赛场次数
            Integer allNums = 0; // 近七天推荐的比赛场次数
            Integer allRightOdds = 0; //近七天命中的选项赔率之和
            for (DanguanProgram danguanProgram : danguanProgramList) {
                allNums++;
                if (!StringUtils.isBlank(danguanProgram.getRightItem())) {
                    allRightNums++;
                    for (String rightItem : danguanProgram.getRightItem().split(",")) {
                        BigDecimal a = new BigDecimal(rightItem.split(":")[1]);
                        allRightOdds += a.multiply(new BigDecimal(100)).intValue();
                    }
                }
            }
            String rightNum = CommonUtil.divide(String.valueOf(allRightNums * 100), String.valueOf(allNums), 0);
            String awardNum = CommonUtil.divide(String.valueOf(allRightOdds), String.valueOf(allNums), 0);

            redisService.set("danguanProgramRightNum", rightNum);
            redisService.set("danguanProgramAwardNum", awardNum);
        }


    }

    // 购买方案
    @Override
    public Map<String, Object> buyDanguanProgram(Long userId, Integer payChannelId, String memo, Integer bankId, Integer
            clientType) {
        // 购买方案
        String matchId = memo.split(":")[1];
        String payId = System.currentTimeMillis() + ":" + matchId;
        DanguanProgram danguanProgram = danguanProgramDao.getDuanguanProgram(matchId);

        Long price = danguanProgram.getPrice();
        if (checkVip(userId) == 1) {
            price = danguanProgram.getVipPrice();
        }

        if (payChannelId.equals(CommonConstant.WISDOM_COIN_CHANNEL_ID)) {
            // 智慧币
            Map<String, Object> payForToken = payService.payCreateFlow(userId, payId, price,
                    CommonConstant.ACCOUNT_TYPE_WISDOM_COIN, payChannelId, price,
                    "智慧币购买单关方案", null, null, null, CommonConstant.PAY_OPERATE_TYPE_DEC, null);
            Integer payStatus = (Integer) payForToken.get("payStatus");

            // 支付成功 处理业务
            self.buyDuanguanProgramBusiness(userId, matchId);
            payForToken.put("code", ResultConstant.SUCCESS);
            payForToken.put("msg", "支付成功");
            return payForToken;
        } else {
            // 现金
            Map<String, Object> payForToken = payService.payCreateFlow(userId, payId, price,
                    CommonConstant.ACCOUNT_TYPE_CASH, payChannelId, price,
                    "现金购买单关方案", "222.129.17.194", clientType, "activityServiceImpl.buyDuanguanProgramBusinessCall",
                    CommonConstant.PAY_OPERATE_TYPE_DEC, bankId);
            return (Map<String, Object>) payForToken.get("payForToken");
        }
    }

    @Override
    public void buyDuanguanProgramBusiness(Long userId, String matchId) {
        // 用户比赛
        try {
            DanguanProgramUser danguanProgramUser = new DanguanProgramUser(userId, matchId);
            danguanProgramUserDao.insert(danguanProgramUser);
        } catch (Exception e) {
            log.error("buyDuanguanProgramBusiness error", e);
            throw new BusinessException(e);
        }
    }

    @Override
    public Boolean buyDuanguanProgramBusinessCall(String matchId, String flowId) {
        Long userIdSuffix = Long.valueOf(flowId.substring(flowId.length() - 2, flowId.length
                ()));
        UserAccountFlow userAccountFlow = userAccountFlowDao.getUserFlowByShardType(flowId, userIdSuffix, Boolean.TRUE);

        //1.检验交易流水状态。是否已经支付或者未付款
        if (userAccountFlow == null || userAccountFlow.getStatus() != CommonConstant.PAY_STATUS_FINISH) {
            Integer status = userAccountFlow == null ? -1 : userAccountFlow.getStatus();
            log.error("流水id:" + flowId + " 异常." + "流水状态为:" + status);
            return Boolean.FALSE;
        }

        self.buyDuanguanProgramBusiness(userAccountFlow.getUserId(), matchId.split(":")[1]);

        return Boolean.TRUE;
    }

    /** 单关方案 活动 end**/
}
