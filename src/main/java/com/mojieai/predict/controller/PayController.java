package com.mojieai.predict.controller;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.PayChannelInfoCache;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.PayConstant;
import com.mojieai.predict.constant.ResultConstant;
import com.mojieai.predict.dao.ActivityUserInfoDao;
import com.mojieai.predict.dao.ExchangeMallDao;
import com.mojieai.predict.entity.bo.JDWithdrawCallBackParam;
import com.mojieai.predict.entity.bo.PrePayInfo;
import com.mojieai.predict.entity.bo.TradePayResult;
import com.mojieai.predict.entity.dto.AsynNotifyResponse;
import com.mojieai.predict.entity.po.*;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.service.*;
import com.mojieai.predict.service.goods.AbstractGoods;
import com.mojieai.predict.service.goods.GoodsFactory;
import com.mojieai.predict.service.purchase.AbstractPurchase;
import com.mojieai.predict.service.purchase.PurchaseFactory;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.PayUtil;
import com.mojieai.predict.util.PropertyUtils;
import com.yeepay.shade.com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by tangxuelong on 2017/11/22.
 */

@RequestMapping("/payment")
@Controller
public class PayController extends BaseController {
    @Autowired
    private PayService payService;
    @Autowired
    private LoginService loginService;
    @Autowired
    private VipMemberService vipMemberService;
    @Autowired
    private UserWisdomCoinFlowService userWisdomCoinFlowService;
    @Autowired
    private ExchangeMallDao exchangeMallDao;
    @Autowired
    private CompatibleService compatibleService;
    @Autowired
    private ActivityUserInfoDao activityUserInfoDao;
    @Autowired
    private UserBuyRecommendService userBuyRecommendService;
    @Autowired
    private UserWithdrawFlowService userWithdrawFlowService;
    @Autowired
    private InternetCelebrityRecommendService internetCelebrityRecommendService;
    @Autowired
    private ActivityService activityService;

    @RequestMapping("/wxNotify")
    @ResponseBody
    public void wxNotify(HttpServletRequest request, HttpServletResponse response) {
        try {
            String resXml = payService.wxPayCallBack(request);
            // 处理业务完毕，将业务结果通知给微信
            BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
            out.write(resXml.getBytes());
            out.flush();
            out.close();
        } catch (Exception e) {
            throw new BusinessException("wxNotify handle error");
        }
    }

    @RequestMapping("/applePayNotify")
    @ResponseBody
    public Object appleNotify(@RequestParam String flowId, @RequestParam String receipt) {
        Map<String, Object> res = payService.applePayValidate(flowId, receipt);
        return buildSuccJson(res);
    }

    @RequestMapping("/aliNotifyWithOutSign")
    @ResponseBody
    public String aliNotify(HttpServletRequest request) {
        String res = payService.aliPayCallBack(request);
        //将结果返回
        return res;

    }

    @RequestMapping("/yopNotifyWithOutSign")
    @ResponseBody
    public String yopNotifyWithOutSign(HttpServletRequest request) {
        String res = payService.yopPayCallBack(request);
        //将结果返回
        return res;
    }

    @RequestMapping("/jdNotifyWithOutSign")
    @ResponseBody
    public String jdNotifyWithOutSign(HttpServletRequest request) {
        return payService.jdPayCallBack(request);
    }

    @RequestMapping("/jdWithdrawNotifyWithOutSign")
    @ResponseBody
    public String jdWithdrawNotifyWithOutSign(JDWithdrawCallBackParam param) {
        log.info(param.toString());
        return payService.jdWithdrawCallBack(param);
    }

    @RequestMapping("/haoDianNotifyWithOutSign")
    @ResponseBody
    public String haoDianNotify(HttpServletRequest request) {
        return payService.haoDianCallBack(request);
    }

