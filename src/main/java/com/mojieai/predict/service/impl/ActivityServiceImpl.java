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
            /* ?????????????????????????????????????????????*/
            String redisKey = RedisConstant.getUserShareWx(gameId, userId);
            Long rank = redisService.kryoZRank(redisKey, openId);
            if (null != rank) {
                return;
            }
            /* ??????????????????*/
            GamePeriod period = PeriodRedis.getCurrentPeriod(gameId);

            redisService.kryoZAddSet(redisKey, System.currentTimeMillis(), openId);
            redisService.expire(redisKey, (int) DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(), period
                    .getAwardTime()));

            /* ????????????*/
            String redisDayKey = "SHARE_USER_RANK:" + DateUtil.getCurrentDay(DateUtil.DATE_FORMAT_YYYYMMDD);
            redisService.kryoZAddSet(redisDayKey, System.currentTimeMillis(), userId);
            redisService.expire(redisDayKey, 60 * 60 * 24 * 30);
            predictNumService.updateUserPredictMaxNums(gameId, period.getPeriodId(), userId, 1);

            // ?????????
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
        /* ??????access_token????????????7200???????????????????????????????????????????????????access_token???*/
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
        /* ?????????????????????access_token ??????http GET??????????????????jsapi_ticket*/
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
            resultVo.setMsg("???????????????");
            return resultVo;
        }
        if (checkUserTakepartActivity(userId, activityId, -1)) {
            resultVo.setMsg("??????????????????");
            return resultVo;
        }
        ActivityUserInfo activityUserInfo = new ActivityUserInfo(activityId, userId, 1, null, DateUtil
                .getCurrentTimestamp(), DateUtil.getCurrentTimestamp());
        try {
            activityUserInfoDao.insert(activityUserInfo);
            resultVo.setCode(ResultConstant.SUCCESS);
        } catch (Exception e) {
            log.error(activityId + "?????????????????????", e);
        }
        return resultVo;
    }

    @Override
    public Map<String, Object> drawLotteryInfo(Integer activityId, Long userId) {
        // ????????????
        Map<String, Object> resultMap = new HashMap<>();
        String dateId = DateUtil.getCurrentDay(DateUtil.DATE_FORMAT_YYYYMMDD);
        ActivityDateUserInfo activityDateUserInfo = activityDateUserInfoDao.getUserTimesByDate(activityId, userId,
                dateId, Boolean.FALSE);
        if (null == activityDateUserInfo) {
            /* ???????????????????????????*/
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

        // ????????????
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

        //?????????
        ActivityUserInfo activityUserInfo = activityUserInfoDao.getUserTotalTimes(activityId, userId);
        if (null == activityUserInfo) {
            activityUserInfo = new ActivityUserInfo(activityId, userId, 0, null, DateUtil.getCurrentTimestamp(),
                    DateUtil.getCurrentTimestamp());
            activityUserInfoDao.insert(activityUserInfo);
        }
        resultMap.put("totalTimes", activityUserInfo.getTotalTimes());

        // ??????????????????
        String redisKey = RedisConstant.getUserShareWxActivity(activityId, userId);
        Long shareNums = redisService.kryoZCard(redisKey);
        resultMap.put("shareNums", shareNums.intValue());

        // ??????????????????
        Integer userDateTimes = 0;
        List<ActivityUserLog> activityUserLogList = activityUserLogDao.getDateUserLog(activityId, userId, dateId);
        if (null != activityUserLogs) {
            userDateTimes = activityUserLogList.size();
        }
        resultMap.put("userDateTimes", userDateTimes);

        resultMap.put("userId", userId);

        resultMap.put("isOver", Boolean.FALSE);
        ActivityInfo activityInfo = activityInfoDao.getActivityInfo(activityId);
        /* ??????????????????????????????????????????*/
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
        /* ??????????????????*/ // TODO: 2017/11/23 activity endTime ???????????? 
        Map<String, Object> awardResult = new HashMap<>();
        ActivityInfo activityInfo = activityInfoDao.getActivityInfo(activityId);
        /* ??????????????????????????????????????????*/
        if (null == activityInfo) {
            return getActivityFinished(awardResult, "??????????????????");
        }
        if (!DateUtil.isBetween(DateUtil.getCurrentTimestamp(), activityInfo.getStartTime(), activityInfo.getEndTime
                ())) {
            return getActivityFinished(awardResult, "??????????????????");
        }

        /* ??????????????????????????????*/
        String dateId = DateUtil.getCurrentDay(DateUtil.DATE_FORMAT_YYYYMMDD);
        ActivityDateUserInfo activityDateUserInfo = activityDateUserInfoDao.getUserTimesByDate(activityId, userId,
                dateId, Boolean.FALSE);
        if (null == activityDateUserInfo) {
            /* ???????????????????????????*/
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
            return getActivityFinished(awardResult, "????????????????????????");
        }
        ActivityUserInfo activityUserInfo = activityUserInfoDao.getUserTotalTimes(activityId, userId);
        if (null == activityUserInfo) {
            activityUserInfo = new ActivityUserInfo(activityId, userId, 0, null, DateUtil.getCurrentTimestamp(),
                    DateUtil.getCurrentTimestamp());
            activityUserInfoDao.insert(activityUserInfo);
        }

        /* ??????*/
        Random random = new Random();
        Integer awardValue = random.nextInt(100) + 1; // 1-100

        ActivityAwardLevel awardLevel = new ActivityAwardLevel();
        ActivityAwardLevel defaultAwardLevel = new ActivityAwardLevel();
        /* ??????levelId??????*/
        List<ActivityAwardLevel> activityAwardLevels = activityAwardLevelDao.getAwardLevelByActivityId(activityId);

        for (ActivityAwardLevel activityAwardLevel : activityAwardLevels) {
            String remark = activityAwardLevel.getRemark();
            Map<String, Object> remarkMap = (Map<String, Object>) JSONObject.parse(remark);
            Map<String, Object> userInfoRemarkMap = (Map<String, Object>) JSONObject.parse(activityUserInfo.getRemark
                    ());
            /*  ????????????(??????) */
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
            /* 3.??????10???????????????(??????)*/
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
            /* ??????????????????*/
            /* ?????????????????????????????????1. ??????????????????*/
            if (awardLevel.getDayLeftCount() < 0) {
                log.error("??????????????????????????????");
            }
            /* ???????????????*/
            if (!DateUtil.formatDate(awardLevel.getLastAwardTime(), DateUtil.DATE_FORMAT_YYYYMMDD).equals(dateId)) {
                awardLevel.setDayLeftCount(awardLevel.getDayAwardCount());

                awardLevel.setUpdateTime(DateUtil.getCurrentTimestamp());
            }
            if (awardLevel.getDayLeftCount() <= 0) {
                /* ??????????????????*/
                awardLevel = defaultAwardLevel;
            }
            /* 2.????????????????????????????????????????????????*/
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
            return getActivityFinished(awardResult, "????????????????????????");
        }
        self.updateActivityInfo(awardLevel, activityId, userId, activityDateUserInfo, activityUserInfo, dateId,
                mustRight);
        // ????????????????????? ??????+1
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
            /* ?????????????????????????????????????????????*/
            String redisKey = RedisConstant.getUserShareWxActivity(activityId, userId);
            Long rank = redisService.kryoZRank(redisKey, openId);
            if (null != rank) {
                return;
            }
            redisService.kryoZAddSet(redisKey, System.currentTimeMillis(), openId);
            redisService.expire(redisKey, 60 * 60 * 24 * 30);

            /* ????????????*/
            String redisDayKey = "ACTIVITY_SHARE_USER_RANK:" + DateUtil.getCurrentDay(DateUtil.DATE_FORMAT_YYYYMMDD);
            redisService.kryoZAddSet(redisDayKey, System.currentTimeMillis(), userId);
            redisService.expire(redisDayKey, 60 * 60 * 24 * 30);

            // ????????????
            String dateId = DateUtil.getCurrentDay(DateUtil.DATE_FORMAT_YYYYMMDD);
            ActivityDateUserInfo activityDateUserInfo = activityDateUserInfoDao.getUserTimesByDate(activityId,
                    userId, dateId, Boolean.FALSE);
            activityDateUserInfo.setTimes(activityDateUserInfo.getTimes() + 1);
            activityDateUserInfo.setUpdateTime(DateUtil.getCurrentTimestamp());
            activityDateUserInfoDao.update(activityDateUserInfo);

            String text = "????????????????????????????????????????????????";
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
            /* ?????????????????????????????????????????????*/
            GamePeriod period = PeriodRedis.getCurrentPeriod(GameCache.getGame(GameConstant.SSQ).getGameId());
            String redisKey = RedisConstant.getUserShareWxActivityPeriod(activityId, userId, period.getPeriodId());
            Long rank = redisService.kryoZRank(redisKey, openId);
            if (null != rank) {
                return;
            }
            // ????????????????????????
            User user = userDao.getUserByUserId(Long.parseLong(openId), Boolean.FALSE);
            if ((int) DateUtil.getDiffMinutes(user.getCreateTime(), DateUtil.getCurrentTimestamp()) > 10) {
                return;
            }
            redisService.kryoZAddSet(redisKey, System.currentTimeMillis(), openId);
            redisService.expire(redisKey, 60 * 60 * 24 * 30);

            /* ????????????*/
            String redisDayKey = "ACTIVITY_SHARE_USER_RANK:" + period.getPeriodId();
            redisService.kryoZAddSet(redisDayKey, System.currentTimeMillis(), userId);
            redisService.expire(redisDayKey, 60 * 60 * 24 * 30);

            String text = "??????????????????????????????????????????????????????";
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
            /* ?????????????????????*/
            awardLevel.setDayLeftCount(awardLevel.getDayLeftCount() - 1);
            awardLevel.setLastAwardTime(DateUtil.getCurrentTimestamp());
            awardLevel.setUpdateTime(DateUtil.getCurrentTimestamp());
            activityAwardLevelDao.update(awardLevel);
        }

        /* ???????????????????????????*/
        ActivityUserLog currentUserLog = new ActivityUserLog(null, activityId, userId, awardLevel.getLevelId(), dateId,
                DateUtil.getCurrentTimestamp());
        activityUserLogDao.insert(currentUserLog);

        /* ??????????????????????????????????????????-1*/
        activityDateUserInfo.setTimes(activityDateUserInfo.getTimes() - 1);
        activityDateUserInfo.setUpdateTime(DateUtil.getCurrentTimestamp());
        activityDateUserInfoDao.update(activityDateUserInfo);

        /* ???????????????????????????*/
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
        awardResult.put("awardMsg", "??????????????????");
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
        // ??????+??????+???????????? ??? ?????? ???redis
        Map<String, Object> resultMap = new HashMap<>();
        String key = "drawNumberAward:" + String.valueOf(activityId) + periodId + String.valueOf(levelId);
        redisService.kryoSetEx(key, RedisConstant.EXPIRE_TIME_SECOND_THIRTY_DAY, awardAmount);
        Integer amount = redisService.kryoGet(key, Integer.class);
        resultMap.put("key", key);
        resultMap.put("amount", amount);
        return resultMap;
    }

    /* ?????????*/
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

    /* ???????????????ID*/
    private String getCurrentWeekId() {
        Calendar current = Calendar.getInstance();
        return new StringBuffer().append(current.get(Calendar.YEAR)).append(DateUtil.getWeekOfYearOfCurrentDay() - 1)
                .toString();
    }

    /* ???????????????ID*/
    private String getCurrentMonthId() {
        return new StringBuffer().append(DateUtil.getCurrentMonth()).toString();
    }

    /* ???????????????ID*/
    private String getWeekIdByDate(Date date) {
        Calendar current = Calendar.getInstance();
        return new StringBuffer().append(current.get(Calendar.YEAR)).append(DateUtil.getWeekOfYear(date) - 1)
                .toString();
    }

    /* ???????????????ID*/
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


    /* ??????????????? ???????????????????????????*/
    @Override
    @Transactional
    public Map<String, Object> newShare(Integer activityId, Long userId) {
        Map<String, Object> resultMap = new HashMap<>();
        // ????????????????????????2??????
        String dateId = DateUtil.getCurrentDay(DateUtil.DATE_FORMAT_YYYYMMDD);
        ActivityDateUserInfo activityDateUserInfo = activityDateUserInfoDao.getUserTimesByDate(activityId, userId,
                dateId, Boolean.TRUE);
        if (null != activityDateUserInfo) {
            return resultMap;
        }

        UserLoginVo userLoginVo = loginService.getUserLoginVo(userId);
        redisService.kryoZAddSet("ACTIVITY_MARQUEE", userId, "???" + userLoginVo.getNickName()
                + "?????????????????????2??????");
        redisService.expire("ACTIVITY_MARQUEE", RedisConstant.EXPIRE_TIME_SECOND_THIRTY_DAY);

        // ????????????????????????
        activityDateUserInfo = new ActivityDateUserInfo(activityId, userId, dateId, 2, null, DateUtil
                .getCurrentTimestamp(), DateUtil.getCurrentTimestamp());
        activityDateUserInfoDao.insert(activityDateUserInfo);

        // ?????????
        resultMap = payService.fillAccount(userId, null, Long.parseLong(String.valueOf(2)), CommonConstant
                .PAY_TYPE_GOLD_COIN, null, Long.parseLong(String.valueOf(2)), "????????????", "127.0.0.1", null);
        return resultMap;
    }

    /* ???????????????????????????*/
    @Override
    @Transactional
    public Map<String, Object> shareUserRegister(Integer activityId, Long fromUserId, Long userId, Integer vipType) {

        // ????????????????????????????????????10???
        User fromUser = userDao.getUserByUserId(fromUserId, Boolean.TRUE);
        ActivityUserInfo activityUserInfo = activityUserInfoDao.getUserTotalTimes(activityId, fromUserId);

        if (null == activityUserInfo) {
            activityUserInfo = new ActivityUserInfo(activityId, fromUserId, 0, null, DateUtil.getCurrentTimestamp(),
                    DateUtil.getCurrentTimestamp());
            activityUserInfoDao.insert(activityUserInfo);
        }

        // ???????????????????????????10??????????????????+1
        if (activityUserInfo.getTotalTimes() < 10) {
            vipMemberService.adminGiftVip(fromUserId, 1L, VipMemberConstant.VIP_SOURCE_TYPE_ADMIN, vipType);
            UserLoginVo userLoginVo = loginService.getUserLoginVo(fromUserId);
            // ?????????????????????
            redisService.kryoZRem("ACTIVITY_MARQUEE", "???" + userLoginVo.getNickName() + "?????????????????????" + (activityUserInfo
                    .getTotalTimes()) + "???VIP");
            redisService.kryoZAddSet("ACTIVITY_MARQUEE", fromUserId, "???" + userLoginVo.getNickName()
                    + "?????????????????????" + (activityUserInfo.getTotalTimes() + 1) + "???VIP");
            redisService.expire("ACTIVITY_MARQUEE", RedisConstant.EXPIRE_TIME_SECOND_THIRTY_DAY);
        }
        // ?????????????????????+1
        activityUserInfo.setTotalTimes(activityUserInfo.getTotalTimes() + 1);
        activityUserInfoDao.update(activityUserInfo);
        // ????????????????????????????????????
        ActivityUserLog activityUserLog = new ActivityUserLog(null, activityId, fromUserId, 0, String.valueOf(userId),
                DateUtil.getCurrentTimestamp());
        activityUserLogDao.insert(activityUserLog);

        // ???????????????????????? 3???vip
        vipMemberService.adminGiftVip(userId, 3L, VipMemberConstant.VIP_SOURCE_TYPE_ADMIN, vipType);

        return null;
    }

    // TODO: 2018/1/18  ???????????????????????????
    @Override
    public Map<String, Object> shareUserIndex(Integer activityId, Long userId) {
        // ?????????????????????
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
            marquee.add("???????????????");
        }
        resultMap.put("marquee", marquee);
        return resultMap;
    }


    // ??????????????????
    // ??????
    @Override
    @Transactional
    public Map<String, Object> festivalIndex(Integer activityId, Long userId) {
        // ??????????????????
        Map<String, Object> resultMap = new HashMap<>();
        // 1. ?????? ?????????????????????????????????
        String dateId = DateUtil.getCurrentDay(DateUtil.DATE_FORMAT_YYYYMMDD);
        String awardPool = redisService.get("Festival_AwardPool_" + dateId);
        if (StringUtils.isBlank(awardPool)) {
            // ???????????????????????????
            awardPool = "1000";
            redisService.set("Festival_AwardPool_" + dateId, "1000");
        }
        Integer awardPoolInt = Integer.valueOf(awardPool) + Integer.valueOf(ActivityIniCache.getActivityIniValue
                ("FESTIVAL_AWARD_POOL" + dateId, "0"));
        resultMap.put("awardPool", awardPoolInt);

        // 2. ????????? ??????????????????8???????????????
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 20);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        Date m20 = c.getTime();
        Long leftTime = DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(), new Timestamp(m20.getTime()));
        resultMap.put("leftTime", leftTime);

        // 3. ????????????
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

        // 4 .????????????8????????????
        Long tomorrowLeftTime = 0L;

        Calendar c2 = Calendar.getInstance();
        c2.add(Calendar.DATE, 1);
        c2.set(Calendar.HOUR_OF_DAY, 0);
        c2.set(Calendar.MINUTE, 0);
        c2.set(Calendar.SECOND, 0);
        Date m2 = c2.getTime();
        tomorrowLeftTime = DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(), new Timestamp(m2.getTime()));

        resultMap.put("tomorrowLeftTime", tomorrowLeftTime);

        // ????????????
        Integer todayQuestionNum = 0;
        if (null != userId) {
            ActivityDateUserInfo activityDateUserInfo = activityDateUserInfoDao.getUserTimesByDate(activityId, userId,
                    dateId, Boolean.TRUE);
            if (null != activityDateUserInfo) {
                // ?????????
                todayQuestionNum = activityDateUserInfo.getTimes();
            } else {
                todayQuestionNum = 0;
            }
        }
        resultMap.put("todayQuestionNum", todayQuestionNum);

        return resultMap;
    }

    // ???????????????
    @Transactional
    @Override
    public Map<String, Object> questionAward(Integer activityId, Long userId) {
        Map<String, Object> resultMap = new HashMap<>();
        String dateId = DateUtil.getCurrentDay(DateUtil.DATE_FORMAT_YYYYMMDD);
        // ????????????????????????
        // ????????? ????????????
        Boolean isOver = Boolean.FALSE;
        ActivityDateUserInfo activityDateUserInfo = activityDateUserInfoDao.getUserTimesByDate(activityId, userId,
                dateId, Boolean.TRUE);
        if (null == activityDateUserInfo) {
            // ?????????
            activityDateUserInfo = new ActivityDateUserInfo(activityId, userId, dateId, 0, null, DateUtil
                    .getCurrentTimestamp(), DateUtil.getCurrentTimestamp());
            activityDateUserInfoDao.insert(activityDateUserInfo);
        }
        if (activityDateUserInfo.getTimes() >= 9) { //????????????
            isOver = Boolean.TRUE;
        }
        List<FestivalQuestion> festivalQuestionList = new ArrayList<>();
        // ?????? ?????? ??????
        if (activityDateUserInfo.getTimes() >= 0 && activityDateUserInfo.getTimes() < 3) {
            // ??????1
            festivalQuestionList = festivalQuestionDao.getQuestionByLevel(1);
        }
        if (activityDateUserInfo.getTimes() >= 3 && activityDateUserInfo.getTimes() < 7) {
            // ??????2
            festivalQuestionList = festivalQuestionDao.getQuestionByLevel(2);
        }
        if (activityDateUserInfo.getTimes() >= 7) {
            // ??????3
            festivalQuestionList = festivalQuestionDao.getQuestionByLevel(3);
        }
        // ?????????????????????
        List<FestivalQuestion> chooseQuestionList = new ArrayList<>();
        List<ActivityUserLog> activityUserLogs = activityUserLogDao.getDateUserLog(activityId, userId, dateId);
        for (FestivalQuestion festivalQuestion : festivalQuestionList) {
            // ?????????
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
        // ??????????????????
        ActivityUserLog activityUserLog = new ActivityUserLog(null, activityId, userId, Integer.valueOf
                (festivalQuestion.getQuestionId()), dateId, DateUtil.getCurrentTimestamp());
        activityUserLogDao.insert(activityUserLog);

        ActivityDateUserInfo activityDateUserInfo = activityDateUserInfoDao.getUserTimesByDate(activityId, userId,
                dateId, Boolean.TRUE);
        // ????????????????????????
        activityDateUserInfo.setTimes(activityDateUserInfo.getTimes() + 1);
        activityDateUserInfoDao.update(activityDateUserInfo);
        // ????????????
        if (activityDateUserInfo.getTimes() == 1) {
            redisService.incr("Festival_AwardPool_" + dateId);
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("todayQuestionNum", activityDateUserInfo.getTimes());
        return resultMap;
    }

    // ??????????????????????????????????????????????????????
    @Transactional
    @Override
    public Map<String, Object> questionWrongWithOutSign(Integer activityId, Long userId, String questionId) {
        FestivalQuestion festivalQuestion = festivalQuestionDao.getQuestionById(questionId);
        String dateId = DateUtil.getCurrentDay(DateUtil.DATE_FORMAT_YYYYMMDD);
        // ??????????????????
        ActivityUserLog activityUserLog = new ActivityUserLog(null, activityId, userId, Integer.valueOf
                (festivalQuestion.getQuestionId()), dateId, DateUtil.getCurrentTimestamp());
        activityUserLogDao.insert(activityUserLog);

        ActivityDateUserInfo activityDateUserInfo = activityDateUserInfoDao.getUserTimesByDate(activityId, userId,
                dateId, Boolean.TRUE);
        // ????????????????????????
        activityDateUserInfo.setTimes(-1);
        activityDateUserInfoDao.update(activityDateUserInfo);
        // ????????????
        if (activityDateUserInfo.getTimes() == 1 || activityDateUserInfo.getTimes() == -1) {
            redisService.incr("Festival_AwardPool_" + dateId);
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("todayQuestionNum", activityDateUserInfo.getTimes());
        return resultMap;
    }

    // ???????????????
    @Override
    public Map<String, Object> rankWithOutSign(Integer activityId, Long selfUserId) {
        Map<String, Object> resultMap = new HashMap<>();
        String dateId = DateUtil.getCurrentDay(DateUtil.DATE_FORMAT_YYYYMMDD);
        // ??????
        String awardPool = redisService.get("Festival_AwardPool_" + dateId);
        Integer awardPoolInt = Integer.valueOf(awardPool) + Integer.valueOf(ActivityIniCache.getActivityIniValue
                ("FESTIVAL_AWARD_POOL" + dateId, "0"));
        resultMap.put("awardPool", awardPoolInt);
        // ????????????
        Integer count = 0;
        List<ActivityDateUserInfo> activityDateUserInfoList = activityDateUserInfoDao.getUserByDate(activityId,
                dateId, Boolean.FALSE);
        for (ActivityDateUserInfo activityDateUserInfo : activityDateUserInfoList) {
            // ???????????? ????????????
            if (activityDateUserInfo.getTimes() == 10) {
                count++;
            }
        }
        String awardSelf = "0";
        // ?????????????????????
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

        // ?????????
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

    // ???????????????
    @Override
    public Map<String, Object> rankDistributeWithOutSign() {
        String dateId = DateUtil.getCurrentDay(DateUtil.DATE_FORMAT_YYYYMMDD);
        // ????????????????????????????????????
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

        // ?????????????????????
        Integer awardPoolInt = Integer.valueOf(awardPool) + Integer.valueOf(ActivityIniCache.getActivityIniValue
                ("FESTIVAL_AWARD_POOL" + dateId, "0"));
        Integer count = 0;
        for (ActivityDateUserInfo activityDateUserInfo : activityDateUserInfoList) {
            // ???????????? ????????????
            if (activityDateUserInfo.getTimes() == 10) {
                count++;
            }
        }
        // ?????????????????????????????????
        Integer each = awardPoolInt / (count + (Integer.valueOf(ActivityIniCache.getActivityIniValue
                ("FESTIVAL_AWARD_POOL" + dateId, "0"))));

        for (ActivityDateUserInfo activityDateUserInfo : activityDateUserInfoList) {
            // ???????????? ????????????
            if (activityDateUserInfo.getTimes() == 10) {
                activityDateUserInfo.setRemark(String.valueOf(each));
                activityDateUserInfoDao.update(activityDateUserInfo);
                // ??????????????????
                redisService.kryoZAddSet("Festival_AwardPool_Rank" + dateId, System.currentTimeMillis(),
                        activityDateUserInfo.getUserId());
            }
        }
        return null;
    }

    @Override
    public Map<String, Object> clearTimesWithOutSign(Integer activityId, String mobile) {
        // ???????????????ID
        Long userId = loginService.getUserId(mobile);
        String dateId = DateUtil.getCurrentDay(DateUtil.DATE_FORMAT_YYYYMMDD);
        ActivityDateUserInfo activityDateUserInfo = activityDateUserInfoDao.getUserTimesByDate(activityId, userId,
                dateId, Boolean.FALSE);
        activityDateUserInfo.setTimes(0);
        activityDateUserInfoDao.update(activityDateUserInfo);
        return null;
    }

    /*
     * ?????????5?????????
     * */
    @Override
    public void registerGiveWisdomCoin(Long userId, Integer activityId) {
        ActivityUserInfo activityUserInfo = activityUserInfoDao.getUserTotalTimes(activityId, userId);
        if (null == activityUserInfo) {
            // ?????????
            payService.fillAccount(userId, String.valueOf(userId), Long.parseLong(String.valueOf(500)), CommonConstant
                    .PAY_TYPE_WISDOM_COIN, null, Long.parseLong(String.valueOf(500)), "??????????????????", "127.0.0.1", null);
            // ???????????????????????????????????????
            activityUserInfo = new ActivityUserInfo(activityId, userId, 0, null, DateUtil
                    .getCurrentTimestamp(), DateUtil.getCurrentTimestamp());
            activityUserInfoDao.insert(activityUserInfo);
        }
    }

    /*
     * ??????????????????????????????????????????
     * */

    @Override
    public Map<String, Object> checkIsGivenWisdomCoin(Long userId, Integer activityId) {
        Map<String, Object> resultMap = new HashMap<>();
        ActivityUserInfo activityUserInfo = activityUserInfoDao.getUserTotalTimes(activityId, userId);

        // ?????????
        Boolean isGiven = Boolean.FALSE;
        if (null != activityUserInfo) {
            isGiven = Boolean.TRUE;
        }

        // ?????????VIP
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
     * ????????????????????????
     * */
    @Override
    public Boolean checkActivityIsEnabled(Integer activityId) {
        Boolean isEnabled = Boolean.TRUE;
        ActivityInfo activityInfo = activityInfoDao.getActivityInfo(activityId);
        /* ??????????????????????????????????????????*/
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
        // ??????????????????????????????
        List<ActivityUserInfo> activityUserInfos = activityUserInfoDao.getUsers(201803001);
        for (ActivityUserInfo activityUserInfo : activityUserInfos) {
            Integer dateCount = DateUtil.getDiffDays(activityUserInfo.getCreateTime(), DateUtil.getCurrentTimestamp());
            // ??????2?????????????????????????????????
            if (activityUserInfo.getTotalTimes() != 2 && dateCount >= 1 && dateCount < 4 && activityUserInfo
                    .getTotalTimes() != 3) {
                // ??????
                String text = "???????????????????????????????????????????????????VIP???10??????????????????????????????????????????>>";
                String url = "";
                Map<String, String> content = new HashMap<>();
                content.put("pushUrl", "mjlottery://mjnative?page=wap&url=https://predictapi.mojieai.com/web/newuser/");
                content.put("killNumPushText", text);
                PushDto pushDto = new PushDto("??????????????????VIP??????10??????", text, url, content);
                AliyunPushTask pushTask = new AliyunPushTask(pushDto, "ACCOUNT", String.valueOf(activityUserInfo
                        .getUserId()),
                        "default");
                ThreadPool.getInstance().getPushExec().submit(pushTask);
                activityUserInfo.setTotalTimes(3);
                activityUserInfoDao.update(activityUserInfo);
            }

            // ??????????????????????????????
            if (activityUserInfo.getTotalTimes() != 2 && dateCount == 4 && activityUserInfo.getTotalTimes() != 4) {
                // ??????
                smsService.sendVerifyCodePushOnly(loginService.getUserLoginVo(activityUserInfo.getUserId()).getMobile
                        (), "?????????VIP?????????10?????????????????????????????????10???????????????https://predictapi.mojieai.com/99/", DateUtil
                        .getCurrentTimestamp());
                activityUserInfo.setTotalTimes(4);
                activityUserInfoDao.update(activityUserInfo);
            }
        }
    }

    /*
     * ?????????????????? ????????????
     * */
    @Override
    public Map<String, Object> getActivityInfo(Integer activityId, Long userId) {
        Map<String, Object> resultMap = new HashMap<>();
        // ?????????????????? ?????????????????????
        Integer isDoneSign = 0;
        // ?????????????????????
        Game game = GameCache.getGame(GameConstant.SSQ);
//        GamePeriod currentPeriod = PeriodRedis.getCurrentPeriod(game.getGameId());
        GamePeriod currentPeriod = PeriodRedis.getPeriodByGameIdAndPeriod(game.getGameId(), "2018055");
        GamePeriod lastOpenPeriod = PeriodRedis.getLastPeriodByGameIdAndPeriodId(game.getGameId(), currentPeriod
                .getPeriodId());
        // ????????????
        // endTime--?????? ?????????????????? ?????????????????????
        Boolean isOver = Boolean.FALSE;
        if ((int) DateUtil.getDiffSeconds(currentPeriod.getStartTime(), DateUtil.getCurrentTimestamp()) < (13 * 60 *
                60)) {
//            currentPeriod = lastOpenPeriod;
            // ????????????
            isOver = Boolean.TRUE;
        }
        resultMap.put("isOver", isOver);

        // ????????????
        Long shareNums = 0L;
        Integer leftTimes = 5;
        if (userId != null) {
            ActivityDateUserInfo activityDateUserInfo = activityDateUserInfoDao.getUserTimesByDate(activityId, userId,
                    currentPeriod.getPeriodId(), Boolean.FALSE);
            if (null == activityDateUserInfo) {
                /* ???????????????????????????*/
                Integer defaultTimes = 0;
                activityDateUserInfo = new ActivityDateUserInfo(activityId, userId, currentPeriod.getPeriodId(),
                        defaultTimes, null, DateUtil.getCurrentTimestamp(), DateUtil.getCurrentTimestamp());
                activityDateUserInfoDao.insert(activityDateUserInfo);
            }
            // ????????????????????? - ?????????????????????????????????
            int userHaveTimes = 0;
            // 1. ??????
            if (userSignService.checkUserSign(userId, DateUtil.formatDate(new Date(), "yyyyMMdd"), CommonConstant
                    .USER_SIGN_TYPE_DAILY)) {
                isDoneSign = 1;
                userHaveTimes += 1;
            }
            // 2.????????????
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

        // ???????????????
        List<ActivityProgram> activityPrograms = activityProgramDao.getActivityPrograms(currentPeriod.getPeriodId());
        List<Object> records = new ArrayList<>();
        List<ActivityUserLog> activityUserLogs = null;
        if (userId != null) {
            activityUserLogs = activityUserLogDao.getUserLog(activityId, userId);
        }
        // ?????????????????????????????????
        List<ActivityProgramVo> activityProgramVos = new ArrayList<>();
        for (ActivityProgram activityProgram : activityPrograms) {
            ActivityProgramVo activityProgramVo = new ActivityProgramVo(activityProgram);
            // ?????????????????????
            String redNumber = activityProgram.getLotteryNumber().split(CommonConstant.COMMON_COLON_STR)[0];
            String blueNumber = activityProgram.getLotteryNumber().split(CommonConstant.COMMON_COLON_STR)[1];
            if ((redNumber.split(CommonConstant.SPACE_SPLIT_STR).length + blueNumber.split(CommonConstant
                    .SPACE_SPLIT_STR).length) > 7) {
                activityProgramVo.setIsSingleFlag(0); //????????????
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
            // ????????????
            if (DateUtil.compareDate(DateUtil.getCurrentTimestamp(), activityProgramVo.getStartTime())) {
                activityProgramVo.setStatus(0);// ?????????
                activityProgramVo.setStatusText(DateUtil.getTodayTomorrowAndAfterTomorrow(activityProgramVo
                        .getStartTime()) + DateUtil.formatTime(activityProgramVo.getStartTime(), DateUtil
                        .DATE_FORMAT_HHMM));// ?????????
            } else {
                activityProgramVo.setStatus(1);// ????????????
                activityProgramVo.setStatusText("????????????");
            }
            if (activityProgramVo.getLeftCount() <= 0) {
                activityProgramVo.setStatus(2);// ????????????
                activityProgramVo.setStatusText("????????????");
            }
            if (isOver) {
                activityProgramVo.setStatus(3);// ?????????
                activityProgramVo.setStatusText("?????????");
            }

            if (userId != null) {
                for (ActivityUserLog activityUserLog : activityUserLogs) {
                    if (activityUserLog.getLevelId().equals(activityProgramVo.getProgramId())) {
                        activityProgramVo.setStatus(4);// ????????????
                        activityProgramVo.setStatusText("????????????");
                    }
                }
            }

            activityProgramVo.setFirstNumber(firstNumber.toString());
            activityProgramVo.setSecondNumber(secondNumber.toString());
            activityProgramVos.add(activityProgramVo);
        }

        resultMap.put("activityPrograms", activityProgramVos);

        // ????????????
        List<Object> myHistory = new ArrayList<>();
        if (userId != null) {
            for (ActivityUserLog activityUserLog : activityUserLogs) {
                Map<String, Object> myHistoryMap = new HashMap<>();
                // ??????
                myHistoryMap.put("period", activityUserLog.getDateId() + "???");
                // ??????
                ActivityProgram activityProgram = activityProgramDao.getActivityProgramByProgramId(activityUserLog
                        .getLevelId(), Boolean.FALSE);
                myHistoryMap.put("activityProgram", activityProgram);
                // ?????? ?????????????????????
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

        // ??????????????????
        GamePeriod nextPeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(game.getGameId(), currentPeriod
                .getPeriodId());
        String dateStr = DateUtil.getTodayTomorrowAndAfterTomorrow(DateUtil.getIntervalSeconds(nextPeriod
                .getStartTime(), 13 * 60 * 60));
        resultMap.put("dateStr", dateStr);

        // ????????????
        Boolean currentAward = Boolean.FALSE;
        if (null != currentPeriod.getOpenTime()) {
            currentAward = Boolean.TRUE;
        }
        resultMap.put("currentAward", currentAward);

        return resultMap;
    }

    @Override
    public void productProgram() {
        // ?????????
        Game game = GameCache.getGame(GameConstant.SSQ);
        GamePeriod currentPeriod = PeriodRedis.getCurrentPeriod(game.getGameId());

        // ??????????????????
        if ((int) DateUtil.getDiffSeconds(currentPeriod.getStartTime(), DateUtil.getCurrentTimestamp()) < (12 * 60 *
                60)) {
            return;
        }
        // ????????????
        // ??????????????????????????????
        /*?????????10???
        ??????7+2???100???
        ??????8+1???200???*/
        List<ActivityProgram> activityPrograms = activityProgramDao.getActivityPrograms(currentPeriod.getPeriodId());
        // ???????????????????????? ????????????
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
                // ??????20
                StringBuffer redSb = new StringBuffer(game.getGameEn()).append(currentPeriod.getPeriodId()).append
                        (IniCache
                                .getIniValue(IniConstant.RANDOM_CODE, CommonConstant.RANDOM_CODE));
                List<String> redLists = Arrays.asList(GameEnum.getGameEnumById(game.getGameId()).getRedBalls());

                Collections.shuffle(redLists, new Random(new Long((long) redSb.toString().hashCode())));
                List<String> redList = new ArrayList<>(redLists);
                for (int i = 32; i > 29; i--) {
                    redList.remove(i);
                }
                // ??????
                StringBuffer sb = new StringBuffer(game.getGameEn()).append(currentPeriod.getPeriodId()).append(IniCache
                        .getIniValue(IniConstant.RANDOM_CODE, CommonConstant.RANDOM_CODE));
                List<String> blueLists = Arrays.asList(GameEnum.getGameEnumById(game.getGameId()).getBlueBalls());

                Collections.shuffle(blueLists, new Random(new Long((long) sb.toString().hashCode())));

                List<String> blueList = new ArrayList<>(blueLists);
                for (int m = 0; m < 4; m++) {
                    blueList.remove(m);
                }
                // ??????????????????
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
                // ???????????????????????????
                Timestamp startTime = DateUtil.getCurrentTimestamp();

                switch (index) {
                    case 0:
                        // ????????????
                        startTime = today10;
                        break;
                    case 1:
                        // ????????????
                        startTime = tomorrow10;
                        break;
                    case 2:
                        // ????????????
                        startTime = today10;
                        break;
                    case 3:
                        // ????????????
                        startTime = tomorrow10;
                        break;
                    case 4:
                        // ??????22???
                        startTime = today22;
                        break;
                    case 5:
                        // ????????????
                        startTime = tomorrow10;
                        break;
                }
                // ?????????????????????
                if (DateUtil.getTargetWeek(DateUtil.getCurrentTimestamp()).equals("??????")) {
                    switch (index) {
                        case 0:
                            // ????????????
                            startTime = today10;
                            break;
                        case 1:
                            // ????????????
                            startTime = afterTomorrow10;
                            break;
                        case 2:
                            // ????????????
                            startTime = today10;
                            break;
                        case 3:
                            // ????????????
                            startTime = afterTomorrow10;
                            break;
                        case 4:
                            // ??????22???
                            startTime = tomorrow10;
                            break;
                        case 5:
                            // ????????????
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
     * ??????????????????
     * ??????????????????
     * */
    @Override
    public Map<String, Object> drawNumber(Integer activityId, Long userId, Integer programId) {
        Map<String, Object> awardResult = new HashMap<>();
        // ????????????
        Game game = GameCache.getGame(GameConstant.SSQ);
        GamePeriod currentPeriod = PeriodRedis.getCurrentPeriod(game.getGameId());
        // TODO: 2018/5/4 ?????????????????????????????? 
        // ???????????????????????? endTime--?????? 
        if ((int) DateUtil.getDiffSeconds(currentPeriod.getStartTime(), DateUtil.getCurrentTimestamp()) < (13 * 60 *
                60)) {
            return getActivityFinished(awardResult, "?????????????????????9?????????");
        }
        // ??????????????? ????????????
        ActivityInfo activityInfo = activityInfoDao.getActivityInfo(activityId);
        /* ??????????????????????????????????????????*/
        if (null == activityInfo) {
            return getActivityFinished(awardResult, "??????????????????");
        }
        if (!DateUtil.isBetween(DateUtil.getCurrentTimestamp(), activityInfo.getStartTime(), activityInfo.getEndTime
                ())) {
            return getActivityFinished(awardResult, "??????????????????");
        }

        // ?????????????????? ????????????
        return self.updateDrawNumber(activityId, programId, userId, currentPeriod, awardResult);
    }

    /*
     * ??????????????????
     * ????????????????????????
     * */
    @Override
    @Transactional
    public Map<String, Object> updateDrawNumber(Integer activityId, Integer programId, Long userId, GamePeriod
            currentPeriod, Map<String, Object> awardResult) {
        ActivityProgram activityProgram = activityProgramDao.getActivityProgramByProgramId(programId, Boolean.TRUE);
        User user = userDao.getUserByUserId(userId, Boolean.TRUE);
        if (activityProgram.getLeftCount() <= 0) {
            return getActivityFinished(awardResult, "??????????????????");
        }

        // ????????????  // TODO: 2018/5/4 ???????????? 
        /*Map<String, Object> leftTimes = getActivityInfo(activityId, userId);
        if (Integer.valueOf(leftTimes.get("leftTimes").toString()) <= 0) {
            return getActivityFinished(awardResult, "????????????????????????");
        }*/

        // ????????????????????????????????????????????????????????????
        List<ActivityUserLog> activityUserLogs = activityUserLogDao.getDateUserLog(activityId, userId, currentPeriod
                .getPeriodId());
        for (ActivityUserLog activityUserLog : activityUserLogs) {
            if (activityUserLog.getLevelId().equals(programId)) {
                return getActivityFinished(awardResult, "???????????????????????????????????????");
            }
        }

        // ???????????? ??????log
        ActivityUserLog currentUserLog = new ActivityUserLog(null, activityId, userId, programId, currentPeriod
                .getPeriodId(), DateUtil.getCurrentTimestamp());
        activityUserLogDao.insert(currentUserLog);

        // ????????????
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

        // ?????????????????????
        activityDateUserInfo.setTimes(activityDateUserInfo.getTimes() + 1);
        if (insert) {
            activityDateUserInfoDao.insert(activityDateUserInfo);
        } else {
            activityDateUserInfoDao.update(activityDateUserInfo);
        }


        return getSuccess(awardResult, "????????????");
    }

    @Override
    public Map<String, Object> activityGiveCoupon2User(Long userId) {
        Map<String, Object> result = new HashMap<>();
        Integer activityId = 201806001;
        String status = ResultConstant.COUPON_DISTRIBUTE_FAIL_STATUS;
        String validDateDesc = "";
        if (!checkActivityIsEnabled(activityId) || checkUserTakepartActivity(userId, activityId, -1)) {
            validDateDesc = checkUserTakepartActivity(userId, activityId, -1) ? "?????????" : "";
            result.put("title", "????????????");
            result.put("status", status);
            result.put("activityName", "????????????????????????+1");
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
                            validDateDesc = "(?????????" + DateUtil.formatTime(beginTime, DateUtil.DATE_FORMAT_M_D)
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
        result.put("title", "????????????");
        result.put("activityName", "????????????????????????+1");
        result.put("status", status);
        result.put("validDateDesc", validDateDesc);
        return result;
    }

    /*
     * ?????????????????????????????? // TODO: 2018/7/14
     * */
    @Override
    public void vipExpireSmsPush() {
        // ??????????????????????????????
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 1);
        Date time = cal.getTime();

        List<VipMember> vipMembers = vipMemberDao.getVipMemberByExpireDate(DateUtil.getEndOfOneDay(new Timestamp(time
                .getTime())));

        // ???????????? ????????????????????????????????????
        for (VipMember vipMember : vipMembers) {
            // ???????????????
            if (vipMember.getVipType().equals(VipMemberConstant.VIP_MEMBER_TYPE_DIGIT)) {
                //
                smsService.sendVerifyCodePushOnly(loginService.getUserLoginVo(vipMember.getUserId()).getMobile
                        (), "", DateUtil
                        .getCurrentTimestamp());
            }
            // ????????????
            if (vipMember.getVipType().equals(VipMemberConstant.VIP_MEMBER_TYPE_SPORTS)) {
                smsService.sendVerifyCodePushOnly(loginService.getUserLoginVo(vipMember.getUserId()).getMobile
                        (), "", DateUtil
                        .getCurrentTimestamp());
            }
        }
    }

    /**
     * ???????????? ?????? start
     **/
    @Override
    public String productDanguanProgram(String matchId, String programInfo, Long price, Long vipPrice) {
        try {
            // ??????????????????
            DetailMatchInfo detailMatchInfo = thirdHttpService.getMatchMapByMatchId(Integer.valueOf(matchId));
            if (null == detailMatchInfo) {
                return "??????????????????,??????????????????";
            }
            if (DateUtil.compareDate(detailMatchInfo.getEndTime(), DateUtil.getCurrentTimestamp())) {
                return "??????????????????,??????????????????";
            }
            DanguanProgram danguanProgram = danguanProgramDao.getDuanguanProgram(matchId);
            if (null != danguanProgram) {
                return "??????????????????,??????????????????????????????";
            }
            // ?????? programInfo
            String result = "";
            for (String playItem : programInfo.split("\\$")) {
                StringBuffer stringBuffer = new StringBuffer();
                for (String item : playItem.split(",")) {
                    if (CommonUtil.isNumeric(item)) {
                        // ?????????
                        if (!item.equals("7")) {
                            stringBuffer.append(item).append("???:");
                        } else {
                            stringBuffer.append(item).append("+:");
                        }
                        // ??????
                        stringBuffer.append(detailMatchInfo.getItemOdd(5, item)).append(",");
                    } else {
                        // ?????? ??????
                        stringBuffer.append(item).append(":");
                        // ??????
                        stringBuffer.append(detailMatchInfo.getItemOddByName(6, item)).append(",");
                    }
                }
                // ????????????????????????
                if (StringUtils.isBlank(result)) {
                    // ??????index == 0 ????????? ??????$
                    result += stringBuffer.substring(0, stringBuffer.length() - 1) + "$";
                } else {
                    result += stringBuffer.substring(0, stringBuffer.length() - 1);
                }
            }
            danguanProgram = new DanguanProgram(matchId, result, price, vipPrice);
            danguanProgramDao.insert(danguanProgram);
        } catch (Exception e) {
            log.info(e);
            return "??????????????????";
        }
        return "??????????????????";
    }

    @Override
    public List<Map<String, Object>> danguanProgramList(Long userId) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        List<DanguanProgram> danguanPrograms = danguanProgramDao.getNotAwardDuanguanProgram();
        if (null != danguanPrograms && danguanPrograms.size() > 0) {
            for (DanguanProgram danguanProgram : danguanPrograms) {
                // ????????????
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("matchId", danguanProgram.getMatchId());
                resultMap.put("price", danguanProgram.getPrice());
                resultMap.put("vipPrice", danguanProgram.getVipPrice());

                // ????????????
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

                // ??????????????????
                DanguanProgramUser danguanProgramUser = null;
                if (null != userId) {
                    danguanProgramUser = danguanProgramUserDao.getDanguanProgramUserLog(userId,
                            danguanProgram.getMatchId());
                }

                if (null != danguanProgramUser) {
                    // ???????????? ????????????
                    // ???????????? ???5???:4.5???1???:3.1$??????:2.5,??????:3.2
                    resultMap.put("programInfo", danguanProgram.getProgramInfo());
                    resultMap.put("isBuyed", 1);
                    resultMap.put("btnMsg", "");
                } else {
                    // ?????????
                    resultMap.put("programInfo", "");
                    resultMap.put("isBuyed", 0);
                    DanguanProgramUserTimes userTimes = danguanProgramUserTimesDao.getDanguanProgramUserTimes(userId,
                            Boolean.FALSE);
                    if (null != userTimes && userTimes.getLeftTimes() > 0) {
                        resultMap.put("btnMsg", "???????????????");
                    } else {
                        resultMap.put("btnMsg", danguanProgram.getPrice() / 100 + "??????????????? (????????????" + danguanProgram
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
                // ????????????
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("matchId", danguanProgram.getMatchId());
                resultMap.put("price", danguanProgram.getPrice());
                resultMap.put("vipPrice", danguanProgram.getVipPrice());

                // ????????????
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

                // ????????????
                Integer isRight = 0;
                String rightItem = "";
                // ????????????
                if (!StringUtils.isBlank(danguanProgram.getRightItem())) {
                    rightItem = danguanProgram.getRightItem();
                    isRight = 1;
                } else {
                    rightItem = "?????????";
                }
                resultMap.put("leftItem", danguanProgram.getProgramInfo());
                resultMap.put("isRight", isRight);
                resultMap.put("rightItem", rightItem);
                resultList.add(resultMap);
            }
        }

        return resultList;
    }

    // ????????????????????????
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

    // ??????????????????
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
        // ??????????????????
        DanguanProgramUserTimes userTimes = danguanProgramUserTimesDao.getDanguanProgramUserTimes(userId, Boolean.TRUE);
        if (null == userTimes || userTimes.getLeftTimes() == 0) {
            return -1;
        }
        // -1 ????????????
        userTimes.setLeftTimes(userTimes.getLeftTimes() - 1);
        danguanProgramUserTimesDao.update(userTimes);
        DanguanProgramUser danguanProgramUser = new DanguanProgramUser(userId, matchId);
        danguanProgramUserDao.insert(danguanProgramUser);
        return 0;
    }

    @Override
    public Map<String, Object> buyDanguanCard(Long userId, Integer payChannelId, String memo, Integer bankId, Integer
            clientType) {
        // ?????????
        Integer cardId = Integer.valueOf(memo.split(":")[1]);
        String payId = System.currentTimeMillis() + ":" + cardId;
        DanguanProgramCards danguanProgramCards = danguanProgramCardsDao.getDanguanProgramCardByCardId(cardId);

        if (payChannelId.equals(CommonConstant.WISDOM_COIN_CHANNEL_ID)) {
            // ?????????
            Map<String, Object> payForToken = payService.payCreateFlow(userId, payId, danguanProgramCards.getPrice(),
                    CommonConstant.ACCOUNT_TYPE_WISDOM_COIN, payChannelId, danguanProgramCards.getPrice(),
                    "??????????????????????????????", null, null, null, CommonConstant.PAY_OPERATE_TYPE_DEC, null);

            // ???????????? ????????????
            self.buyDuanguanCardBusiness(userId, danguanProgramCards.getTimes());
            payForToken.put("code", ResultConstant.SUCCESS);
            payForToken.put("msg", "????????????");

            return payForToken;
        } else {
            // ??????
            Map<String, Object> payForToken = payService.payCreateFlow(userId, payId, danguanProgramCards.getPrice(),
                    CommonConstant.ACCOUNT_TYPE_CASH, payChannelId, danguanProgramCards.getPrice(),
                    "???????????????????????????", "222.129.17.194", clientType, "activityServiceImpl.buyDuanguanCardBusinessCall",
                    CommonConstant.PAY_OPERATE_TYPE_DEC, bankId);
            return (Map<String, Object>) payForToken.get("payForToken");
        }
    }

    @Override
    @Transactional
    public void buyDuanguanCardBusiness(Long userId, Integer cardTimes) {
        // ?????????
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

        //1.????????????????????????????????????????????????????????????
        if (userAccountFlow == null || userAccountFlow.getStatus() != CommonConstant.PAY_STATUS_FINISH) {
            Integer status = userAccountFlow == null ? -1 : userAccountFlow.getStatus();
            log.error("??????id:" + flowId + " ??????." + "???????????????:" + status);
            return Boolean.FALSE;
        }

        DanguanProgramCards danguanProgramCards = danguanProgramCardsDao.getDanguanProgramCardByCardId(Integer
                .valueOf(cardId.split(":")[1]));
        self.buyDuanguanCardBusiness(userAccountFlow.getUserId(), danguanProgramCards.getTimes());

        return Boolean.TRUE;

    }


    @Override
    public void updateDanguanProgramStatus() {
        // ????????????????????????
        List<DanguanProgram> danguanPrograms = danguanProgramDao.getNotAwardDuanguanProgram();
        if (null != danguanPrograms && danguanPrograms.size() > 0) {
            for (DanguanProgram danguanProgram : danguanPrograms) {
                // ?????????????????? ????????????
                MatchSchedule matchSchedule = matchScheduleDao.getMatchScheduleByPk(Integer.valueOf(danguanProgram
                        .getMatchId()), CommonConstant.LOTTERY_CODE_FOOTBALL);
                if (null == matchSchedule) {
                    log.error("matchSchedule is null" + danguanProgram.getMatchId());
                    continue;
                }
                if (matchSchedule.getIfEnd() != null && matchSchedule.getIfEnd() == 2) {
                    // ?????????????????? ????????????
                    DetailMatchInfo detailMatchInfo = thirdHttpService.getMatchMapByMatchId(matchSchedule.getMatchId());
                    if (null == detailMatchInfo) {
                        log.error("detailMatchInfo is null" + danguanProgram.getMatchId());
                        continue;
                    }
                    // ??????
                    Integer rightBallNums = detailMatchInfo.getHostScore() + detailMatchInfo.getAwayScore();

                    // ???????????????
                    String allBallResult = rightBallNums + "???";
                    // ???????????????
                    String halfAllResult = "";
                    // ?????????
                    log.info("halfScore::::" + detailMatchInfo.getHalfScore());
                    if (null == detailMatchInfo.getHalfScore()) {
                        log.error(detailMatchInfo.getMatchId() + "??????????????????");
                        continue;
                    }
                    String[] halfScore = detailMatchInfo.getHalfScore().replace("??????", "").split(":");
                    log.info("halfScore::::" + halfScore);
                    if (Integer.valueOf(halfScore[0]) > Integer.valueOf(halfScore[1])) {
                        halfAllResult += "???";
                    }
                    if (Integer.valueOf(halfScore[0]) < Integer.valueOf(halfScore[1])) {
                        halfAllResult += "???";
                    }
                    if (Integer.valueOf(halfScore[0]).equals(Integer.valueOf(halfScore[1]))) {
                        halfAllResult += "???";
                    }
                    if (detailMatchInfo.getHostScore() > detailMatchInfo.getAwayScore()) {
                        halfAllResult += "???";
                    }
                    if (detailMatchInfo.getHostScore() < detailMatchInfo.getAwayScore()) {
                        halfAllResult += "???";
                    }
                    if (detailMatchInfo.getHostScore().equals(detailMatchInfo.getAwayScore())) {
                        halfAllResult += "???";
                    }

                    log.info("halfScore::::" + halfAllResult);
                    String programRightItem = "";
                    // ????????????
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
            // ???????????????????????????????????????????????????????????????/?????????????????????????????????????????????1??????????????????
            // ??????????????????????????????????????????????????????????????????/?????????????????????????????????
            // ??????????????????????????????

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(DateUtil.getBeginOfToday());
            calendar.add(Calendar.DATE, -7);
            List<DanguanProgram> danguanProgramList = danguanProgramDao.getDanguanProgramListByLimitDate(new
                    Timestamp(calendar.getTime().getTime()));
            log.info("halfScore::::size" + danguanProgramList.size());
            Integer allRightNums = 0; // ?????????????????????????????????
            Integer allNums = 0; // ?????????????????????????????????
            Integer allRightOdds = 0; //????????????????????????????????????
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

    // ????????????
    @Override
    public Map<String, Object> buyDanguanProgram(Long userId, Integer payChannelId, String memo, Integer bankId, Integer
            clientType) {
        // ????????????
        String matchId = memo.split(":")[1];
        String payId = System.currentTimeMillis() + ":" + matchId;
        DanguanProgram danguanProgram = danguanProgramDao.getDuanguanProgram(matchId);

        Long price = danguanProgram.getPrice();
        if (checkVip(userId) == 1) {
            price = danguanProgram.getVipPrice();
        }

        if (payChannelId.equals(CommonConstant.WISDOM_COIN_CHANNEL_ID)) {
            // ?????????
            Map<String, Object> payForToken = payService.payCreateFlow(userId, payId, price,
                    CommonConstant.ACCOUNT_TYPE_WISDOM_COIN, payChannelId, price,
                    "???????????????????????????", null, null, null, CommonConstant.PAY_OPERATE_TYPE_DEC, null);
            Integer payStatus = (Integer) payForToken.get("payStatus");

            // ???????????? ????????????
            self.buyDuanguanProgramBusiness(userId, matchId);
            payForToken.put("code", ResultConstant.SUCCESS);
            payForToken.put("msg", "????????????");
            return payForToken;
        } else {
            // ??????
            Map<String, Object> payForToken = payService.payCreateFlow(userId, payId, price,
                    CommonConstant.ACCOUNT_TYPE_CASH, payChannelId, price,
                    "????????????????????????", "222.129.17.194", clientType, "activityServiceImpl.buyDuanguanProgramBusinessCall",
                    CommonConstant.PAY_OPERATE_TYPE_DEC, bankId);
            return (Map<String, Object>) payForToken.get("payForToken");
        }
    }

    @Override
    public void buyDuanguanProgramBusiness(Long userId, String matchId) {
        // ????????????
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

        //1.????????????????????????????????????????????????????????????
        if (userAccountFlow == null || userAccountFlow.getStatus() != CommonConstant.PAY_STATUS_FINISH) {
            Integer status = userAccountFlow == null ? -1 : userAccountFlow.getStatus();
            log.error("??????id:" + flowId + " ??????." + "???????????????:" + status);
            return Boolean.FALSE;
        }

        self.buyDuanguanProgramBusiness(userAccountFlow.getUserId(), matchId.split(":")[1]);

        return Boolean.TRUE;
    }

    /** ???????????? ?????? end**/
}
