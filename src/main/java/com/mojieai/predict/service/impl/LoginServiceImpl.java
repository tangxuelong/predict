package com.mojieai.predict.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.constant.ResultConstant;
import com.mojieai.predict.dao.*;
import com.mojieai.predict.entity.bo.GoldTask;
import com.mojieai.predict.entity.po.*;
import com.mojieai.predict.entity.vo.UserLoginVo;
import com.mojieai.predict.enums.LoginValidateEnum;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.ActivityService;
import com.mojieai.predict.service.GoldTaskAwardService;
import com.mojieai.predict.service.LoginService;
import com.mojieai.predict.thread.GoldCoinTask;
import com.mojieai.predict.thread.ThreadPool;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.Md5Util;
import com.mojieai.predict.util.TrendUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutorService;

@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    public RedisService redisService;
    @Autowired
    private UserDao userDao;
    @Autowired
    private MobileUserDao mobileUserDao;
    @Autowired
    private ThirdUserDao thirdUserDao;
    @Autowired
    private UserInfoDao userInfoDao;
    @Autowired
    private UserTokenDao userTokenDao;
    @Autowired
    private UserIdSequenceDao userIdSequenceDao;
    @Autowired
    private UserDeviceInfoDao userDeviceInfoDao;
    @Autowired
    private GoldTaskAwardService goldTaskAwardService;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private UserActiveDao userActiveDao;

    protected Logger log = LogConstant.commonLog;

    @Override
    public String verifyCodeValidate(String mobile, String verifyCode) {
        /* 验证码是否正确校验*/
        String serverVerifyCode = redisService.kryoGet(RedisConstant.PREFIX_SEND_VERIFY_CODE + mobile, String.class);
        if (Strings.isBlank(serverVerifyCode)) {
            return ResultConstant.VALIDATE_VERIFY_CODE_EXPIRE_MSG;
        }
        if (!verifyCode.equals(serverVerifyCode)) {
            return ResultConstant.VALIDATE_VERIFY_CODE_ERR_MSG;
        }
        return null;
    }

    @Override
    public Boolean passwordValidate(String mobile, String password) {
        /* 检查用户是否存在*/
        UserLoginVo userLoginVo = getUserLoginVo(mobile, null, null);
        if (userLoginVo == null) {
            return Boolean.FALSE;
        }
        /* 校验用户密码*/
        User user = userDao.getUserByUserId(userLoginVo.getUserId(), Boolean.FALSE);
        if (user.getPassword() == null || !user.getPassword().equals(Md5Util.getMD5String(password))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean checkValidateTimes(String mobile, String type) {
        Integer verifyCodeTimes = redisService.kryoGet(LoginValidateEnum.getLoginValidateEnum(type).getValidateType
                () + mobile, Integer.class);
        if (null == verifyCodeTimes) {
            verifyCodeTimes = 0;
        }
        verifyCodeTimes++;
        if (verifyCodeTimes >= LoginValidateEnum.getLoginValidateEnum(type).getValidateMaxTimes()) {
            return Boolean.FALSE;
        }
        redisService.kryoSetEx(LoginValidateEnum.getLoginValidateEnum(type).getValidateType() + mobile,
                LoginValidateEnum.getLoginValidateEnum(type).getMaxTimesExpireTime(), verifyCodeTimes);
        return Boolean.TRUE;
    }

    @Override
    public Boolean passwordValidateByUserId(Long userId, String password) {
        User user = userDao.getUserByUserId(userId, Boolean.FALSE);
        if (user.getPassword() == null || !user.getPassword().equals(Md5Util.getMD5String(password))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public UserLoginVo getUserLoginVo(String mobile, String oauthId, Integer oauthType) {
        /*在缓存中查找是否有该用户*/
        try {
            String userLoginVoKey;
            if (Strings.isNotBlank(mobile)) {
                userLoginVoKey = RedisConstant.PREFIX_USER_LOGIN_VO + mobile;
            } else {
                userLoginVoKey = RedisConstant.PREFIX_USER_LOGIN_VO + oauthId + CommonConstant.COMMON_SPLIT_STR +
                        oauthType;
            }
            UserLoginVo userLoginVo = redisService.kryoGet(userLoginVoKey, UserLoginVo.class);
            if (userLoginVo != null) {
                return userLoginVo;
            }
            /* 在数据库里查找该用户*/
            Long userId;
            if (Strings.isBlank(mobile)) {
                userId = thirdUserDao.getUserIdByThird(oauthId, oauthType);
            } else {
                userId = mobileUserDao.getUserIdByMobile(mobile);
            }
            if (userId == null) {
                return null;
            } else {
                UserToken userToken = userTokenDao.getTokenByUserIdByShardType(userId, String.valueOf(userId).substring
                        (String.valueOf(userId).length() - 2));
                if (DateUtil.compareDate(userToken.getExpireTime(), DateUtil.getCurrentTimestamp())) {
                    Timestamp newExpireTime = DateUtil.getIntervalDays(userToken.getExpireTime(), 365L);
                    int updateRes = userTokenDao.updateExpireTime(userToken.getUserId(), userToken.getToken(),
                            userToken.getExpireTime(), newExpireTime);
                    if (updateRes > 0) {
                        userToken.setExpireTime(newExpireTime);
                    }
                }
                return refreshUserLoginVoRedis(mobile, userLoginVoKey, userToken);
            }
        } catch (Exception e) {
            log.error("getUserLoginVo error mobile is " + mobile, e.getMessage());
            throw new BusinessException("getUserLoginVo error mobile is " + mobile + e.getMessage());
        }
    }

    private UserLoginVo refreshUserLoginVoRedis(String mobile, String userLoginVoKey, UserToken userToken) {
        UserInfo userInfo = userInfoDao.getUserInfo(userToken.getUserId());
        UserLoginVo userLoginVo = new UserLoginVo(userToken.getToken(), userInfo.getUserId(), userInfo.getNickName(),
                userInfo.getHeadImgUrl(), mobile, analysisRealNameAuthentication(userInfo.getRemark()));

        redisService.kryoSetEx(userLoginVoKey, TrendUtil.getExprieSecond(userToken.getExpireTime(), 0), userLoginVo);
        return userLoginVo;
    }

    @Override
    public UserLoginVo getUserLoginVo(Long userId) {
        User user = userDao.getUserByUserId(userId, Boolean.FALSE);
        if (user == null) {
            log.error("get user is null on checkToken userId:" + userId);
            throw new BusinessException("get user is null on checkToken userId:" + userId);
        }
        UserLoginVo userLoginVo = getUserLoginVo(user.getMobile(), null, null);
        return userLoginVo;
    }

    @Override
    public Long generateUserId() {
        String timePrefix = DateUtil.formatDate(new Date(), DateUtil.DATE_FORMAT_YYMMDDHH);
        long seq = userIdSequenceDao.getUserIdSequence();
        Long userId = Long.parseLong(timePrefix + CommonUtil.formatSequence(seq));
        return userId;
    }

    @Override
    public String generateUserToken(Long userId) {
        UUID uuid = UUID.randomUUID();
        String userIdStr = userId.toString();
        StringBuffer sb = new StringBuffer();
        sb.append(uuid).append(userIdStr.substring(userIdStr.length() - 2));
        return sb.toString();
    }

    @Override
    public UserToken checkToken(String token) {
//        log.info("token:" + token);
        /* 校验token*/
        UserToken userToken = getUserToken(token);
        if (userToken != null) {
            return userToken;
        }
        return null;
    }

    @Override
    public Boolean checkUser(Long userId) {
        try {
            User user = userDao.getUserByUserId(userId, Boolean.FALSE);
            if (user == null) {
                return Boolean.FALSE;
            }
        } catch (Exception e) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public void setPassword(Long userId, String password) {
        User user = userDao.getUserByUserId(userId, Boolean.TRUE);
        if (user == null) {
            log.error("set password user is not exist userId:" + userId);
            throw new BusinessException("set password user is not exist userId:" + userId);
        }
        user.setPassword(Md5Util.getMD5String(password));
        userDao.update(user);
    }

    @Override
    public UserLoginVo modifyHeadImgOrNickName(String token, Long userId, String headImgUrl, String nickName) {
        try {
            boolean modifyImgAddCoin = false;
            boolean modifyNameAddCoin = false;
            UserInfo userInfo = userInfoDao.getUserInfo(userId);
            if (userInfo == null) {
                log.error("set headImgUrl or nickname userInfo is not exist userId:" + userId);
                throw new BusinessException("set headImgUrl or nickname userInfo is not exist userId:" + userId);
            }
            User user = userDao.getUserByUserId(userId, Boolean.FALSE);
            if (null != headImgUrl) {
                if (userInfo.getHeadImgUrl().equals(CommonConstant.DEFAULT_HEAD_IMG_URL) && !headImgUrl.equals
                        (CommonConstant.DEFAULT_HEAD_IMG_URL)) {
                    modifyImgAddCoin = true;
                }
                userInfo.setHeadImgUrl(headImgUrl);
            }
            if (null != nickName) {
                String defaultNickName = CommonUtil.getUserMoblieDefaultName(user.getMobile());
                if (StringUtils.isNotBlank(defaultNickName) && userInfo.getNickName().equals(defaultNickName) &&
                        !nickName.equals(defaultNickName)) {
                    modifyNameAddCoin = true;
                }
                userInfo.setNickName(nickName);
            }
            userInfoDao.update(userInfo);
            UserLoginVo userLoginVo = refreshUserLoginVoRedis(user.getMobile(), RedisConstant.PREFIX_USER_LOGIN_VO +
                    user.getMobile(), getUserToken(token));

            ExecutorService taskExec = ThreadPool.getInstance().getUserSocialTaskExec();
            if (modifyImgAddCoin) {
                GoldCoinTask task = new GoldCoinTask(userId, GoldTask.TASK_TYPE_UPLOAD_HEAD_IMG, goldTaskAwardService);
                taskExec.submit(task);
            }
            if (modifyNameAddCoin) {
                GoldCoinTask task = new GoldCoinTask(userId, GoldTask.TASK_TYPE_MODIFY_NICK_NAME, goldTaskAwardService);
                taskExec.submit(task);
            }
            return userLoginVo;
        } catch (Exception e) {
            log.error("modifyHeadImgOrNickName error: e" + e.getMessage());
            return null;
        }
    }

    @Override
    public UserLoginVo userLogin(String mobile, String password, String channelType, String oauthId, Integer
            oauthType, String deviceId) {
        UserLoginVo userLoginVo = getUserLoginVo(mobile, oauthId, oauthType);
        /* 如果用户不存在，创建用户*/
        if (userLoginVo == null) {
            userLoginVo = addUser(mobile, password, channelType, oauthId, oauthType, deviceId);
            /* 建立用户缓存*/
            String userLoginVoKey;
            if (Strings.isNotBlank(mobile)) {
                userLoginVoKey = RedisConstant.PREFIX_USER_LOGIN_VO + mobile;
            } else {
                userLoginVoKey = RedisConstant.PREFIX_USER_LOGIN_VO + oauthId + CommonConstant.COMMON_SPLIT_STR +
                        oauthType;
            }
            redisService.kryoSetEx(userLoginVoKey, RedisConstant.EXPIRE_TIME_SECOND_THIRTY_DAY, userLoginVo);
        }
        /* 更换设备更新设备信息*/
        if (null != deviceId) {
            deviceId = generateDeviceId(deviceId);
            List<String> userDeviceIdList = redisService.kryoZRangeByScoreGet(RedisConstant.USER_DEVICE, userLoginVo
                    .getUserId(), userLoginVo.getUserId(), String.class);
            if (null != userDeviceIdList && userDeviceIdList.size() > 0) {
                if (!userDeviceIdList.get(0).equals(deviceId)) {
                    userInfoDao.updateDeviceId(deviceId, userLoginVo.getUserId());
                }
            }
            redisService.kryoZAddSet(RedisConstant.USER_DEVICE, userLoginVo.getUserId(), deviceId);
        }
        return userLoginVo;
    }

    @Override
    public Long getUserId(String mobile) {
        if (StringUtils.isBlank(mobile)) {
            return null;
        }
        return mobileUserDao.getUserIdByMobile(mobile);
    }

    @Override
    public void updateDeviceInfo(UserDeviceInfo userDeviceInfo) {
        try {
            // 更新设备信息，没有记录插入操作
            UserDeviceInfo userDeviceInfoDB = userDeviceInfoDao.getUserDeviceInfoByDeviceId(userDeviceInfo
                    .getDeviceId());
            if (null == userDeviceInfoDB) {
                userDeviceInfoDao.insert(userDeviceInfo);
            } else {
                if (null == userDeviceInfo.getUserId()) {
                    userDeviceInfo.setUserId(userDeviceInfoDB.getUserId());
                }
                userDeviceInfoDao.update(userDeviceInfo);
            }
            if (userDeviceInfo.getUserId() != null) {
                try {
                    Integer date = Integer.valueOf(DateUtil.formatDate(new Date(), "yyyyMMdd"));
                    userActiveDao.insert(new UserActive(userDeviceInfo.getUserId(), date));
                } catch (Exception e) {

                }
            }

            // 更新设备信息按照彩种存储
            List<Long> gameIdList = new ArrayList<>(GameCache.getAllGameMap().keySet());
            for (Long gameId : gameIdList) {
                Game game = GameCache.getGame(gameId);
                if (null == game) {
                    log.error("copyPushUsers get game is null");
                    throw new BusinessException("copyPushUsers get game is null");
                }
                /* 大盘彩*/
                if (game.getGameType().equals(Game.GAME_TYPE_COMMON)) {
                    if (null != userDeviceInfoDB) {
                        /* 如果是阿里推送账号 去掉DB中clientId(个推)redis缓存*/
                        if (null != userDeviceInfo.getPushType() && userDeviceInfo.getPushType().equals(1)) {
                            redisService.kryoZRem(RedisConstant.getPushClientList(gameId), userDeviceInfoDB
                                    .getClientId());
                        }
                        if (null != userDeviceInfoDB) {
                            if (userDeviceInfoDB.getPushGameEns().indexOf(game.getGameEn()) > -1) {
                                /* 如果是阿里推送账号 不添加个推 redis缓存 添加阿里推送缓存*/
                                if (null == userDeviceInfoDB.getPushType() || !userDeviceInfoDB.getPushType().equals
                                        (1)) {
                                    redisService.kryoZAddSet(RedisConstant.getPushClientList(gameId), System
                                            .currentTimeMillis(), userDeviceInfo.getClientId());
                                } else {
                                    redisService.kryoZAddSet(RedisConstant.getAliPushClientList(gameId), System
                                            .currentTimeMillis(), userDeviceInfo.getClientId());
                                }
                            }
                        } else {
                            if (userDeviceInfo.getPushType().equals(1)) {
                                redisService.kryoZAddSet(RedisConstant.getAliPushClientList(gameId), System
                                        .currentTimeMillis(), userDeviceInfo.getClientId());
                            } else {
                                redisService.kryoZAddSet(RedisConstant.getPushClientList(gameId), System
                                        .currentTimeMillis(), userDeviceInfo.getClientId());
                            }

                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("update device info to db error deviceId" + userDeviceInfo.getDeviceId() + e
                    .getMessage());
            throw new BusinessException("update device info to db error deviceId" + userDeviceInfo.getDeviceId() + e
                    .getMessage());
        }

    }

    @Override
    public String generateDeviceId(String deviceId) {
        if (StringUtils.isNotBlank(deviceId)) {
            String hashCode = String.valueOf(deviceId.hashCode());
            return deviceId + hashCode.substring(hashCode.length() - 2, hashCode.length());
        }
        return deviceId;
    }

    @Override
    public Boolean checkUserIsSetPassword(Long userId) {
        User user = userDao.getUserByUserId(userId, Boolean.FALSE);
        if (StringUtils.isNotBlank(user.getPassword())) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public Map<String, Object> editUserFootballIntroduction(Long userId, String text) {
        Map<String, Object> res = new HashMap<>();
        res.put("code", 0);
        res.put("msg", "保存成功");
        UserInfo userInfo = userInfoDao.getUserInfo(userId);
        if (userInfo.getIsReMaster() == null || userInfo.getIsReMaster().equals(0)) {
            res.put("code", -1);
            res.put("msg", "您还不是智慧师，只有智慧师可以编辑简介");
            return res;
        }
        userInfo.setFootballIntroduce(CommonUtil.filterEmoji(text));
        userInfoDao.update(userInfo);
        return res;
    }

    @Transactional
    public UserLoginVo addUser(String mobile, String password, String channelType, String oauthId, Integer
            oauthType, String deviceId) {
        try {
            Long userId = generateUserId();
            String token = generateUserToken(userId);
            if (StringUtils.isNotBlank(password)) {
                password = Md5Util.getMD5String(password);
            }
            User user = new User(userId, mobile, password, oauthId, oauthType, new Timestamp
                    (System.currentTimeMillis()),
                    new Timestamp(System.currentTimeMillis()));
            /* 用户*/
            userDao.insert(user);
            StringBuffer mobileStr = new StringBuffer(mobile);
            String defaultNickName = mobileStr.replace(3, 7, "****").toString();
            Random random = new Random();
            Integer rand = random.nextInt(25);
            String defaultUrl = "https://cdn.caiqr.com/headsculpture" + String.valueOf(rand) + ".jpg";
            UserInfo userInfo = new UserInfo(userId, defaultNickName, defaultUrl, channelType,
                    generateDeviceId(deviceId), new Timestamp(System.currentTimeMillis()), new Timestamp(System
                    .currentTimeMillis()));
            /* 用户信息*/
            userInfoDao.insert(userInfo);
            if (Strings.isNotBlank(mobile)) {
                /* 手机号关联*/
                MobileUser mobileUser = new MobileUser(mobile, userId, new Timestamp(System.currentTimeMillis()));
                mobileUserDao.insert(mobileUser);
            }
            if (Strings.isNotBlank(oauthId)) {
                /* 三方信息关联*/
                ThirdUser thirdUser = new ThirdUser(userId, oauthId, oauthType, new Timestamp(System
                        .currentTimeMillis()));
                thirdUserDao.insert(thirdUser);
            }
            /* userToken*/
            UserToken userToken = new UserToken(userId, token, DateUtil.getIntervalTimestamp(new Date(), 365),
                    DateUtil.getCurrentTimestamp());
            userTokenDao.insert(userToken);

            return new UserLoginVo(token, userId, defaultNickName, defaultUrl, mobile,
                    analysisRealNameAuthentication(userInfo.getRemark()));
        } catch (Exception e) {
            log.error("add user error", e);
            throw new BusinessException("add user error", e);
        }

    }

    private Boolean analysisRealNameAuthentication(String userInfoRemark) {
        Boolean realNameAuthentication = Boolean.FALSE;
        if (StringUtils.isNotBlank(userInfoRemark)) {
            Map<String, Object> remark = JSONObject.parseObject(userInfoRemark, HashMap.class);
            if (remark != null && remark.containsKey("realName") && remark.containsKey("idCard") && remark.get
                    ("realName") != null && remark.get("idCard") != null) {
                realNameAuthentication = Boolean.TRUE;
            }
        }
        return realNameAuthentication;
    }

    private UserToken getUserToken(String token) {
        try {
            if (StringUtils.isBlank(token)) {
//                log.error("check token is null");
                return null;
            }
            UserToken userToken = redisService.kryoGet(RedisConstant.PREFIX_USER_TOKEN + token, UserToken.class);
            if (null == userToken) {
                userToken = userTokenDao.getUserTokenByToken(token);
                /* 如果数据库有，设置缓存*/
                if (userToken != null) {
                    redisService.kryoSetEx(RedisConstant.PREFIX_USER_TOKEN + token, RedisConstant
                            .EXPIRE_TIME_SECOND_THIRTY_DAY, userToken);
                } else {
                    log.error("check token is not exist" + token);
                }

            }
            return userToken;
        } catch (Exception e) {
            log.error("check token error" + e.getMessage());
            throw new BusinessException("check token error" + e.getMessage());
        }


    }
}