    @RequestMapping("/paymentList")
    @ResponseBody
    public Object channelList(@RequestParam String token, @RequestAttribute Integer clientType, @RequestAttribute
            Integer versionCode, String payAmount) {
        if (StringUtils.isBlank(token)) {
            return buildErrJson(ResultConstant.PARAMS_ERR_MSG);
        }
        UserToken userToken = loginService.checkToken(token);
        if (null == userToken) {
            return buildErrJson(ResultConstant.PARAMS_ERR_MSG);
        }
        Map<String, Object> res = payService.getChannelList(userToken.getUserId(), clientType, versionCode);
        res = compatibleService.exchangeDefaultPayChannel(userToken.getUserId(), clientType, versionCode, payAmount,
                res);
        return buildSuccJson(res);
    }

    @RequestMapping("/exchangeMall")
    @ResponseBody
    public Object exchangeMall(@RequestParam String token) {
        if (StringUtils.isBlank(token)) {
            return buildErrJson(ResultConstant.PARAMS_ERR_MSG);
        }
        UserToken userToken = loginService.checkToken(token);
        if (null == userToken) {
            return buildErrJson(ResultConstant.PARAMS_ERR_MSG);
        }
        return buildSuccJson(payService.getExchangeMall(userToken.getUserId()));
    }

    @RequestMapping("/exchangeItem")
    @ResponseBody
    public Object exchangeMall(@RequestParam String token, @RequestParam Integer itemId, @RequestAttribute String
            visitorIp, @RequestAttribute Integer clientType) {
        if (StringUtils.isBlank(token)) {
            return buildErrJson(ResultConstant.PARAMS_ERR_MSG);
        }
        UserToken userToken = loginService.checkToken(token);
        if (null == userToken) {
            return buildErrJson(ResultConstant.PARAMS_ERR_MSG);
        }
        return buildSuccJson(payService.exchangeItem(userToken.getUserId(), itemId, visitorIp, clientType));
    }

    @RequestMapping("/flowList")
    @ResponseBody
    public Object flowList(@RequestParam String token, @RequestParam Integer payType, @RequestParam(required = false)
            Integer page) {
        if (StringUtils.isBlank(token)) {
            return buildErrJson(ResultConstant.PARAMS_ERR_MSG);
        }
        UserToken userToken = loginService.checkToken(token);
        if (null == userToken) {
            return buildErrJson(ResultConstant.PARAMS_ERR_MSG);
        }
        return buildSuccJson(payService.flowList(userToken.getUserId(), payType, page));
    }

    @RequestMapping("/taskList")
    @ResponseBody
    public Object taskList(@RequestParam String token, @RequestAttribute Integer versionCode, @RequestAttribute
            Integer clientType) {
        if (StringUtils.isBlank(token)) {
            return buildErrJson(ResultConstant.PARAMS_ERR_MSG);
        }
        UserToken userToken = loginService.checkToken(token);
        if (null == userToken) {
            return buildErrJson(ResultConstant.PARAMS_ERR_MSG);
        }
        return buildSuccJson(payService.taskList(userToken.getUserId(), versionCode, clientType));
    }

