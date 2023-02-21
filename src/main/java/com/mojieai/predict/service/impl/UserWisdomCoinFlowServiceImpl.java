package com.mojieai.predict.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.PayChannelInfoCache;
import com.mojieai.predict.cache.PayClientChannelCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.*;
import com.mojieai.predict.entity.bo.PaginationList;
import com.mojieai.predict.entity.dto.HttpParamDto;
import com.mojieai.predict.entity.po.*;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.ActivityService;
import com.mojieai.predict.service.CompatibleService;
import com.mojieai.predict.service.PayService;
import com.mojieai.predict.service.UserWisdomCoinFlowService;
import com.mojieai.predict.service.beanself.BeanSelfAware;
import com.mojieai.predict.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

@Service
public class UserWisdomCoinFlowServiceImpl implements UserWisdomCoinFlowService, BeanSelfAware {
    protected Logger log = LogConstant.commonLog;
    @Autowired
    private UserWisdomCoinFlowDao userWisdomCoinFlowDao;
    @Autowired
    private WisdomFlowsequenceIdSequenceDao wisdomFlowseqIdSeqDao;
    @Autowired
    private PayService payService;
    @Autowired
    private ExchangeMallDao exchangeMallDao;
    @Autowired
    private UserAccountDao userAccountDao;
    @Autowired
    private UserAccountFlowDao userAccountFlowDao;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private ActivityUserInfoDao activityUserInfoDao;
    @Autowired
    private ActivityInfoDao activityInfoDao;
    @Autowired
    private RedisService redisService;
    @Autowired
    private CompatibleService compatibleService;
    private UserWisdomCoinFlowService self;

    @Override
    public Map<String, Object> getUserWisdomCoinFlows(Long userId, Integer page) {
        Integer pageSize = 31;
        PaginationList<UserWisdomCoinFlow> userWisdomCoinFlowsPage = userWisdomCoinFlowDao.getUserWisdomCoinFlowsByPage
                (userId, pageSize, page + 1);
//        List<UserWisdomCoinFlow> userWisdomCoinFlowList = userWisdomCoinFlowsPage
        return null;
    }

    @Override
    public Map getWisdomPriceList(Long userId, Integer clientId, Integer versionCode) {
        Map<String, Object> res = new HashMap<>();
        UserAccount userAccount = userAccountDao.getUserAccountBalance(userId, CommonConstant.PAY_TYPE_WISDOM_COIN,
                false);
        Long userBalance = userAccount == null ? 0 : userAccount.getAccountBalance();

        List<ExchangeMall> exchangeMalls = getWisdomCoinExchangeMall(clientId, versionCode);
        List<Map<String, Object>> fillAmountList = packageWisdomCoinExchangeMall2ShowMap(exchangeMalls, clientId,
                versionCode);

        Map<String, Object> tags = null;
        if (getWisdomCoinActivityStatus(userId, clientId, versionCode)) {
            wisdomCoinPriceActivtityDiscount(fillAmountList);

            tags = new HashMap<>();
            tags.put("img", "http://sportsimg.mojieai.com/activity_wisdom_title_new.png");
            tags.put("ratio", "166:34");
        }

        res.putAll(PayUtil.getWeChatSubscription(clientId, versionCode));
        res.put("balanceText", "智慧币余额");
        res.put("paymentList", getWisdomCoinPayChannelList(userId, clientId, versionCode));
        res.put("fillAmountList", fillAmountList);
        res.put("balanceAmount", CommonUtil.convertFen2Yuan(userBalance).toString());
        res.put("tags", tags);
        return res;
    }

