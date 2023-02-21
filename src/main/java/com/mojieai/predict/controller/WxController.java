package com.mojieai.predict.controller;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.cache.PayChannelInfoCache;
import com.mojieai.predict.cache.PayClientChannelCache;
import com.mojieai.predict.cache.VipPriceCache;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.constant.ResultConstant;
import com.mojieai.predict.constant.VipMemberConstant;
import com.mojieai.predict.dao.ActivityInfoDao;
import com.mojieai.predict.dao.ActivityUserInfoDao;
import com.mojieai.predict.dao.ExchangeMallDao;
import com.mojieai.predict.entity.dto.HttpParamDto;
import com.mojieai.predict.entity.po.*;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.*;
import com.mojieai.predict.util.CheckoutUtil;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.HttpServiceUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.enterprise.inject.Model;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by tangxuelong on 2017/11/3.
 */
@RequestMapping("/wx")
@Controller
public class WxController extends BaseController {
    @Autowired
    private LoginService loginService;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private UserWisdomCoinFlowService userWisdomCoinFlowService;
    @Autowired
    private VipPriceService vipPriceService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private ActivityInfoDao activityInfoDao;
    @Autowired
    private VipMemberService vipMemberService;
    @Autowired
    private ActivityUserInfoDao activityUserInfoDao;
    @Autowired
    private ExchangeMallDao exchangeMallDao;

    /**
     * 微信消息接收和token验证
     *
     * @param model
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping("/token")
    public void hello(Model model, HttpServletRequest request, HttpServletResponse response) throws IOException {
        boolean isGet = request.getMethod().toLowerCase().equals("get");
        PrintWriter print;
        if (isGet) {
            // 微信加密签名
            String signature = request.getParameter("signature");
            // 时间戳
            String timestamp = request.getParameter("timestamp");
            // 随机数
            String nonce = request.getParameter("nonce");
            // 随机字符串
            String echostr = request.getParameter("echostr");
            // 通过检验signature对请求进行校验，若校验成功则原样返回echostr，表示接入成功，否则接入失败
            if (signature != null && CheckoutUtil.checkSignature(signature, timestamp, nonce)) {
                try {
                    print = response.getWriter();
                    print.write(echostr);
                    print.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*第二步：通过code换取网页授权access_token*/
    @RequestMapping("/getUserInfoByCode")
    @ResponseBody
    public Object getUserInfoByCode(@RequestParam String code) {
        StringBuffer url = new StringBuffer();
        url.append("https://api.weixin.qq.com/sns/oauth2/access_token").append("?appid=").append(CommonConstant
                .WX_APP_ID).append("&secret=").append(CommonConstant.WX_APP_SECRET).append("&code=").append(code)
                .append("&grant_type=authorization_code");
        String result = HttpServiceUtils.sendHttpsPostRequest(url.toString(), "", HttpParamDto.DEFAULT_CHARSET);
        Map<String, String> resultMap = (Map<String, String>) JSONObject.parse(result);
        /* 第四步：拉取用户信息(需scope为 snsapi_userinfo)*/
        StringBuffer userUrl = new StringBuffer();
        userUrl.append("https://api.weixin.qq.com/sns/userinfo");
        String access_token = resultMap.get("access_token");
        userUrl.append("?access_token=").append(access_token);
        String openid = resultMap.get("openid");
        userUrl.append("&openid=").append(openid);
        userUrl.append("&lang=").append("zh_CN");
        String userInfoResult = HttpServiceUtils.sendHttpsPostRequest(userUrl.toString(), "", HttpParamDto
                .DEFAULT_CHARSET);
        Map<String, String> userInfoResultMap = (Map<String, String>) JSONObject.parse(userInfoResult);
        return buildSuccJson(userInfoResultMap);
    }

