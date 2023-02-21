package com.mojieai.predict.controller;

import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.cache.BannerCache;
import com.mojieai.predict.cache.IniCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.entity.po.UserDeviceInfo;
import com.mojieai.predict.entity.po.UserToken;
import com.mojieai.predict.entity.vo.BannerVo;
import com.mojieai.predict.entity.vo.ResultVo;
import com.mojieai.predict.entity.vo.UserLoginVo;
import com.mojieai.predict.entity.vo.UserLoginVoPack;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.*;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by tangxuelong on 2017/7/3.
 */
@RequestMapping("/UserAuth")
@Controller
public class UserAuthController extends BaseController {

    @Autowired
    private LoginService loginService;
    @Autowired
    private SMSService smsService;
    @Autowired
    private VipMemberService vipMemberService;
    @Autowired
    private TouristUserService touristUserService;
    @Autowired
    private CompatibleService compatibleService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private SocialIntegralLogService socialIntegralLogService;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private UserAccountService userAccountService;
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private BindBankCardService bindBankCardService;
    @Autowired
    private UserBankCardService userBankCardService;
    @Autowired
    private UserWithdrawFlowService userWithdrawFlowService;

    @RequestMapping("/encryptLogin")
    @ResponseBody
    public Object encryptLogin(@RequestAttribute String mobile, @RequestAttribute(required = false) String verifyCode,
                               @RequestAttribute(required = false) String password, @RequestAttribute(required = false)
                                       String channelType, @RequestParam(required = false) String deviceId) {
        Integer clientType = null;
        if (channelType != null && channelType.equals("appStore")) {
            clientType = CommonConstant.CLIENT_TYPE_IOS;
        }
        return login(mobile, verifyCode, password, channelType, deviceId, clientType);
    }

    /* 登录验证接口*/
    @RequestMapping("/login")
    @ResponseBody
    public Object login(@RequestParam String mobile, @RequestParam(required = false) String verifyCode,
                        @RequestParam(required = false) String password, @RequestParam(required = false)
                                String channelType, @RequestParam(required = false) String deviceId, Integer
                                clientType) {
        UserLoginVo userLoginVo;
        /* 参数校验*/
        if (!mobileFormat(mobile)) {
            return buildErrJson(ResultConstant.MOBILE_FORMAT_ERR_MSG);
        }
        if (StringUtils.isNotBlank(password) && !passwordFormat(password)) {
            return buildErrJson(ResultConstant.PASSWORD_FORMAT_ERR_MSG);
        }
        /* 验证码注册*/
        if (Strings.isNotBlank(verifyCode)) {
            if (!loginService.checkValidateTimes(mobile, "SMS")) {
                /* 验证验证码次数*/
                return buildErrJson(ResultConstant.VALIDATE_VERIFY_CODE_TIMES_FAILURE_MSG);
            }
            String verifyCodeValidateMsg = loginService.verifyCodeValidate(mobile, verifyCode);
            if (StringUtils.isNotBlank(verifyCodeValidateMsg)) {
                /* 验证失败*/
                return buildErrJson(verifyCodeValidateMsg);
            }
        } else {
            /* 安全校验*/
            if (!userInfoService.safeCheck(deviceId, mobile)) {
                log.error("deviceId warn:" + deviceId + " 未通过安全校验，请及时处理");
                return buildErrJson("账号异常");
            }

            /* 密码登录*/
            if (!loginService.checkValidateTimes(mobile, "PASSWORD")) {
                /* 验证密码次数*/
                return buildErrJson(ResultConstant.VALIDATE_PASSWORD_TIMES_FAILURE_MSG);
            }
            if (!passwordFormat(password) || !loginService.passwordValidate(mobile, password)) {
                /* 用户名或密码失效*/
                return buildErrJson(ResultConstant.VALIDATE_PASSWORD_FAILURE_MSG);
            }
        }

        Boolean isNewUser = Boolean.FALSE;
        UserLoginVo userFlag = loginService.getUserLoginVo(mobile, null, null);
        // 如果用户不存在
        if (null == userFlag) {
            // 用户是新手用户
            isNewUser = Boolean.TRUE;
        }

        userLoginVo = loginService.userLogin(mobile, password, channelType, null, null, deviceId);

        /* 返回用户信息和token*/
        Map<String, Object> resultData = new HashMap<>();
        mobileHidden(userLoginVo);
        if (clientType != null && clientType % 2 == 1) {
            UserLoginVoPack userLoginVoPack = new UserLoginVoPack(userLoginVo);
            resultData.put("userLogin", userLoginVoPack);
        } else {
            resultData.put("userLogin", userLoginVo);
        }
        resultData.put("isNewUser", isNewUser);
        resultData.put("isVip", vipMemberService.checkUserIsVip(userLoginVo.getUserId(), VipMemberConstant
                .VIP_MEMBER_TYPE_DIGIT));
        resultData.put("isSportsVip", vipMemberService.checkUserIsVip(userLoginVo.getUserId(), VipMemberConstant
                .VIP_MEMBER_TYPE_SPORTS));
        resultData.put("isSetPassword", loginService.checkUserIsSetPassword(userLoginVo.getUserId()));
        try {
            Map<String, Object> activityCheck = activityService.checkIsGivenWisdomCoin(userLoginVo.getUserId(),
                    201803001);
            if ((Boolean) activityCheck.get("IsGivenWisdomCoin")) {
                resultData.put("IsGivenWisdomCoin", activityCheck.get("IsGivenWisdomCoin"));
            }
        } catch (Exception e) {
            log.error("activityCheck error", e);
        }
        return buildSuccJson(resultData);
    }