    @Override
    public Map<String, Object> getWxApiWisdomPriceList(Long userId, String wxCode) {
        Map<String, Object> result = new HashMap<>();
        Long userBalance = 0l;
        if (userId != null) {
            UserAccount userAccount = userAccountDao.getUserAccountBalance(userId, CommonConstant.PAY_TYPE_WISDOM_COIN,
                    false);
            userBalance = userAccount == null ? 0 : userAccount.getAccountBalance();
        }

        Integer clientId = CommonConstant.CLIENT_TYPE_IOS_WISDOM_ENTERPRISE;
        Integer versionCode = CommonConstant.VERSION_CODE_4_6_1;
        List<ExchangeMall> exchangeMalls = getWisdomCoinExchangeMall(clientId, versionCode);
        List<Map<String, Object>> fillAmountList = packageWisdomCoinExchangeMall2ShowMap(exchangeMalls, clientId,
                versionCode);


        String payClientChannelKey = String.valueOf(CommonConstant.CLIENT_TYPE_ANDRIOD) + CommonConstant
                .COMMON_COLON_STR + String.valueOf(CommonConstant.WX_PAY_CHANNEL_WX_JSAPI);
        PayClientChannel payClientChannel = PayClientChannelCache.getClientChannel(payClientChannelKey);
        String openId = CommonUtil.getWxOpenid(wxCode, payClientChannel);
        if (StringUtils.isNotBlank(openId)) {
            redisService.kryoSetEx(RedisConstant.getWxJSAPICodeOpenIdMapKey(wxCode), 1800, openId);
        }

        result.put("fillAmounts", fillAmountList);
        result.put("userBalance", CommonUtil.convertFen2Yuan(userBalance) + "");
        return result;
    }

    @Override
    public Long getOfflineAmount(Timestamp beginTime, Timestamp endTime) {
        return userWisdomCoinFlowDao.getUserWisdomCoinFlowSumByStatusByOtter(beginTime, endTime, UserAccountConstant
                .WISDOM_COIN_EXCHANGE_TYPE_PROGRAM_OUT_LINE_TRANSFER);
    }

    private boolean getWisdomCoinActivityStatus(Long userId, Integer clientId, Integer versionCode) {
        if (CommonUtil.getIosReview(versionCode, clientId).equals(CommonConstant.IOS_REVIEW_STATUS_WAIT)) {
            return false;
        }
        boolean activityPermission = false;
        Integer activityId = 201805002;

        if (ActivityUtils.wisdomActivityVersionControl(clientId, versionCode)) {
            ActivityUserInfo activityUserInfo = activityUserInfoDao.getUserTotalTimes(activityId, userId);
            if (activityService.checkActivityIsEnabled(activityId) && (activityUserInfo == null || (activityUserInfo
                    .getTotalTimes() != null && activityUserInfo.getTotalTimes() < 1))) {
                activityPermission = true;
            }
        }
        return activityPermission;
    }

    @Override
    public Map<String, Object> cashPurchaseWisdomCoin(Long userId, Integer payChannelId, Long amount, Long
            wisdomAmount, String clientIp, Integer clientId, Integer goodsId, Integer versionCode, Integer bankId) {
        return cashPurchaseWisdomCoin(userId, payChannelId, amount, wisdomAmount, clientIp, clientId, goodsId,
                versionCode, bankId, null);
    }

