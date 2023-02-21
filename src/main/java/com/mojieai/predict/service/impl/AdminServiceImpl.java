package com.mojieai.predict.service.impl;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.dao.*;
import com.mojieai.predict.entity.bo.PaginationList;
import com.mojieai.predict.entity.bo.SocialKillNumFilter;
import com.mojieai.predict.entity.po.*;
import com.mojieai.predict.entity.vo.UserLoginVo;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.AdminService;
import com.mojieai.predict.service.LoginService;
import com.mojieai.predict.service.PredictNumService;
import com.mojieai.predict.service.SMSService;
import com.mojieai.predict.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Ynght on 2016/11/25.
 */
@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private BannerDao bannerDao;

    @Autowired
    private ActivityIniDao activityIniDao;

    @Autowired
    private MobileUserDao mobileUserDao;

    @Autowired
    private LoginService loginService;

    @Autowired
    private RedisService redisService;
    @Autowired
    private PredictNumbersOperateDao predictNumbersOperateDao;
    @Autowired
    private PredictNumService predictNumService;
    @Autowired
    private SocialEncircleCodeDao socialEncircleCodeDao;
    @Autowired
    private UserInfoDao userInfoDao;
    @Autowired
    private SMSService smsService;

    // 查询banner
    @Override
    public List<Banner> queryBanner() {
        List<Banner> bannerVos = bannerDao.getAllUsableBanners();
        return bannerVos;
    }

    @Override
    public String login(String mobile, String password) {
        UserLoginVo userLoginVo = loginService.getUserLoginVo(mobile, null, null);
        if (null != userLoginVo) {
            /* 用户验证*/
            String adminKey = redisService.kryoGet(RedisConstant.getAdminKey(mobile), String.class);
            if (loginService.passwordValidate(mobile, password) && null != adminKey) {
                return mobile;
            }
        }
        return null;
    }

    @Override
    public void setAdmin(String mobile) {
        redisService.kryoSet(RedisConstant.getAdminKey(mobile), "admin");
    }

    @Override
    public Banner bannerAdd(Banner banner, String isNew) {
        // 添加 删除 修改banner
        if (StringUtils.isNotBlank(isNew)) {
            // 添加 banner
            banner.setBannerId(null);
            bannerDao.insertBanner(banner);
        } else {
            // 删除 修改banner
            bannerDao.updateBanner(banner);
        }
        return banner;
    }

    @Override
    public List<ActivityIni> getAllActivityInis() {
        return activityIniDao.getAllIni();
    }


    @Override
    public void activityIniAddOrUpdate(ActivityIni activityIni) {
        ActivityIni ini1 = activityIniDao.getIni(activityIni.getIniName());
        if (ini1 != null) {
            activityIniDao.updateIni(activityIni);
            return;
        }
        activityIniDao.insert(activityIni);
    }

    @Override
    public void updateActivityIni(Map<String, String> keyValueMap) {
        for (Map.Entry entry : keyValueMap.entrySet()) {
            ActivityIni activityIni = activityIniDao.getIni(entry.getKey().toString());
            activityIni.setIniValue(entry.getValue().toString());
            activityIniDao.updateIni(activityIni);
        }
    }

    @Override
    public Map<String, Object> setPeriodManualRule(String gameEn, String ruleStr) {
        Map<String, Object> result = new HashMap<>();
        Game game = GameCache.getGame(gameEn);

        String[] ruleArr = ruleStr.split(CommonConstant.COMMON_COLON_STR);
        if (ruleArr.length != 3) {
            result.put("msg", "规则错误，请重新输入");
            return result;
        }
        GamePeriod gamePeriod = PeriodRedis.getAwardCurrentPeriod(game.getGameId());

        Map operateRule = predictNumbersOperateDao.getPredictNumsByGameIdAndPeriodId(game.getGameId(), gamePeriod
                .getPeriodId());
        int res;
        if (operateRule == null || operateRule.isEmpty()) {
            PredictNumbersOperate predictNumbersOperate = new PredictNumbersOperate();
            predictNumbersOperate.setGameId(game.getGameId());
            predictNumbersOperate.setPeriodId(gamePeriod.getPeriodId());
            predictNumbersOperate.setRuleStr(ruleStr);
            predictNumbersOperate.setOperateNums(null);
            res = predictNumbersOperateDao.insert(predictNumbersOperate);
        } else {
            res = predictNumbersOperateDao.saveRuleStr(game.getGameId(), gamePeriod.getPeriodId(), ruleStr);
        }

        if (res > 0) {
            result.put("msg", "保存成功");
            result.put("periodId", gamePeriod.getPeriodId());
        }
        return result;
    }

    @Override
    public Map<String, Object> updateOperateStatus(long gameId, String periodId, Integer status) {
        Map<String, Object> result = new HashMap<>();

        int res = predictNumbersOperateDao.updateStatus(gameId, periodId, status);
        if (res > 0) {
            if (status == 0) {
                result.put("msg", periodId + "期已开启人工干预");
            } else {
                result.put("msg", periodId + "期已关闭人工干预");
            }
        }
        return result;
    }

    @Override
    public List<PredictNumbersOperate> getOpertePredict(Long gameId, String minPeriodId, String maxPeriodId, String
            manualFlag) {
        List<PredictNumbersOperate> result = new ArrayList<>();
        result = predictNumbersOperateDao.getPredictNumsByCondition(gameId, minPeriodId, maxPeriodId, manualFlag);
        return result;
    }

    @Override
    public String updateUserPredictNums(String mobile, Long gameId, Integer addNums) {
        try {
            Long userId = mobileUserDao.getUserIdByMobile(mobile);
            if (userId == null) {
                return "用户不存在";
            }
            GamePeriod currentPeriod = PeriodRedis.getCurrentPeriod(gameId);
            predictNumService.updateUserPredictMaxNums(gameId, currentPeriod.getPeriodId(), userId, addNums);
            return null;
        } catch (Exception e) {
            return "添加次数失败";
        }
    }

    @Override
    public Integer getUserPredictNums(String mobile, Long gameId) {
        try {
            Long userId = mobileUserDao.getUserIdByMobile(mobile);
            if (userId == null) {
                return CommonConstant.USERID_PREDICT_MAX_TIMES;
            }
            GamePeriod currentPeriod = PeriodRedis.getCurrentPeriod(gameId);
            return predictNumService.getUserPredictMaxNums(gameId, currentPeriod.getPeriodId(), userId);
        } catch (Exception e) {
            return CommonConstant.USERID_PREDICT_MAX_TIMES;
        }
    }

    @Override
    public PaginationList<SocialEncircle> getSocialEncircleList(long gameId, String periodId, Integer page,
                                                                SocialKillNumFilter socialKillNumFilter) {
        PaginationList<SocialEncircle> paginationList = socialEncircleCodeDao.getSocialEncircleByPage(gameId,
                periodId, page);
        return paginationList;
    }

    /*
     * 发送推送短信
     * */
    @Override
    public void sendPushSms(String typeStr, String msg) {
        String typeDigital = "0";
        String typeSports = "1";
        String typeNull = "2";
        // 查出所有用户
        List<UserInfo> userInfos = userInfoDao.geAllUserInfos();
        if (null != userInfos && userInfos.size() > 0) {
            for (UserInfo userInfo : userInfos) {
                if (typeStr.contains(typeDigital) && userInfo.getRemark().contains(typeDigital)) {
                    smsService.sendVerifyCodePushOnly(loginService.getUserLoginVo(userInfo.getUserId()).getMobile
                            (), msg, DateUtil.getCurrentTimestamp());
                }
                if (typeStr.contains(typeSports) && userInfo.getRemark().contains(typeSports)) {
                    smsService.sendVerifyCodePushOnly(loginService.getUserLoginVo(userInfo.getUserId()).getMobile
                            (), msg, DateUtil.getCurrentTimestamp());
                }
                if (typeStr.contains(typeNull) && !userInfo.getRemark().contains(typeDigital) && !userInfo.getRemark
                        ().contains(typeSports)) {
                    smsService.sendVerifyCodePushOnly(loginService.getUserLoginVo(userInfo.getUserId()).getMobile
                            (), msg, DateUtil.getCurrentTimestamp());
                }
            }
        }
    }
}