    /* check token*/
    @RequestMapping("/checkToken")
    @ResponseBody
    public Object checkToken(@RequestParam String token, @RequestAttribute(required = false) Integer clientType) {
        /* token校验*/
        UserToken userToken = loginService.checkToken(token);
        if (userToken == null) {
            return buildJson(ResultConstant.VALIDATE_TOKEN_FAILURE_CODE, ResultConstant.VALIDATE_TOKEN_FAILURE_MSG);
        }
        UserLoginVo userLoginVo = loginService.getUserLoginVo(userToken.getUserId());
        /* 返回用户信息和token*/
        Map<String, Object> resultData = new HashMap<>();
        mobileHidden(userLoginVo);

        /* 游客*/
        boolean isTourist = false;
        String touristModle = ActivityIniCache.getActivityIniValue(ActivityIniConstant.TOURIST_MODLE_SWITCH, "off");
        if (StringUtils.isNotBlank(touristModle) && touristModle.equals(ActivityIniConstant.TOURIST_MODLE_SWITCH_ON)) {
            isTourist = touristUserService.checkUserIdIsTourist(userLoginVo.getUserId());
        }
        resultData.put("isVip", vipMemberService.checkUserIsVip(userLoginVo.getUserId(), VipMemberConstant
                .VIP_MEMBER_TYPE_DIGIT));
        resultData.put("isSportsVip", vipMemberService.checkUserIsVip(userLoginVo.getUserId(), VipMemberConstant
                .VIP_MEMBER_TYPE_SPORTS));
        if (clientType != null && clientType % 2 == 1) {
            UserLoginVoPack userLoginVoPack = new UserLoginVoPack(userLoginVo);
            resultData.put("userLogin", userLoginVoPack);
        } else {
            resultData.put("userLogin", userLoginVo);
        }
        resultData.put("isTourist", isTourist);
        resultData.put("isSetPassword", loginService.checkUserIsSetPassword(userLoginVo.getUserId()));
        return buildSuccJson(resultData);
    }

    @RequestMapping("/encryptThirdLogin")
    @ResponseBody
    public Object encryptThirdLogin(@RequestAttribute String oauthId, @RequestAttribute(required = false) Integer
            oauthType,
                                    @RequestAttribute(required = false) String deviceId) {
        return thirdLogin(oauthId, oauthType, deviceId);
    }

