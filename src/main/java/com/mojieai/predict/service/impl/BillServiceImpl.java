package com.mojieai.predict.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.cache.PayClientChannelCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.*;
import com.mojieai.predict.entity.bo.PaginationList;
import com.mojieai.predict.entity.bo.ThirdPartBillOrderInfo;
import com.mojieai.predict.entity.po.*;
import com.mojieai.predict.entity.vo.ChannelBillVo;
import com.mojieai.predict.entity.vo.ProductBillVo;
import com.mojieai.predict.entity.vo.UserLoginVo;
import com.mojieai.predict.enums.BillEnum;
import com.mojieai.predict.enums.PayChannelEnum;
import com.mojieai.predict.enums.WithdrawEnum;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.*;
import com.mojieai.predict.util.*;
import com.mojieai.predict.util.JDBill.JDBillUtil;
import com.mojieai.predict.util.yopBill.YopBillUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

/**
 * 对账
 */
@Service
public class BillServiceImpl implements BillService {
    protected Logger log = LogConstant.commonLog;
    @Autowired
    private UserAccountFlowDao userAccountFlowDao;
    @Autowired
    private LoginService loginService;
    @Autowired
    private ProgramSaleStatsDao programSaleStatsDao;
    @Autowired
    private UserProgramDao userProgramDao;
    @Autowired
    private ProgramDao programDao;
    @Autowired
    private VipMemberService vipMemberService;
    @Autowired
    private UserDeviceInfoDao userDeviceInfoDao;
    @Autowired
    private UserStatisticTableDao userStatisticTableDao;
    @Autowired
    private UserInfoDao userInfoDao;
    @Autowired
    private UserStatisticTableDayDao userStatisticTableDayDao;
    @Autowired
    private UserActiveDao userActiveDao;
    @Autowired
    private OrderStatisticReportDao orderStatisticReportDao;
    @Autowired
    private UserDeviceWeekReportDao userDeviceWeekReportDao;
    @Autowired
    private UserDeviceMonthReportDao userDeviceMonthReportDao;
    @Autowired
    private ThirdPartyBillInfoDao thirdPartyBillInfoDao;
    @Autowired
    private RedisService redisService;
    @Autowired
    private ThirdPartyBillInfoService thirdPartyBillInfoService;
    @Autowired
    private UserWithdrawFlowDao userWithdrawFlowDao;
    @Autowired
    private UserBuyRecommendService userBuyRecommendService;
    @Autowired
    private UserWisdomCoinFlowService userWisdomCoinFlowService;

    /* 获取指定时间的资金日报*/
    @Override
    public Map<String, Object> getDailyBill(Timestamp beginTime, Timestamp endTime) {
        Map<String, Object> result = new HashMap<>();
        List<ChannelBillVo> channelReportFrom = new ArrayList<>();
        List<ProductBillVo> productReportFrom = new ArrayList<>();
        //1.根据支付渠道统计报表
        Map<String, Object> channelReportMap = new HashMap<>();
        Map<String, Map<String, Long>> productReportMap = new HashMap<>();
        for (int i = 0; i < 100; i++) {
            List<Map> tempSum = getSumAmountByChannelId(i, beginTime, endTime);
            if (tempSum == null || tempSum.size() <= 0) {
                continue;
            }
            for (Map channlMap : tempSum) {
                if (channlMap != null) {
                    if (!channelReportMap.containsKey(channlMap.get("channelid").toString())) {
                        channelReportMap.put(channlMap.get("channelid").toString(), channlMap.get("totalamount"));
                    } else {
                        Long totalAmount = Long.valueOf(channelReportMap.get(channlMap.get("channelid").toString())
                                .toString());
                        totalAmount = totalAmount + Long.valueOf(channlMap.get("totalamount").toString());
                        channelReportMap.put(channlMap.get("channelid").toString(), totalAmount);
                    }
                }
            }
            //1.2统计不同产品支付的钱
            List<UserAccountFlow> userAccountFlows = userAccountFlowDao.getUserFlowByPayType(i * 1l, CommonConstant
                    .PAY_TYPE_CASH, CommonConstant.PAY_STATUS_HANDLED, beginTime, endTime);
            analysisProduct(userAccountFlows, productReportMap);

        }

        Long offlineMoney = userWisdomCoinFlowService.getOfflineAmount(beginTime, endTime);
        String offlineMoneyStr = "";
        if (offlineMoney != null) {
            offlineMoneyStr = CommonUtil.convertFen2Yuan(offlineMoney).toString();
        }

        //2.转换渠道
        if (channelReportMap.isEmpty()) {
            channelReportMap.put(PayChannelEnum.WX.getChannel() + "", 0);
        }
        Map<Integer, ThirdPartBillOrderInfo> thirdPartChannelInfo = thirdPartyBillInfoService
                .getThirdPartySumIntervalTime(beginTime, endTime);
        for (String key : channelReportMap.keySet()) {
            BigDecimal testDbVipMoney = BillUtil.getTestDbDate(beginTime, endTime, Integer.valueOf(key));//截止到18-1月23
            // 测试库支付了0.12分
            ChannelBillVo channelBillVo = BillUtil.getChannelReportVo(channelReportMap, key, testDbVipMoney, thirdPartChannelInfo);
            channelReportFrom.add(channelBillVo);
        }

        //添加线下渠道
        if (offlineMoney != null && offlineMoney > 0l) {
            ChannelBillVo channelBillVo = new ChannelBillVo();
            channelBillVo.setGroupName("智慧彩票");
            channelBillVo.setChannelName("线下支付");
            channelBillVo.setOff_poundage("0");
            channelBillVo.setOff_amount(offlineMoneyStr);
            channelBillVo.setOrderCharge(offlineMoneyStr);
            channelReportFrom.add(channelBillVo);
        }

        //3.产品收入
        Map<Integer, Map<String, Long>> testAmountWithOutTestDb = getTestMoney(beginTime, endTime, false);
        for (String key : productReportMap.keySet()) {
            ProductBillVo productBillVo = BillUtil.getProductReportVo(productReportMap, key, testAmountWithOutTestDb);
            if (key.equals(CommonConstant.BILL_PRODUCT_TYPE_WISDOM + "")) {
                //智慧币添加线下渠道
                productBillVo.setOffLineAmount(offlineMoneyStr);
                productBillVo.setOrderAmount(CommonUtil.addByBigDecimal(productBillVo.getOrderAmount(), offlineMoneyStr));
            }
            productReportFrom.add(productBillVo);
        }

        Map<Integer, Map<String, Long>> testAmount = getTestMoney(beginTime, endTime, true);
        //4.添加测试收入
        Long testMoney = 0l;
        Long wxTestMoney = 0l;
        Long wx2TestMoney = 0l;
        Long wx3TestMoney = 0l;
        Long aliTestMoney = 0l;
        Long appleTestMoney = 0l;
        Long jdTestMoney = 0l;
        Long yibaoTestMoney = 0l;
        Long haodianTestMoney = 0l;
        for (Map<String, Long> testVal : testAmount.values()) {
            Long testTotal = testVal.get("total") == null ? 0l : testVal.get("total");
            Long testWxTotal = testVal.get(CommonConstant.WX_PAY_CHANNEL_ID + "") == null ? 0l : testVal.get
                    (CommonConstant.WX_PAY_CHANNEL_ID + "");
            Long testWx2Total = testVal.get(CommonConstant.WX_PAY_CHANNEL_ID_1 + "") == null ? 0l : testVal.get
                    (CommonConstant.WX_PAY_CHANNEL_ID_1 + "");
            Long testWx3Total = testVal.get(CommonConstant.WX_PAY_CHANNEL_WX_JSAPI + "") == null ? 0l : testVal.get
                    (CommonConstant.WX_PAY_CHANNEL_WX_JSAPI + "");
            Long testAliTotal = testVal.get(CommonConstant.ALI_PAY_CHANNEL_ID + "") == null ? 0l : testVal.get
                    (CommonConstant.ALI_PAY_CHANNEL_ID + "");
            Long testAppleTotal = testVal.get(CommonConstant.APPLE_PAY_CHANNEL_ID + "") == null ? 0l : testVal.get
                    (CommonConstant.APPLE_PAY_CHANNEL_ID + "");
            Long testJdTotal = testVal.get(CommonConstant.JD_PAY_CHANNEL_ID + "") == null ? 0l : testVal.get
                    (CommonConstant.JD_PAY_CHANNEL_ID + "");
            Long testYiBaoTotal = testVal.get(CommonConstant.YOP_PAY_CHANNEL_ID + "") == null ? 0l : testVal.get
                    (CommonConstant.YOP_PAY_CHANNEL_ID + "");
            Long testHaoDianTotal = testVal.get(CommonConstant.HAO_DIAN_PAY_CHANNEL_ID + "") == null ? 0l : testVal.get
                    (CommonConstant.HAO_DIAN_PAY_CHANNEL_ID + "");
            testMoney += testTotal;
            wxTestMoney += testWxTotal;
            wx2TestMoney += testWx2Total;
            wx3TestMoney += testWx3Total;
            aliTestMoney += testAliTotal;
            appleTestMoney += testAppleTotal;
            jdTestMoney += testJdTotal;
            yibaoTestMoney += testYiBaoTotal;
            haodianTestMoney += testHaoDianTotal;
        }
        productReportFrom.add(new ProductBillVo("测试收入", CommonUtil.convertFen2Yuan(testMoney).toString(), CommonUtil
                .convertFen2Yuan(wxTestMoney).toString(), CommonUtil.convertFen2Yuan(aliTestMoney).toString(),
                CommonUtil.convertFen2Yuan(appleTestMoney).toString(), CommonUtil.convertFen2Yuan(wx2TestMoney)
                .toString(), CommonUtil.convertFen2Yuan(wx3TestMoney).toString(), CommonUtil.convertFen2Yuan
                (jdTestMoney).toString(), CommonUtil.convertFen2Yuan(yibaoTestMoney).toString(), "0",
                CommonUtil.convertFen2Yuan(haodianTestMoney).toString(), 10));

        productReportFrom.sort(new Comparator<ProductBillVo>() {
            @Override
            public int compare(ProductBillVo o1, ProductBillVo o2) {
                return o1.getWeight().compareTo(o2.getWeight());
            }
        });

        result.put("channelReportFrom", channelReportFrom);
        result.put("productReportFrom", productReportFrom);
        return result;
    }

