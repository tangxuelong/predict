package com.mojieai.predict.service.impl;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.jd.jr.pay.gate.signature.util.JdPayUtil;
import com.mojieai.predict.cache.*;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.*;
import com.mojieai.predict.entity.bo.GoldTask;
import com.mojieai.predict.entity.bo.JDWithdrawCallBackParam;
import com.mojieai.predict.entity.bo.PaginationList;
import com.mojieai.predict.entity.bo.PrePayInfo;
import com.mojieai.predict.entity.dto.AsynNotifyResponse;
import com.mojieai.predict.entity.po.*;
import com.mojieai.predict.entity.vo.UserLoginVo;
import com.mojieai.predict.enums.CommonStatusEnum;
import com.mojieai.predict.enums.PayChannelEnum;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.*;
import com.mojieai.predict.service.beanself.BeanSelfAware;
import com.mojieai.predict.thread.AllProductBillTask;
import com.mojieai.predict.thread.ThreadPool;
import com.mojieai.predict.util.*;
import com.mojieai.predict.util.JDDefray.JDDefrayCodeConst;
import com.mojieai.predict.util.JDDefray.RequestUtil;
import com.yeepay.g3.sdk.yop.encrypt.DigitalEnvelopeDTO;
import com.yeepay.g3.sdk.yop.utils.DigitalEnvelopeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * Created by tangxuelong on 2017/11/7.
 */
@Service
public class PayServiceImpl implements PayService, BeanSelfAware {

    @Autowired
    private PayService self;
    @Autowired
    private FlowIdSequenceDao flowIdSequenceDao;
    @Autowired
    private UserAccountFlowDao userAccountFlowDao;
    @Autowired
    private UserAccountDao userAccountDao;
    @Autowired
    private PayChannelInfoDao payChannelInfoDao;
    @Autowired
    private ExchangeMallDao exchangeMallDao;
    @Autowired
    private UserAccessDao userAccessDao;
    @Autowired
    private UserAccessInfoDao userAccessInfoDao;
    @Autowired
    private VipMemberService vipMemberService;
    @Autowired
    private UserSignService userSignService;
    @Autowired
    private UserSocialTaskAwardService userSocialTaskAwardService;
    @Autowired
    private UserInfoDao userInfoDao;
    @Autowired
    private LoginService loginService;
    @Autowired
    private WisdomFlowsequenceIdSequenceDao wisdomFlowIdSeqDao;
    @Autowired
    private UserWisdomCoinFlowDao userWisdomCoinFlowDao;
    @Autowired
    private AllProductBillService allProductBillService;
    @Autowired
    private UserBuyRecommendService userBuyRecommendService;
    @Autowired
    private UserSportSocialRecommendService userSportSocialRecommendService;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private ActivityInfoDao activityInfoDao;
    @Autowired
    private UserCouponService userCouponService;
    @Autowired
    private UserBankCardDao userBankCardDao;
    @Autowired
    private UserWithdrawFlowService userWithdrawFlowService;
    @Autowired
    private RedisService redisService;

    protected Logger log = LogConstant.commonLog;

    @Override
    public Map<String, Object> payCreateFlow(Long userId, String payId, Long totalAmount, Integer payType, Integer
            channel, Long payAmount, String payDesc, String clientIp, Integer clientId, String clazzMethodName,
                                             Integer operateType, Integer bankId) {
        return payCreateFlow(userId, payId, totalAmount, payType, channel, payAmount, payDesc, clientIp, clientId,
                clazzMethodName, operateType, bankId, null);
    }

    /*
     * 创建流水 金币支付，微信支付 智慧币支付
     * */
    @Override
    public Map<String, Object> payCreateFlow(Long userId, String payId, Long totalAmount, Integer payType, Integer
            channel, Long payAmount, String payDesc, String clientIp, Integer clientId, String clazzMethodName,
                                             Integer operateType, Integer bankId, String wxCode) {
        try {
            String openId = "";
            Map<String, Object> payInfo = new HashMap<>();
            if (channel != null && channel.equals(CommonConstant.WX_PAY_CHANNEL_WX_JSAPI)) {
                openId = redisService.kryoGet(RedisConstant.getWxJSAPICodeOpenIdMapKey(wxCode), String.class);
                if (StringUtils.isBlank(openId)) {
                    payInfo.put("payStatus", ResultConstant.ERROR);
                    payInfo.put("payMsg", "支付失败");
                    return payInfo;
                }
            }
            UserAccountFlow userAccountFlow = self.transCreatePayFlow(userId, payId, totalAmount, payType, channel,
                    payAmount, payDesc, clientIp, clientId, clazzMethodName, payInfo, operateType, bankId, openId);
            if (null == userAccountFlow) {
                payInfo.put("payStatus", ResultConstant.REPEAT_CODE);
                payInfo.put("payMsg", ResultConstant.REPEAT_MSG);
                return payInfo;
            }
            // 如果是金币交易
            if (!payType.equals(CommonConstant.ACCOUNT_TYPE_CASH)) {
                if (!payType.equals(CommonConstant.PAY_TYPE_COUPON)) {
                    // 检查账户余额
                    self.operateAccountDec(userId, payAmount, payInfo, userAccountFlow, payType);
                } else {
                    payInfo.put("payStatus", ResultConstant.SUCCESS);
                    payInfo.put("payMsg", "支付成功");
                }
            } else {
                Map<String, String> payForToken = PayChannelEnum.getPayChannelEnum(channel).getChannelPrePay
                        (userAccountFlow, clientId);
                String prePayId = null;
                if (channel.equals(CommonConstant.WX_PAY_CHANNEL_ID)) {
                    prePayId = payForToken.get("prepayid");
                }
                // 更新返回的预支付流水号
                userAccountFlow.setPrePayId(prePayId);
                userAccountFlow.setUpdateTime(DateUtil.getCurrentTimestamp());
                userAccountFlowDao.update(userAccountFlow);
                payInfo.put("payForToken", payForToken);
                payInfo.put("payStatus", ResultConstant.SUCCESS);
                payInfo.put("payMsg", "支付成功");
            }
            payInfo.put("flowId", userAccountFlow.getFlowId());
            return payInfo;
        } catch (Exception e) {
            log.error("支付创建流水异常", e);
            throw new BusinessException("支付创建流水异常", e);
        }
    }

    @Override
    @Transactional
    public UserAccountFlow transCreatePayFlow(Long userId, String payId, Long totalAmount, Integer payType, Integer
            channel, Long payAmount, String payDesc, String clientIp, Integer clientId, String clazzMethodName,
                                              Map<String, Object> payInfo, Integer operateType, Integer bankId,
                                              String openId) {
        //1.同一订单的重复支付校验
        UserAccountFlow userAccountFlowCheck = userAccountFlowDao.getUserFlowCheck(payId, payType, userId, Boolean
                .FALSE);
        if (null != userAccountFlowCheck && !userAccountFlowCheck.getStatus().equals(PayConstant
                .OUT_TRADE_ORDER_STATUS_NO_PAY)) {
            return null;
        }
        //2.现金切换支付渠道的时候主动查询一次
        if (userAccountFlowCheck != null && userAccountFlowCheck.getChannel() != null) {
            PayChannelEnum pce = PayChannelEnum.getPayChannelEnum(userAccountFlowCheck.getChannel());
            if (pce != null && pce.getAccountType().equals(CommonConstant.ACCOUNT_TYPE_CASH) && !userAccountFlowCheck
                    .getChannel().equals(channel)) {
                Map<String, Object> queryRes = pce.payQuery(userAccountFlowCheck);
                if (queryRes != null && !PayConstant.OUT_TRADE_ORDER_STATUS_NO_PAY.equals(Integer.valueOf(queryRes
                        .get("orderStatus").toString()))) {
                    return userAccountFlowCheck;
                }
            }
        }

        Map<String, Object> remarkMap = null;
        UserAccountFlow userAccountFlow;
        if (null == userAccountFlowCheck) {
            remarkMap = new HashMap<>();
            // 创建支付流水ID
            String flowId = generateFlowId(userId);
            userAccountFlow = new UserAccountFlow(flowId, userId, payId, totalAmount, payType, channel,
                    payAmount, payDesc, clientIp, clientId, null, operateType);
            if (StringUtils.isNotBlank(clazzMethodName)) {
                remarkMap.put("clazzMethodName", clazzMethodName);
                userAccountFlow.setRemark(JSONUtils.toJSONString(remarkMap));
            }
            userAccountFlowDao.insert(userAccountFlow);
        } else {
            if (StringUtils.isNotBlank(userAccountFlowCheck.getRemark())) {
                remarkMap = JSONObject.parseObject(userAccountFlowCheck.getRemark(), HashMap.class);
            }
            userAccountFlow = userAccountFlowCheck;
        }
        if (channel != null && channel.equals(CommonConstant.JD_PAY_CHANNEL_ID) && bankId != null) {
            UserBankCard userBankCard = userBankCardDao.getUserBankCardById(userId, bankId);
            remarkMap.put("bankCard", userBankCard.getBankCard());
        }
        if (channel != null && channel.equals(CommonConstant.WX_PAY_CHANNEL_WX_JSAPI) && StringUtils.isNotBlank(openId)) {
            remarkMap.put("openId", openId);
        }
        userAccountFlow.setRemark(JSONUtils.toJSONString(remarkMap));
        userAccountFlow.setPayType(payType);
        userAccountFlow.setChannel(channel);
        return userAccountFlow;
    }

    /*
     * 增加金币余额
     * */
    @Override
    @Transactional
    public Map<String, Object> fillAccount(Long userId, String payId, Long totalAmount, Integer payType, Integer
            channel, Long payAmount, String payDesc, String clientIp, Integer clientId) {
        // 同一订单的重复支付校验
        Map<String, Object> payInfo = new HashMap<>();
        UserAccountFlow userAccountFlowCheck = userAccountFlowDao.getUserFlowCheck(payId, payType, userId, Boolean
                .TRUE);
        if (null != userAccountFlowCheck) {
            payInfo.put("payStatus", ResultConstant.REPEAT_CODE);
            payInfo.put("payMsg", ResultConstant.REPEAT_MSG);
            return payInfo;
        }
        // 创建支付流水ID
        String flowId = generateFlowId(userId);
        UserAccountFlow userAccountFlow = new UserAccountFlow(flowId, userId, payId, totalAmount, payType, channel,
                payAmount, payDesc, clientIp, clientId, null, CommonConstant.PAY_OPERATE_TYPE_ADD);
        userAccountFlowDao.insert(userAccountFlow);
        // 如果是内部币种交易
        if (!payType.equals(CommonConstant.PAY_TYPE_CASH)) {
            self.operateAccountAdd(userId, payAmount, payInfo, userAccountFlow);
        } else {
            payInfo.put("payStatus", ResultConstant.ERROR);
            payInfo.put("payMsg", "非内部币种不能充值");
        }
        return payInfo;
    }