    @RequestMapping("/purchaseWisdomCoin")
    @ResponseBody
    public Object purchaseWisdomCoin(@RequestParam String token, @RequestParam Integer itemId, String price, String
            wisdomCoin, @RequestParam Integer payChannelId, Integer bankId, @RequestAttribute Integer clientType,
                                     @RequestAttribute String visitorIp, @RequestAttribute Integer versionCode) {
        UserToken userToken = loginService.checkToken(token);
        if (null == userToken) {
            return buildErrJson(ResultConstant.PARAMS_ERR_MSG);
        }
        //1.校验价格
        ExchangeMall exchangeMall = exchangeMallDao.getExchangeMall(itemId);
        if (exchangeMall == null) {
            return buildErrJson("价格异常");
        }
        //1.1依据参数设计价格
        Long wisdomCount = null;
        Long payAmount = null;
        if (StringUtils.isNotBlank(wisdomCoin) && StringUtils.isNotBlank(price)) {
            wisdomCount = CommonUtil.convertYuan2Fen(wisdomCoin).longValue();
            payAmount = CommonUtil.convertYuan2Fen(price).longValue();
        } else {
            wisdomCount = exchangeMall.getItemCount();
            payAmount = exchangeMall.getItemPrice();
        }

        //1.2其它金额校验
        if (exchangeMall.getItemName().equals("其它金额")) {
            if (wisdomCount == null || wisdomCount >= 999999900l) {
                return buildErrJson("价格异常");
            }
            Long ratio = Long.valueOf(CommonUtil.divide(price, wisdomCoin, 0));
            if (!ratio.equals(1L)) {
                return buildErrJson("价格异常");
            }
        }

        //2.验证channelid
        PayChannelInfo payChannelInfo = PayChannelInfoCache.getChannelInfo(payChannelId);
        Long checkMoney = exchangeMall.getItemPrice();
        if (checkMoney == null) {
            checkMoney = wisdomCount;
        }
        if (payChannelInfo == null || payChannelInfo.getChannelStatus(checkMoney).equals(0)) {
            return buildErrJson("微信支付最高支持99元");
        }

        Map res = userWisdomCoinFlowService.cashPurchaseWisdomCoin(userToken.getUserId(), payChannelId, payAmount,
                wisdomCount, visitorIp, clientType, itemId, versionCode, bankId);
        res.put("channel", payChannelId);
        res.put("webPay", CommonUtil.getWebPayStatus(payChannelId));
        return buildSuccJson(res);
    }

    @RequestMapping("/wisdomPriceList")
    @ResponseBody
    public Object getWisdomPriceList(@RequestParam String token, @RequestAttribute Integer clientType,
                                     @RequestAttribute Integer versionCode) {
        UserToken userToken = loginService.checkToken(token);
        if (null == userToken) {
            return buildErrJson(ResultConstant.PARAMS_ERR_MSG);
        }
        Map res = userWisdomCoinFlowService.getWisdomPriceList(userToken.getUserId(), clientType, versionCode);
        return buildSuccJson(res);
    }