    /* 登录验证接口*/
    @RequestMapping("/thirdLogin")
    @ResponseBody
    public Object thirdLogin(@RequestParam String oauthId, @RequestParam(required = false) Integer oauthType,
                             @RequestParam(required = false) String deviceId) {
        UserLoginVo userLoginVo;
        /* 参数校验*/
        if (Strings.isBlank(oauthId)) {
            return buildErrJson(ResultConstant.PARAMS_ERR_MSG);
        }
        userLoginVo = loginService.userLogin(null, null, null, oauthId, oauthType, deviceId);
        /* 返回用户信息和token*/
        Map<String, Object> resultData = new HashMap<>();
        mobileHidden(userLoginVo);
        resultData.put("userLogin", userLoginVo);
        return buildSuccJson(resultData);
    }

    /* 发送验证码接口*/
    @RequestMapping("/sendVerifyCode")
    @ResponseBody
    public Object sendVerifyCode(@RequestParam String mobile, @RequestParam(required = false) String type) {
        if (!mobileFormat(mobile)) {
            return buildErrJson(ResultConstant.MOBILE_FORMAT_ERR_MSG);
        }
        if (StringUtils.isNotBlank(type) && type.equals(CommonConstant.SMS_TYPE_LOGIN)) {
            /* 注册用户校验*/
            /*if (null != loginService.getUserLoginVo(mobile, null, null)) {
                return buildErrJson(ResultConstant.VALIDATE_USER_IS_EXIST_MSG);
            }*/
        }
        if (!smsService.sendVerifyCode(mobile, CommonUtil.getSendVerifyCodePrefix(type))) {
            return buildErrJson(ResultConstant.SEND_VERIFY_CODE_ERR_MSG);
        }
        Map<String, String> data = new HashMap<>();
        data.put("showText", ResultConstant.SEND_VERIFY_CODE_SUCCESS_MSG);
        return buildSuccJson(data);
    }

    @RequestMapping("/encryptSetPassword")
    @ResponseBody
    public Object encryptSetPassword(@RequestAttribute String token, @RequestAttribute String password) {
        return setPassword(token, password);
    }

    /* 设置密码*/
    @RequestMapping("/setPassword")
    @ResponseBody
    public Object setPassword(@RequestParam String token, @RequestParam String password) {
        if (!passwordFormat(password)) {
            return buildErrJson(ResultConstant.PASSWORD_FORMAT_ERR_MSG);
        }
        /* token校验*/
        UserToken userToken = loginService.checkToken(token);
        if (userToken == null) {
            return buildJson(ResultConstant.VALIDATE_TOKEN_FAILURE_CODE, ResultConstant.VALIDATE_TOKEN_FAILURE_MSG);
        }
        loginService.setPassword(userToken.getUserId(), password);
        Map<String, String> data = new HashMap<>();
        data.put("showText", ResultConstant.SET_PASSWORD_SUCCESS_MSG);
        return buildSuccJson(data);
    }

    @RequestMapping("/encryptModifyPassword")
    @ResponseBody
    public Object encryptModifyPassword(@RequestAttribute String token, @RequestAttribute String password,
                                        @RequestAttribute String newPassword) {
        return modifyPassword(token, password, newPassword);
    }

    /* 修改密码*/
    @RequestMapping("/modifyPassword")
    @ResponseBody
    public Object modifyPassword(@RequestParam String token, @RequestParam String password, @RequestParam String
            newPassword) {
        /* 密码格式校验*/
        if (!(passwordFormat(password) && passwordFormat(newPassword))) {
            return buildErrJson(ResultConstant.PASSWORD_FORMAT_ERR_MSG);
        }
        /* token校验*/
        UserToken userToken = loginService.checkToken(token);
        if (userToken == null) {
            return buildJson(ResultConstant.VALIDATE_TOKEN_FAILURE_CODE, ResultConstant.VALIDATE_TOKEN_FAILURE_MSG);
        }
        /* 旧密码是否正确*/
        if (!loginService.passwordValidateByUserId(userToken.getUserId(), password)) {
            return buildErrJson(ResultConstant.OLD_PASSWORD_FAILURE_MSG);
        }
        /* 设置密码*/
        loginService.setPassword(userToken.getUserId(), newPassword);
        Map<String, String> data = new HashMap<>();
        data.put("showText", ResultConstant.MODIFY_PROTOTYPE_SUCCESS_MSG);
        return buildSuccJson(data);
    }