    /*
     * 生成流水ID，流水ID后俩位为userId
     * */
    @Override
    public String generateFlowId(Long userId) {
        String timePrefix = DateUtil.formatDate(new Date(), DateUtil.DATE_FORMAT_YYMMDDHH);
        long seq = flowIdSequenceDao.getFlowIdSequence();
        String userIdStr = String.valueOf(userId);
        String flowId = String.valueOf(timePrefix + CommonUtil.formatSequence(seq)) + userIdStr.substring(userIdStr
                .length() - 2, userIdStr.length());
        return flowId;
    }

    @Override
    public void wxNotifySuccessBusinessErrorCompensateTiming() {
        //1.查询所有现金支付，且状态是1的流水
        // TODO: 18/10/23 用otter库来处理
        for (int i = 1; i <= 100; i++) {
            Long userPrx = Long.valueOf(i);
            List<UserAccountFlow> flows = userAccountFlowDao.getCashUserFlowByStatus(userPrx, CommonConstant
                    .PAY_STATUS_FINISH);
            if (flows == null || flows.size() == 0) {
                continue;
            }
            for (UserAccountFlow flow : flows) {
                try {
                    paySuccessDo(flow);
                } catch (Exception e) {
                    log.error("定时任务：微信通知业务方异常", e);
                }
            }
        }
    }

    /*
     * 微信支付通知回调
     * */
    @Transactional
    @Override
    public String wxPayCallBack(HttpServletRequest request) {
        try {
            // 解析微信返回的信息
            String result = PayUtil.wxNotify(request);
//            Document document = DocumentHelper.parseText(result);
            Document document = PayUtil.parseText(result);
            Map<String, Object> notify = PayUtil.convertNodesFromXml(document);
            // 当收到通知进行处理时，首先检查对应业务数据的状态，判断该通知是否已经处理过，如果没有处理过再进行处理，
            String flowId = notify.get("out_trade_no").toString();
            UserAccountFlow userAccountFlow = userAccountFlowDao.getUserFlowByShardType(flowId, Long.parseLong(String
                    .valueOf(flowId).substring(String.valueOf(flowId).length() - 2)), Boolean.TRUE);
            if (null == userAccountFlow) {
                log.error("微信支付成功通知业务处理异常，流水记录不存在!" + result);
                return PayUtil.WX_PAY_SIGN_ERROR_MSG;
            }
            // 如果处理过直接返回结果成功。在对业务数据进行状态检查和处理之前，要采用数据锁进行并发控制，以避免函数重入造成的数据混乱。
            if (userAccountFlow.getStatus() == CommonConstant.PAY_STATUS_FINISH || userAccountFlow.getStatus()
                    == CommonConstant.PAY_STATUS_HANDLED) {
                return PayUtil.WX_PAY_SIGN_SUCCESS_MSG;
            }
            SortedMap<Object, Object> packageParams = new TreeMap<>();
            Iterator it = notify.keySet().iterator();
            while (it.hasNext()) {
                String parameter = (String) it.next();
                String parameterValue = notify.get(parameter).toString();
                String v = "";
                if (null != parameterValue) {
                    v = parameterValue.trim();
                }
                packageParams.put(parameter, v);
            }
            // 商户系统对于支付结果通知的内容一定要做签名验证
            String payClientChannelKey = String.valueOf(userAccountFlow.getClientId()) + CommonConstant
                    .COMMON_COLON_STR + String
                    .valueOf(userAccountFlow.getChannel());
            PayClientChannel payClientChannel = PayClientChannelCache.getClientChannel(payClientChannelKey);
            Map<String, String> payKeyStr = (Map<String, String>) JSONObject.parse(payClientChannel.getPayKeyStr());
            String appid = payKeyStr.get(IniConstant.WX_PAY_APP_ID); //微信开放平台审核通过的应用APPID
            String mch_id = payKeyStr.get(IniConstant.WX_PAY_MCH_ID); //微信支付分配的商户号
            String key = payKeyStr.get(IniConstant.WX_PAY_KEY);
            if (PayUtil.checkPaySign("UTF-8", packageParams, key)) {
                if ("SUCCESS".equals((String) packageParams.get("return_code"))) {
                    // 如果返回成功
                    // 并校验返回的订单金额是否与商户侧的订单金额一致，
                    // 防止数据泄漏导致出现“假通知”，造成资金损失。
                    // 过滤空 设置 TreeMap
                    String result_mch_id = (String) packageParams.get("mch_id"); // 商户号
                    String total_fee = (String) packageParams.get("total_fee");
                    String transaction_id = (String) packageParams.get("transaction_id"); // 微信支付订单号
                    if (StringUtils.isBlank(transaction_id)) {
                        log.error("get wx notify transaction_id is null .all notify:" + JSONObject.toJSONString
                                (packageParams));
                        return PayUtil.WX_PAY_SIGN_PARAMS_ERROR_MSG;
                    }
                    // 查询订单 根据订单号查询订单
                    // 验证商户ID 和 价格 以防止篡改金额
                    if (mch_id.equals(result_mch_id) && total_fee.trim().equals(userAccountFlow.getPayAmount()
                            .toString())) {
                        // 处理业务
                        // 更新支付时间，订单，调用回调
                        userAccountFlow.setTransactionId(transaction_id);
                        userAccountFlow.setPayTime(DateUtil.getCurrentTimestamp());
                        userAccountFlow.setStatus(CommonConstant.PAY_STATUS_FINISH);
                        userAccountFlowDao.update(userAccountFlow);
                        // 处理业务
                        try {
                            paySuccessDo(userAccountFlow);
                        } catch (Exception e) {
                            log.error("微信支付成功通知业务处理异常", e);
                            return PayUtil.WX_PAY_SIGN_SUCCESS_MSG;
                        }
                        return PayUtil.WX_PAY_SIGN_SUCCESS_MSG;
                    } else {
                        return PayUtil.WX_PAY_SIGN_PARAMS_ERROR_MSG;
                    }
                } else // 如果微信返回支付失败，将错误信息返回给微信
                {
                    return PayUtil.WX_PAY_FAILED_MSG;
                }
            } else {
                log.error("微信支付成功通知业务处理异常,验证签名失败" + result);
                return PayUtil.WX_PAY_SIGN_ERROR_MSG;
            }
        } catch (Exception e) {
            log.error("微信支付校验异常", e);
            return PayUtil.WX_PAY_FAILED_MSG;
        }
    }

    @Override
    public String aliPayCallBack(HttpServletRequest request) {
        String res = PayUtil.ALI_PAY_FAILURE_MSG;
        Map<String, String> paramsMap = analysisAliParam(request.getParameterMap());
        if (paramsMap == null || !paramsMap.containsKey("body")) {
            return res;
        }
        log.info("支付宝回调：" + JSONObject.toJSONString(paramsMap));
        //
        //2.调用SDK验证签名
        boolean signVerified = false;
        try {
            signVerified = AlipaySignature.rsaCheckV1(paramsMap, PayConstant.ALI_APP_PUBLIC_KEY, CommonConstant
                    .CHARSET_UTF_8, CommonConstant.SIGN_TYPE_RSA2);
            log.info("交易状态" + paramsMap.get("trade_status"));
            if (signVerified && "TRADE_SUCCESS".equals(paramsMap.get("trade_status"))) {
                //1、商户需要验证该通知数据中的out_trade_no是否为商户系统中创建的订单号，
                String flowId = paramsMap.get("out_trade_no");
                Long preFix = Long.parseLong(flowId);
                UserAccountFlow userAccountFlow = userAccountFlowDao.getUserFlowByShardType(flowId, preFix, false);
                if (userAccountFlow == null) {
                    log.error("支付宝回调异常,通知了不存在的flowId", flowId);
                    return PayUtil.ALI_PAY_FAILURE_MSG;
                }
                if (!userAccountFlow.getStatus().equals(CommonConstant.PAY_STATUS_START)) {
                    return PayUtil.ALI_PAY_SUCCESS_MSG;
                }
                // 2、判断total_amount是否确实为该订单的实际金额（即商户订单创建时的金额），
                String amount = paramsMap.get("total_amount");
                if (StringUtils.isBlank(amount)) {
                    log.error("支付宝回调金额不匹配,通知金额:" + amount + "元 流水记录:" + userAccountFlow.getPayAmount() + "分");
                    return PayUtil.ALI_PAY_FAILURE_MSG;
                }
                amount = CommonUtil.removeZeroAfterPoint(CommonUtil.multiply(amount, "100").toString());
                if (!amount.equals(userAccountFlow.getPayAmount().toString())) {
                    log.error("支付宝回调金额不匹配,通知金额:" + amount + "元 流水记录:" + userAccountFlow.getPayAmount() + "分");
                    return PayUtil.ALI_PAY_FAILURE_MSG;
                }
                // 3、校验通知中的seller_id（或者seller_email) 是否为out_trade_no这笔单据的对应的操作方
                // （有的时候，一个商户可能有多个seller_id/seller_email），
                // 4、验证app_id是否为该商户本身。
                String payClientChannelKey = userAccountFlow.getClientId() + CommonConstant.COMMON_COLON_STR + String
                        .valueOf(CommonConstant.ALI_PAY_CHANNEL_ID);
                PayClientChannel payClientChannel = PayClientChannelCache.getClientChannel(payClientChannelKey);
                Map<String, String> payKeyStr = (Map<String, String>) JSONObject.parse(payClientChannel.getPayKeyStr());
                String appid = payKeyStr.get(IniConstant.ALI_PAY_APP_ID); //支付宝开放平台审核通过的应用APPID
                if (StringUtils.isBlank(paramsMap.get("app_id")) || !paramsMap.get("app_id").equals(appid)) {
                    log.error("支付宝回调商户app_id不匹配 通知app_id:" + paramsMap.get("app_id"));
                }
                // 更新支付时间，订单，调用回调
                //支付宝交易号
                String transaction_id = paramsMap.get("trade_no") == null ? "" : paramsMap.get("trade_no");
                userAccountFlow.setTransactionId(transaction_id);
                userAccountFlow.setPayTime(DateUtil.getCurrentTimestamp());
                userAccountFlow.setStatus(CommonConstant.PAY_STATUS_FINISH);
                userAccountFlowDao.update(userAccountFlow);
                // 处理业务
                try {
                    paySuccessDo(userAccountFlow);
                } catch (Exception e) {
                    log.error("支付宝支付成功通知业务处理异常", e);
                    return PayUtil.ALI_PAY_SUCCESS_MSG;
                }
                res = PayUtil.ALI_PAY_SUCCESS_MSG;
            } else if ("TRADE_CLOSED".equals(paramsMap.get("trade_status"))) {
                log.info("交易关闭" + JSONObject.toJSONString(paramsMap));
            } else if ("TRADE_FINISHED".equals(paramsMap.get("trade_status"))) {
//                log.error("支付宝交易状态 TRADE_FINISHED 即将通知三方正常处理" + JSONObject.toJSONString(paramsMap));
                return PayUtil.ALI_PAY_SUCCESS_MSG;
            } else {
                log.error("支付宝验签失败" + JSONObject.toJSONString(paramsMap));
            }
        } catch (AlipayApiException e) {
            log.error("支付宝验签过程中发生异常", e);
        }
        return res;
    }