    private List<Map> getSumAmountByChannelId(int i, Timestamp beginTime, Timestamp endTime) {
        List<Map> result = new ArrayList<>();
        Timestamp tempTime = DateUtil.formatString("2018-08-30 21:52:00", "yyyy-MM-dd HH:mm:ss");
        if (DateUtil.getDiffMinutes(beginTime, tempTime) > 0l && DateUtil.getDiffMinutes(tempTime, endTime) > 0l) {
            List<Map> wxOld = userAccountFlowDao.getSumAmountByChannelId(i * 1l, beginTime, tempTime);
            List<Map> wxNew = userAccountFlowDao.getSumAmountByChannelId(i * 1l, tempTime, endTime);
            if (wxOld == null) {
                wxOld = new ArrayList<>();
            }
            if (wxNew != null && wxNew.size() > 0) {
                for (Map temp : wxNew) {
                    if (temp.containsKey("channelid")) {
                        String channelId = temp.get("channelid").toString();
                        if (channelId.equals(CommonConstant.WX_PAY_CHANNEL_ID + "")) {
                            temp.put("channelid", CommonConstant.WX_PAY_CHANNEL_ID_1);
                        }
                    }
                }

                for (Map newMap : wxNew) {
                    String newChannelId = newMap.get("channelid").toString();
                    String newAmount = newMap.get("totalamount").toString();
                    Boolean excludeFlag = Boolean.TRUE;
                    if (wxOld.size() == 0) {
                        wxOld.addAll(wxNew);
                        break;
                    }
                    int oldLength = wxOld.size();
                    for (int j = 0; j < oldLength; j++) {
                        Map oldMap = wxOld.get(j);
                        String oldChannelId = oldMap.get("channelid").toString();
                        String oldAmount = oldMap.get("totalamount").toString();
                        if (oldChannelId.equals(newChannelId)) {
                            excludeFlag = Boolean.FALSE;
                            String sum = CommonUtil.addByBigDecimal(oldAmount, newAmount);
                            oldMap.put("totalamount", sum);
                        }
                        if ((j == oldLength - 1) && excludeFlag) {
                            wxOld.add(newMap);
                        }
                    }
                }

                result.addAll(wxOld);
            } else {
                result = wxOld;
            }
        } else {
            result = userAccountFlowDao.getSumAmountByChannelId(i * 1l, beginTime, endTime);
            if (DateUtil.getDiffMinutes(tempTime, beginTime) > 0) {
                for (Map temp : result) {
                    if (temp.containsKey("channelid")) {
                        String channelId = temp.get("channelid").toString();
                        if (channelId.equals(CommonConstant.WX_PAY_CHANNEL_ID + "")) {
                            temp.put("channelid", CommonConstant.WX_PAY_CHANNEL_ID_1);
                        }
                    }
                }
            }
        }

        return result;
    }

    private void analysisProduct(List<UserAccountFlow> userAccountFlows, Map<String, Map<String, Long>>
            productReportMap) {
        if (userAccountFlows == null || userAccountFlows.size() == 0) {
            return;
        }
        Timestamp tempTime = DateUtil.formatString("2018-08-30 21:52:00", "yyyy-MM-dd HH:mm:ss");
        for (UserAccountFlow flow : userAccountFlows) {
            String method = BillUtil.getCallBackMethodStr(flow.getRemark());
            BillEnum billEnum = BillEnum.getBillEnumByCallBackMethod(method);
            if (billEnum == null) {
                log.error("对账分析异常,productType不存在" + flow.getRemark());
                continue;
            }
            if (billEnum.getProductType().equals(CommonConstant.BILL_PRODUCT_TYPE_FOOTBALL_RECOMMEND)) {
                boolean manualOperate = false;
                if (DateUtil.compareDate(flow.getPayTime(), DateUtil.formatString("2018-10-11 00:00:00", "yyyy-MM-dd " +
                        "HH:mm:ss"))) {
                    manualOperate = true;
                }
                if (userBuyRecommendService.checkUserByProgramIsRobot(flow.getUserId(), flow.getPayId()) || manualOperate) {
                    billEnum = BillEnum.getBillEnumByProductType(CommonConstant
                            .BILL_PRODUCT_TYPE_ROBOT_FOOTBALL_RECOMMEND);
                } else {
                    divideUserIncome(productReportMap, flow, tempTime);
                    continue;
                }
            }
            Integer productType = billEnum.getProductType();
            Integer channel = flow.getChannel();
            if (DateUtil.getDiffMinutes(tempTime, flow.getPayTime()) >= 0 && channel.equals(CommonConstant.WX_PAY_CHANNEL_ID)) {
                channel = CommonConstant.WX_PAY_CHANNEL_ID_1;
            }
            Map<String, Long> productChannelAmountMap = null;
            if (!productReportMap.containsKey(productType + "")) {
                productChannelAmountMap = new HashMap<>();
            } else {
                productChannelAmountMap = productReportMap.get(productType + "");
            }
            Long channelAmount = productChannelAmountMap.get(channel + "") == null ? 0l : productChannelAmountMap.get
                    (channel + "");
            Long totalAmount = productChannelAmountMap.get("total") == null ? 0l : productChannelAmountMap.get("total");
            totalAmount = totalAmount + flow.getPayAmount();
            channelAmount = channelAmount + flow.getPayAmount();

            productChannelAmountMap.put("total", totalAmount);
            productChannelAmountMap.put(channel + "", channelAmount);
            productReportMap.put(productType + "", productChannelAmountMap);
        }
    }

    private void divideUserIncome(Map<String, Map<String, Long>> productReportMap, UserAccountFlow flow, Timestamp
            tempTime) {
        BillEnum plateEm = BillEnum.getBillEnumByProductType(CommonConstant.BILL_PRODUCT_TYPE_USER_FOOTBALL_RECOMMEND);
        BillEnum userEm = BillEnum.getBillEnumByProductType(CommonConstant
                .BILL_PRODUCT_TYPE_USER_FOOTBALL_RECOMMEND_INCOME);
        Integer plateProductType = plateEm.getProductType();
        Integer userProductType = userEm.getProductType();

        Long totalAmount = flow.getPayAmount();
        if (totalAmount == null) {
            return;
        }

        Integer ratio = ActivityIniCache.getActivityIniIntValue(ActivityIniConstant.FOOTBALL_WITHDRAW_OCCUPY_RATIO,
                SportsProgramConstant.SPORT_WITHDRAW_DEFAULT_OCCUPY_RATIO);
        if (DateUtil.compareDate(flow.getPayTime(), DateUtil.formatString("2018-10-11 00:00:00", "yyyy-MM-dd " +
                "HH:mm:ss"))) {
            ratio = 0;
        }
        Long userDivideAmount = Long.valueOf(CommonUtil.divide(CommonUtil.multiply(ratio + "", totalAmount + "")
                .toString(), "100", 0));
        Long plateDivideAmount = CommonUtil.subtract(totalAmount + "", userDivideAmount + "").longValue();

        Integer channel = flow.getChannel();
        if (DateUtil.getDiffMinutes(tempTime, flow.getPayTime()) >= 0 && channel.equals(CommonConstant.WX_PAY_CHANNEL_ID)) {
            channel = CommonConstant.WX_PAY_CHANNEL_ID_1;
        }
        Map<String, Long> plateProductChannelAmountMap = null;
        if (!productReportMap.containsKey(plateProductType + "")) {
            plateProductChannelAmountMap = new HashMap<>();
        } else {
            plateProductChannelAmountMap = productReportMap.get(plateProductType + "");
        }
        Long plateChannelAmount = plateProductChannelAmountMap.get(channel + "") == null ? 0l :
                plateProductChannelAmountMap.get(channel + "");
        Long plateTotalAmount = plateProductChannelAmountMap.get("total") == null ? 0l : plateProductChannelAmountMap
                .get("total");

        plateTotalAmount = plateTotalAmount + plateDivideAmount;
        plateChannelAmount = plateChannelAmount + plateDivideAmount;

        plateProductChannelAmountMap.put("total", plateTotalAmount);
        plateProductChannelAmountMap.put(channel + "", plateChannelAmount);
        productReportMap.put(plateProductType + "", plateProductChannelAmountMap);


        Map<String, Long> userProductChannelAmountMap = null;
        if (!productReportMap.containsKey(userProductType + "")) {
            userProductChannelAmountMap = new HashMap<>();
        } else {
            userProductChannelAmountMap = productReportMap.get(userProductType + "");
        }
        Long userChannelAmount = userProductChannelAmountMap.get(channel + "") == null ? 0l :
                userProductChannelAmountMap.get(channel + "");
        Long userTotalAmount = userProductChannelAmountMap.get("total") == null ? 0l : userProductChannelAmountMap
                .get("total");

        userTotalAmount = userTotalAmount + userDivideAmount;
        userChannelAmount = userChannelAmount + userDivideAmount;

        userProductChannelAmountMap.put("total", userTotalAmount);
        userProductChannelAmountMap.put(channel + "", userChannelAmount);
        productReportMap.put(userProductType + "", userProductChannelAmountMap);
    }