    @Override
    public Map<String, Object> cashPurchaseWisdomCoin(Long userId, Integer payChannelId, Long amount, Long
            wisdomAmount, String clientIp, Integer clientId, Integer goodsId, Integer versionCode, Integer bankId,
                                                      String wxCode) {
        Map<String, Object> result = new HashMap<>();
        ExchangeMall exchangeMall = exchangeMallDao.getExchangeMall(goodsId);
        //1.check活动
        boolean activityPermission = false;
        if (ActivityUtils.wisdomActivityVersionControl(clientId, versionCode)) {
            ActivityUserInfo activityUserInfo = activityUserInfoDao.getUserTotalTimes(201805002, userId);
            if (activityService.checkActivityIsEnabled(201805002) && (activityUserInfo == null || (activityUserInfo
                    .getTotalTimes() != null && activityUserInfo.getTotalTimes() == 0))) {
                activityPermission = true;
                if (activityUserInfo == null) {
                    activityUserInfo = new ActivityUserInfo(201805002, userId, 0, null, DateUtil.getCurrentTimestamp
                            (), DateUtil.getCurrentTimestamp());
                    activityUserInfoDao.insert(activityUserInfo);
                }
            }
        }

        ActivityInfo activityInfo = activityInfoDao.getActivityInfo(201805002);
        String activityDesc = "";
        if (activityPermission) {
            if (exchangeMall.getItemName().equals("其它金额")) {
                amount = ActivityUtils.getActivityWisdomPrice(activityInfo, amount);
            } else {
                amount = exchangeMall.getItemOriginPrice();
                wisdomAmount = ActivityUtils.getActivityWisdomCount(activityInfo, exchangeMall.getItemCount());
            }
            activityDesc = "活动";
        }

        //1.插入智慧币流水
        if (!activityPermission) {
            amount = PayUtil.randomDiscountPrice(amount, payChannelId);
        }
        String payDesc = activityDesc + "充值" + CommonUtil.getMoneyStr(amount, CommonConstant.PAY_TYPE_WISDOM_COIN) +
                CommonConstant.CASH_MONETARY_UNIT_YUAN;
        UserWisdomCoinFlow userWisdomCoinFlow = new UserWisdomCoinFlow();
        String flowId = CommonUtil.generateStrId(userId, CommonConstant.WISDOM_COIN_FLOW_ID_SQE, wisdomFlowseqIdSeqDao);
        userWisdomCoinFlow.initUserWisdomCoinFlow(flowId, userId, payDesc, UserAccountConstant
                .WISDOM_COIN_EXCHANGE_TYPE_CASH_PURCHASE, amount, wisdomAmount, goodsId);
        Integer resFlow = userWisdomCoinFlowDao.insert(userWisdomCoinFlow);

        try {
            if (resFlow > 0) {
                UserAccount userAccount = payService.getUserAccount(userId, CommonConstant.PAY_TYPE_WISDOM_COIN,
                        Boolean.FALSE);
                //2.生成交易流水
                Map<String, Object> payMap = payService.payCreateFlow(userId, flowId, amount, CommonConstant
                        .PAY_TYPE_CASH, payChannelId, amount, payDesc, clientIp, clientId, UserAccountConstant
                        .WISDOM_COIN_PURCHASE_CALL_BACK_METHOD, CommonConstant.PAY_OPERATE_TYPE_ADD, bankId, wxCode);
                //3.ios商品
                String iosMallGoodId = "";
                if (PayChannelInfoCache.getChannelInfo(payChannelId).getChannelName().equals(CommonConstant
                        .APPLY_PAY_NAME)) {
                    iosMallGoodId = exchangeMall.getIosMallId();
                }
                if (payMap != null) {
                    Integer opeRes = userWisdomCoinFlowDao.saveUserWisdomCoinAccountFlowId(userWisdomCoinFlow
                            .getFlowId(), userId, payMap.get("flowId").toString());
                    if (opeRes > 0) {
                        result.put("iosMallGoodId", iosMallGoodId);
                        result.put("flowId", payMap.get("flowId"));
                        result.putAll((Map<? extends String, ?>) payMap.get("payForToken"));
                        result.put("msg", "购买成功");
                    }
                }
            }
        } catch (Exception e) {
            result.put("msg", "购买失败");
            log.error("购买智慧币异常", e);
        }
        return result;
    }

    @Override
    public Map<String, Object> wxApiPurchaseWisdomCoin(Long userId, Long payAmount, Long wisdomCount, Integer itemId,
                                                       String wxCode) {
        return cashPurchaseWisdomCoin(userId, CommonConstant.WX_PAY_CHANNEL_WX_JSAPI, payAmount, wisdomCount,
                "127.0.0.1", CommonConstant.CLIENT_TYPE_ANDRIOD, itemId, CommonConstant.VERSION_CODE_4_6_1, null, wxCode);
    }