    /* 易宝支付*/
    @Override
    public String yopPayCallBack(HttpServletRequest request) {
        //String
        // response="qNjNqLTYaSK98W9F04PghhPishrb_Z6u4g6oADsKxc_pwmajFQJZQnhVH5PeKrjf7SyvYr0VmZ2qZ66ev2PZohf0rSbkrTdV8yaN4MSvt4jhv2qBAaB39N9tDzr2avJ4uLZQ_QNvGG2cp4HJAp9h6Xa5LNkSqJrnxqzHQWzWDDIylNKirNeXp99qfip5yQYRaHE9B8CiJ8FaoY44d8J9udzdJPyABjpYbbEtGV6ad7xcmdE6EsXq6pC5JEbl8DDa1RJuwVA_ZFWABW7wJRgqwmJg_eQgiJ83C8HMg8nL0TVk7wp8hPUXLseOWbq021a7TDddn9mC8_i7l85BIvGVxA$6NFh3P1wfzZhMMwce5FxengavYXb_woW7KGriA2slwq5DNKu7ydSjQ1YuBiicFyxVHdtclXNptwJPCT8lGWKSxVRa9GWyljqGeG7nHNLaqTv65h5WGzidSlB1Z6HUN655Mf8ZtoVqs7p6kMotEbjDcBNT7KQBYJlrPJS70v2A1fco-9jzl4Ptaf0JFGCxwQv9erUM72KAbsZBWAKxGE9mjhlOB6H5z3Sb2qbVN9Sf-OYI9TZd_wQrnYH16OrYqFIIbzEHFmZLfYV5bwXu682W8Ewk4RJ0MSAL8-I-cdMMP7gUFweBpK2EOZTRyvkUBb5-i-qt9stRxlThfzTIgLeHOyI17WE_bAq7_84wl5iAbBlUxUdT87ArKFwyBVXzygwQUDvNlafzo6uYYQQd-keExyiMOj3YDTfTvvOPjjWxLbv65h5WGzidSlB1Z6HUN65tl6_6BIOnFDRpuCdhV4aDQtDszcvOWKpTar8RTCIduq0h2Ukk2B6UpSCb_2lPPAcTe8JM0LDr5yz0aM8zPBJdbHo_AoY1X4_aJ2CojGOMXUvMIbj6dViu9dQoBQHzv6krCL40XxBs3NB8qsuAfDj3lrpMDCnr6pRZpE20mfNw67c_Vh2K9miFTtwC0Cfp7SOlc2LPOiA-mGx-wdmwUM4cgl7GIiyX1nafzqF4gSvNn1phKf0IcnXtXfRTEFeHIAgllNxaPb-YfsQ_slWLmMbnJxDQFnRkFNiITC4P1ZyPZKdjEGyfHOHiRpgV8nywudTfCB_hDh0PVRyijcXXfnWncEvBz8k80XM4JBWIvO-DqyUVCzDoyv7-yCdb3a2B-ehTHNel7QGNu-U6iIrTiUlk236CYqxCKCdH79g0cPiHez1kL3rgeWds0nlK_XYyD29Sa0l3WEBcrdK38Szh_pEV1K0AYgh_zOTv3J3VV4ssNG736AebkCSK3-HJAr88O_i952OFXsMpUsLuNoI8QQMjxQeiWNkEAg2IEBIx-Bs1TJbApZwD8X_LZh1SYXxOLjqJppz3CZ7ZPSkOAQuyciB4CBxn-q5OYE4fcD8fbuYzEs$AES$SHA256";
//        String response = "gLEwS6lS1RBdMdwCBZRW9ELI4vZGE3g-gCHxBORJ_AtM8Aqve1_O5wbCJnNhJi9YN5RrQnhs8ACZjoCLEUR2QFBT1bs1Oe5OkxnHXuq_K8SxQbRwRKcubYQ1kG2R7lpHeFH385AJC0es44ByybuxzBEreAXWSYGpmbeKE1S2-HrRQHHzDyvYNigpmZK4I3VK5MIUYmgC-MfIumz8K4AQuwIs1KIg8OmnkSo3vEn9zLyBB8C1jCrGaMqFfux6bZkVsvswD8XVZaE1eitwAneRDD1bts9HDc9tYSMIyrZQgaCFJwY9kgGOe5yKqm2-bpaK0uDJwiRbfF2G_dQHwYJZXQ$hzAqs4cC7FbpByxGKyXAfJ-ApueRT0zSzFsPIqu0_T_jNs1rm6YVgdHixAmFMuU4UInwKET2gqoCipTqIhJzE6W3HHdiWhi0DA3lW7g1Z5pNgvW69CGr1Xm-01-zhNc5LpNR0rOBtr4hY5hdL3RJYsHKp2TSzQadxk8HBg9J1z1BspaZq-yuwp6jjqBOulxA6S1MG5_yJGRv92Ugv0YzIptoJ-aoU_U3erhKhmntYHXEpu6vrbA_YY2zzcjL_oYsHq5EV92484mfD3TH_6GkGuTvJbqYWlL8l82UZFmYET_OlxzJKvt5ZxT9mgW7a1DuhoDhO5RZP_dv-E5Ux3kIRGAbh9R1nBJhjegwg8lAbRPBALvYpMYmjrkIfMzC3zIuNTbNLepxib5rxzOG3pMUpT6CiltiCR_X0K9EFfIpsHe4tzhpV4xjGt7uRllPDCWrjXzoqfDKFoWqq_GVI3QuwQjgtdlNVgQyGN-lGVkDCGzPSnoy8lFj-S9Y72shL2d-g03_MyjdPoHvbgF5uO2er7epWFvTcUwfo9JOm7RymRhy2ofCGo-m7Hma5UkT5FSS7IjyWV2jvo-ryGbx91aEYahcO-K2g0A0_wE2hlwFlswcJGEzeA2W8Oz9Fkg6_t-sOAGpeQRPSylgLoXA-gkjL_tPmZRwONnkdG_4cGq_KhokL9JM4gTdH2z-aUNxDYakPbSTf2ggT_yw02T6Pcd300TcuIjgz7HJX9dQqk3nj1Dlb-nXnJGASIUqyHfsmdxIfKGMRNE8XKmRElx9DnHuiPqmWlwXhUz4hel5JAdxmKqXWO_8puR60R58CiKNAf4oS0Mh8udK2k6a_J-b3XqocAzxf5AktE4deZkm80Tr-nyDrny0TRZ3ksEhajd5uACNG6gaM5oGWpVkgdwoRMCLV6LwAMYl99QgYv5bDGNHszRpTW0UxcUTZmZD5g_W1erA_0x6SmuiF1pF_z2nK_Kg66HUBlmNZHQ7VIRBnUyjeeGuHI40dIrYIRCsbPjL7eDz2eX9A8trOjnhKiZ1-dUn8MQF72gZM6KxPDPWzerTvGUPdYOf7W_t0XA2sOP4oZAHXePRnjJZizsY88fltaHAcdQGfiljdVCy3jBK2wllnDwG3wglV8t0DPKjJZO294HifkRYp1grmjW0WJ1pDy5UEQ$AES$SHA256";
        String response = request.getParameter("response");
        log.info("YOP" + response);
        try {
            //开始解密
            Map<String, String> jsonMap = new HashMap<>();
            DigitalEnvelopeDTO dto = new DigitalEnvelopeDTO();
            dto.setCipherText(response);
            //InternalConfig internalConfig = InternalConfig.Factory.getInternalConfig();
            PrivateKey privateKey = CommonUtil.getPrivateKey(PropertyUtils.getProperty("yopPrivateKey"));
            log.info("privateKey: " + PropertyUtils.getProperty("yopPrivateKey"));
            PublicKey publicKey = CommonUtil.loadPublicKey(PropertyUtils.getProperty("yopPublicKey"));

            dto = DigitalEnvelopeUtils.decrypt(dto, privateKey, publicKey);
            log.info("YOP解密结果:" + dto.getPlainText());
            jsonMap = parseResponse(dto.getPlainText());
            log.info(jsonMap);
            // 交易状态
            if (jsonMap.get("status").equals("SUCCESS")) {
                //1、商户需要验证该通知数据中的out_trade_no是否为商户系统中创建的订单号，
                String flowId = jsonMap.get("orderId");
                Long preFix = Long.parseLong(flowId);
                UserAccountFlow userAccountFlow = userAccountFlowDao.getUserFlowByShardType(flowId, preFix, false);
                if (userAccountFlow == null) {
                    log.error("易宝回调异常,通知了不存在的flowId", flowId);
                    return "SUCCESS";
                }
                if (!userAccountFlow.getStatus().equals(CommonConstant.PAY_STATUS_START)) {
                    return "SUCCESS";
                }
                // 商户号校验
                String payClientChannelKey = userAccountFlow.getClientId() + CommonConstant.COMMON_COLON_STR + String
                        .valueOf(CommonConstant.YOP_PAY_CHANNEL_ID);
                PayClientChannel payClientChannel = PayClientChannelCache.getClientChannel(payClientChannelKey);
                Map<String, String> payKeyStr = (Map<String, String>) JSONObject.parse(payClientChannel.getPayKeyStr());
                if (!payKeyStr.get("merchantNo").equals(jsonMap.get("merchantNo"))) {
                    return "SUCCESS";
                }
                String amount = jsonMap.get("payAmount");
                log.info("yop 金额校验start");
                if (!userAccountFlow.getPayAmount().equals(CommonUtil.multiply(amount, "100").longValue())) {
                    return "SUCCESS";
                }
                log.info("yop 金额校验end");
                String transaction_id = jsonMap.get("uniqueOrderNo") == null ? "" : jsonMap.get("uniqueOrderNo");
                userAccountFlow.setTransactionId(transaction_id);
                userAccountFlow.setPayTime(DateUtil.getCurrentTimestamp());
                userAccountFlow.setStatus(CommonConstant.PAY_STATUS_FINISH);
                userAccountFlowDao.update(userAccountFlow);
                // 处理业务
                try {
                    paySuccessDo(userAccountFlow);
                    log.info("YOP业务处理完成！！！");
                } catch (Exception e) {
                    log.error("易宝支付成功通知业务处理异常", e);
                    return "ERROR";
                }
            }
        } catch (Exception e) {
            throw new BusinessException("易宝支付回调解密失败！");
        }
        return "SUCCESS";
    }

