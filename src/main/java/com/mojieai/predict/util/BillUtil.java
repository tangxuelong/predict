package com.mojieai.predict.util;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.PayChannelInfoCache;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.UserAccountConstant;
import com.mojieai.predict.constant.VipMemberConstant;
import com.mojieai.predict.entity.bo.BillTestDbData;
import com.mojieai.predict.entity.bo.ThirdPartBillOrderInfo;
import com.mojieai.predict.entity.vo.ChannelBillVo;
import com.mojieai.predict.entity.vo.ProductBillVo;
import com.mojieai.predict.enums.BillEnum;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BillUtil {
    /*****业务utils******/

    public static ChannelBillVo getChannelReportVo(Map<String, Object> channelReportMap, String key, BigDecimal
            testDbMoney, Map<Integer, ThirdPartBillOrderInfo> thirdPartChannelInfo) {
        ChannelBillVo channelBillVo = new ChannelBillVo();
        //1.获取渠道名称
        channelBillVo.setChannelName(getBillChannelName(key));
        //2.计算订单金额
        BigDecimal orderMoneyFen = new BigDecimal(channelReportMap.get(key).toString());
        //2.1 wx添加测试库金额
        if (testDbMoney.intValue() > 0) {
            orderMoneyFen = orderMoneyFen.add(testDbMoney);
        }
        String orderCharge = orderMoneyFen.divide(new BigDecimal(100), 2, RoundingMode.HALF_UP).toString();
        channelBillVo.setOrderCharge(orderCharge);
        //3.计算服务费
        String serviceCharge = orderMoneyFen.multiply(new BigDecimal(CommonConstant.WEI_XIN_SERVICE_CHARGE))
                .divide(new BigDecimal(100), 5, RoundingMode.HALF_UP).toString();
        channelBillVo.setServiceCharge(serviceCharge);
        //4.计算到账金额
        String arrvieAmount = new BigDecimal(orderCharge).subtract(new BigDecimal(serviceCharge)).toString();
        channelBillVo.setArriveAmount(arrvieAmount);

        //5.三方订单和手续费
        ThirdPartBillOrderInfo thirdBillInfo = thirdPartChannelInfo.get(Integer.valueOf(key));
        if (thirdBillInfo != null && thirdBillInfo.getTotalAmount() != null) {
            channelBillVo.setOff_amount(thirdBillInfo.getTotalAmount().toString());
            channelBillVo.setOff_poundage(thirdBillInfo.getTotalPoundage().toString());
        }

        return channelBillVo;
    }

    private static String getBillChannelName(String channelIdStr) {
        if (StringUtils.isBlank(channelIdStr)) {
            return "";
        }
        String name = "";
        Integer channelId = Integer.valueOf(channelIdStr);
        if (channelId.equals(CommonConstant.WX_PAY_CHANNEL_ID_1)) {
            name = "财付通11992";
        } else if (channelId.equals(CommonConstant.WX_PAY_CHANNEL_WX_JSAPI)) {
            name = "财付通46002";
        } else if (channelId.equals(CommonConstant.JD_PAY_CHANNEL_ID)) {
            name = "京东支付40002";
        } else if (channelId.equals(CommonConstant.YOP_PAY_CHANNEL_ID)) {
            name = "易宝29882";
        } else if (channelId.equals(CommonConstant.HAO_DIAN_PAY_CHANNEL_ID)) {
            name = "支付宝(好店)";
        } else {
            name = PayChannelInfoCache.getChannelInfo(Integer.valueOf(channelId)).getChannelName();
        }
        return name;
    }

    /* 产品收入。内部会减去对应的测试金额*/
    public static ProductBillVo getProductReportVo(Map<String, Map<String, Long>> productReportMap, String key,
                                                   Map<Integer, Map<String, Long>> testAmountMap) {
        ProductBillVo billVo = new ProductBillVo();
        //1.获取产品名称
        BillEnum billEnum = BillEnum.getBillEnumByProductType(Integer.valueOf(key));
        String name = billEnum.getBillShowName();
        billVo.setProductName(name);
        billVo.setWeight(billEnum.getWeight());
        //2.获取各个金额
        Long totalAmount = productReportMap.get(key).get("total");
        Long wxTotalAmount = productReportMap.get(key).get(CommonConstant.WX_PAY_CHANNEL_ID + "");
        Long wx2TotalAmount = productReportMap.get(key).get(CommonConstant.WX_PAY_CHANNEL_ID_1 + "");
        Long wx3TotalAmount = productReportMap.get(key).get(CommonConstant.WX_PAY_CHANNEL_WX_JSAPI + "");
        Long aliTotalAmount = productReportMap.get(key).get(CommonConstant.ALI_PAY_CHANNEL_ID + "");
        Long appleTotalAmount = productReportMap.get(key).get(CommonConstant.APPLE_PAY_CHANNEL_ID + "");
        Long jdTotalAmount = productReportMap.get(key).get(CommonConstant.JD_PAY_CHANNEL_ID + "");
        Long yopTotalAmount = productReportMap.get(key).get(CommonConstant.YOP_PAY_CHANNEL_ID + "");
        Long haoDianTotalAmount = productReportMap.get(key).get(CommonConstant.HAO_DIAN_PAY_CHANNEL_ID + "");
        totalAmount = totalAmount == null ? 0 : totalAmount;
        wxTotalAmount = wxTotalAmount == null ? 0 : wxTotalAmount;
        wx2TotalAmount = wx2TotalAmount == null ? 0 : wx2TotalAmount;
        wx3TotalAmount = wx3TotalAmount == null ? 0 : wx3TotalAmount;
        aliTotalAmount = aliTotalAmount == null ? 0 : aliTotalAmount;
        appleTotalAmount = appleTotalAmount == null ? 0 : appleTotalAmount;
        jdTotalAmount = jdTotalAmount == null ? 0 : jdTotalAmount;
        yopTotalAmount = yopTotalAmount == null ? 0 : yopTotalAmount;
        haoDianTotalAmount = haoDianTotalAmount == null ? 0 : haoDianTotalAmount;
        //2.产品总收入。总金额－测试金额
        Map<String, Long> testChannelAmountMap = testAmountMap.get(Integer.valueOf(key));
        Long testAmount = 0l;
        Long wxTestAmount = 0l;
        Long wx2TestAmount = 0l;
        Long wx3TestAmount = 0l;
        Long aliTestAmount = 0l;
        Long appleTestAmount = 0l;
        Long jdTestAmount = 0l;
        Long yopTestAmount = 0l;
        Long haoDianTestAmount = 0l;
        if (testChannelAmountMap != null) {
            testAmount = testChannelAmountMap.get("total") == null ? 0l : testChannelAmountMap.get("total");
            wxTestAmount = testChannelAmountMap.get(CommonConstant.WX_PAY_CHANNEL_ID + "");
            wxTestAmount = wxTestAmount == null ? 0 : wxTestAmount;
            wx2TestAmount = testChannelAmountMap.get(CommonConstant.WX_PAY_CHANNEL_ID_1 + "");
            wx2TestAmount = wx2TestAmount == null ? 0 : wx2TestAmount;
            wx3TestAmount = testChannelAmountMap.get(CommonConstant.WX_PAY_CHANNEL_WX_JSAPI + "");
            wx3TestAmount = wx3TestAmount == null ? 0 : wx3TestAmount;
            aliTestAmount = testChannelAmountMap.get(CommonConstant.ALI_PAY_CHANNEL_ID + "");
            aliTestAmount = aliTestAmount == null ? 0 : aliTestAmount;
            appleTestAmount = testChannelAmountMap.get(CommonConstant.APPLE_PAY_CHANNEL_ID + "");
            appleTestAmount = appleTestAmount == null ? 0 : appleTestAmount;
            jdTestAmount = testChannelAmountMap.get(CommonConstant.JD_PAY_CHANNEL_ID + "");
            jdTestAmount = jdTestAmount == null ? 0 : jdTestAmount;
            yopTestAmount = testChannelAmountMap.get(CommonConstant.YOP_PAY_CHANNEL_ID + "");
            yopTestAmount = yopTestAmount == null ? 0 : yopTestAmount;
            haoDianTestAmount = testChannelAmountMap.get(CommonConstant.HAO_DIAN_PAY_CHANNEL_ID + "");
            haoDianTestAmount = haoDianTestAmount == null ? 0 : haoDianTestAmount;
        }

        Long realAmount = CommonUtil.subtract(totalAmount + "", testAmount + "").longValue();
        String orderAmount = CommonUtil.convertFen2Yuan(realAmount).toString();
        billVo.setOrderAmount(orderAmount);
        //3.产品分渠道收入

        Long wxRealAmount = CommonUtil.subtract(wxTotalAmount + "", wxTestAmount + "").longValue();
        String wxOrderAmount = CommonUtil.convertFen2Yuan(wxRealAmount).toString();
        billVo.setWxOrderAmount(wxOrderAmount);

        Long wx2RealAmount = CommonUtil.subtract(wx2TotalAmount + "", wx2TestAmount + "").longValue();
        String wx2OrderAmount = CommonUtil.convertFen2Yuan(wx2RealAmount).toString();
        billVo.setWx2OrderAmount(wx2OrderAmount);

        Long wx3RealAmount = CommonUtil.subtract(wx3TotalAmount + "", wx3TestAmount + "").longValue();
        String wx3OrderAmount = CommonUtil.convertFen2Yuan(wx3RealAmount).toString();
        billVo.setWx3OrderAmount(wx3OrderAmount);

        Long aliRealAmount = CommonUtil.subtract(aliTotalAmount + "", aliTestAmount + "").longValue();
        String aliOrderAmount = CommonUtil.convertFen2Yuan(aliRealAmount).toString();
        billVo.setAliOrderAmount(aliOrderAmount);

        Long appleRealAmount = CommonUtil.subtract(appleTotalAmount + "", appleTestAmount + "").longValue();
        String appleOrderAmount = CommonUtil.convertFen2Yuan(appleRealAmount).toString();
        billVo.setAppleOrderAmount(appleOrderAmount);

        Long jdRealAmount = CommonUtil.subtract(jdTotalAmount + "", jdTestAmount + "").longValue();
        String jdOrderAmount = CommonUtil.convertFen2Yuan(jdRealAmount).toString();
        billVo.setJingdongAmount(jdOrderAmount);

        Long yopRealAmount = CommonUtil.subtract(yopTotalAmount + "", yopTestAmount + "").longValue();
        String yopOrderAmount = CommonUtil.convertFen2Yuan(yopRealAmount).toString();
        billVo.setYibaoAmount(yopOrderAmount);

        Long haoDianRealAmount = CommonUtil.subtract(haoDianTotalAmount + "", haoDianTestAmount + "").longValue();
        String haoDianOrderAmount = CommonUtil.convertFen2Yuan(haoDianRealAmount).toString();
        billVo.setHaoDianAmount(haoDianOrderAmount);
        return billVo;
    }

    /* 获取测试库的账单*/
    public static BigDecimal getTestDbDate(Timestamp beginTime, Timestamp endTime, Integer channelId) {
        BigDecimal res = new BigDecimal(0);
        List<BillTestDbData> testDbData = new ArrayList<>();
        if (channelId.equals(CommonConstant.ALI_PAY_CHANNEL_ID)) {
            testDbData.add(new BillTestDbData("2018-03-14 14:08:24", 1l));
            testDbData.add(new BillTestDbData("2018-03-14 14:08:53", 1l));
            testDbData.add(new BillTestDbData("2018-04-11 16:03:11", 600l));
            testDbData.add(new BillTestDbData("2018-05-26 14:03:11", 2l));
            testDbData.add(new BillTestDbData("2018-05-28 16:22:29", 10l));
        } else if (channelId.equals(CommonConstant.WX_PAY_CHANNEL_ID)) {
            testDbData.add(new BillTestDbData("2018-01-02 21:25:40", 8l));
            testDbData.add(new BillTestDbData("2018-01-03 21:25:40", 1l));
            testDbData.add(new BillTestDbData("2018-01-04 21:25:40", 2l));
            testDbData.add(new BillTestDbData("2018-01-05 21:25:40", 1l));
        }

        for (BillTestDbData testDate : testDbData) {
            boolean addFlag = false;
            if (beginTime == null) {
                if (DateUtil.compareDate(testDate.getPayDate(), endTime)) {
                    addFlag = true;
                }
            } else {
                if (DateUtil.compareDate(beginTime, testDate.getPayDate()) && DateUtil.compareDate(testDate
                        .getPayDate(), endTime)) {
                    addFlag = true;
                }
            }
            if (addFlag) {
                res = res.add(new BigDecimal(testDate.getMoney()));
            }
        }
        return res;
    }

    /******* utils********/

    public static String getCallBackMethodStr(String remark) {
        if (StringUtils.isBlank(remark)) {
            return null;
        }
        Map<String, Object> resMap = JSONObject.parseObject(remark, HashMap.class);
        if (resMap == null || !resMap.containsKey("clazzMethodName")) {
            return null;
        }
        return resMap.get("clazzMethodName").toString();
    }
}