    @Override
    public Boolean callBackAddAccount(String businessFlowId, String exchangeFlowId) {
        Boolean res = Boolean.FALSE;
        //1.查询购买智慧币的交易流水是否已经处理
        Long userIdPart = Long.parseLong(String.valueOf(exchangeFlowId).substring(String.valueOf(exchangeFlowId)
                .length() - 2));
        UserWisdomCoinFlow userWisdomCoinFlow = userWisdomCoinFlowDao.getUserWisdomCoinFlowByFlowId(businessFlowId,
                userIdPart);
        if (userWisdomCoinFlow == null || !userWisdomCoinFlow.getIsPay().equals(CommonConstant.PAY_STATUS_START)) {
            return Boolean.TRUE;
        }
        //2.check记录金额和支付金额是否一致
        ExchangeMall exchangeMall = exchangeMallDao.getExchangeMall(userWisdomCoinFlow.getGoodsId());
        if (exchangeMall == null) {
            log.error("Wisdom Coin exchangeMall does not exisit. businessFlowId " + businessFlowId +
                    " exchangeFlowId" + exchangeFlowId);
            return res;
        }
        //3.创建账户增加流水
        Long preFix = Long.parseLong(exchangeFlowId.substring(exchangeFlowId.length() - 2));
        UserAccountFlow cashFlow = userAccountFlowDao.getUserFlowByShardType(exchangeFlowId, preFix, false);
//        Map<String, Object> payInfo = new HashMap<>();
//        UserAccountFlow wisdomFlow = userAccountFlowDao.getUserFlowCheck(cashFlow.getPayId(), cashFlow.getUserId(),
//                Boolean.TRUE);
//        if (null != wisdomFlow) {
//            payInfo.put("payStatus", ResultConstant.REPEAT_CODE);
//            payInfo.put("payMsg", ResultConstant.REPEAT_MSG);
//            return true;
//        }
        // 创建支付流水ID
        String flowId = payService.generateFlowId(cashFlow.getUserId());
        UserAccountFlow userAccountFlow = new UserAccountFlow(flowId, cashFlow.getUserId(), cashFlow.getPayId(),
                cashFlow.getTotalAmount(), CommonConstant.PAY_TYPE_WISDOM_COIN, cashFlow.getChannel(), cashFlow
                .getPayAmount(), cashFlow.getPayDesc(), null, null, null, CommonConstant
                .PAY_OPERATE_TYPE_ADD);
        userAccountFlow.setStatus(CommonConstant.PAY_STATUS_HANDLED);
//        if (exchangeMall.getItemPrice() != null && exchangeMall.getItemCount() != null) {
        //3.1check是否参加活动
        ActivityUserInfo activityUser = activityUserInfoDao.getUserTotalTimes(201805002, cashFlow.getUserId());
        if (checkPriceIsActivityPrice(activityUser, exchangeMall, userWisdomCoinFlow)) {
            ActivityInfo activityInfo = activityInfoDao.getActivityInfo(201805002);
            //活动价格check
            Long activityAmount = userWisdomCoinFlow.getExchangeWisdomAmount();
            if (exchangeMall.getItemCount() != null) {
                activityAmount = ActivityUtils.getActivityWisdomCount(activityInfo, exchangeMall.getItemCount());
            }

//                CommonUtil.multiply(exchangeMall.getItemCount() + "", "2").longValue();

            Long activityPrice = userWisdomCoinFlow.getExchangeAmount();
            if (exchangeMall.getItemOriginPrice() != null) {
                activityPrice = exchangeMall.getItemOriginPrice();
            }

//                Long activityPrice = ActivityUtils.getActivityWisdomPrice(activityInfo, exchangeMall.getItemPrice());
            if (!activityAmount.equals(userWisdomCoinFlow.getExchangeWisdomAmount()) ||
                    !activityPrice.equals(userWisdomCoinFlow.getExchangeAmount())) {
                log.error("Activity purchase Wisdom Coin goods price is not match order pls manulDeal ," +
                        "businessFlowId " + businessFlowId + " exchangeFlowId" + exchangeFlowId);
                return res;
            }
            //3.2更新用户活动已参与
            activityUser.setTotalTimes(2);
            activityUserInfoDao.update(activityUser);
        } else {
            //平常价格check
            if (!userAccountFlow.getChannel().equals(CommonConstant.HAO_DIAN_PAY_CHANNEL_ID)) {
                if (exchangeMall.getItemCount() == null || exchangeMall.getItemPrice() == null) {
                    if (!userWisdomCoinFlow.getExchangeAmount().equals(userWisdomCoinFlow.getExchangeWisdomAmount())) {
                        log.error("Wisdom Coin goods price is not match order pls manulDeal ,businessFlowId " + businessFlowId +
                                " exchangeFlowId" + exchangeFlowId);
                        return res;
                    }
                } else {
                    if (!exchangeMall.getItemCount().equals(userWisdomCoinFlow.getExchangeWisdomAmount()) ||
                            !exchangeMall.getItemPrice().equals(userWisdomCoinFlow.getExchangeAmount())) {
                        log.error("Wisdom Coin goods price is not match order pls manulDeal ,businessFlowId " + businessFlowId +
                                " exchangeFlowId" + exchangeFlowId);
                        return res;
                    }
                }
            }
        }
//        }

        //3.更新
        res = self.updateWisdomFlowAndAddAccount(userWisdomCoinFlow.getFlowId(), userWisdomCoinFlow.getUserId(),
                userWisdomCoinFlow.getExchangeWisdomAmount(), userAccountFlow);
        return res;
    }