    @RequestMapping("/encryptForgetPassword")
    @ResponseBody
    public Object encryptForgetPassword(@RequestAttribute String mobile, @RequestAttribute String verifyCode,
                                        @RequestAttribute String newPassword) {
        return forgetPassword(mobile, verifyCode, newPassword);
    }

    /* 找回密码 重新设置密码*/
    @RequestMapping("/forgetPassword")
    @ResponseBody
    public Object forgetPassword(@RequestParam String mobile, @RequestParam String verifyCode, @RequestParam String
            newPassword) {
        /* 参数校验*/
        if (!mobileFormat(mobile)) {
            return buildErrJson(ResultConstant.MOBILE_FORMAT_ERR_MSG);
        }
        if (StringUtils.isBlank(verifyCode)) {
            return buildErrJson(ResultConstant.VERIFY_CODE_FORMAT_ERR_MSG);
        }
        Long userId = loginService.getUserId(mobile);
        if (userId == null) {
            return buildErrJson(ResultConstant.PARAMS_MOBILE_IS_NOT_REGISTER);
        }
        /* 验证码是否正确校验*/
        String verifyCodeValidateMsg = loginService.verifyCodeValidate(mobile, verifyCode);
        if (StringUtils.isNotBlank(verifyCodeValidateMsg)) {
            /* 验证失败*/
            return buildErrJson(verifyCodeValidateMsg);
        }
        /* 设置密码*/
        loginService.setPassword(userId, newPassword);
        Map<String, String> data = new HashMap<>();
        data.put("showText", ResultConstant.SET_PASSWORD_SUCCESS_MSG);
        return buildSuccJson(data);
    }

    /* 头像修改*/
    @RequestMapping("/modifyHeadImgOrNickName")
    @ResponseBody
    public Object setPassword(@RequestParam String token, @RequestParam(required = false) String headImgUrl,
                              @RequestParam(required = false) String nickName) {
        if (StringUtils.isBlank(headImgUrl) && StringUtils.isBlank(nickName)) {
            return buildErrJson(ResultConstant.PARAMS_ERR_MSG);
        }
        if (StringUtils.isNotBlank(nickName) && !nickNameFormat(nickName)) {
            return buildErrJson(ResultConstant.NICKNAME_FORMAT_ERR_MSG);
        }
        /* token校验*/
        UserToken userToken = loginService.checkToken(token);
        if (userToken == null) {
            return buildJson(ResultConstant.VALIDATE_TOKEN_FAILURE_CODE, ResultConstant.VALIDATE_TOKEN_FAILURE_MSG);
        }
        UserLoginVo userLoginVo = loginService.modifyHeadImgOrNickName(token, userToken.getUserId(), headImgUrl,
                nickName);
        /* 返回用户信息和token*/
        Map<String, Object> resultData = new HashMap<>();
        mobileHidden(userLoginVo);
        resultData.put("userLogin", userLoginVo);
        return buildSuccJson(resultData);
    }

