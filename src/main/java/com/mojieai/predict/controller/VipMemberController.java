package com.mojieai.predict.controller;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.PayChannelInfoCache;
import com.mojieai.predict.cache.VipPriceCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.ActivityInfoDao;
import com.mojieai.predict.dao.ActivityUserInfoDao;
import com.mojieai.predict.entity.po.ActivityInfo;
import com.mojieai.predict.entity.po.ActivityUserInfo;
import com.mojieai.predict.entity.po.PayChannelInfo;
import com.mojieai.predict.entity.po.UserToken;
import com.mojieai.predict.entity.vo.UserLoginVo;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.ActivityService;
import com.mojieai.predict.service.LoginService;
import com.mojieai.predict.service.VipMemberService;
import com.mojieai.predict.service.VipPriceService;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.PayUtil;
import com.mojieai.predict.util.TrendUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@RequestMapping("/vipMember")
@Controller
public class VipMemberController extends BaseController {

    @Autowired
    private LoginService loginService;
    @Autowired
    private VipMemberService vipMemberService;
    @Autowired
    private VipPriceService vipPriceService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private ActivityUserInfoDao activityUserInfoDao;
    @Autowired
    private ActivityInfoDao activityInfoDao;

    @RequestMapping("/vipSaleList")
    @ResponseBody
    public Object getVipSaleList(String token, @RequestAttribute Integer clientType, @RequestAttribute Integer
            versionCode, @RequestParam(required = false, defaultValue = "0") Integer vipType) {
        Map result = new HashMap<>();
        Long userId = null;
        UserToken userToken = exchangeUserId(token);
        if (userToken == null || userToken.getUserId() == null) {
            if (versionCode < CommonConstant.VERSION_CODE_3_6) {
                return buildErrJson("请重新登陆");
            }
        }
        //1.获取用户信息
        String userName = "未登录";
        String imgUrl = "";
        if (userToken != null && userToken.getUserId() != null) {
            userId = userToken.getUserId();
            UserLoginVo userLoginVo = loginService.getUserLoginVo(userId);
            userName = userLoginVo.getNickName();
            imgUrl = userLoginVo.getHeadImgUrl();
        }

        //2.获取vip价格信息
        Map salePriceMap = vipPriceService.getVipSaleList(userId, clientType, versionCode, vipType);
        if (salePriceMap != null) {
            result.putAll(salePriceMap);
        }

        result.putAll(PayUtil.getWeChatSubscription(clientType, versionCode));
        result.put("userName", userName);
        result.put("headImgUrl", imgUrl);
        return buildSuccJson(result);
    }

    @RequestMapping("/vipUserCenter")
    @ResponseBody
    public Object vipUserCenter(String token, @RequestAttribute Integer versionCode, @RequestAttribute Integer
            clientType) {
        UserToken userToken = exchangeUserId(token);
        Long userId = null;
        if (userToken != null) {
            userId = userToken.getUserId();
        }
        Map<String, Object> result = vipMemberService.getUserCenterShowInfo(userId, versionCode, clientType);
        return buildSuccJson(result);
    }