    private Boolean checkPriceIsActivityPrice(ActivityUserInfo activityUser, ExchangeMall exchangeMall,
                                              UserWisdomCoinFlow userWisdomCoinFlow) {
        Boolean result = Boolean.FALSE;
        if (activityUser == null) {
            return result;
        }

        if (!activityService.checkActivityIsEnabled(activityUser.getActivityId())) {
            return result;
        }

        if (activityService.checkUserTakepartActivity(activityUser.getUserId(), activityUser.getActivityId(), -1)) {
            return result;
        }

        if (exchangeMall.getItemCount() == null) {
            if (!userWisdomCoinFlow.getExchangeAmount().equals(userWisdomCoinFlow.getExchangeWisdomAmount())) {
                result = Boolean.TRUE;
            }
        } else {
            if (!exchangeMall.getItemCount().equals(userWisdomCoinFlow.getExchangeWisdomAmount())) {
                result = Boolean.TRUE;
            }
        }

        return result;
    }

    @Override
    @Transactional
    public Boolean updateWisdomFlowAndAddAccount(String wisdomFlowId, Long userId, Long wisdomAmount, UserAccountFlow
            userAccountFlow) {
        Boolean res = Boolean.FALSE;
        UserAccount account = userAccountDao.getUserAccountBalance(userId, CommonConstant.PAY_TYPE_WISDOM_COIN, true);
        userAccountFlow.setPayAmount(wisdomAmount);
        userAccountFlowDao.insert(userAccountFlow);
        Long balance = account.getAccountBalance() == null ? 0 : account.getAccountBalance();
        balance += wisdomAmount;
        account.setAccountBalance(balance);
        int tempRes = userAccountDao.update(account);
        if (tempRes > 0) {
            int tempUpdateRes = userWisdomCoinFlowDao.updateUserWisdomFlowIsPay(wisdomFlowId, userId,
                    UserAccountConstant.IS_PAIED_YES);
            if (tempUpdateRes > 0) {
                res = Boolean.TRUE;
            }
        }
        return res;
    }

    @Override
    public void setSelf(Object proxyBean) {
        self = (UserWisdomCoinFlowService) proxyBean;
    }