    /* 用户设备信息更新*/
    @RequestMapping("/updateDeviceInfo")
    @ResponseBody
    public Object updateDeviceInfo(@RequestParam String deviceId, @RequestParam(required = false) String token,
                                   @RequestParam(required = false) String deviceImei, @RequestParam String deviceName,
                                   @RequestParam Integer clientType, @RequestParam String clientId, @RequestParam
                                           (required = false) String channel, @RequestParam(required = false) Integer
                                           pushType) {
        try {
            Long userId = null;
            if (StringUtils.isNotBlank(token)) {
                if (null != loginService.checkToken(token)) {
                    userId = loginService.checkToken(token).getUserId();
                }
            }
            Timestamp currentTime = DateUtil.getCurrentTimestamp();
            String deviceIdConvert = loginService.generateDeviceId(deviceId);
            /* 默认存储ssq和dlt*/
            StringBuffer pushGameEns = new StringBuffer(IniCache.getIniValue(IniConstant.DEFAULT_PUSH_GAMES, "ssq," +
                    "dlt"));
            UserDeviceInfo userDeviceInfo = new UserDeviceInfo(userId, deviceIdConvert, deviceImei, deviceName,
                    clientType, clientId, pushGameEns.toString(), channel, pushType, currentTime, currentTime);
            loginService.UPDATE_DEVICE_QUEUE.offer(userDeviceInfo);
        } catch (Exception e) {
            log.warn("[loginservice] : add task fail. deviceId" + deviceId, e);
        }
        return buildSuccJson();
    }

    @RequestMapping("/getUserCenterBanner")
    @ResponseBody
    public Object getUserCenterBanner(String token, @RequestAttribute(required = false) Integer clientType,
                                      @RequestAttribute Integer versionCode) {
        Map<String, Object> result = new HashMap<>();

        UserToken userToken = loginService.checkToken(token);
        Long userId = null;
        if (userToken != null && userToken.getUserId() != null) {
            userId = userToken.getUserId();
        }
        Map<String, Object> userCenterShowInfo = vipMemberService.getUserCenterShowInfo(userId, versionCode, clientType);
        result.putAll(userCenterShowInfo);

        List<BannerVo> banners = BannerCache.getBannerVosV2(BannerCache.POSITION_TYPE_USER, versionCode, clientType);
        if (banners != null && (null == clientType || clientType.equals(CommonConstant.CLIENT_TYPE_IOS))) {
            int count = 0;
            for (int i = 0; i < banners.size(); i++) {
                if (banners.get(i).getBannerId() == 60 || banners.get(i).getBannerId() == 61 || banners.get(i)
                        .getBannerId() == 62 || banners.get(i).getBannerId() == 63 || banners.get(i).getBannerId() ==
                        81 || banners.get(i).getBannerId() == 82) {
                    banners.remove(i - count);
                    count++;
                }
            }
        }
        result.put("banners", banners);
        result.put("showType", 1);//0客户端展示余额 1:h5展示
        result.put("h5Url", "https://predictapi.mojieai.com/web/withdraw/");//0客户端展示余额 1:h5展示
        return buildSuccJson(result);
    }

    @RequestMapping("/userWithdrawBalanceCenter")
    @ResponseBody
    public Object userWithdrawBalanceCenter(@RequestParam String token) {
        UserToken userToken = loginService.checkToken(token);
        if (userToken == null) {
            return buildErrJson("用户不存在");
        }
        Map res = userAccountService.getUserWithdrawBalanceCenter(userToken.getUserId());
        return buildSuccJson(res);
    }

    @RequestMapping("/balanceWithOutSign")
    @ResponseBody
    public Object balanceWithOutSign(@RequestParam String token) {
        return userWithdrawBalanceCenter(token);
    }

    @RequestMapping("/saveUserLotteryType")
    @ResponseBody
    public Object saveUserLotteryType(@RequestParam String token, @RequestParam Integer type) {

        UserToken userToken = loginService.checkToken(token);
        if (userToken == null) {
            return buildErrJson("用户不存在");
        }
        return buildSuccJson(userInfoService.saveUserLotteryType(userToken.getUserId(), type));
    }

    @RequestMapping("/get_personal_data")
    @ResponseBody
    public Object getPersonalData(@RequestParam String token) {
        UserToken userToken = loginService.checkToken(token);
        if (userToken == null) {
            return buildErrJson("用户不存在");
        }
        return buildSuccJson(userInfoService.getPersonalData(userToken.getUserId()));
    }