    @RequestMapping("/h5Pay")
    @ResponseBody
    public Object h5Pay(@RequestParam String memo, @RequestParam Integer payChannelId, Integer bankId, @RequestParam
            String token, @RequestAttribute Integer clientType) {
        Map result = new HashMap();
        log.info("newUserActivity1");
        UserToken userToken = loginService.checkToken(token);
        if (null == userToken) {
            return buildErrJson(ResultConstant.PARAMS_ERR_MSG);
        }
        log.info("newUserActivity2");
        // 根据memo创建不同的流水 解析memo
        // 其中activity_type 5 表示已经购买足彩VIP
        // 其中 vipType1 表示足彩
        // 其中 sourceType 为 23
        if (memo.equals("newUserActivity2")) {
            log.info("newUserActivity222");
            // 如果是新手活动购买VIP检查是否具有资格
            ActivityUserInfo activityUserInfo = activityUserInfoDao.getUserTotalTimes(201803001, userToken
                    .getUserId());
            if (null != activityUserInfo && activityUserInfo.getTotalTimes() != 5) {
                // 创建VIP流水订单
                // 支付
                Map resultTemp;
                int accountType = CommonConstant.ACCOUNT_TYPE_CASH;
                if (payChannelId.equals(CommonConstant.WISDOM_COIN_CHANNEL_ID)) {
                    accountType = CommonConstant.ACCOUNT_TYPE_WISDOM_COIN;
                    resultTemp = vipMemberService.wisdomCoinPurchaseVip(userToken.getUserId(), payChannelId, 7500L
                            , 30, 23, 0, 1);
                } else {
                    resultTemp = vipMemberService.cashPurchaseVip(userToken.getUserId(), payChannelId, "75", 30,
                            "222.129.17.194", clientType, 1, 23, 0, 1, bankId, null);
                }
                resultTemp.put("accountType", accountType);
                resultTemp.put("channel", payChannelId);
                resultTemp.put("webPay", CommonUtil.getWebPayStatus(payChannelId));

                result.put("payForToken", resultTemp);//todo上线后删除
                log.info("newUserActivity6" + JSONObject.toJSONString(resultTemp));
                if (clientType.equals(CommonConstant.CLIENT_TYPE_IOS)) {
                    result.put("iosMallGoodId", "");
                    result.put("flowId", "");
                }
                return buildSuccJson(result);
            }
        } else if (memo.equals("newUserActivity")) {
            log.info("newUserActivity3");
            // 如果是新手活动购买VIP检查是否具有资格
            ActivityUserInfo activityUserInfo = activityUserInfoDao.getUserTotalTimes(201803001, userToken
                    .getUserId());
            if (null != activityUserInfo && activityUserInfo.getTotalTimes() != 2) {
                log.info("newUserActivity4");
                Integer dateNumber = DateUtil.getDiffDays(activityUserInfo.getCreateTime(), DateUtil
                        .getCurrentTimestamp());
                log.info("newUserActivity5");
                // 创建VIP流水订单
                // 支付
                Map resultTemp;
                int accountType = CommonConstant.ACCOUNT_TYPE_CASH;
                if (payChannelId.equals(CommonConstant.WISDOM_COIN_CHANNEL_ID)) {
                    accountType = CommonConstant.ACCOUNT_TYPE_WISDOM_COIN;
                    resultTemp = vipMemberService.wisdomCoinPurchaseVip(userToken.getUserId(), payChannelId, 1000L
                            , 30, 13, 0, 0);
                } else {
                    resultTemp = vipMemberService.cashPurchaseVip(userToken.getUserId(), payChannelId, "10", 30,
                            "222.129.17.194", clientType, 1, 13, 0, 0, bankId, null);
                }
                resultTemp.put("accountType", accountType);
                resultTemp.put("channel", payChannelId);
                resultTemp.put("webPay", CommonUtil.getWebPayStatus(payChannelId));

                result.put("payForToken", resultTemp);//todo上线后删除
                log.info("newUserActivity6" + JSONObject.toJSONString(resultTemp));
                if (clientType.equals(CommonConstant.CLIENT_TYPE_IOS)) {
                    result.put("iosMallGoodId", "");
                    result.put("flowId", "");
                }
                return buildSuccJson(result);
            }
        } else if (memo.indexOf("danguanCard") > -1) {
            // 单关卡
            Map<String, Object> resultTemp = activityService.buyDanguanCard(userToken.getUserId(), payChannelId, memo, bankId,
                    clientType);

            int accountType = CommonConstant.ACCOUNT_TYPE_CASH;
            if (payChannelId.equals(CommonConstant.WISDOM_COIN_CHANNEL_ID)) {
                accountType = CommonConstant.ACCOUNT_TYPE_WISDOM_COIN;
            }
            resultTemp.put("accountType", accountType);

            resultTemp.put("channel", payChannelId);
            resultTemp.put("webPay", CommonUtil.getWebPayStatus(payChannelId));

            result.put("payForToken", resultTemp);
            log.info("payForToken"+resultTemp.toString());
            if (clientType.equals(CommonConstant.CLIENT_TYPE_IOS)) {
                result.put("iosMallGoodId", "");
                result.put("flowId", "");
            }
            return buildSuccJson(result);
        } else if (memo.indexOf("danguanProgram") > -1) {
            // 单关方案
            Map<String, Object> resultTemp = activityService.buyDanguanProgram(userToken.getUserId(), payChannelId, memo, bankId,
                    clientType);

            int accountType = CommonConstant.ACCOUNT_TYPE_CASH;
            if (payChannelId.equals(CommonConstant.WISDOM_COIN_CHANNEL_ID)) {
                accountType = CommonConstant.ACCOUNT_TYPE_WISDOM_COIN;
            }
            resultTemp.put("accountType", accountType);

            resultTemp.put("channel", payChannelId);
            resultTemp.put("webPay", CommonUtil.getWebPayStatus(payChannelId));

            result.put("payForToken", resultTemp);
            log.info("payForToken"+resultTemp.toString());
            if (clientType.equals(CommonConstant.CLIENT_TYPE_IOS)) {
                result.put("iosMallGoodId", "");
                result.put("flowId", "");
            }
            return buildSuccJson(result);
        } else {
            Map<String, Object> memoMap = JSONObject.parseObject(memo, HashMap.class);
            if (memoMap != null && memoMap.containsKey("goodsType")) {
                String goodsType = memoMap.get("goodsType").toString();
                String goodsId = memoMap.get("goodsId").toString();
                int accountType = CommonConstant.ACCOUNT_TYPE_CASH;
                if (payChannelId.equals(CommonConstant.WISDOM_COIN_CHANNEL_ID)) {
                    accountType = CommonConstant.ACCOUNT_TYPE_WISDOM_COIN;
                }
                if (Objects.equals("celebrityRecommendCard", goodsType)) {
                    Map<String, Object> resultTemp = Maps.newHashMap();
                    resultTemp = internetCelebrityRecommendService.buyCelebrityRecommendCard(userToken.getUserId(),
                            Long.parseLong(goodsId), (Integer) memoMap.get("activityId"), payChannelId,
                            "222.129.17.194",
                            clientType, bankId);
                    if (null == resultTemp) {
                        resultTemp = Maps.newHashMap();
                        resultTemp.put("code", -1);
                        resultTemp.put("msg", "支付失败");
                    }
                    resultTemp.put("accountType", accountType);
                    resultTemp.put("channel", payChannelId);
                    resultTemp.put("webPay", CommonUtil.getWebPayStatus(payChannelId));
                    result.put("payForToken", resultTemp);
                    return buildSuccJson(result);
                }
//                Integer recommendPayType = (Integer) memoMap.get("recommendPayType");
//                if (!Objects.equals(InternetCelebrityRecommend.CELEBRITY_RECOMMEND_PAY_TYPE_COIN,
//                        recommendPayType)) {
//                    return buildSuccJson(result);
//                }
                Integer versionCode = Integer.valueOf(memoMap.get("versionCode").toString());
                AbstractGoods goods = GoodsFactory.getInstance().getBean(goodsType);
                PrePayInfo prePayInfo = goods.getPrePayInfo(goodsId);
                AbstractPurchase purchase = PurchaseFactory.getInstance().getPurchaseInfo(goodsType);
                TradePayResult res = purchase.purchaseGoods(userToken.getUserId(), prePayInfo, payChannelId, bankId,
                        "222.129.17.194", clientType, versionCode);
                Map<String, Object> resultTemp = null;
                if (res.getCode() == 0) {
                    resultTemp = res.getPayInfo();
                } else {
                    resultTemp = new HashMap<>();
                    resultTemp.put("code", res.getCode());
                    resultTemp.put("msg", res.getMsg());
                }

                resultTemp.put("accountType", accountType);
                resultTemp.put("channel", payChannelId);
                resultTemp.put("webPay", CommonUtil.getWebPayStatus(payChannelId));

                result.put("payForToken", resultTemp);//todo上线后删除
                log.info("payForToken"+resultTemp.toString());
                log.info("newUserActivity6" + JSONObject.toJSONString(resultTemp));
                if (clientType.equals(CommonConstant.CLIENT_TYPE_IOS)) {
                    result.put("iosMallGoodId", "");
                    result.put("flowId", "");
                }
                return buildSuccJson(result);
            }
        }
        return buildSuccJson();
    }