    private void wisdomCoinPriceActivtityDiscount(List<Map<String, Object>> fillAmountList) {
        Integer activityId = 201805002;
        ActivityInfo activityInfo = activityInfoDao.getActivityInfo(activityId);
        for (Map<String, Object> fillAmount : fillAmountList) {
            Integer goodsId = Integer.valueOf(fillAmount.get("fillAmountId").toString());
            ExchangeMall exchangeMall = exchangeMallDao.getExchangeMall(goodsId);
            if (exchangeMall != null && exchangeMall.getItemPrice() != null) {

                fillAmount.put("cashText", CommonConstant.COMMON_YUAN_STR + CommonUtil.removeZeroAfterPoint(CommonUtil
                        .convertFen2Yuan(exchangeMall.getItemOriginPrice()).toString()));
                fillAmount.put("fillAmountIcon", "");
                fillAmount.put("originCashText", "");
                fillAmount.put("fillAmount", CommonUtil.divide(exchangeMall.getItemOriginPrice() + "", "100", 0));

                Long itemCount = ActivityUtils.getActivityWisdomCount(activityInfo, exchangeMall.getItemCount());
                fillAmount.put("fillWisdomAmount", CommonUtil.divide(itemCount + "", "100", 0));

                fillAmount.put("balanceText", CommonUtil.removeZeroAfterPoint(CommonUtil.convertFen2Yuan(itemCount)
                        .toString()) + CommonConstant.GOLD_WISDOM_COIN_MONETARY_UNIT);
            }
        }
    }

    private List<Map<String, Object>> getWisdomCoinPayChannelList(Long userId, Integer clientId, Integer versionCode) {
        List<Map<String, Object>> paymentList = new ArrayList<>();
        List<Map<String, Object>> payments = new ArrayList<>();
        Map<String, Object> payment = payService.getChannelList(userId, clientId, versionCode);
        if (payment != null && payment.containsKey("paymentList")) {
            payments = (List<Map<String, Object>>) payment.get("paymentList");
            for (Map<String, Object> o : payments) {
                Integer channelId = Integer.valueOf(o.get("channelId").toString());
                if (o.get("status").toString().equals("0")) {
                    String img = PayUtil.getDisableChannelIcon(channelId, versionCode);
                    if (StringUtils.isNotBlank(img)) {
                        o.put("channelIcon", img);
                    }
                }
                o.put("tags", PayUtil.getPayChannelNotEnoughTags(channelId));
                if (!CommonConstant.WISDOM_COIN_CHANNEL_ID.equals(channelId)) {
                    paymentList.add(o);
                }
            }
        }
        compatibleService.setDefaultChannel(userId, paymentList, 0l);
        PayUtil.sortedPaymentList(paymentList);
        return paymentList;
    }

    private List<Map<String, Object>> packageWisdomCoinExchangeMall2ShowMap(List<ExchangeMall> exchangeMalls, Integer
            clientId, Integer versionCode) {
        if (exchangeMalls == null || exchangeMalls.size() == 0) {
            return null;
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (ExchangeMall exchangeMall : exchangeMalls) {
            Map<String, Object> temp = new HashMap<>();
            temp = UserAccountUtil.convertExchangeMall2WisdomMap(exchangeMall, clientId, versionCode);
            result.add(temp);
        }

        if (result.size() != 0) {
            Collections.sort(result, new Comparator<Map<String, Object>>() {
                @Override
                public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                    Integer weight1 = Integer.valueOf(o1.get("weight").toString());
                    Integer weight2 = Integer.valueOf(o2.get("weight").toString());
                    return weight2.compareTo(weight1);
                }
            });
        }
        return result;
    }

    private List<ExchangeMall> getWisdomCoinExchangeMall(Integer clientId, Integer versionCode) {
        Integer itemType = ExchangeMallConstant.EXCHANGE_MALL_ITEM_TYPE_WISDOM_COIN;
        if (CommonUtil.getIosReview(versionCode, clientId).equals(CommonConstant.IOS_REVIEW_STATUS_WAIT)) {
            itemType = ExchangeMallConstant.EXCHANGE_MALL_ITEM_TYPE_IOS_REVIEW_WISDOM_COIN;
        }

        return exchangeMallDao.getExchangeMallList(itemType, null, clientId);
    }
}