    /**
     * @param beginTime
     * @param endTime
     * @return
     */
    private Map<Integer, Map<String, Long>> getTestMoney(Timestamp beginTime, Timestamp endTime, boolean
            containTestDb) {
        if (endTime == null) {
            return null;
        }
        Map<Integer, Map<String, Long>> testMoneyMap = new HashMap<>();
        //1.从主库中查询测试钱
        //1.1 ini查询参与测试的人
        List<Long> ourUserId = new ArrayList<>();
        String ourMobile = ActivityIniCache.getActivityIniValue(ActivityIniConstant.OUR_PEOPLE_PHONE_NUM, "136");
        if (StringUtils.isNotBlank(ourMobile)) {
            String[] phoneNums = ourMobile.split(CommonConstant.COMMA_SPLIT_STR);
            for (String moblie : phoneNums) {
                UserLoginVo userLoginVo = loginService.getUserLoginVo(moblie, null, null);
                ourUserId.add(userLoginVo.getUserId());
            }
        }
        //2.查询对应人购买测试总额
        for (Long userId : ourUserId) {
            List<UserAccountFlow> flows = userAccountFlowDao.getTestUserFlow(userId, beginTime, endTime);
            if (flows == null || flows.size() == 0) {
                continue;
            }
            for (UserAccountFlow flow : flows) {
                String method = BillUtil.getCallBackMethodStr(flow.getRemark());
                BillEnum billEnum = BillEnum.getBillEnumByCallBackMethod(method);
                if (billEnum == null) {
                    continue;
                }
                Integer key = billEnum.getProductType();
                Map<String, Long> testChannelAmountMap = null;

                if (testMoneyMap.containsKey(key)) {
                    testChannelAmountMap = testMoneyMap.get(key);
                } else {
                    testChannelAmountMap = new HashMap<>();
                }
                long sum = testChannelAmountMap.get("total") == null ? 0l : testChannelAmountMap.get("total");
                long sumChannel = testChannelAmountMap.get(flow.getChannel() + "") == null ? 0l :
                        testChannelAmountMap.get(flow.getChannel() + "");
                sum = sum + flow.getPayAmount();
                sumChannel = sumChannel + flow.getPayAmount();
                testChannelAmountMap.put("total", sum);
                testChannelAmountMap.put(flow.getChannel() + "", sumChannel);
                testMoneyMap.put(key, testChannelAmountMap);
            }
        }

        if (containTestDb) {
            //3.vip在测试环境中存在金额，要加上  截止到18-1月23 测试库vip支付了0.12分
            BigDecimal wxTestDbMoney = BillUtil.getTestDbDate(beginTime, endTime, CommonConstant.WX_PAY_CHANNEL_ID);
            BigDecimal aliTestDbMoney = BillUtil.getTestDbDate(beginTime, endTime, CommonConstant.ALI_PAY_CHANNEL_ID);
            Map<String, Long> productMap = testMoneyMap.get(CommonConstant.BILL_PRODUCT_TYPE_VIP);
            if (productMap == null) {
                productMap = new HashMap<>();
            }

            long wxChannelAmount = productMap.get(CommonConstant.WX_PAY_CHANNEL_ID + "") == null ? 0 : productMap.get
                    (CommonConstant.WX_PAY_CHANNEL_ID + "");
            long originAmount = productMap.get("total") == null ? 0 : productMap.get("total");
            long aliChannelAmount = productMap.get(CommonConstant.ALI_PAY_CHANNEL_ID + "") == null ? 0 : productMap.get
                    (CommonConstant.ALI_PAY_CHANNEL_ID + "");

            BigDecimal wxAmount = wxTestDbMoney.add(new BigDecimal(wxChannelAmount));
            originAmount = wxTestDbMoney.add(new BigDecimal(originAmount)).longValue();

            productMap.put(CommonConstant.WX_PAY_CHANNEL_ID + "", wxAmount.longValue());

            //4.买方案支付宝花了2分钱
            BigDecimal aliAmount = aliTestDbMoney.add(new BigDecimal(aliChannelAmount));
            originAmount = aliTestDbMoney.add(new BigDecimal(originAmount)).longValue();

            productMap.put(CommonConstant.ALI_PAY_CHANNEL_ID + "", aliAmount.longValue());
            productMap.put("total", originAmount);

            testMoneyMap.put(CommonConstant.BILL_PRODUCT_TYPE_VIP, productMap);
        }

        return testMoneyMap;
    }

    @Override
    public String wxReconciliation(String billDate) {
        String wxReconciliationUrl = "https://api.mch.weixin.qq.com/pay/downloadbill";
        String payClientChannelKey = String.valueOf(1001) + CommonConstant.COMMON_COLON_STR + String.valueOf(1001);
        PayClientChannel payClientChannel = PayClientChannelCache.getClientChannel(payClientChannelKey);
        Map<String, String> payKeyStr = (Map<String, String>) JSONObject.parse(payClientChannel.getPayKeyStr());
        String appid = payKeyStr.get(IniConstant.WX_PAY_APP_ID); //微信开放平台审核通过的应用APPID
        String mch_id = payKeyStr.get(IniConstant.WX_PAY_MCH_ID); //微信支付分配的商户号
        String key = payKeyStr.get(IniConstant.WX_PAY_KEY);

        SortedMap<String, String> params = new TreeMap<>();
        params.put("appid", appid);
        params.put("bill_date", billDate);
        params.put("bill_type", "ALL");
        params.put("mch_id", mch_id);
        params.put("nonce_str", PayUtil.wxNonceStr());
        String sign = PayUtil.wxSignStr("UTF-8", params, key);
        params.put("sign", sign);

        String paramsXML = PayUtil.getRequestXml(params);
        String reponseStr = HttpServiceUtils.sendHttpsPostRequest(wxReconciliationUrl, paramsXML, "UTF-8");
        String res = reponseStr.substring(reponseStr.lastIndexOf("总交易单数"));
        return res;
    }