    @Override
    public String jdPayCallBack(HttpServletRequest request) {
        String res = "";
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
            String line = null;

            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            log.info("异步通知原始数据:" + sb);
        } catch (IOException e) {
            log.error("异步通知原始数据异常:" + e);
            return "fail";
        }

//        String b = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><jdpay>  " +
//                "<version>V2.0</version> <merchant>111069740002</merchant>  <result>    <code>000000</code>    <desc>success</desc>  </result>  <encrypt>ZGRjYmQxNmQ1NTRhMTBlOWZlNzdhYmJmYmZiMWQ5MGYwN2FhMDVmMTVhNDc2N2E5YWJmZDE1MTc2MzY0ZjdhYzM4NzE1ZDlkM2M0Mzk3MDgxNWNiZjc3NTRkM2EwM2E3YWY4MzIzOWY3MTlkNTgzMTA3ZWJiZmYyY2RiODI4NDA4NmY0ZWU1NWEwYTBlODQ3YzU3MmU1MDM0OGZjY2I4ZDM3ZjlmMDk1NDQzMzRiMjk5ZDVjYjVhMjI5NzExZDhkMGMwOTEyMzMwNDg0YzcxNDkyOGZhZDgzMzU4MTBlNWVmY2Q5NmY0Yzg5MmYwYmZhNGNkMWQ5MzVlYjYxYTdlOGJhNWY3YWIxM2Q3MTYwYjNmYjRiNThhMDc1MmU2YWY2MWIwOWJkODE1Zjc0MDI2M2VkYTdiYzkzMzFkN2VlZmFiZDMwMzhhZjU3OGMyY2JmMDI1OTE4ODM2MThhZGE3OGI1ZmE5ZWNhZWUxODRhZDY5ZDM4ZmIxNmY2MTQzMTM5ZjUzOWNiYmI5MDgxYzExYTljODNhOWIxNjUzOTIzN2Y3ZjNhYmRkNjc3YWUyZGQ4YmI2NjUyOTYwYzYyOTYzZDk0MDM1NGYzNjBhMDdiNmU1ZWU1MjA1NWI3ZTNkZWI1MGM5OWVlYTE5MjllOWJhZTAxN2E0YjM3YWNiMWVmZGU0MWVmNDk1YjBlNzIyMDVkYmViYjhlNzgyYmZjNTUwODMxYzA4MGZhMTQyZmYyM2MyNjcyYzlkNzhmMWM1ZDQzMTU4ZWU0ODcyMTE1YTU4YWY5Y2MwY2I4Mzk0NmE3ZDIwODZlYWYzNjdjODJlYWYzNzNkNzZhYzk0ODcyMWIzYTJlYTUzMTA3YzY0MWFhZDkyMDMxYWQxNmE3ZjA3ZTZkNDhiOGI3NTNiZmUzZDNhMGRjODI4ZWFhYTlmYzg3MDQxZTA1ZjgxZWZhNGEyNTliMzQxYmFiNDc2ZWI1ZjVjMjg1NWI3MzdjMWU2YWUzYzU1ZjNlNGZiNTEwOGJkMjA5ODBlZDU5NTUwY2E4YTRlZmNjYTcyMWE5MzA2Yjc4MzZjNDc4YzA0MzVmMDhkODcwMWFmYmRlMDc2MTQyMTQ1NGIxYzExYTJjNTc3ZmJjNzExNjM4MmNjYzRiZTYyOTE5YWU3YmRlMmZiN2M1MzNlMWFkODJlMzI5MTI0Yzg0NWQ5ZDMzYzFlODE3MGM1MTRhNzRmY2M4NDM2MDQ4Y2EzOWNkNGJmYWViOGI3NTg1YzVmYzBhMDU4MmEwNzc1MTg2YzdkZDE4MmU2ZjFiNzNjMTE0ZGJhZTk1NzQ1MGEyYTM4ODA2NjY5ZDVlYmQzNmQ1ODMwYzZhYWQ1ODRjYjhiM2M4MmU2ZWZmNTVmYTA0YmE5ODBmNzljYjgxN2MzZGZkNGQ4MjM4ODQ4N2YzMGFlNGU5MTVkYmRjMmE0NzM4NzJhZDJhZWU5ODQ5MTg5OTZjZGRjY2ZjNDcxY2ZmMzkzMjliMDRjYTZjMGU5ODE1YmY1NGI2MzU4ZTM1NmM1MDZiOTBjNDNjOTlmMzEwNjQ0MTIzNjI2ZTgwY2M3NjQwYmQyMGQ4OTk3MWQ0ZmUzNWIwN2Q2Njk4YWMzMzdkZTg0YWQ3YzViNDAyNGRkYjY2NTQzYjdmNTliZmUyOTM4YTRkMGY2ZWU2YTljMTQwYzYxNTRmOGE2NjFlMGYxZTQ2MjRkZWRkODdmNzBjNzVjOTkwODk1NTFlZjU5YzU5YzM1ZTc2NWQyNWU0YzBlNDM0MzY3MzUxZTkxYWU4NmRhNzlhNzk3M2MxM2QzZTg3MzNjNDE3ZjdkM2FjMTA0ZWI3MTE3OTczYzEzZDNlODczM2M0OGY0NTJlMmEzY2Y0ZGQ5ZDgwZDU0ZTEwZTkyMjdiMmI5ZWU4NmIzZGIwNmYxYzM5NjhlNzNiYjU3Njk4ODAwZmQyNDlhM2I5MDUxNjU0MDRlMTI2YjE2YjM1YjJlOTA1MDQxOGNiMzAzNTJlMzY3M2U0Njc1MjhlMzAxYTRhYzc4N2I3YzJlNTRmOWUzOWNiMjFiYTVmNjZhOWUzNGQ1OTQ0ODQ4OTBhMjUwNDM3MjcwMTU2NTUxZGY2Yjk4MWQxNTFmZWE0ZmRlM2UwNzZhZjNiYWUxZGNjOGUwNGMyZWY4OTNiY2IzMWU4MzdmYWYwNTFjZTVhZTQ3YzVlYTc4YjQ3M2M3ODA5OWJmZjU1ZjVmNTJlZTNiZDg5MjVjNGM3</encrypt></jdpay>";
//
//
//        sb.append(b);
        PayClientChannel payClientChannel = PayClientChannelCache.getClientChannel(CommonConstant
                .CLIENT_TYPE_ANDRIOD + CommonConstant.COMMON_COLON_STR + CommonConstant.JD_PAY_CHANNEL_ID);
        if (payClientChannel == null || StringUtils.isBlank(payClientChannel.getPayKeyStr())) {
            log.error("京东支付获取desc key 异常。" + sb.toString());
            return "fail";
        }
        Map<String, String> payKey = JSONObject.parseObject(payClientChannel.getPayKeyStr(), HashMap.class);