    @RequestMapping("/encrypt_real_name_authentication")
    @ResponseBody
    public Object realNameAuthentication(@RequestAttribute String token, @RequestAttribute String userName,
                                         @RequestAttribute String idCard) {
        UserToken userToken = loginService.checkToken(token);
        if (userToken == null || userToken.getUserId() == null) {
            return buildErrJson("请登录");
        }

        return buildSuccJson(userInfoService.authenticateRealName(userToken.getUserId(), userName, idCard));
    }

    @RequestMapping("/bind_bank_card_detail")
    @ResponseBody
    public Object bindBankCardDetail(@RequestParam String token) {

        UserToken userToken = loginService.checkToken(token);
        if (userToken == null || userToken.getUserId() == null) {
            return buildErrJson("用户不存在");
        }

        return buildSuccJson(bindBankCardService.getBindBankCardDetail(userToken.getUserId()));
    }

    @RequestMapping("/encrypt_user_bank_card")
    @ResponseBody
    public Object userBindBankCard(@RequestAttribute String token, @RequestAttribute String mobile, @RequestAttribute
            String bankNo, @RequestAttribute String verifyCode, @RequestAttribute(required = false) String idCard,
                                   @RequestAttribute(required = false) String userName) {
        UserToken userToken = loginService.checkToken(token);
        if (userToken == null || userToken.getUserId() == null) {
            return buildErrJson("用户不存在");
        }
        String prefix = CommonUtil.getSendVerifyCodePrefix(CommonConstant.SMS_TYPE_BIND_BANK);
        String redisVerifyCode = redisService.kryoGet(prefix + mobile, String.class);
        if (StringUtils.isBlank(redisVerifyCode)) {
            return buildErrJson("验证码已失效，请重新获取");
        }
        if (!redisVerifyCode.equals(verifyCode)) {
            return buildErrJson("验证码填写错误");
        }
        return buildSuccJson(bindBankCardService.userBindBankCard(userToken.getUserId(), userName, idCard, mobile,
                bankNo));
    }

    @RequestMapping("/encrypt_simple_bind_card")
    @ResponseBody
    public Object simpleBindBankCard(@RequestAttribute String token, @RequestAttribute String bankNo, @RequestAttribute
            String userName, @RequestAttribute(required = false) String mobile) {
        UserToken userToken = loginService.checkToken(token);
        if (userToken == null || userToken.getUserId() == null) {
            return buildErrJson("用户不存在");
        }
        ResultVo resultVo = userBankCardService.addUserBankCardWithOutAuth(userToken.getUserId(), userName, bankNo,
                mobile);
        if (resultVo.getCode().equals(ResultConstant.ERROR)) {
            return buildErrJson(resultVo.getMsg());
        }
        return buildSuccJson(resultVo);
    }

    @RequestMapping("/unbind_bank_card")
    @ResponseBody
    public Object unbindBankCard(@RequestParam String token, @RequestParam Integer bankId) {
        UserToken userToken = loginService.checkToken(token);
        if (userToken == null || userToken.getUserId() == null) {
            return buildErrJson("用户不存在");
        }
        return buildSuccJson(userBankCardService.unbindUserBankCard(userToken.getUserId(), bankId));
    }

    @RequestMapping("/get_user_bank_list")
    @ResponseBody
    public Object getUserBankList(@RequestParam String token) {
        UserToken userToken = loginService.checkToken(token);
        if (userToken == null || userToken.getUserId() == null) {
            return buildErrJson("用户不存在");
        }
        return buildSuccJson(bindBankCardService.getUserBankList(userToken.getUserId()));
    }

    @RequestMapping("/user_bank_card_detail")
    @ResponseBody
    public Object getUserBankCardDetail(@RequestParam String token, @RequestParam Integer bankId) {
        UserToken userToken = loginService.checkToken(token);
        if (userToken == null || userToken.getUserId() == null) {
            return buildErrJson("用户不存在");
        }
        return buildSuccJson(bindBankCardService.getUserBankCardDetail(userToken.getUserId(), bankId));
    }