    /* 通用支付渠道展示接口*/
    @RequestMapping("/prePayChannel")
    @ResponseBody
    public Object prePayChannel(@RequestParam String token, @RequestParam String goodsId, @RequestParam String
            goodsType, @RequestAttribute Integer clientType, @RequestAttribute Integer versionCode) {
        UserToken userToken = loginService.checkToken(token);
        if (null == userToken) {
            return buildErrJson(ResultConstant.PARAMS_ERR_MSG);
        }

        AbstractGoods goods = GoodsFactory.getInstance().getBean(goodsType);
        if (goods == null) {
            return buildErrJson("商品不存在");
        }
        PrePayInfo prePayInfo = goods.getPrePayInfo(goodsId);

        Map<String, Object> res = payService.getConfirmPayPopInfo(userToken.getUserId(), prePayInfo, clientType,
                versionCode);
        res = compatibleService.exchangeDefaultPayChannel(userToken.getUserId(), clientType, versionCode, res.get
                ("price").toString(), res);
        return buildSuccJson(res);
    }

    /* 通用支付接口*/
    @RequestMapping("/tradePay")
    @ResponseBody
    public Object tradePay(@RequestParam String token, @RequestParam Integer payChannelId, @RequestParam String
            goodsId, @RequestParam String goodsType, Integer bankId, @RequestAttribute Integer clientType,
                           @RequestAttribute String visitorIp, @RequestAttribute Integer versionCode) {
        UserToken userToken = loginService.checkToken(token);
        if (null == userToken) {
            return buildErrJson(ResultConstant.PARAMS_ERR_MSG);
        }

        AbstractGoods goods = GoodsFactory.getInstance().getBean(goodsType);
        PrePayInfo prePayInfo = goods.getPrePayInfo(goodsId);
        AbstractPurchase purchase = PurchaseFactory.getInstance().getPurchaseInfo(goodsType);
        TradePayResult res = purchase.purchaseGoods(userToken.getUserId(), prePayInfo, payChannelId, bankId, visitorIp,
                clientType, versionCode);

        if (res.getCode().equals(PayConstant.PAY_CHECK_ERROR_CODE)) {
            return buildErrJson(res.getMsg());
        }
        return buildSuccJson(res.getPayInfo());
    }