        String desKey = payKey.get(IniConstant.JD_PRIVATE_DES_KEY);
        String pubKey = PropertyUtils.getProperty("wepay.jd.rsaPublicKey");
        try {
            AsynNotifyResponse anRes = JdPayUtil.parseResp(pubKey, desKey, sb.toString(), AsynNotifyResponse.class);
//            AsynNotifyResponse anRes = PayUtil.parseJDResp(pubKey, desKey, sb.toString(), AsynNotifyResponse.class);
            if (StringUtils.isNotBlank(anRes.getStatus()) && anRes.getStatus().equals("2")) {
                //1、商户需要验证该通知数据中的out_trade_no是否为商户系统中创建的订单号，
                String flowId = anRes.getTradeNum();
                Long preFix = Long.parseLong(flowId);
                UserAccountFlow userAccountFlow = userAccountFlowDao.getUserFlowByShardType(flowId, preFix, false);
                if (userAccountFlow == null) {
                    log.error("京东回调异常,通知了不存在的flowId", flowId);
                    return "ok";
                }
                if (!userAccountFlow.getStatus().equals(CommonConstant.PAY_STATUS_START)) {
                    return "ok";
                }
                // 商户号校验
                Map<String, String> payKeyStr = (Map<String, String>) JSONObject.parse(payClientChannel.getPayKeyStr());
                if (payKeyStr.containsKey("jdPayMchId") && !payKeyStr.get("jdPayMchId").equals(anRes.getMerchant())) {
                    return "ok";
                }

                if (!userAccountFlow.getPayAmount().equals(anRes.getAmount())) {
                    log.error("回调金额和支付金额不匹配" + anRes.toString());
                    return "fail";
                }
//                String transaction_id = jsonMap.get("uniqueOrderNo") == null ? "" : jsonMap.get("uniqueOrderNo");
//                userAccountFlow.setTransactionId(transaction_id);
                userAccountFlow.setPayTime(DateUtil.getCurrentTimestamp());
                userAccountFlow.setStatus(CommonConstant.PAY_STATUS_FINISH);
                userAccountFlowDao.update(userAccountFlow);
                // 处理业务
                try {
                    paySuccessDo(userAccountFlow);
                } catch (Exception e) {
                    log.error("京东支付成功通知业务处理异常", e);
                    return "fail";
                }
            }
            log.info("异步通知解析数据:" + anRes);
            log.info("异步通知订单号：" + anRes.getTradeNum() + ",状态：" + anRes.getStatus() + "成功!!!!");

        } catch (Exception e) {
            log.error(e);
            return "fail";
        }
        return "ok";
    }

    @Override
    public String jdWithdrawCallBack(JDWithdrawCallBackParam param) {
        String res = "";

        RequestUtil demoUtil = new RequestUtil();
        try {
            Map<String, String> map = demoUtil.verifySingNotify(param);
            log.info(map);
            if (map == null) {
                log.info("验证签名不成功");
                return "fail";
            }
            String trade_status = param.getTrade_status();
            if (JDDefrayCodeConst.TRADE_FINI.equals(trade_status)) {
                userWithdrawFlowService.threePartyWithdrawSuccessHandler(map.get("out_trade_no"));
            } else if (JDDefrayCodeConst.TRADE_CLOS.equals(trade_status)) {
                log.info("代付失败：" + JSONObject.toJSONString(map));
                userWithdrawFlowService.threePartyWithdrawErrorHandler(map.get("out_trade_no"), map.get("trade_respmsg"));
            } else if (JDDefrayCodeConst.TRADE_WPAR.equals(trade_status) || JDDefrayCodeConst.TRADE_BUID.equals
                    (trade_status) || JDDefrayCodeConst.TRADE_ACSU.equals(trade_status)) {
                log.info("等待支付结果，处理中");//需查询交易获取结果或等待通知结果
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "fail";
    }

    @Override
    public String haoDianCallBack(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
            String line = null;

            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            log.info("好店异步通知原始数据:" + sb);
        } catch (IOException e) {
            log.error("好店异步通知原始数据异常:" + e);
            return "fail";
        }
        String paramStr = sb.toString();
        if (StringUtils.isBlank(paramStr)) {
            log.error("好店异步通知串为空");
            return "fail";
        }
        Map<String, Object> paramMap = JSONObject.parseObject(paramStr, HashMap.class);
        log.info("haodian pay param is" + paramStr);

        if (!paramMap.containsKey("status") || paramMap.get("status") == null) {
            log.error("好店异步通知串异常：" + paramStr);
            return "fail";
        }
        if (!Integer.valueOf(paramMap.get("status").toString()).equals(0)) {
            log.error("好店支付错误status:" + paramMap.get("status") + " msg" + paramMap.get("message"));
            return "fail";
        }
        // 根据客户端ID获取支付配置
        String payClientChannelKey = String.valueOf(CommonConstant.CLIENT_TYPE_ANDRIOD) + CommonConstant
                .COMMON_COLON_STR + CommonConstant.HAO_DIAN_PAY_CHANNEL_ID;
        PayClientChannel payClientChannel = PayClientChannelCache.getClientChannel(payClientChannelKey);
        Map<String, String> payKeyStr = (Map<String, String>) JSONObject.parse(payClientChannel.getPayKeyStr());
        String dsId = payKeyStr.get(IniConstant.HAO_DIAN_PAY_DS_ID);
        String mchId = payKeyStr.get(IniConstant.HAO_DIAN_PAY_MCH_ID);
        if (haoDianCheckSign(paramMap, payKeyStr.get(IniConstant.HAO_DIAN_PAY_MD5_KEY))) {
            if (!paramMap.containsKey("ds_id") || !paramMap.get("ds_id").toString().equals(dsId)) {
                log.error("好店渠道商系统付款交易号不匹配,http get:" + paramMap.get("ds_id") + " db save:" + dsId);
                return "fail";
            }
            if (!paramMap.containsKey("mch_id") || !paramMap.get("mch_id").toString().equals(mchId)) {
                log.error("好店商户编号不匹配,http get:" + paramMap.get("mch_id") + " db save:" + mchId);
                return "fail";
            }
            String flowId = PayUtil.removeHaoDianFlowId(paramMap.get("ds_trade_no").toString());
            UserAccountFlow userAccountFlow = userAccountFlowDao.getUserFlowByShardType(flowId, CommonUtil
                    .getUserIdSuffix(flowId), false);
            if (userAccountFlow == null) {
                log.error("好店回调异常,通知了不存在的flowId", flowId);
                return "ok";
            }
            if (!userAccountFlow.getStatus().equals(CommonConstant.PAY_STATUS_START)) {
                return "ok";
            }

            Long payFee = CommonUtil.convertYuan2Fen(paramMap.get("pay_fee").toString()).longValue();
            if (!userAccountFlow.getPayAmount().equals(payFee)) {
                log.error("回调金额和支付金额不匹配，三方金额: " + payFee + " 流水金额：" + userAccountFlow.getPayAmount());
                return "fail";
            }
            String transaction_id = paramMap.get("trade_no") == null ? "" : paramMap.get("trade_no").toString();
            userAccountFlow.setTransactionId(transaction_id);
            userAccountFlow.setPayTime(DateUtil.getCurrentTimestamp());
            userAccountFlow.setStatus(CommonConstant.PAY_STATUS_FINISH);
            userAccountFlowDao.update(userAccountFlow);
            // 处理业务
            try {
                paySuccessDo(userAccountFlow);
            } catch (Exception e) {
                log.error("好店支付成功通知业务处理异常", e);
                return "fail";
            }
            return "success";
        }

        return "fail";
    }

    private Boolean haoDianCheckSign(Map<String, Object> paramMap, String md5Key) {
        SortedMap<String, String> paramterMap = new TreeMap<>();
        for (String key : paramMap.keySet()) {
            paramterMap.put(key, paramMap.get(key).toString());
        }
        String signStr = PayUtil.getSortedKeyString(paramterMap) + md5Key;
        String calculateMD5Str = Md5Util.getMD5String(signStr).toUpperCase();
        String httpMD5Str = paramMap.get("sign").toString();
        if (httpMD5Str.equals(calculateMD5Str)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    public static Map<String, String> parseResponse(String response) {

        Map<String, String> jsonMap = new HashMap<>();
        jsonMap = JSON.parseObject(response,
                new TypeReference<TreeMap<String, String>>() {
                });

        return jsonMap;
    }

    private Map<String, String> analysisAliParam(Map<String, String[]> parameterMap) {
        Map<String, String> res = new HashMap<>();
        for (Iterator<String> it = parameterMap.keySet().iterator(); it.hasNext(); ) {
            String name = it.next();
            String[] values = parameterMap.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
//            try {
//                valueStr = new String(valueStr.getBytes("ISO-8859-1"), CommonConstant.CHARSET_UTF_8);
//            } catch (UnsupportedEncodingException e) {
//                log.error(e);
//            }
            res.put(name, valueStr);
        }
        return res;
    }

    /*
     * 微信订单查询方法, 微信支付的流水主动去查询结果, 过期关闭
     * */
    @Override
    public void wxOrderQuery(UserAccountFlow userAccountFlow) {
        // 超过1小时未支付的停止查询，订单关闭
        if ((int) DateUtil.getDiffSeconds(userAccountFlow.getCreateTime(), DateUtil.getCurrentTimestamp()) > (60 *
                60)) {
            if (!wxCloseOrder(userAccountFlow)) {
                PayService.FLOW_LIST_QUEUE.offer(userAccountFlow);
            }
            return;
        }
        Map<String, Object> params = PayChannelEnum.getPayChannelEnum(userAccountFlow.getChannel()).payQuery
                (userAccountFlow);
        if (null != params) {
            // 处理业务
            // 更新支付时间，订单，调用回调
            if (params.containsKey("transaction_id")) {
                String transaction_id = params.get("transaction_id").toString(); // 微信支付订单号
                userAccountFlow.setTransactionId(transaction_id);
            }
            userAccountFlow.setPayTime(DateUtil.getCurrentTimestamp());
            userAccountFlow.setStatus(CommonConstant.PAY_STATUS_FINISH);
            userAccountFlowDao.update(userAccountFlow);
            // 处理业务
            try {
                paySuccessDo(userAccountFlow);
            } catch (Exception e) {
                log.error("微信支付成功通知业务处理异常");
            }
        } else {
            PayService.FLOW_LIST_QUEUE.offer(userAccountFlow);
        }
    }

    /*
     * 苹果支付成功二次验证 验证通过添加流水并且调用业务方法
     * */
    @Override
    @Transactional
    public Map applePayValidate(String flowId, String receipt) {
        Map<String, Object> res = new HashMap<>();
        String requestUrl = IniCache.getIniValue(IniConstant.APPLE_PAY_VALIDATE_URL, "https://buy.itunes.apple" +
                ".com/verifyReceipt");
        String content = String.format(Locale.CHINA, "{\"receipt-data\":\"" + receipt + "\"}");
        String result = HttpServiceUtils.sendHttpsPostRequest(requestUrl, content, "UTF-8");
        Map<String, Object> remarkMap = new HashMap<>();
        remarkMap.put("environment", "production");
        if (JSONObject.parseObject(result).getString("status").equals("21007")) {
            log.error("somebody test flowId:" + flowId + " result is " + result);
            String sandbox = "https://sandbox.itunes.apple.com/verifyReceipt";
            result = HttpServiceUtils.sendHttpsPostRequest(sandbox, content, "UTF-8");
            remarkMap.put("environment", "sandbox");
        }
        // TODO 商户ID验证
        Map<String, Object> appleCheck = JSONObject.parseObject(result, HashMap.class);
        try {
            if (appleCheck.get("status").toString().equals("0")) {
                // 记录苹果的订单ID
                UserAccountFlow userAccountFlow = userAccountFlowDao.getUserFlowByShardType(flowId, Long.parseLong
                        (String.valueOf(flowId).substring(String.valueOf(flowId).length() - 2)), Boolean.TRUE);
                if (null == userAccountFlow) {
                    log.error("苹果支付成功通知业务处理异常，流水记录不存在!" + result);
                    return null;
                }
                // 如果处理过直接返回结果成功。在对业务数据进行状态检查和处理之前，要采用数据锁进行并发控制，以避免函数重入造成的数据混乱。
                if (userAccountFlow.getStatus() == CommonConstant.PAY_STATUS_FINISH || userAccountFlow.getStatus()
                        == CommonConstant.PAY_STATUS_HANDLED) {
                    log.error("苹果支付成功通知业务处理异常，流水记录已经处理!" + result);
                    return null;
                }
                Map<String, Object> receiptMap = (Map<String, Object>) appleCheck.get("receipt");
                List<Map<String, Object>> inApp = (List<Map<String, Object>>) receiptMap.get("in_app");
                String transactionId = (String) inApp.get(0).get("transaction_id");
                userAccountFlow.setTransactionId(transactionId);
//                userAccountFlow.setTransactionId(JSONObject.parseObject(result).getString("transaction_id"));

                // remark
                if (StringUtils.isNotBlank(userAccountFlow.getRemark())) {
                    try {
                        Map<String, Object> tempRemark = JSONObject.parseObject(userAccountFlow.getRemark());
                        if (tempRemark.containsKey("environment")) {
                            tempRemark.remove("environment");
                        }
                        remarkMap.putAll(tempRemark);
                    } catch (Exception e) {

                    }
                }

                userAccountFlow.setRemark(JSONObject.toJSONString(remarkMap));
                userAccountFlow.setTransactionId(transactionId);
                userAccountFlow.setPayTime(DateUtil.getCurrentTimestamp());
                userAccountFlow.setStatus(CommonConstant.PAY_STATUS_FINISH);
                userAccountFlowDao.update(userAccountFlow);
                // 处理业务
                try {
                    paySuccessDo(userAccountFlow);
                    res.put("successFlag", true);
                    res.put("msg", "付款成功");
                } catch (Exception e) {
                    log.error("苹果支付成功通知业务处理异常");
                }
            } else {
                log.error("苹果支付验证失败 receipt" + receipt);
            }
        } catch (Exception e) {
            log.error("苹果支付验证失败 receipt" + receipt + "result" + result);
        }
        return res;
    }

    /*
     * 支付成功 反射调用业务方 注：业务必须具有相同的返回格式
     * */
    @Override
    public void paySuccessDo(UserAccountFlow userAccountFlow) throws Exception {
        Map<String, Object> remarkMap = (Map<String, Object>) JSONObject.parse(userAccountFlow
                .getRemark());
        String[] clazzMethodName = String.valueOf(remarkMap.get("clazzMethodName")).split
                (CommonConstant.COMMON_ESCAPE_STR + CommonConstant.COMMON_DOT_STR);
        Class clazz = SpringContextHolder.getBean(clazzMethodName[0]).getClass();
        Method method = clazz.getDeclaredMethod(clazzMethodName[1], String.class, String
                .class);
        Boolean bzResult = (Boolean) method.invoke(SpringContextHolder
                .getBean(clazzMethodName[0]), userAccountFlow.getPayId(), userAccountFlow.getFlowId());
        if (bzResult) {
            userAccountFlow.setStatus(CommonConstant.PAY_STATUS_HANDLED);
            userAccountFlowDao.update(userAccountFlow);
            //开线程统计
            try {
                ExecutorService taskExec = ThreadPool.getInstance().getAllProductBillExec();
                AllProductBillTask task = new AllProductBillTask(userAccountFlow.getUserId(), userAccountFlow
                        .getFlowId(), allProductBillService);
                taskExec.submit(task);
            } catch (Exception e) {
                log.error("统计product异常" + userAccountFlow.getFlowId());
            }
        }
    }

    @Override
    public Map<String, Object> checkFlowIdStatus(String flowId) {
        Map<String, Object> result = new HashMap<>();
        Long userIdSuffix = CommonUtil.getUserIdSuffix(flowId);
        Integer status = 0;
        UserAccountFlow flow = userAccountFlowDao.getUserFlowByShardType(flowId, userIdSuffix, false);
        if (flow != null && flow.getStatus() != null && !flow.getStatus().equals(CommonConstant.PAY_STATUS_START)) {
            status = 1;
        }
        result.put("status", status);
        return result;
    }

    @Override
    public Map<String, Object> getConfirmPayPopInfo(Long userId, PrePayInfo prePayInfo, Integer clientId, Integer
            versionCode) {
        Map<String, Object> res = new HashMap<>();
        List<Map> payDescList = new ArrayList<>();
        //1.商品名称 价格
        Long price = prePayInfo.getPrice();
        Map<String, Object> desc = new HashMap<>();
        desc.put("payDesc", "<font color='#999999'>" + prePayInfo.getGoodsName() + "</font>");
        desc.put("amountText", CommonUtil.convertFen2Yuan(price + "") + CommonConstant
                .GOLD_WISDOM_COIN_MONETARY_UNIT);
        payDescList.add(desc);
        //2.vip折扣信息
        boolean isVip = vipMemberService.checkUserIsVip(userId, prePayInfo.getGoodsVipType());
        if (isVip) {
            Long vipPrice = prePayInfo.getUserNeedPayAmount(isVip);
            String discount = CommonUtil.subtract(price + "", String.valueOf(vipPrice)).toString();
            Map<String, Object> vipDesc = new HashMap<>();
            vipDesc.put("payDesc", "<font color='#999999'>会员优惠</font>");
            vipDesc.put("amountText", "-" + CommonUtil.convertFen2Yuan(discount) + CommonConstant
                    .GOLD_WISDOM_COIN_MONETARY_UNIT);
            payDescList.add(vipDesc);
            price = vipPrice;
        }
        //3.实付款
        Map<String, Object> realPrice = new HashMap<>();
        String realPriceDesc = "<font color='#999999'>实付款</font>";
        if (isVip) {
            realPriceDesc = "<font color='#999999'>还需支付</font>";
        }
        realPrice.put("payDesc", realPriceDesc);
        realPrice.put("amountText", "<font color='#FF5050'>" + CommonUtil.convertFen2Yuan(price) + "智慧币</font>");
        payDescList.add(realPrice);

        //4.支付渠道
        Map<String, Object> paymentList = getChannelList(userId, clientId, versionCode);
        res.putAll(paymentList);
        res.put("price", CommonUtil.convertFen2Yuan(price).doubleValue());
        res.put("payDescList", payDescList);
        return res;
    }

    @Override
    public Integer checkOrderOutTradeStatus(String payId, Integer payType, Long userId) {
        UserAccountFlow flow = userAccountFlowDao.getUserFlowCheck(payId, payType, userId, false);
        if (flow == null) {
            return PayConstant.OUT_TRADE_ORDER_STATUS_NO_PAY;
        }
        Map<String, Object> res = PayChannelEnum.getPayChannelEnum(flow.getChannel()).payQuery(flow);
        if (res == null || !res.containsKey("orderStatus")) {
            return PayConstant.OUT_TRADE_ORDER_STATUS_NO_PAY;
        }
        return Integer.valueOf(res.get("orderStatus").toString());
    }

    @Override
    public Map<String, Object> manualWithdraw(Long userId, Long withdrawAmount, Long operateUserId) {
        Map<String, Object> res = new HashMap<>();
        //1.check用户预测
        UserAccount userAccount = userAccountDao.getUserAccountBalance(userId, CommonConstant.ACCOUNT_TYPE_BALANCE,
                false);
        if (userAccount == null || userAccount.getAccountBalance() == null || userAccount.getAccountBalance() <
                withdrawAmount) {
            res.put("status", -1);
            res.put("msg", "提现失败,账户余额不足。");
            return res;
        }
        String msg = "提现失败";
        Integer status = ResultConstant.ERROR;
        String payDesc = "人工提现";
        String payId = System.currentTimeMillis() + "WITHDRAW" + CommonUtil.getUserIdSuffix(String.valueOf(userId));
        Map<String, Object> payInfo = payCreateFlow(userId, payId, withdrawAmount, CommonConstant.PAY_TYPE_BALANCE,
                null, withdrawAmount, payDesc, "", null, null, CommonConstant.PAY_OPERATE_TYPE_DEC, null);
        if (payInfo != null && payInfo.get("payStatus").equals(ResultConstant.PAY_SUCCESS_CODE)) {
            //todo 记录人工提现log
            status = ResultConstant.SUCCESS;
            msg = "余额已扣除，可放心转账给用户";
        }
        res.put("status", status);
        res.put("msg", msg);
        return res;
    }

    /*
     * 通知微信关闭订单 关闭订单后，不在继续查询
     * */
    private Boolean wxCloseOrder(UserAccountFlow userAccountFlow) {
        Map<String, Object> params = PayChannelEnum.getPayChannelEnum(userAccountFlow.getChannel()).payClose
                (userAccountFlow);
        if (null != params) {
            return Boolean.TRUE;
        }
        log.error("订单关闭失败" + userAccountFlow.getFlowId());
        return Boolean.FALSE;
    }

    /*
     * 根据支付渠道配置信息 获取支付渠道列表
     * */
    @Override
    public Map<String, Object> getChannelList(Long userId, Integer clientId, Integer versionCode) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> channelList = new ArrayList<>();
        List<PayChannelInfo> payChannelInfoList = payChannelInfoDao.getAllChannel();
        for (PayChannelInfo payChannelInfo : payChannelInfoList) {
            if (payChannelInfo.getChannelId().equals(CommonConstant.WX_PAY_CHANNEL_WX_JSAPI)) {
                continue;
            }
            String key = String.valueOf(clientId) + CommonConstant.COMMON_COLON_STR + String.valueOf(payChannelInfo
                    .getChannelId()) + CommonConstant.COMMON_COLON_STR + String.valueOf(versionCode);
            PayClientVersionControl payClientVersionControl = PayClientVersionControlCache.getActivityIniValue(key);
            if (null == payClientVersionControl || payClientVersionControl.getIsDelete() != (CommonStatusEnum.YES
                    .getStatus())) {
                Map channelMap = new HashMap();
                channelMap.putAll(convertPayChannelInfo2Map(userId, payChannelInfo));
                channelList.add(channelMap);
            }
        }

        // 渠道配置
        result.put("paymentList", channelList);
        return result;
    }

    private Map convertPayChannelInfo2Map(Long userId, PayChannelInfo payChannelInfo) {
        Map<String, Object> res = new HashMap<>();
        String balanceText = "";
        double balance = 0l;
        if (payChannelInfo.getChannelId().equals(CommonConstant.WISDOM_COIN_CHANNEL_ID)) {
            UserAccount userAccount = userAccountDao.getUserAccountBalance(userId, CommonConstant
                    .ACCOUNT_TYPE_WISDOM_COIN, false);
            Long userBalance = userAccount == null ? 0 : userAccount.getAccountBalance();
            balance = CommonUtil.convertFen2Yuan(userBalance).doubleValue();
            balanceText = "(余额:" + balance + ")";
        }
        Integer realNameAuthenticate = 0;
        Integer bindCard = 0;
        String channelDesc = "";
        String limitTime = "";
        String upperLimit = "";
        String lowerLimit = "";
        List<Map<String, Object>> tags = null;
        if (StringUtils.isNotBlank(payChannelInfo.getRemark())) {
            Map<String, Object> remarkMap = JSONObject.parseObject(payChannelInfo.getRemark(), HashMap.class);
            if (remarkMap.containsKey("realNameAuth")) {
                realNameAuthenticate = Integer.valueOf(remarkMap.get("realNameAuth").toString());
            }
            if (remarkMap.containsKey("bindCard")) {
                bindCard = Integer.valueOf(remarkMap.get("bindCard").toString());
            }

            channelDesc = CommonUtil.getValueFromMap("channelDesc", remarkMap) == null ? "" : CommonUtil
                    .getValueFromMap("channelDesc", remarkMap);
            limitTime = CommonUtil.getValueFromMap("limitTime", remarkMap) == null ? "" : CommonUtil.getValueFromMap
                    ("limitTime", remarkMap);
            channelDesc = channelDesc + limitTime;

            if (remarkMap.containsKey("tags")) {
                tags = (List<Map<String, Object>>) remarkMap.get("tags");
            }
            upperLimit = CommonUtil.getValueFromMap("upperLimit", remarkMap);
            lowerLimit = CommonUtil.getValueFromMap("lowerLimit", remarkMap);
            if (StringUtils.isNotBlank(upperLimit) && Long.valueOf(upperLimit) > 0l) {
                balanceText = CommonUtil.getValueFromMap("upperLimitDesc", remarkMap) + CommonUtil
                        .removeZeroAfterPoint(CommonUtil.convertFen2Yuan(upperLimit).toString()) + CommonConstant
                        .CASH_MONETARY_UNIT_YUAN;
            }
            if (StringUtils.isNotBlank(lowerLimit) && Long.valueOf(lowerLimit) > 0l) {
                balanceText = CommonUtil.getValueFromMap("lowerLimitDesc", remarkMap) + CommonUtil
                        .removeZeroAfterPoint(CommonUtil.convertFen2Yuan(lowerLimit).toString()) + CommonConstant
                        .CASH_MONETARY_UNIT_YUAN;
            }
        }

        res.put("channelName", payChannelInfo.getChannelName());
        res.put("channelId", payChannelInfo.getChannelId());
        res.put("notifyUrl", payChannelInfo.getNotifyUrl());
        res.put("payUrl", payChannelInfo.getPayUrl());
        res.put("channelIcon", payChannelInfo.getChannelIcon());
        res.put("createTime", payChannelInfo.getCreateTime());
        res.put("isDefault", payChannelInfo.getIsDefault());
        res.put("showText", payChannelInfo.getShowText());
        res.put("updateTime", payChannelInfo.getUpdateTime());
        res.put("channelBalanceText", balanceText);
        res.put("channelBalance", balance);
        res.put("realNameAuthenticate", realNameAuthenticate);
        res.put("bindCard", bindCard);
        res.put("channelDesc", channelDesc);
        res.put("status", payChannelInfo.getChannelStatus());
        res.put("tags", tags);
        res.put("upperLimit", upperLimit);
        res.put("lowerLimit", lowerLimit);
        res.put("weight", payChannelInfo.getWeight());
        return res;
    }

    /*
     * 金币商城可以兑换的商品列表
     * */
    @Override
    public Map<String, Object> getExchangeMall(Long userId) {
        Map<String, Object> result = new HashMap<>();
        List<Object> exchangeMallList = new ArrayList<>();
        List<ExchangeMall> exchangeMalls = exchangeMallDao.getExchangeMallList(ExchangeMallConstant
                .EXCHANGE_MALL_DIGIT_VIP);
        for (ExchangeMall exchangeMall : exchangeMalls) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("itemId", exchangeMall.getItemId());
            itemMap.put("itemName", exchangeMall.getItemName());
            itemMap.put("itemImg", exchangeMall.getItemImg());
            itemMap.put("exchangeCount", exchangeMall.getItemPrice());
            itemMap.put("exchangeName", exchangeMall.getItemPrice() + "金币");
            itemMap.put("originExchangeName", exchangeMall.getItemOriginPrice() + CommonConstant
                    .GOLD_COIN_MONETARY_UNIT);
            exchangeMallList.add(itemMap);
        }
        result.put("itemList", exchangeMallList);
        // 用户余额
        UserAccount userAccount = getUserAccount(userId, CommonConstant.ACCOUNT_TYPE_GOLD, Boolean.FALSE);
        result.put("userAccountBalance", userAccount.getAccountBalance());
        result.put("userAccountIcon", ActivityIniCache.getActivityIniValue(ActivityIniConstant.ACCOUNT_GOLD_ICON,
                "http://sportsimg.mojieai.com/gold_coin.png"));
        return result;
    }

    /*
     * 用户使用金币兑换产生的流水
     * */
    @Override
    public Map<String, Object> flowList(Long userId, Integer payType, Integer page) {
        if (null == page) {
            page = 1;
        }
        Integer pageCount = 30;
        Map<String, Object> resultMap = new HashMap<>();
        List<Object> flowList = new ArrayList<>();
        PaginationList<UserAccountFlow> userAccountFlows = userAccountFlowDao.getUserFlowListByPage(userId, payType,
                page, pageCount);
        if (null != userAccountFlows && userAccountFlows.size() > 0) {
            for (UserAccountFlow userAccountFlow : userAccountFlows) {
                String monetaryUnit = CommonUtil.getUnitCnByPayType(payType);
                // 金币兑换操作
                String prefix = "<font color=#FF5050>" + CommonConstant.COMMON_ADD_STR;
                if (userAccountFlow.getOperateType().equals(CommonConstant.PAY_OPERATE_TYPE_DEC)) {
                    prefix = "<font color=#1FBF43>" + CommonConstant.COMMON_DASH_STR;
                }
                Map<String, Object> flow = new HashMap<>();
                flow.put("flowId", userAccountFlow.getFlowId());
                flow.put("flowDesc", userAccountFlow.getPayDesc());
                flow.put("flowCount", prefix + CommonUtil.getMoneyStr(userAccountFlow.getPayAmount(), payType) +
                        monetaryUnit + "</font>");
                flow.put("flowDate", DateUtil.formatDate(userAccountFlow.getCreateTime(), DateUtil
                        .formatTab[DateUtil.FMT_DATE_YYYYMMDD]));
                flowList.add(flow);
            }
        }
        resultMap.put("flowList", flowList);
        // count
        Integer count = userAccountFlows.getPaginationInfo().getTotalRecord();
        resultMap.put("isHaveNextPage", CommonStatusEnum.NO.getStatus());
        resultMap.put("nextPageIndex", page);
        if (page * pageCount < count) {
            resultMap.put("isHaveNextPage", CommonStatusEnum.YES.getStatus());
            resultMap.put("nextPageIndex", page + 1);
        }
        // 流水添加余额
        return resultMap;
    }

    /*
     * 获取用户金币余额方法，当金币账户不存在，创建账户
     * */
    @Override
    public UserAccount getUserAccount(Long userId, Integer accountType, Boolean isLock) {
        UserAccount userAccount = userAccountDao.getUserAccountBalance(userId, accountType,
                isLock);
        // 创建用户账户
        if (null == userAccount) {
            userAccount = new UserAccount(userId, accountType);
            userAccountDao.insert(userAccount);
        }
        return userAccount;
    }


    /*
     * 金币兑换商品方法
     * */
    @Override
    public Map<String, Object> exchangeItem(Long userId, Integer itemId, String clientIp, Integer clientId) {
        try {
            ExchangeMall exchangeMall = exchangeMallDao.getExchangeMall(itemId);
            Boolean isBzzSuccess = Boolean.FALSE;
            Map<String, Object> payInfo = new HashMap<>();
            // 金币兑换vip
            if (exchangeMall.getItemType().equals(ExchangeMallConstant.EXCHANGE_MALL_DIGIT_VIP) || exchangeMall
                    .getItemType().equals(ExchangeMallConstant.EXCHANGE_MALL_SPORTS_VIP)) {
                // 支付
                payInfo = payCreateFlow(userId, null, exchangeMall.getItemPrice(), exchangeMall.getAccountType(),
                        null, exchangeMall.getItemPrice(), exchangeMall.getItemName(), clientIp, clientId, null,
                        CommonConstant.PAY_OPERATE_TYPE_DEC, null);
                // 支付成功 发送奖励
                if (payInfo.get("payStatus").equals(ResultConstant.PAY_SUCCESS_CODE)) {
                    Map<String, Object> result = vipMemberService.goldCoinPurchaseVip(userId, exchangeMall
                                    .getItemPrice(), exchangeMall.getItemCount(), String.valueOf(payInfo.get("flowId")),
                            null, ExchangeMallUtil.getVipTypeByExchangeMall(exchangeMall));
                    if (result != null && StringUtils.isNotBlank(result.get("msg").toString())) {
                        payInfo.put("payMsg", result.get("msg"));
                    }
                    if ((Integer) result.get("code") == 0) {
                        isBzzSuccess = Boolean.TRUE;
                    }
                }
            } else if (exchangeMall.getItemType().equals(ExchangeMallConstant.EXCHANGE_MALL_SPORTS_COUPON)) {
                Integer activityId = 201806002;
                ActivityInfo activityInfo = activityInfoDao.getActivityInfo(activityId);
                if (activityService.checkActivityIsEnabled(activityId) && activityInfo != null && StringUtils
                        .isNotBlank(activityInfo.getRemark())) {
                    // 支付
                    String payId = DateUtil.getCurrentDay() + "GOLDCOINEXCHANGE" + activityId;
                    payInfo = payCreateFlow(userId, payId, exchangeMall.getItemPrice(), exchangeMall.getAccountType()
                            , null, exchangeMall.getItemPrice(), exchangeMall.getItemName(), clientIp, clientId,
                            null, CommonConstant.PAY_OPERATE_TYPE_DEC, null);
                    // 支付成功 发送奖励
                    if (payInfo.get("payStatus").equals(ResultConstant.PAY_SUCCESS_CODE)) {
                        Map<String, Object> result = userCouponService.goldCoinExchangeCoupon(userId, String.valueOf
                                (payInfo.get("flowId")));
                        if (result != null && StringUtils.isNotBlank(result.get("msg").toString())) {
                            payInfo.put("payMsg", result.get("msg"));
                        }
                        if ((Integer) result.get("code") == 0) {
                            isBzzSuccess = Boolean.TRUE;
                        }
                    } else if (payInfo.get("payStatus").equals(ResultConstant.REPEAT_CODE)) {
                        payInfo.put("payMsg", "今天已兑换");
                    }
                }
            } else {
                GamePeriod lastOpenPeriod = PeriodRedis.getLastOpenPeriodByGameId(exchangeMall.getGameId());
                GamePeriod currentPeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(exchangeMall.getGameId(),
                        lastOpenPeriod.getPeriodId());
                // access period 期次 期次权限用户只需要购买一次
                // 检查流水 payId userId+期次ID+itemID
                String payId = String.valueOf(userId) + currentPeriod.getPeriodId() + String.valueOf(exchangeMall
                        .getItemId());
                payInfo = payCreateFlow(userId, payId, exchangeMall.getItemPrice(), exchangeMall
                                .getAccountType(), null, exchangeMall.getItemPrice(), exchangeMall.getItemName(),
                        clientIp, clientId, null, CommonConstant.PAY_OPERATE_TYPE_DEC, null);
                // 支付成功 发送奖励
                if (payInfo.get("payStatus").equals(ResultConstant.PAY_SUCCESS_CODE)) {
                    for (int i = 0; i < exchangeMall.getItemCount(); i++) {
                        // 当前期次
                        UserAccess userAccess = userAccessDao.getUserAccess(userId, currentPeriod.getPeriodId(),
                                currentPeriod.getGameId());
                        Boolean insert = Boolean.FALSE;
                        if (null == userAccess) {
                            userAccess = new UserAccess(userId, currentPeriod.getGameId(), currentPeriod
                                    .getPeriodId(),
                                    String.valueOf(exchangeMall.getItemType()));
                            insert = Boolean.TRUE;
                        }
                        List<String> accessL = Arrays.asList(userAccess.getAccessList().split(CommonConstant
                                .COMMA_SPLIT_STR));
                        List accessList = new ArrayList(accessL);
                        if (!accessList.contains(exchangeMall.getItemType().toString())) {
                            accessList.add(exchangeMall.getItemType().toString());
                        }
                        StringBuffer sb = new StringBuffer();
                        for (int j = 0; j < accessList.size(); j++) {
                            sb.append(accessList.get(j));
                            if (j != (accessList.size() - 1)) {
                                sb.append(CommonConstant.COMMA_SPLIT_STR);
                            }
                        }
                        userAccess.setAccessList(sb.toString());
                        if (insert) {
                            userAccessDao.insert(userAccess);
                        } else {
                            userAccessDao.update(userAccess);
                        }
                        currentPeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(currentPeriod.getGameId(),
                                currentPeriod.getPeriodId());
                    }
                    isBzzSuccess = Boolean.TRUE;
                }
            }

            // 业务处理成功
            if (isBzzSuccess) {
                // 更新成功
                String flowId = String.valueOf(payInfo.get("flowId"));
                handledFlow(flowId);
            }
            return payInfo;
        } catch (Exception e) {
            log.error("金币兑换失败", e);
            throw new BusinessException("金币兑换支付失败");
        }
    }

    @Override
    public void handledFlow(String flowId) {
        UserAccountFlow userAccountFlow = userAccountFlowDao.getUserFlowByShardType(flowId, Long.parseLong
                (String.valueOf(flowId).substring(String.valueOf(flowId).length() - 2)), Boolean.FALSE);
        userAccountFlow.setStatus(CommonConstant.PAY_STATUS_HANDLED);
        userAccountFlowDao.update(userAccountFlow);
    }

    // 权限对应的商品ID配置在activityIni
    @Override
    public Map<String, Object> getAccessIdByType(String type, Long gameId) {
        Map<String, Object> resultMap = new HashMap<>();
        Integer itemId = ActivityIniCache.getActivityIniIntValue(type + String.valueOf(gameId));
        resultMap.put("itemId", itemId);
        ExchangeMall exchangeMall = exchangeMallDao.getExchangeMall(itemId);
        resultMap.put("itemPrice", exchangeMall.getItemPrice());
        return resultMap;
    }


    /*
     * 如何赚金币列表，杀号次数和奖励可以配置
     *
     * TODO: 18/6/4   重构 紧急程度严重
     * */
    @Override
    public Map<String, Object> taskList(Long userId, Integer versionCode, Integer clientType) {
        Map<String, Object> resultMap = new HashMap<>();
        // 用户余额
        UserAccount userAccount = getUserAccount(userId, CommonConstant.ACCOUNT_TYPE_GOLD, Boolean.FALSE);
        resultMap.put("userAccountBalance", userAccount.getAccountBalance());
        resultMap.put("userAccountName", ActivityIniCache.getActivityIniValue(ActivityIniConstant
                .ACCOUNT_BALANCE_NAME, "我的金币"));
        List<Object> taskList = new ArrayList<>();
        // 签到

//        Map<String, Object> sign = new HashMap<>();
//        sign.put("taskName", "签到");
//        sign.put("taskDate", DateUtil.formatDate(DateUtil.getCurrentTimestamp(), DateUtil
//                .formatTab[DateUtil.FMT_DATE_YYYYMMDD]));
//        sign.put("taskType", "0");
//        GoldTask signAward = getTaskMap(sign.get("taskType").toString());
//        sign.put("taskAward", "+" + signAward.getTaskAward() + "金币"); // TODO: 2017/12/29
//        sign.put("taskStatus", userSignService.checkUserSign(userId, DateUtil.formatDate(DateUtil.getCurrentTimestamp
//                (), DateUtil.formatTab[DateUtil.FMT_DATE_SPECIAL]), CommonUtil.getUserSignTypeByVersion(clientType,
//                versionCode)) ? 1 : 0);
//        sign.put("taskStatusText", userSignService.checkUserSign(userId, DateUtil.formatDate(DateUtil
//                .getCurrentTimestamp(), DateUtil.formatTab[DateUtil.FMT_DATE_SPECIAL]), CommonUtil
//                .getUserSignTypeByVersion(clientType, versionCode)) ? "已签到" : "未签到");
//        taskList.add(sign);
        // 杀号
        GamePeriod currentPeriod = PeriodRedis.getCurrentPeriod(GameCache.getGame(GameConstant.SSQ).getGameId());
        Map<String, Object> kill = new HashMap<>();
        GoldTask killAward = getTaskMap("1");
        kill.put("taskName", killAward.getTaskTimes() + "次杀号");
        kill.put("taskDate", currentPeriod.getPeriodId() + "期");
        kill.put("taskType", "1");
        kill.put("taskAward", "+" + killAward.getTaskAward() + "金币");
        kill.put("taskStatus", userSocialTaskAwardService.checkUserFinishTask(currentPeriod.getGameId(), currentPeriod
                .getPeriodId(), userId, 1) ? 1 : 0);
        kill.put("taskStatusText", userSocialTaskAwardService.checkUserFinishTask(currentPeriod.getGameId(),
                currentPeriod.getPeriodId(), userId, 1) ? "已完成" : "去杀号");

        taskList.add(kill);
        // 围号
        Map<String, Object> encircle = new HashMap<>();
        GoldTask encircleAward = getTaskMap("2");
        encircle.put("taskName", encircleAward.getTaskTimes() + "次围号");
        encircle.put("taskDate", currentPeriod.getPeriodId() + "期");
        encircle.put("taskType", "2");
        encircle.put("taskAward", "+" + encircleAward.getTaskAward() + "金币");
        encircle.put("taskStatus", userSocialTaskAwardService.checkUserFinishTask(currentPeriod.getGameId(),
                currentPeriod.getPeriodId(), userId, 2) ? 1 : 0);
        encircle.put("taskStatusText", userSocialTaskAwardService.checkUserFinishTask(currentPeriod.getGameId(),
                currentPeriod.getPeriodId(), userId, 2) ? "已完成" : "去围号");
        taskList.add(encircle);

        //日常任务
        List<Map> onceGoldTaskList = new ArrayList<>();
        String onceChangeHeadTask = GoldTask.TASK_TYPE_UPLOAD_HEAD_IMG;
        UserLoginVo userLoginVo = loginService.getUserLoginVo(userId);
        boolean changeHeadStatus = false;
        boolean changeNameStatus = false;
        if (userLoginVo != null && StringUtils.isNotBlank(userLoginVo.getHeadImgUrl())) {
            changeHeadStatus = !userLoginVo.getHeadImgUrl().equals(CommonConstant.DEFAULT_HEAD_IMG_URL);
            changeNameStatus = !userLoginVo.getNickName().equals(CommonUtil.getUserMoblieDefaultName(userLoginVo
                    .getMobile()));
        }

        Map<String, Object> changeHeadTask = convertGoldTask2Map(onceChangeHeadTask, userId, changeHeadStatus);
        if (changeHeadTask != null) {
            onceGoldTaskList.add(changeHeadTask);
        }
        String onceChangeNameTask = GoldTask.TASK_TYPE_MODIFY_NICK_NAME;
        Map<String, Object> changeNameTask = convertGoldTask2Map(onceChangeNameTask, userId, changeNameStatus);
        if (changeHeadTask != null) {
            onceGoldTaskList.add(changeNameTask);
        }

        List<Map<String, Object>> totalTaskList = new ArrayList<>();
        Map<String, Object> dailyTask = new HashMap<>();
        dailyTask.put("title", "日常任务");
        dailyTask.put("task", taskList);
        Map<String, Object> improveTask = new HashMap<>();
        improveTask.put("title", "完善任务");
        improveTask.put("task", onceGoldTaskList);
        totalTaskList.add(dailyTask);
        totalTaskList.add(improveTask);

        resultMap.put("taskList", taskList);
        resultMap.put("totalTaskList", totalTaskList);
        return resultMap;
    }

    private Map<String, Object> convertGoldTask2Map(String type, Long userId, boolean taskStatus) {
        Map<String, Object> res = new HashMap<>();
        String payId = userId + GoldTask.getTaskEn(type) + type;
        GoldTask task = getTaskMap(type);
        res.put("taskName", GoldTask.getTaskName(type));
        Timestamp taskDate = DateUtil.getCurrentTimestamp();
        UserAccountFlow userAccountFlow = userAccountFlowDao.getUserFlowCheck(payId, CommonConstant
                .PAY_TYPE_GOLD_COIN, userId, false);
        if (userAccountFlow != null && userAccountFlow.getCreateTime() != null) {
            taskDate = userAccountFlow.getCreateTime();
        }
        res.put("taskDate", DateUtil.formatDate(taskDate, DateUtil.formatTab[DateUtil.FMT_DATE_YYYYMMDD]));
        res.put("taskType", type);
        res.put("taskAward", "+" + task.getTaskAward() + "金币");
        res.put("taskStatus", taskStatus ? 1 : 0);
        res.put("taskStatusText", taskStatus ? "已完善" : "去完善");
        return res;
    }

    @Override
    public GoldTask getTaskMap(String type) {
        Map<String, Object> objectMap = JSONObject.parseObject(ActivityIniCache.getActivityIniValue
                (ActivityIniConstant.TASK_LIST_MAP));
        Map<String, Object> encircleAward = (Map<String, Object>) objectMap.get(type);
        GoldTask goldTask = new GoldTask((Integer) encircleAward.get("taskTimes"), (Integer) encircleAward.get
                ("taskAward"));
        return goldTask;
    }

    /*
     * 检查用户权限兑换
     * */
    @Override
    public Boolean checkUserAccess(Long userId, Long gameId, String periodId, Integer itemId) {
        UserAccess userAccess = userAccessDao.getUserAccess(userId, periodId, gameId);
        if (null != userAccess) {
            ExchangeMall exchangeMall = exchangeMallDao.getExchangeMall(itemId);
            List<String> userAccessList = Arrays.asList(userAccess.getAccessList().split(CommonConstant
                    .COMMA_SPLIT_STR));
            if (userAccessList.contains(String.valueOf(exchangeMall.getItemType()))) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    /*
     * 减少金币余额方法 事务
     * */
    @Override
    @Transactional
    public void operateAccountDec(Long userId, Long payAmount, Map<String, Object> payInfo, UserAccountFlow
            userAccountFlow, Integer payType) {
        UserAccount userAccount = getUserAccount(userId, payType, Boolean.TRUE);
        if (userAccount.getAccountBalance() < payAmount) {
            payInfo.put("payStatus", ResultConstant.PAY_FAILED_CODE);
            payInfo.put("payMsg", ResultConstant.PAY_GOLD_COIN_FAILED);
        } else {
            // 更新支付流水
            userAccountFlow.setPayTime(DateUtil.getCurrentTimestamp());
            userAccountFlow.setStatus(CommonConstant.PAY_STATUS_FINISH);
            userAccountFlow.setUpdateTime(DateUtil.getCurrentTimestamp());
            userAccountFlowDao.update(userAccountFlow);
            // 更新账户余额
            userAccount.setAccountBalance(userAccount.getAccountBalance() - payAmount);
            userAccount.setUpdateTime(DateUtil.getCurrentTimestamp());
            userAccountDao.update(userAccount);

            if (payType.equals(CommonConstant.ACCOUNT_TYPE_WISDOM_COIN)) {
                // 插入userWisdomCoin
                UserWisdomCoinFlow userWisdomCoinFlow = new UserWisdomCoinFlow();
                userWisdomCoinFlow.initConsumeUserWisdomFlow(CommonUtil.generateStrId(userId, CommonConstant
                                .WISDOM_COIN_FLOW_ID_SQE, wisdomFlowIdSeqDao), userId, userAccountFlow.getPayDesc(),
                        payAmount, userAccountFlow.getFlowId());
                userWisdomCoinFlowDao.insert(userWisdomCoinFlow);
            }
            // 通知金币回调(此处直接返回是否成功)
            payInfo.put("payStatus", ResultConstant.PAY_SUCCESS_CODE);
            payInfo.put("payMsg", ResultConstant.PAY_GOLD_COIN_SUCCESS);
        }
    }

    /*
     * 增加账户余额 事务
     * */
    @Override
    @Transactional
    public void operateAccountAdd(Long userId, Long payAmount, Map<String, Object> payInfo, UserAccountFlow
            userAccountFlow) {
        UserAccount userAccount = getUserAccount(userId, userAccountFlow.getPayType(), Boolean.TRUE);
        // 更新支付流水
        userAccountFlow.setPayTime(DateUtil.getCurrentTimestamp());
        userAccountFlow.setStatus(CommonConstant.PAY_STATUS_HANDLED);
        userAccountFlowDao.update(userAccountFlow);
        // 更新账户余额
        userAccount.setAccountBalance(userAccount.getAccountBalance() + payAmount);
        userAccount.setUpdateTime(DateUtil.getCurrentTimestamp());
        userAccountDao.update(userAccount);
        // 通知金币回调(此处直接返回是否成功)
        payInfo.put("payStatus", ResultConstant.PAY_SUCCESS_CODE);
        payInfo.put("payMsg", ResultConstant.PAY_GOLD_COIN_SUCCESS);
    }

    @Override
    public void setSelf(Object proxyBean) {
        self = (PayService) proxyBean;
    }
}