    @RequestMapping("/encrypt_set_withdraw_password")
    @ResponseBody
    public Object setWithdrawPassword(@RequestAttribute String token, @RequestAttribute String password) {
        UserToken userToken = loginService.checkToken(token);
        if (userToken == null || userToken.getUserId() == null) {
            return buildErrJson("用户不存在");
        }
        return buildSuccJson(userInfoService.saveUserWithdrawPwd(userToken.getUserId(), password));
    }

    @RequestMapping("/encrypt_edit_withdraw_password")
    @ResponseBody
    public Object editWithdrawPassword(@RequestAttribute String token, @RequestAttribute String oldPassword,
                                       @RequestAttribute String newPassword) {
        UserToken userToken = loginService.checkToken(token);
        if (userToken == null || userToken.getUserId() == null) {
            return buildErrJson("用户不存在");
        }
        return buildSuccJson(userInfoService.updateUserWithdrawPwd(userToken.getUserId(), oldPassword, newPassword));
    }

    @RequestMapping("/encrypt_create_withdraw_order")
    @ResponseBody
    public Object createWithdrawOrder(@RequestAttribute String token, @RequestAttribute Integer bankId,
                                      @RequestAttribute String withdrawAmount, @RequestAttribute String password,
                                      @RequestAttribute Integer clientType, @RequestAttribute String visitorIp) {
        UserToken userToken = loginService.checkToken(token);
        if (userToken == null || userToken.getUserId() == null) {
            return buildErrJson("用户不存在");
        }
        if (!CommonUtil.isNumericeFloat(withdrawAmount)) {
            return buildErrJson("请输入正确的提现金额");
        }
        Long withdrawAmountL = CommonUtil.multiply(withdrawAmount, "100").longValue();
        String minWithdrawMoney = ActivityIniCache.getActivityIniValue(ActivityIniConstant.MIN_WITHDRAW_MONEY, "100");
        Long minWithdrawAmount = CommonUtil.multiply(minWithdrawMoney, "100").longValue();
        Long userMaxAmount = userWithdrawFlowService.getUserCurrentMonthMaxWithDrawMoney(userToken.getUserId());
        if (userMaxAmount < minWithdrawAmount) {
            String msg = "本月还可提现金额小于" + minWithdrawMoney + "元，下个月再来吧";
            ResultVo resultVo = new ResultVo(ResultConstant.ERROR, msg);
            return buildSuccJson(resultVo);
        }
        if (withdrawAmountL < minWithdrawAmount) {
            return buildErrJson("最新提现金额为" + minWithdrawMoney + "元");
        }
        if (userMaxAmount < withdrawAmountL) {
            return buildErrJson("本月还可提现额度为" + CommonUtil.convertFen2Yuan(userMaxAmount) + "元");
        }

        BigDecimal withdrawAmountBig = CommonUtil.multiply(withdrawAmount, "100");
        return buildSuccJson(userWithdrawFlowService.createWithdrawOrder(userToken.getUserId(), bankId,
                withdrawAmountBig.longValue(), clientType, visitorIp, password));
    }

    /* 手机号码校验*/
    public Boolean mobileFormat(String mobile) {
        //String REGEX_MOBILE = "^((13[0-9])|(15[^4])|(18[0,2,3,5-9])|(17[0-8])|(147))\\\\d{8}$";
        return Boolean.TRUE;
    }

    /* 密码格式校验*/
    public Boolean passwordFormat(String password) {
        String REGEX_PASSWORD = "^[a-zA-Z0-9]{6,18}$";
        return Pattern.matches(REGEX_PASSWORD, password);
    }

    /* 昵称格式校验*/
    public Boolean nickNameFormat(String nickName) {
        String REGEX_NICK_NAME = "^[\\u4e00-\\u9fa5_a-zA-Z0-9_]{1,10}$";
        return Pattern.matches(REGEX_NICK_NAME, nickName);
    }

    private void mobileHidden(UserLoginVo userLoginVo) {
        if (null != userLoginVo.getMobile()) {
            userLoginVo.setMobile(new StringBuffer(userLoginVo.getMobile()).replace(3, 7, "****").toString());
        }
    }
}