    @Override
    public void programSaleDailyStats() {
        // 今天的方案售卖情况
        // 获取到今天的售卖流水
        Integer orderDate = Integer.valueOf(DateUtil.getYesterday("yyyyMMdd").toString());

        List<ProgramSaleStats> programSaleStatss = programSaleStatsDao.getStatsByDate(orderDate, orderDate);
        if (null != programSaleStatss && programSaleStatss.size() > 0) {
            return;
        }
        Game game = GameCache.getGame(GameConstant.SSQ);
        String key = game.getGameEn() + ActivityIniConstant.PROGRAM_PRICE_AFTER;
        String programPriceStr = ActivityIniCache.getActivityIniValue(key);
        Map<String, Map<String, Object>> programPrice = JSONObject.parseObject(programPriceStr, HashMap.class);
        Map<String, Object> p150DiscountInfo = programPrice.get("0" + "_" + "0");
        String p150Price = p150DiscountInfo.get("price").toString();
        Map<String, Object> p151DiscountInfo = programPrice.get("0" + "_" + "1");
        String p151Price = p151DiscountInfo.get("price").toString();
        Map<String, Object> p152DiscountInfo = programPrice.get("0" + "_" + "2");
        String p152Price = p152DiscountInfo.get("price").toString();
        Map<String, Object> p120DiscountInfo = programPrice.get("1" + "_" + "0");
        String p120Price = p120DiscountInfo.get("price").toString();
        Map<String, Object> p121DiscountInfo = programPrice.get("1" + "_" + "1");
        String p121Price = p121DiscountInfo.get("price").toString();
        Map<String, Object> p122DiscountInfo = programPrice.get("1" + "_" + "2");
        String p122Price = p122DiscountInfo.get("price").toString();
        Map<String, Object> p90DiscountInfo = programPrice.get("2" + "_" + "0");
        String p90Price = p90DiscountInfo.get("price").toString();
        Map<String, Object> p91DiscountInfo = programPrice.get("2" + "_" + "1");
        String p91Price = p91DiscountInfo.get("price").toString();
        Map<String, Object> p92DiscountInfo = programPrice.get("2" + "_" + "2");
        String p92Price = p92DiscountInfo.get("price").toString();
        Map<String, Object> p80DiscountInfo = programPrice.get("3" + "_" + "0");
        String p80Price = p80DiscountInfo.get("price").toString();
        Map<String, Object> p81DiscountInfo = programPrice.get("3" + "_" + "1");
        String p81Price = p81DiscountInfo.get("price").toString();
        Map<String, Object> p82DiscountInfo = programPrice.get("3" + "_" + "2");
        String p82Price = p82DiscountInfo.get("price").toString();

        // 方案类型和购买类型
        ProgramSaleStats p150 = new ProgramSaleStats(orderDate, "15红5蓝", "限购", p150Price, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        ProgramSaleStats p151 = new ProgramSaleStats(orderDate, "15红5蓝", "包赔", p151Price, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        ProgramSaleStats p152 = new ProgramSaleStats(orderDate, "15红5蓝", "普通", p152Price, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        ProgramSaleStats p120 = new ProgramSaleStats(orderDate, "12红3蓝", "限购", p120Price, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        ProgramSaleStats p121 = new ProgramSaleStats(orderDate, "12红3蓝", "包赔", p121Price, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        ProgramSaleStats p122 = new ProgramSaleStats(orderDate, "12红3蓝", "普通", p122Price, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        ProgramSaleStats p90 = new ProgramSaleStats(orderDate, "9红3蓝", "限购", p90Price, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        ProgramSaleStats p91 = new ProgramSaleStats(orderDate, "9红3蓝", "包赔", p91Price, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        ProgramSaleStats p92 = new ProgramSaleStats(orderDate, "9红3蓝", "普通", p92Price, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        ProgramSaleStats p80 = new ProgramSaleStats(orderDate, "8红3蓝", "限购", p80Price, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        ProgramSaleStats p81 = new ProgramSaleStats(orderDate, "8红3蓝", "包赔", p81Price, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        ProgramSaleStats p82 = new ProgramSaleStats(orderDate, "8红3蓝", "普通", p82Price, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

        Map<String, ProgramSaleStats> statsMap = new HashMap<>();
        statsMap.put("0:0", p150);
        statsMap.put("0:1", p151);
        statsMap.put("0:2", p152);
        statsMap.put("1:0", p120);
        statsMap.put("1:1", p121);
        statsMap.put("1:2", p122);
        statsMap.put("2:0", p90);
        statsMap.put("2:1", p91);
        statsMap.put("2:2", p92);
        statsMap.put("3:0", p80);
        statsMap.put("3:1", p81);
        statsMap.put("3:2", p82);

        for (int i = 0; i < 100; i++) {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DATE, -1);
            c.set(Calendar.HOUR_OF_DAY, 24);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            Date t24 = c.getTime();
            c.add(Calendar.DATE, -1);
            Date y24 = c.getTime();
            List<UserAccountFlow> userAccountFlows = userAccountFlowDao.getUserFlowByDate(i * 1l, new Timestamp
                    (y24.getTime()), new Timestamp(t24.getTime()));
            for (UserAccountFlow userAccountFlow : userAccountFlows) {
                // 购买方案
                if (userAccountFlow.getPayDesc().contains("智慧指数") || userAccountFlow.getPayDesc().contains("购买方案")) {
                    // 首次下单
                    Boolean isFirstOrder = Boolean.TRUE;
                    Long userId = userAccountFlow.getUserId();
                    UserProgram currentUserProgram = userProgramDao.getUserProgramByUserProgramId(userAccountFlow
                            .getPayId(), Boolean.FALSE);
                    if (null == currentUserProgram) {
                        continue;
                    }
                    Program currentProgram = programDao.getProgramById(currentUserProgram.getProgramId(), Boolean
                            .FALSE);
                    if (null == currentProgram) {
                        continue;
                    }
                    ProgramSaleStats currentStats = statsMap.get(currentProgram.getProgramType() + CommonConstant
                            .COMMON_COLON_STR + currentProgram.getBuyType());

                    List<UserProgram> userPrograms = userProgramDao.getUserPrograms(game.getGameId(), userId);
                    for (UserProgram userProgram : userPrograms) {
                        try {
                            if (DateUtil.compareDate(userProgram.getCreateTime(), currentUserProgram.getCreateTime())) {
                                // 不是首次下单 （已支付）
                                if (userProgram.getIsPay() == 1) {
                                    isFirstOrder = Boolean.FALSE;
                                }
                            }
                        } catch (Exception e) {
                            continue;
                        }
                    }
                    // 是否购买成功
                    if (isFirstOrder) {
                        currentStats.setFirstOrder(currentStats.getFirstOrder() + 1);
                        if (userAccountFlow.getStatus().equals(CommonConstant.PAY_STATUS_HANDLED)) {
                            currentStats.setFirstSuccess(currentStats.getFirstSuccess() + 1);
                            currentStats.setTotalSuccess(currentStats.getTotalSuccess() + 1);
                        }
                    } else {
                        currentStats.setAgainOrder(currentStats.getAgainOrder() + 1);
                        if (userAccountFlow.getStatus().equals(CommonConstant.PAY_STATUS_HANDLED)) {
                            currentStats.setAgainSuccess(currentStats.getAgainSuccess() + 1);
                            currentStats.setTotalSuccess(currentStats.getTotalSuccess() + 1);
                        }
                    }
                    currentStats.setTotalOrder(currentStats.getTotalOrder() + 1);

                    // 微信 智慧币
                    if (userAccountFlow.getChannel() == 1001) {
                        if (userAccountFlow.getStatus().equals(CommonConstant.PAY_STATUS_HANDLED)) {
                            currentStats.setWechatCount(currentStats.getWechatCount() + 1);
                            currentStats.setWechatAmount(currentStats.getWechatAmount() + userAccountFlow.getPayAmount
                                    ().intValue());
                        }
                    }
                    if (userAccountFlow.getChannel() == 1003) {
                        if (userAccountFlow.getStatus().equals(CommonConstant.PAY_STATUS_HANDLED)) {
                            currentStats.setWisdomCount(currentStats.getWisdomCount() + 1);
                            currentStats.setWisdomAmount(currentStats.getWisdomAmount() + userAccountFlow.getPayAmount
                                    ().intValue());
                        }
                    }
                    if (vipMemberService.checkUserIsVip(userAccountFlow.getUserId(), VipMemberConstant
                            .VIP_MEMBER_TYPE_DIGIT)) {
                        currentStats.setIsVip(1);
                    }
                }
            }
        }
        programSaleStatsDao.insert(p150);
        programSaleStatsDao.insert(p151);
        programSaleStatsDao.insert(p152);
        programSaleStatsDao.insert(p120);
        programSaleStatsDao.insert(p121);
        programSaleStatsDao.insert(p122);
        programSaleStatsDao.insert(p90);
        programSaleStatsDao.insert(p91);
        programSaleStatsDao.insert(p92);
        programSaleStatsDao.insert(p80);
        programSaleStatsDao.insert(p81);
        programSaleStatsDao.insert(p82);
    }

    @Override
    public List<Map<String, Object>> getProgramSaleDailyStats(Integer beginDate, Integer endDate, Integer isVip) {
        List<ProgramSaleStats> programSaleStatsListAll = programSaleStatsDao.getStatsByDate(beginDate, endDate);
        List<ProgramSaleStats> programSaleStatsList = new ArrayList<>();
        if (isVip != 2) {
            for (ProgramSaleStats programSaleStats : programSaleStatsListAll) {
                if (programSaleStats.getIsVip() == isVip) {
                    programSaleStatsList.add(programSaleStats);
                }
            }
        } else {
            programSaleStatsList = programSaleStatsListAll;
        }
        List<Map<String, Object>> resultList = new ArrayList<>();

        for (ProgramSaleStats programSaleStats : programSaleStatsList) {
            Map<String, Object> dateMap = new HashMap<>();
            List<ProgramSaleStats> data = new ArrayList<>();
            // 遍历List
            if (resultList.size() > 0) {
                for (Map<String, Object> entry : resultList) {
                    if (entry.get("dateName").equals(programSaleStats.getOrderDate())) {
                        data = (List<ProgramSaleStats>) entry.get("data");
                        resultList.remove(entry);
                        break;
                    }
                }
            }
            data.add(programSaleStats);
            dateMap.put("dateName", programSaleStats.getOrderDate());
            dateMap.put("data", data);
            resultList.add(dateMap);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> entry : resultList) {
            List<ProgramSaleStats> data = (List<ProgramSaleStats>) entry.get("data");
            ProgramSaleStats all = new ProgramSaleStats(Integer.valueOf(entry.get("dateName").toString()), "全部", "全部",
                    "", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
            for (ProgramSaleStats programSaleStats : data) {
                all.setFirstOrder(all.getFirstOrder() + programSaleStats.getFirstOrder());
                all.setFirstSuccess(all.getFirstSuccess() + programSaleStats.getFirstSuccess());
                all.setAgainOrder(all.getAgainOrder() + programSaleStats.getAgainOrder());
                all.setAgainSuccess(all.getAgainSuccess() + programSaleStats.getAgainSuccess());
                all.setTotalOrder(all.getTotalOrder() + programSaleStats.getTotalOrder());
                all.setTotalSuccess(all.getTotalSuccess() + programSaleStats.getTotalSuccess());
                all.setWechatCount(all.getWechatCount() + programSaleStats.getWechatCount());
                all.setWechatAmount(all.getWechatAmount() + programSaleStats.getWechatAmount());
                all.setWisdomCount(all.getWisdomCount() + programSaleStats.getWisdomCount());
                all.setWisdomAmount(all.getWisdomAmount() + programSaleStats.getWisdomAmount());
            }
            all.setWisdomCount(all.getWisdomCount() / 100);
            all.setWechatCount(all.getWechatCount() / 100);
            entry.put("dataSum", all);
            result.add(entry);
        }
        return result;
    }

    /*
     * 这是一个定时任务
     * */
    @Override
    public void userStatisticTable() {
        // 获取每日用户注册的数据
        productUserStatisticTable(new Date());
        userStatisticTableDay();
        userStatisticTableWeek();
        userStatisticTableMonth();
    }

    private void productUserStatisticTable(Date date) {
        // 获取今天得数据报表，更新
        Integer newDeviceCount = 0;
        Integer newRegisterUserCount = 0;
        Integer dateId = Integer.valueOf(DateUtil.formatDate(date, DateUtil.DATE_FORMAT_YYYYMMDD));

        // 设备
        List<UserDeviceInfo> userDeviceInfos = userDeviceInfoDao.getNewDeviceCountByDate(DateUtil.getBeginOfOneDay
                (new Timestamp(date.getTime())), DateUtil.getEndOfOneDay(new Timestamp(date.getTime())));

        if (null != userDeviceInfos && userDeviceInfos.size() > 0) {
            newDeviceCount = userDeviceInfos.size();
        }

        // 注册用户
        List<UserInfo> userInfos = userInfoDao.geUserInfosByDate(DateUtil.getBeginOfOneDay(new Timestamp(date.getTime
                ())), DateUtil.getEndOfOneDay(new Timestamp(date.getTime())));

        if (null != userInfos && userInfos.size() > 0) {
            newRegisterUserCount = userInfos.size();
        }

        // 查询 更新
        UserStatisticTable userStatisticTable = userStatisticTableDao.getUserStatisticTableByDateId(dateId);
        if (null == userStatisticTable) {
            userStatisticTable = new UserStatisticTable(dateId, newDeviceCount, newRegisterUserCount);
            userStatisticTableDao.insert(userStatisticTable);
        } else {
            userStatisticTable.setNewDeviceCount(newDeviceCount);
            userStatisticTable.setNewUserRegisterCount(newRegisterUserCount);
            userStatisticTableDao.update(userStatisticTable);
        }
    }

    // 最近一个月的时间
    @Override
    public void productLastMonthUserStatistic() {
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < 30; i++) {
            cal.add(Calendar.DATE, -1);
            Date time = cal.getTime();
            productUserStatisticTable(time);
        }
    }

    /*
     * 用户日报统计
     * 这是一个定时任务
     * */
    @Override
    public void userStatisticTableDay() {
        try {
            // DATE_ID 日期ID
            Integer dateId = Integer.valueOf(DateUtil.formatDate(new Date(), DateUtil.DATE_FORMAT_YYYYMMDD));
            // 查询今日是否已经有统计记录
            UserStatisticTableDay userStatisticTableDay = userStatisticTableDayDao.getUserStatisticTableDayByDateId
                    (dateId);
            if (null == userStatisticTableDay) {
                userStatisticTableDay = new UserStatisticTableDay(dateId);
                userStatisticTableDayDao.insert(userStatisticTableDay);
            }
            // 总设备数(所有的设备count)
            userStatisticTableDay.setDeviceTotalNum(userDeviceInfoDao.getAllDeviceCount());
            // 新增设备(createTime今日的设备Count)
            List<UserDeviceInfo> userDeviceInfos = userDeviceInfoDao.getNewDeviceCountByDate(DateUtil.getBeginOfOneDay
                    (new Timestamp(new Date().getTime())), DateUtil.getEndOfOneDay(new Timestamp(new Date().getTime()
            )));
            if (null != userDeviceInfos && userDeviceInfos.size() > 0) {
                userStatisticTableDay.setDeviceNewNum(userDeviceInfos.size());
            }
            // 日活跃设备数(updateTime今日的设备Count)
            Integer deviceActiveCount = userDeviceInfoDao.getDayActiveDeviceCountByDate(DateUtil
                    .getBeginOfOneDay(new Timestamp(new Date().getTime())), DateUtil.getEndOfOneDay(new Timestamp(new
                    Date().getTime())));
            userStatisticTableDay.setDeviceDayActiveNum(deviceActiveCount);
            // ios活跃设备数(活跃设备里是IOS（1001 1021）的)
            Integer iosDeviceActiveCount = userDeviceInfoDao.getIosDayActiveDeviceCountByDate(DateUtil
                    .getBeginOfOneDay(new Timestamp(new Date().getTime())), DateUtil.getEndOfOneDay(new Timestamp(new
                    Date().getTime())));
            userStatisticTableDay.setDeviceIosActiveNum(iosDeviceActiveCount);
            // 安卓活跃设备数 (日活跃-IOS)
            userStatisticTableDay.setDeviceAndroidActiveNum(deviceActiveCount - iosDeviceActiveCount);
            // 日活跃历史设备数 (日活跃-新增)
            userStatisticTableDay.setDeviceDayActiveHistoryNum(deviceActiveCount - userDeviceInfos.size());

            // 总注册用户数
            Integer allUserCount = userInfoDao.getCountAllUserInfos();
            userStatisticTableDay.setUserTotalRegisterNum(allUserCount);
            // 新增用户数(CreateTime为今天得用户数量)
            Integer todayNewUserCount = userInfoDao.getTodayNewUserCount(DateUtil
                    .getBeginOfOneDay(new Timestamp(new Date().getTime())), DateUtil.getEndOfOneDay(new Timestamp(new
                    Date().getTime())));
            userStatisticTableDay.setUserNewNum(todayNewUserCount);
            // 日活跃用户数
            Integer userActiveCount = userActiveDao.getCountUserActive(dateId);
            userStatisticTableDay.setUserDayActiveNum(userActiveCount);
            // ios活跃用户数
            Integer iosActiveCount = 0;
            Integer vipActiveCount = 0;
            List<UserActive> userActives = userActiveDao.getActiveUsers(dateId);
            if (null != userActives && userActives.size() > 0) {
                for (UserActive userActive : userActives) {
                    UserInfo userInfo = userInfoDao.getUserInfo(userActive.getUserId());
                    if (userInfo.getChannelType().equals("appStore")) {
                        iosActiveCount++;
                    }
                    Boolean isVip = vipMemberService.checkUserIsVip(userActive.getUserId(), VipMemberConstant
                            .VIP_MEMBER_TYPE_DIGIT);
                    Boolean isSportsVip = vipMemberService.checkUserIsVip(userActive.getUserId(), VipMemberConstant
                            .VIP_MEMBER_TYPE_SPORTS);
                    if (isVip || isSportsVip) {
                        vipActiveCount++;
                    }
                }
            }
            userStatisticTableDay.setUserIosActiveNum(iosActiveCount);
            userStatisticTableDay.setUserAndroidActiveNum(userActiveCount - iosActiveCount);

            // 日活跃vip数
            userStatisticTableDay.setUserDayVipActiveNum(vipActiveCount);

            // 当日收入
            OrderStatisticReport orderStatisticReport = orderStatisticReportDao.getOrderStatisticReportByDate(dateId);
            userStatisticTableDay.setDayIncome(orderStatisticReport.getRealPayAmount().intValue() / 100);
            userStatisticTableDay.setRemarkIncome(orderStatisticReport.getRealPayAmount().intValue() / 100);

            // 日付费用户数
            userStatisticTableDay.setUserDayPayNum(orderStatisticReport.getRealPayNum());
            // 最高收入
            Integer maxDay = userAccountFlowDao.maxDayCashFlow(DateUtil.getBeginOfOneDay(new Timestamp(new Date()
                    .getTime
                            ())), DateUtil.getEndOfOneDay(new Timestamp(new Date().getTime())));
            // 总收入
            userStatisticTableDay.setTotalIncome(userAccountFlowDao.sumAllCashFlow().intValue() / 100);
            userStatisticTableDay.setMaxPay(maxDay / 100);
            // 更新数据库
            userStatisticTableDayDao.update(userStatisticTableDay);
        } catch (Exception e) {
            log.error("用户统计日报异常，速速查看！！！！！！！！！！");
            throw new BusinessException(e);
        }
    }

    /* 周报统计*/
    @Override
    public void userStatisticTableWeek() {
        // 统计本周的数据 更新数据库
        String weekId = CommonUtil.getWeekIdByDate(new Date());
        Integer weekStartDate = Integer.valueOf(DateUtil.formatDate(new Date(), DateUtil.DATE_FORMAT_YYYYMMDD));
        ;
        Integer weekEndDate = Integer.valueOf(DateUtil.formatDate(new Date(), DateUtil.DATE_FORMAT_YYYYMMDD));
        ;
        Timestamp startTime = DateUtil.getBeginOfToday();
        Integer currentDateId = Integer.valueOf(DateUtil.formatDate(new Date(), DateUtil.DATE_FORMAT_YYYYMMDD));
        // 统计周数据
        Integer weekNewDeviceCount = 0;
        Integer weekNewUserCount = 0;
        Integer weekActiveUserCount = 0;
        Integer weekPayCount = 0;
        Integer weekMaxPayValue = 0;
        // 今天的
        UserStatisticTableDay userStatisticTableDay = userStatisticTableDayDao
                .getUserStatisticTableDayByDateId(Integer.valueOf(DateUtil.formatDate(new Date(), DateUtil
                        .DATE_FORMAT_YYYYMMDD)));
        weekNewDeviceCount += userStatisticTableDay.getDeviceNewNum();
        weekNewUserCount += userStatisticTableDay.getUserNewNum();
        weekActiveUserCount += userActiveDao.getCountUserActive(Integer.valueOf(DateUtil.formatDate(new Date(),
                DateUtil.DATE_FORMAT_YYYYMMDD)));

        weekPayCount += userStatisticTableDay.getDayIncome();
        weekMaxPayValue = userStatisticTableDay.getMaxPay();
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < 7; i++) {
            cal.add(Calendar.DATE, -1);
            Date time = cal.getTime();
            if (CommonUtil.getWeekIdByDate(time).equals(weekId)) {
                // 本周开始时间
                weekStartDate = Integer.valueOf(DateUtil.formatDate(time, DateUtil.DATE_FORMAT_YYYYMMDD));
                startTime = new Timestamp(time.getTime());
                UserStatisticTableDay userStatisticTableDayHistory = userStatisticTableDayDao
                        .getUserStatisticTableDayByDateId(Integer.valueOf(DateUtil.formatDate(time, DateUtil
                                .DATE_FORMAT_YYYYMMDD)));
                if (null == userStatisticTableDayHistory) {
                    continue;
                }
                weekNewDeviceCount += userStatisticTableDayHistory.getDeviceNewNum();
                weekNewUserCount += userStatisticTableDayHistory.getUserNewNum();
                weekActiveUserCount += userActiveDao.getCountUserActive(Integer.valueOf(DateUtil.formatDate(time,
                        DateUtil.DATE_FORMAT_YYYYMMDD)));
                weekPayCount += userStatisticTableDayHistory.getDayIncome();
                if (userStatisticTableDayHistory.getMaxPay() > weekMaxPayValue) {
                    weekMaxPayValue = userStatisticTableDayHistory.getMaxPay();
                }
            }
        }
        Calendar cal2 = Calendar.getInstance();
        for (int i = 0; i < 7; i++) {
            cal2.add(Calendar.DATE, 1);
            Date time = cal2.getTime();
            if (CommonUtil.getWeekIdByDate(time).equals(weekId)) {
                // 本周开始时间
                weekEndDate = Integer.valueOf(DateUtil.formatDate(time, DateUtil.DATE_FORMAT_YYYYMMDD));
            }
        }
        // 数据库保存的key
        StringBuffer dbWeekKey = new StringBuffer();
        dbWeekKey.append(String.valueOf(weekStartDate)).append("~").append(weekEndDate);
        UserDeviceWeekReport userDeviceWeekReport = userDeviceWeekReportDao.getUserDeviceWeekReportByDate(dbWeekKey
                .toString());
        if (null == userDeviceWeekReport) {
            userDeviceWeekReport = new UserDeviceWeekReport(dbWeekKey.toString());
            userDeviceWeekReportDao.insert(userDeviceWeekReport);
        }
        // 总设备数
        userDeviceWeekReport.setTotalActivetionUserNum(userStatisticTableDay.getDeviceTotalNum());
        // 新增设备数
        userDeviceWeekReport.setNewActivetionUserNum(weekNewDeviceCount);
        // 周活跃设备数
        userDeviceWeekReport.setWauDevice(userDeviceInfoDao.getDayActiveDeviceCountByDate(startTime, DateUtil
                .getCurrentTimestamp()));
        // 周活跃历史设备数
        // 总注册用户数
        userDeviceWeekReport.setTotalUserNum(userStatisticTableDay.getUserTotalRegisterNum());
        // 新增用户数
        userDeviceWeekReport.setNewUserNum(weekNewUserCount);
        // 周活跃用户数
        userDeviceWeekReport.setWau(weekActiveUserCount);
        // 周活跃历史用户数
        // 周付费用户数
        userDeviceWeekReport.setPayMoneyUserNum(userAccountFlowDao.getCountCashFlowFromOtterByDate(startTime,
                DateUtil.getCurrentTimestamp()));
        // 周收入
        userDeviceWeekReport.setDailyIncome(Long.valueOf(weekPayCount));
        // 总收入
        userDeviceWeekReport.setTotalIncome(Long.valueOf(userStatisticTableDay.getTotalIncome()));
        userDeviceWeekReport.setMaxDailyIncome(Long.valueOf(weekMaxPayValue));
        // 更新数据库
        userDeviceWeekReportDao.update(userDeviceWeekReport);
    }

    /* 月报统计*/
    @Override
    public void userStatisticTableMonth() {
        // 统计本月的数据 更新数据库
        String monthId = CommonUtil.getMonthIdByDate(new Date());
        Integer monthStartDate = Integer.valueOf(DateUtil.formatDate(new Date(), DateUtil.DATE_FORMAT_YYYYMMDD));
        Integer monthEndDate = Integer.valueOf(DateUtil.formatDate(new Date(), DateUtil.DATE_FORMAT_YYYYMMDD));
        Timestamp startTime = DateUtil.getBeginOfToday();
        Integer currentDateId = Integer.valueOf(DateUtil.formatDate(new Date(), DateUtil.DATE_FORMAT_YYYYMMDD));
        // 统计周数据
        Integer monthNewDeviceCount = 0;
        Integer monthNewUserCount = 0;
        Integer monthActiveUserCount = 0;
        Integer monthPayCount = 0;
        Integer monthMaxPayValue = 0;
        // 今天的
        UserStatisticTableDay userStatisticTableDay = userStatisticTableDayDao
                .getUserStatisticTableDayByDateId(Integer.valueOf(DateUtil.formatDate(new Date(), DateUtil
                        .DATE_FORMAT_YYYYMMDD)));
        monthNewDeviceCount += userStatisticTableDay.getDeviceNewNum();
        monthNewUserCount += userStatisticTableDay.getUserNewNum();
        monthActiveUserCount += userActiveDao.getCountUserActive(Integer.valueOf(DateUtil.formatDate(new Date(),
                DateUtil.DATE_FORMAT_YYYYMMDD)));

        monthPayCount += userStatisticTableDay.getDayIncome();
        monthMaxPayValue = userStatisticTableDay.getMaxPay();
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < 32; i++) {
            cal.add(Calendar.DATE, -1);
            Date time = cal.getTime();
            if (CommonUtil.getMonthIdByDate(time).equals(monthId)) {
                // 本周开始时间
                monthStartDate = Integer.valueOf(DateUtil.formatDate(time, DateUtil.DATE_FORMAT_YYYYMMDD));
                startTime = new Timestamp(time.getTime());
                UserStatisticTableDay userStatisticTableDayHistory = userStatisticTableDayDao
                        .getUserStatisticTableDayByDateId(Integer.valueOf(DateUtil.formatDate(time, DateUtil
                                .DATE_FORMAT_YYYYMMDD)));
                if (null == userStatisticTableDayHistory) {
                    continue;
                }
                monthNewDeviceCount += userStatisticTableDayHistory.getDeviceNewNum();
                monthNewUserCount += userStatisticTableDayHistory.getUserNewNum();
                monthActiveUserCount += userActiveDao.getCountUserActive(Integer.valueOf(DateUtil.formatDate(time,
                        DateUtil.DATE_FORMAT_YYYYMMDD)));
                monthPayCount += userStatisticTableDayHistory.getDayIncome();
                if (userStatisticTableDayHistory.getMaxPay() > monthMaxPayValue) {
                    monthMaxPayValue = userStatisticTableDayHistory.getMaxPay();
                }
            }
        }
        Calendar cal2 = Calendar.getInstance();
        for (int i = 0; i < 32; i++) {
            cal2.add(Calendar.DATE, 1);
            Date time = cal2.getTime();
            if (CommonUtil.getMonthIdByDate(time).equals(monthId)) {
                // 本周开始时间
                monthEndDate = Integer.valueOf(DateUtil.formatDate(time, DateUtil.DATE_FORMAT_YYYYMMDD));
            }
        }
        // 数据库保存的key
        StringBuffer dbWeekKey = new StringBuffer();
        dbWeekKey.append(String.valueOf(monthStartDate)).append("~").append(monthEndDate);
        UserDeviceMonthReport userDeviceMonthReport = userDeviceMonthReportDao.getUserDeviceMonthReportByDate(dbWeekKey
                .toString());
        if (null == userDeviceMonthReport) {
            userDeviceMonthReport = new UserDeviceMonthReport(dbWeekKey.toString());
            userDeviceMonthReportDao.insert(userDeviceMonthReport);
        }
        // 总设备数
        userDeviceMonthReport.setTotalActivetionUserNum(userStatisticTableDay.getDeviceTotalNum());
        // 新增设备数
        userDeviceMonthReport.setNewActivetionUserNum(monthNewDeviceCount);
        // 周活跃设备数
        userDeviceMonthReport.setWauDevice(userDeviceInfoDao.getDayActiveDeviceCountByDate(startTime, DateUtil
                .getCurrentTimestamp()));
        // 周活跃历史设备数
        // 总注册用户数
        userDeviceMonthReport.setTotalUserNum(userStatisticTableDay.getUserTotalRegisterNum());
        // 新增用户数
        userDeviceMonthReport.setNewUserNum(monthNewUserCount);
        // 周活跃用户数
        userDeviceMonthReport.setWau(monthActiveUserCount);
        // 周活跃历史用户数
        // 周付费用户数
        userDeviceMonthReport.setPayMoneyUserNum(userAccountFlowDao.getCountCashFlowFromOtterByDate(startTime,
                DateUtil.getCurrentTimestamp()));
        // 周收入
        userDeviceMonthReport.setDailyIncome(Long.valueOf(monthPayCount));
        // 总收入
        userDeviceMonthReport.setTotalIncome(Long.valueOf(userStatisticTableDay.getTotalIncome()));
        userDeviceMonthReport.setMaxDailyIncome(Long.valueOf(monthMaxPayValue));
        // 更新数据库
        userDeviceMonthReportDao.update(userDeviceMonthReport);
    }

    @Override
    public Map<String, Object> getUserStatisticTableByType(Integer page, Integer type) {
        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> data = new ArrayList<>();
        // 日报
        if (type == 1) {
            PaginationList<UserStatisticTableDay> userStatisticTableDays = userStatisticTableDayDao
                    .getUserStatisticTableDayByPage(page);
            for (UserStatisticTableDay userStatisticTableDay : userStatisticTableDays) {
                Map<String, Object> row = new HashMap<>();
                row.put("date", userStatisticTableDay.getDateId());
                row.put("total_user_num", userStatisticTableDay.getUserTotalRegisterNum());
                row.put("total_activetion_user_num", userStatisticTableDay.getDeviceTotalNum());
                row.put("new_activetion_user_num", userStatisticTableDay.getDeviceNewNum());
                row.put("dau_device", userStatisticTableDay.getDeviceDayActiveNum());
                row.put("dau_device_android", userStatisticTableDay.getDeviceAndroidActiveNum());
                row.put("dau_device_ios", userStatisticTableDay.getDeviceIosActiveNum());
                row.put("new_user_num", userStatisticTableDay.getDeviceNewNum());
                row.put("dau", userStatisticTableDay.getUserDayActiveNum());
                row.put("dau_android", userStatisticTableDay.getUserAndroidActiveNum());
                row.put("dau_ios", userStatisticTableDay.getUserIosActiveNum());
                row.put("vip_dau", userStatisticTableDay.getUserDayVipActiveNum());
                row.put("pay_money_user_num", userStatisticTableDay.getUserDayPayNum());
                row.put("daily_income", userStatisticTableDay.getDayIncome());
                row.put("backup_01", 0);
                row.put("max_daily_income", userStatisticTableDay.getMaxPay());
                row.put("total_income", userStatisticTableDay.getTotalIncome());
                data.add(row);
            }
        }
        // 周报
        if (type == 2) {
            PaginationList<UserDeviceWeekReport> userDeviceWeekReports = userDeviceWeekReportDao
                    .getUserStatisticTableWeekByPage(page);
            for (UserDeviceWeekReport userDeviceWeekReport : userDeviceWeekReports) {
                Map<String, Object> row = new HashMap<>();
                row.put("backup_01", 0);
                row.put("daily_per_cnt", 0);
                row.put("new_activetion_user_num", userDeviceWeekReport.getNewActivetionUserNum());
                row.put("new_user_num", userDeviceWeekReport.getNewUserNum());
                row.put("daily_income", userDeviceWeekReport.getDailyIncome());
                row.put("max_daily_income", userDeviceWeekReport.getMaxDailyIncome());
                row.put("total_user_num", userDeviceWeekReport.getTotalUserNum());
                row.put("total_income", userDeviceWeekReport.getTotalIncome());
                row.put("total_activetion_user_num", userDeviceWeekReport.getTotalActivetionUserNum());
                row.put("wau_device", userDeviceWeekReport.getWauDevice());
                row.put("wau", userDeviceWeekReport.getWau());
                row.put("pay_money_user_num", userDeviceWeekReport.getPayMoneyUserNum());
                row.put("date", userDeviceWeekReport.getDateId());
                data.add(row);
            }
        }
        // 月报
        if (type == 3) {
            PaginationList<UserDeviceMonthReport> userDeviceMonthReports = userDeviceMonthReportDao
                    .getUserStatisticTableMonthByPage(page);
            for (UserDeviceMonthReport userDeviceMonthReport : userDeviceMonthReports) {
                Map<String, Object> row = new HashMap<>();
                row.put("backup_01", 0);
                row.put("daily_per_cnt", 0);
                row.put("new_activetion_user_num", userDeviceMonthReport.getNewActivetionUserNum());
                row.put("new_user_num", userDeviceMonthReport.getNewUserNum());
                row.put("daily_income", userDeviceMonthReport.getDailyIncome());
                row.put("max_daily_income", userDeviceMonthReport.getMaxDailyIncome());
                row.put("total_user_num", userDeviceMonthReport.getTotalUserNum());
                row.put("total_income", userDeviceMonthReport.getTotalIncome());
                row.put("total_activetion_user_num", userDeviceMonthReport.getTotalActivetionUserNum());
                row.put("wau_device", userDeviceMonthReport.getWauDevice());
                row.put("wau", userDeviceMonthReport.getWau());
                row.put("pay_money_user_num", userDeviceMonthReport.getPayMoneyUserNum());
                row.put("date", userDeviceMonthReport.getDateId());
                data.add(row);
            }
        }
        resultMap.put("data", data);
        return resultMap;
    }

    @Override
    public void downloadYop() {
        String[] merchantNoArr = {"10023508731", "10023629882"};

        String method = "tradeday";
        String dataType = "success";
//        String dateday = "2018-10-01";
        String dateday = DateUtil.getYesterday("yyyy-MM-dd");
        Integer redisDateday = Integer.valueOf(dateday.replaceAll("-", ""));
        //判断装入map的字段
        Map<String, String> params = new HashMap<>();
        params.put("method", method + "download");
        params.put("dateday", dateday);
        params.put("dataType", dataType);
        int i = 1;
        for (String merchantNo : merchantNoArr) {
            String key = RedisConstant.threePartyBillFileKey("YOP", merchantNo);
            Integer lastDate = redisService.kryoGet(key, Integer.class);
            if (lastDate != null && lastDate >= redisDateday) {
                continue;
            }

            String partPath = "/config/yop_sdk_config_" + merchantNo + ".json";
            YopBillUtil yopBillUtil = new YopBillUtil(merchantNo, partPath);
            String downloadFile = yopBillUtil.getDateYopBillFileFromHttp(params);
            Boolean saveRes = yopBillUtil.saveDownloadFile2DB(downloadFile, thirdPartyBillInfoDao, i);
            if (saveRes) {
                redisService.kryoSetEx(key, 86400, redisDateday);
            }
            i++;
        }
    }

    @Override
    public void downloadJDBill() {

        List<Map<String, String>> merchantInfo = new ArrayList<>();
        Map<String, String> zhMap = new HashMap<>();
        zhMap.put("key", "Gl6Sp8bVgaRsMCyQCIZkTIMbWiZrrDiu");
        zhMap.put("mchId", "111069740002");
        zhMap.put("owner", "111069740");
        merchantInfo.add(zhMap);

        Map<String, String> mjMap = new HashMap<>();
        mjMap.put("key", "oj3gEqLjuqJeYnKa8gtuJsOfjzeLzyNj");
        mjMap.put("mchId", "111063721002");
        mjMap.put("owner", "111063721");
        merchantInfo.add(mjMap);

        String yesterday = DateUtil.getCurrentDay("yyyyMMdd");
        Integer redisDateday = Integer.valueOf(yesterday);

        for (Map<String, String> merchant : merchantInfo) {
            String key = merchant.get("key");
            String mchId = merchant.get("mchId");
            String owner = merchant.get("owner");

            String redisKey = RedisConstant.threePartyBillFileKey("JDBILL", mchId);
            Integer lastDate = redisService.kryoGet(redisKey, Integer.class);
            if (lastDate != null && lastDate >= redisDateday) {
                continue;
            }
            log.info("begin download jd fill:" + mchId);
            JDBillUtil jdBillUtil = new JDBillUtil(mchId, owner, key);
            String downloadFilePath = jdBillUtil.downloadJDBillFile(yesterday);
            if (StringUtils.isBlank(downloadFilePath)) {
                return;
            }
            ZipUtil.unZip(downloadFilePath);
            String fileName = downloadFilePath.substring(downloadFilePath.lastIndexOf("/") + 1, downloadFilePath.length()
                    - 4) + ".csv";
            log.info("jd download file:" + fileName + "  path:" + downloadFilePath);
            Boolean res = jdBillUtil.saveJDBillFile2DB(fileName, mchId, thirdPartyBillInfoDao);
            if (res) {
                redisService.kryoSetEx(redisKey, 86400, redisDateday);
            }
        }
//        String downloadFilePath = "/data/mojiecp/predict/out/test/resources/downloadFile/20160823ordercreate_22294531001.zip";
    }

    @Override
    public void downloadJDBillCompensate(String date) {

        List<Map<String, String>> merchantInfo = new ArrayList<>();
        Map<String, String> zhMap = new HashMap<>();
        zhMap.put("key", "Gl6Sp8bVgaRsMCyQCIZkTIMbWiZrrDiu");
        zhMap.put("mchId", "111069740002");
        zhMap.put("owner", "111069740");
        merchantInfo.add(zhMap);

        Map<String, String> mjMap = new HashMap<>();
        mjMap.put("key", "oj3gEqLjuqJeYnKa8gtuJsOfjzeLzyNj");
        mjMap.put("mchId", "111063721002");
        mjMap.put("owner", "111063721");
        merchantInfo.add(mjMap);

        String yesterday = date;
        Integer redisDateday = Integer.valueOf(yesterday);

        for (Map<String, String> merchant : merchantInfo) {
            String key = merchant.get("key");
            String mchId = merchant.get("mchId");
            String owner = merchant.get("owner");

            String redisKey = RedisConstant.threePartyBillFileKey("JDBILL", mchId);
            //Integer lastDate = redisService.kryoGet(redisKey, Integer.class);
//            if (lastDate != null && lastDate >= redisDateday) {
//                continue;
//            }
            log.info("begin download jd fill:" + mchId);
            JDBillUtil jdBillUtil = new JDBillUtil(mchId, owner, key);
            String downloadFilePath = jdBillUtil.downloadJDBillFile(yesterday);
            if (StringUtils.isBlank(downloadFilePath)) {
                return;
            }
            ZipUtil.unZip(downloadFilePath);
            String fileName = downloadFilePath.substring(downloadFilePath.lastIndexOf("/") + 1, downloadFilePath.length()
                    - 4) + ".csv";
            log.info("jd download file:" + fileName + "  path:" + downloadFilePath);
            Boolean res = jdBillUtil.saveJDBillFile2DB(fileName, mchId, thirdPartyBillInfoDao);
            if (res) {
                redisService.kryoSetEx(redisKey, 86400, redisDateday);
            }
        }
//        String downloadFilePath = "/data/mojiecp/predict/out/test/resources/downloadFile/20160823ordercreate_22294531001.zip";
    }

    @Override
    public void absolutePathJDBillFileImport() {
        String zhkey = "Gl6Sp8bVgaRsMCyQCIZkTIMbWiZrrDiu";
        String zhmch2 = "111069740002";
        String zhOwner = "111069740";

        String mjOwner = "111063721";
        String mjmch2 = "111063721002";
        String mjkey = "oj3gEqLjuqJeYnKa8gtuJsOfjzeLzyNj";
        JDBillUtil jdBillUtil = new JDBillUtil(mjmch2, mjOwner, mjkey);

        String path = "/data/mojiecp/predict/src/test/resources/downloadFile/manualJDFile/";
        File file = new File(path);
        File[] tempList = file.listFiles();
        if (tempList == null || tempList.length == 0) {
            return;
        }
        for (File tempFile : tempList) {
            if (tempFile.isFile()) {
                List<String> tempContent = CSVFileUtil.readFile("GBK", tempFile);
                Boolean saveRes = jdBillUtil.saveJDBillFile2DB(mjmch2, thirdPartyBillInfoDao, tempContent);
                if (!saveRes) {
                    System.out.println("文件读取失败：" + tempFile.getName());
                }
            }
        }
    }

    @Override
    public List<ThirdPartyBillInfo> getYopHistoryAccount(Integer beginTime, Integer endTime, String mchId) {
        return getJdHistoryAccount(beginTime, endTime, mchId, null, null);
    }

    @Override
    public List<ThirdPartyBillInfo> getJdHistoryAccount(Integer beginTime, Integer endTime, String mchId, String
            status, String businessType) {
        return thirdPartyBillInfoDao.getThirdPartyBillInfoByTime(beginTime, endTime, mchId, status, businessType);
    }

    @Override
    public Map<String, Object> getWithdrawReportForm(Timestamp beginTimeT, Timestamp endTimeT) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> jdWithdrawMap = new HashMap<>();
        Long totalAmount = userWithdrawFlowDao.getUserWithdrawTotalAmountByOtter(beginTimeT, endTimeT);
        totalAmount = (totalAmount == null ? 0l : totalAmount) + getWithdrawTestMoney(beginTimeT, endTimeT);
        String businessType = "代付单";
        String status = "成功";
        Map<String, Object> sumThirdBillMap = thirdPartyBillInfoDao.getSumThirdPartyBillInfoByTimeAndType(beginTimeT,
                endTimeT, businessType, status, "111069740002");
        String thirdPartOrderAmount = "";
        String thirdPartPoundage = "";
        if (sumThirdBillMap != null && !sumThirdBillMap.isEmpty()) {
            thirdPartOrderAmount = CommonUtil.getValueFromMap("amount", sumThirdBillMap);
            thirdPartPoundage = CommonUtil.getValueFromMap("fee", sumThirdBillMap);
        }
        jdWithdrawMap.put("name", "智慧彩票用户代付");
        jdWithdrawMap.put("thirdPartName", "京东支付40002");
        jdWithdrawMap.put("orderAmount", CommonUtil.convertFen2Yuan(totalAmount).doubleValue());
        jdWithdrawMap.put("thirdPartOrderAmount", thirdPartOrderAmount);
        jdWithdrawMap.put("thirdPartPoundage", thirdPartPoundage);
        jdWithdrawMap.put("arriveAmount", CommonUtil.addByBigDecimal(thirdPartOrderAmount, thirdPartPoundage));
        List<Map<String, Object>> withdrawReport = new ArrayList<>();
        withdrawReport.add(jdWithdrawMap);
        result.put("withdrawReport", withdrawReport);
        return result;
    }

    private Long getWithdrawTestMoney(Timestamp beginTimeT, Timestamp endTimeT) {
        Map<String, Long> testMoney = new HashMap<>();
        testMoney.put("20181004", 370l);
        testMoney.put("20181002", 255l);
        Long result = 0l;

        for (String key : testMoney.keySet()) {
            Timestamp keyTime = DateUtil.formatString(key, "yyyyMMdd");
            if (beginTimeT.equals(keyTime) || endTimeT.equals(keyTime) || (DateUtil.compareDate(beginTimeT, keyTime) &&
                    DateUtil.compareDate(keyTime, endTimeT))) {
                result += testMoney.get(key);
            }
        }
        return result;
    }

    /*
     * 获取每日用户注册结果
     * */
    @Override
    public Map<String, Object> getUserStatisticTable(Integer page) {
        Map<String, Object> resultMap = new HashMap<>();
        PaginationList<UserStatisticTable> userStatisticTables = userStatisticTableDao.getUserStatisticTableByPage
                (page);

        List<Map<String, Object>> data = new ArrayList<>();
        for (UserStatisticTable userStatisticTable : userStatisticTables) {
            Map<String, Object> row = new HashMap<>();
            row.put("date", userStatisticTable.getDateId());
            row.put("new_download", userStatisticTable.getNewDeviceCount());
            row.put("new_register", userStatisticTable.getNewUserRegisterCount());
            row.put("create_time", userStatisticTable.getCreateTime());
            row.put("update_time", userStatisticTable.getUpdateTime());
            data.add(row);
        }
        resultMap.put("data", data);
        resultMap.put("total", userStatisticTableDao.countRecords());
        return resultMap;
    }

    private boolean checkUserIsRobot(Long userId) {
        if (userId == null) {
            return false;
        }
        UserInfo userInfo = userInfoDao.getUserInfo(userId);
        if (userInfo != null && userInfo.getChannelType().equals("robot")) {
            return true;
        }
        return false;
    }
}