    /* 分享添加次数*/
    @RequestMapping("/share")
    @ResponseBody
    public Object share(@RequestParam String gameEn, @RequestParam String token, @RequestParam(required = false) String
            openId) {
        if (null != openId && StringUtils.isNotBlank(token)) {
            Game game = GameCache.getGame(gameEn);
            UserToken userToken = loginService.checkToken(token);
            if (null == userToken) {
                return buildJson(ResultConstant.VALIDATE_TOKEN_FAILURE_CODE, ResultConstant.VALIDATE_TOKEN_FAILURE_MSG);
            }
            activityService.share(game.getGameId(), userToken.getUserId(), openId);
        }
        return buildSuccJson();
    }

    /* 分享添加次数*/
    @RequestMapping("/shareUsersToday")
    @ResponseBody
    public Object shareUsersToday(@RequestParam String gameEn) {
        Game game = GameCache.getGame(gameEn);
        if (game == null) {
            return buildErrJson("game is not exist");
        }
        Map<String, Integer> resultMap = activityService.shareUsersToday(game.getGameId());
        return buildSuccJson(sortByValue(resultMap));
    }

    /* 查询用户次数*/
    @RequestMapping("/shareResult")
    @ResponseBody
    public Object shareResult(@RequestParam String gameEn, @RequestParam String token) {
        Game game = GameCache.getGame(gameEn);
        if (game == null) {
            return buildErrJson("game is not exist");
        }
        UserToken userToken = loginService.checkToken(token);
        if (null == userToken) {
            return buildJson(ResultConstant.VALIDATE_TOKEN_FAILURE_CODE, ResultConstant.VALIDATE_TOKEN_FAILURE_MSG);
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("shareResult", activityService.shareResult(game.getGameId(), userToken.getUserId()));
        return buildSuccJson(resultMap);
    }

    /* 分享获取签名*/
    @RequestMapping("/getJsApiTicket")
    @ResponseBody
    public Object getJsApiTicket(@RequestParam String pageUrl) {
        return buildSuccJson(activityService.getJsApiTicket(pageUrl));
    }

    /* QQUserInfo*/
    @RequestMapping("/getUserInfoQQ")
    @ResponseBody
    public Object getUserInfoQQ(@RequestParam String accessToken, @RequestParam String openId, @RequestParam String
            appKey) {
        StringBuffer url = new StringBuffer();
        url.append("https://graph.qq.com/user/get_user_info?").append("access_token=").append(accessToken).append
                ("&openid=").append(openId).append("&oauth_consumer_key=").append(appKey);
        String result = HttpServiceUtils.sendRequest(url.toString());
        Map<String, String> resultMap = (Map<String, String>) JSONObject.parse(result);
        return buildSuccJson(resultMap);
    }

    @RequestMapping("/wxapi_get_wisdom_price_list")
    @ResponseBody
    public Object getWxApiWisdomPriceList(String mobile, @RequestParam String wxCode) {
        Long userId = loginService.getUserId(mobile);
        return buildSuccJson(userWisdomCoinFlowService.getWxApiWisdomPriceList(userId, wxCode));
    }

    @RequestMapping("/wxapi_get_vip_price_list")
    @ResponseBody
    public Object getWxApiVipPriceList(String mobile, @RequestParam String wxCode, Integer vipType) {
        Map<String, Object> result = new HashMap<>();
        Long userId = loginService.getUserId(mobile);
        Integer clientType = CommonConstant.CLIENT_TYPE_ANDRIOD;
        Integer versionCode = CommonConstant.VERSION_CODE_4_6_1;
        Map salePriceMap = vipPriceService.getVipSaleList(userId, clientType, versionCode, vipType);
        if (salePriceMap != null) {
            result.putAll(salePriceMap);
        }
        String payClientChannelKey = String.valueOf(CommonConstant.CLIENT_TYPE_ANDRIOD) + CommonConstant
                .COMMON_COLON_STR + String.valueOf(CommonConstant.WX_PAY_CHANNEL_WX_JSAPI);
        PayClientChannel payClientChannel = PayClientChannelCache.getClientChannel(payClientChannelKey);
        String openId = CommonUtil.getWxOpenid(wxCode, payClientChannel);
        if (StringUtils.isNotBlank(openId)) {
            redisService.kryoSetEx(RedisConstant.getWxJSAPICodeOpenIdMapKey(wxCode), 1800, openId);
        }
        return buildSuccJson(result);
    }

    @RequestMapping("/wxapi_purchase_vip")
    @ResponseBody
    public Object wxapiPurchaseVip(@RequestParam String mobile, @RequestParam String wxCode, @RequestParam Integer
            vipPriceId, Integer activityStatus, Integer vipType) {
        Long userId = loginService.getUserId(mobile);
        if (userId == null) {
            return buildErrJson("请登陆后购买");
        }
        //1.验证5秒是否重复购买
        String key = RedisConstant.getUserPurchaseLazySecond(userId);
        if (redisService.isKeyByteExist(key)) {
            return buildErrJson("参数异常");
        }

        activityStatus = activityStatus == null ? 0 : activityStatus;

        //2.验证channelid
        PayChannelInfo payChannelInfo = PayChannelInfoCache.getChannelInfo(CommonConstant.WX_PAY_CHANNEL_WX_JSAPI);
        if (payChannelInfo == null) {
            return buildErrJson("参数异常");
        }
        //活动价格
        Integer activityId = 201803001;
        if (vipType != null && vipType.equals(VipMemberConstant.VIP_MEMBER_TYPE_SPORTS)) {
            activityId = 201806004;
        }
        VipPrice vipPrice = VipPriceCache.getVipPriceById(vipPriceId);
        Long price = vipPrice.getPrice();
        if (activityStatus == 1) {
            ActivityUserInfo activityUserInfo = activityUserInfoDao.getUserTotalTimes(activityId, userId);
            if ((null == activityUserInfo || activityUserInfo.getTotalTimes() != 2) && activityService
                    .checkActivityIsEnabled(activityId)) {
                ActivityInfo activityInfo = activityInfoDao.getActivityInfo(activityId);
                if (activityInfo != null && StringUtils.isNotBlank(activityInfo.getRemark())) {
                    Map<String, Object> remarkMap = JSONObject.parseObject(activityInfo.getRemark(), HashMap.class);
                    Long discountAmount = Long.valueOf(remarkMap.get("discountAmount").toString());
                    price = price - discountAmount;
                }
            }
        }

        //3.下单
        Map result = vipMemberService.wxjsapiPurchaseVip(userId, vipPriceId, VipMemberConstant
                .VIP_SOURCE_TYPE_WX_JSAPI, activityStatus, vipType, price, vipPrice.getVipDate(), wxCode);

        redisService.kryoSetEx(key, VipMemberConstant.PURCHASE_VIP_LAZY_SECOUND, 1);
        return buildSuccJson(result);
    }

    @RequestMapping("/wxapi_purchase_wisdom")
    @ResponseBody
    public Object wxapiPurchaseWisdom(@RequestParam String mobile, @RequestParam Integer itemId, @RequestParam String
            wxCode) {
        Long userId = loginService.getUserId(mobile);
        if (null == userId) {
            return buildErrJson(ResultConstant.PARAMS_ERR_MSG);
        }
        //1.校验价格
        ExchangeMall exchangeMall = exchangeMallDao.getExchangeMall(itemId);
        if (exchangeMall == null) {
            return buildErrJson("价格异常");
        }

        Map<String, Object> result = userWisdomCoinFlowService.wxApiPurchaseWisdomCoin(userId, exchangeMall
                .getItemPrice(), exchangeMall.getItemCount(), itemId, wxCode);
        result.remove("msg");
        result.remove("iosMallGoodId");
        result.remove("flowId");
        return buildSuccJson(result);
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        Map<K, V> result = new LinkedHashMap<>();
        Stream<Map.Entry<K, V>> st = map.entrySet().stream();

        st.sorted(Comparator.comparing(Map.Entry::getValue)).forEach(e -> result.put(e.getKey(), e.getValue()));

        return result;
    }

}
