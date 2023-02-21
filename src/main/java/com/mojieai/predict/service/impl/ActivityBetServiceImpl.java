package com.mojieai.predict.service.impl;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.cache.IniCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.*;
import com.mojieai.predict.entity.dto.HttpParamDto;
import com.mojieai.predict.entity.dto.PushDto;
import com.mojieai.predict.entity.po.*;
import com.mojieai.predict.entity.vo.ActivityProgramVo;
import com.mojieai.predict.entity.vo.UserLoginVo;
import com.mojieai.predict.enums.GameEnum;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.*;
import com.mojieai.predict.service.beanself.BeanSelfAware;
import com.mojieai.predict.thread.AliyunPushTask;
import com.mojieai.predict.thread.ThreadPool;
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
public class ActivityBetServiceImpl implements ActivityBetService {
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
    private BetOrderDao betOrderDao;
    @Autowired
    private UserAccountDao userAccountDao;

//    private ActivityBetService self;

    protected Logger log = LogConstant.commonLog;


//    @Override
//    public void setSelf(Object proxyBean) {
//        self = (ActivityBetService) proxyBean;
//    }

    @Override
    public Integer betMatchItem(Long userId, String matchId, String betItem, Integer betNums, Integer betType,
                                Integer isAward) {
        // 创建订单
        createBetOrder(userId, matchId, betItem, betNums, betType, isAward);
        // 检查用户余额 余额不足 支付失败
        // 指定货币 todo 智慧币
        UserAccount userAccount = userAccountDao.getUserAccountBalance(userId, CommonConstant
                .ACCOUNT_TYPE_WISDOM_COIN, Boolean.TRUE);
        if (userAccount.getAccountBalance() < (betNums * 2)) {
            return -1;
        }
        // 支付 扣除用户余额创建流水
        Long amount = Long.parseLong(String.valueOf(betNums * 2));
        payService.payCreateFlow(userId, matchId + betItem, amount, CommonConstant.PAY_TYPE_WISDOM_COIN, null,
                amount, "比赛投注", null, null, null, CommonConstant.PAY_OPERATE_TYPE_DEC, null);
        // 支付成功
        return 0;
    }

    private void createBetOrder(Long userId, String matchId, String betItem, Integer betNums, Integer betType, Integer
            isAward) {
        /*"1111$3^1&1112$1^0&1113$1^0"
        （&分割不同比赛，$分割match_id和answer_id,^分割answer_id）*/
        // 金额计算 TODO 默认单关
        BetOrder betOrder = new BetOrder(null, userId, matchId, betNums * 2, null, 0, betItem, isAward);
    }

    @Override
    public void userAppointment(Long userId, Integer activityId) {
        // 查询
        List<ActivityUserLog> activityUserLogs = activityUserLogDao.getUserLog(activityId, userId);
        if (null == activityUserLogs || activityUserLogs.size() == 0) {
            ActivityUserLog activityUserLog = new ActivityUserLog(null, activityId, userId,
                    100, "0", DateUtil
                    .getCurrentTimestamp());
            activityUserLogDao.insert(activityUserLog);
        }
    }

    @Override
    public Integer confirmAwardGoods(Long userId, Integer activityId, Integer levelId, String uniqueId) {
        ActivityUserLog activityUserLog = new ActivityUserLog(null, activityId, userId,
                levelId, uniqueId, DateUtil.getCurrentTimestamp());
        activityUserLogDao.insert(activityUserLog);
        return 0;
    }
}