    @RequestMapping("/purchaseVip")
    @ResponseBody
    public Object createVip(@RequestParam String token, @RequestParam Integer payChannelId, @RequestParam Integer
            numbers, @RequestParam String money, @RequestParam Integer vipPriceId, @RequestParam(required = false,
            defaultValue = "0") Integer vipType, Integer bankId, @RequestAttribute Integer clientType,
                            @RequestAttribute String visitorIp, Integer sourceType, Integer activityStatus,
                            @RequestAttribute Integer versionCode) {
        UserToken userToken = exchangeUserId(token);
        if (userToken == null || userToken.getUserId() == null) {
            return buildErrJson("请登陆后购买");
        }
        //1.验证5秒是否重复购买
        String key = RedisConstant.getUserPurchaseLazySecond(userToken.getUserId());
        if (redisService.isKeyByteExist(key)) {
            return buildErrJson("参数异常");
        }

        //ios4.1的bug客户端传错了activityStatus,为兼容4.1做特殊处理
        if (clientType.equals(CommonConstant.CLIENT_TYPE_IOS) && versionCode.equals(CommonConstant.VERSION_CODE_4_1)) {
            if (activityStatus != null && activityStatus != 0) {
                activityStatus = 1;
            }
        }

        activityStatus = activityStatus == null ? 0 : activityStatus;
        if (clientType.equals(CommonConstant.CLIENT_TYPE_IOS) && CommonUtil.getIosReview(versionCode) == 0) {
            activityStatus = 0;
        }
        //活动价格
        Integer activityId = 201803001;
        if (vipType != null && vipType.equals(VipMemberConstant.VIP_MEMBER_TYPE_SPORTS)) {
            activityId = 201806004;
        }
        if (activityStatus == 1) {
            ActivityUserInfo activityUserInfo = activityUserInfoDao.getUserTotalTimes(activityId, userToken.getUserId
                    ());
            if ((null == activityUserInfo || activityUserInfo.getTotalTimes() != 2) && activityService
                    .checkActivityIsEnabled(activityId)) {
                sourceType = 13;//回调
            }
        }

        //1.验证money+number天数
        BigDecimal priceFen = CommonUtil.convertYuan2Fen(money);
        boolean ifVery = VipPriceCache.verifyDateAndMoney(numbers, priceFen.longValue(), vipPriceId, clientType);
        if (!ifVery) {
            ActivityInfo activityInfo = activityInfoDao.getActivityInfo(activityId);
            Long activityPrice = VipPriceCache.getVipPriceById(vipPriceId).getPrice();
            if (activityInfo != null && StringUtils.isNotBlank(activityInfo.getRemark())) {
                Map<String, Object> remarkMap = JSONObject.parseObject(activityInfo.getRemark(), HashMap.class);
                Long discountAmount = Long.valueOf(remarkMap.get("discountAmount").toString());
                activityPrice = activityPrice - discountAmount;
            }
            if (activityStatus != 1 && !priceFen.toString().equals(activityPrice + "")) {
                return buildErrJson("参数异常");
            }
        }
        //2.验证channelid
        PayChannelInfo payChannelInfo = PayChannelInfoCache.getChannelInfo(payChannelId);
        if (payChannelInfo == null) {
            return buildErrJson("参数异常");
        }

        //3.下单
        Integer accountType = CommonConstant.ACCOUNT_TYPE_CASH;
        Map result = new HashMap();
        if (payChannelInfo.getChannelId().equals(CommonConstant.WISDOM_COIN_CHANNEL_ID)) {
            result = vipMemberService.wisdomCoinPurchaseVip(userToken.getUserId(), payChannelId, priceFen.longValue()
                    , numbers, sourceType, activityStatus, vipType);
            accountType = CommonConstant.ACCOUNT_TYPE_WISDOM_COIN;
            if (result != null && result.containsKey("flag") && Integer.valueOf(result.get("flag").toString()) ==
                    ResultConstant.PAY_FAILED_CODE) {
                buildErrJson(result.get("msg").toString());
            }
        } else {
            result = vipMemberService.cashPurchaseVip(userToken.getUserId(), payChannelId, money, numbers, visitorIp,
                    clientType, vipPriceId, sourceType, activityStatus, vipType, bankId, null);
        }
        result.put("accountType", accountType);
        result.put("channel", payChannelId);
        result.put("webPay", CommonUtil.getWebPayStatus(payChannelId));
        redisService.kryoSetEx(key, VipMemberConstant.PURCHASE_VIP_LAZY_SECOUND, 1);
        return buildSuccJson(result);
    }

    /**
     * @param mobile
     * @param prizeType  奖品类型
     * @param uniqueFlag 派发是否唯一 0一次性  1多次
     * @return
     */
    @RequestMapping("/prizeRecord")
    @ResponseBody
    public Object record(@RequestParam String mobile, @RequestParam Integer prizeType, Integer uniqueFlag) {
        Map result = new HashMap();
        if (uniqueFlag == null) {
            uniqueFlag = 0;
        }
        if (mobileFormat(mobile)) {
            return buildErrJson("手机号格式错误");
        }

        String key = RedisConstant.getOthrePlateFormPrizeMoblie(prizeType, uniqueFlag);
        if (uniqueFlag == 0) {
            redisService.kryoSAddSet(key, mobile);
        } else {
            redisService.kryoRPush(key, mobile);
        }
        int expireTime = TrendUtil.getExprieSecond(DateUtil.getCurrentTimestamp(), 604800);
        redisService.expire(key, expireTime);//7天过期时间

        result.put("msg", "派发成功");
        result.put("mobile", mobile);
        return buildSuccJson(result);
    }

    public Boolean mobileFormat(String mobile) {
        String REGEX_MOBILE = "^((13[0-9])|(15[^4])|(18[0,2,3,5-9])|(17[0-8])|(147))\\\\d{8}$";
        return Pattern.matches(REGEX_MOBILE, mobile);
    }

    private UserToken exchangeUserId(String token) {
        UserToken userToken = null;
        if (StringUtils.isNotBlank(token)) {
            userToken = loginService.checkToken(token);
        }
        return userToken;
    }

}