    @RequestMapping("/pay_order_by_coupon")
    @ResponseBody
    public Object payOrderByCoupon(@RequestParam String token, @RequestParam String couponId, @RequestParam String
            orderId) {

        UserToken userToken = loginService.checkToken(token);
        if (userToken == null || userToken.getUserId() == null) {
            return buildErrJson("用户不存在");
        }

        return buildSuccJson(userBuyRecommendService.couponPurchaseRecommend(userToken.getUserId(), couponId, orderId));
    }

    @RequestMapping("/get_user_withdraw_list")
    @ResponseBody
    public Object getUserWithdrawList(@RequestParam String token, Integer page) {
        UserToken userToken = loginService.checkToken(token);
        if (userToken == null || userToken.getUserId() == null) {
            return buildErrJson("用户不存在");
        }
        return buildSuccJson(userWithdrawFlowService.getUserWithdrawOrder(userToken.getUserId(), page));
    }

    @RequestMapping("/get_user_withdraw_detail")
    @ResponseBody
    public Object getUserWithdrawDetail(@RequestParam String withdrawOrderId, @RequestParam String token) {
        UserToken userToken = loginService.checkToken(token);
        if (userToken == null || userToken.getUserId() == null) {
            return buildErrJson("用户不存在");
        }
        Map<String, Object> result = null;
        try {
            result = userWithdrawFlowService.getUserWithdrawDetail(withdrawOrderId, userToken.getUserId());
        } catch (Exception e) {
            buildErrJson(e.getMessage());
        }

        return buildSuccJson(result);
    }

    @RequestMapping("/get_apply_withdraw_index")
    @ResponseBody
    public Object getApplyWithdrawIndex(@RequestParam String token) {
        UserToken userToken = loginService.checkToken(token);
        if (userToken == null || userToken.getUserId() == null) {
            return buildErrJson("用户不存在");
        }

        return buildSuccJson(userWithdrawFlowService.getApplyWithdrawIndex(userToken.getUserId()));
    }

    @RequestMapping("/check_flow_status")
    @ResponseBody
    public Object checkFlowStatus(@RequestParam String flowId) {
        return buildSuccJson(payService.checkFlowIdStatus(flowId));
    }
}
