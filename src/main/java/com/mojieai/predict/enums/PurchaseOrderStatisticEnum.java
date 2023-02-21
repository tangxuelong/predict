package com.mojieai.predict.enums;

import com.mojieai.predict.cache.VipPriceCache;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.PurchaseOrderConstant;
import com.mojieai.predict.entity.po.PurchaseOrderStatistic;
import com.mojieai.predict.entity.po.VipPrice;
import com.mojieai.predict.service.impl.PurchaseStatisticBaseDao;
import com.mojieai.predict.util.DateUtil;
import org.apache.logging.log4j.Logger;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum PurchaseOrderStatisticEnum {
    VIP(PurchaseOrderConstant.PURCHASE_ORDER_CLASS_VIP) {
        //1.统计指定时间的订单数据
        @Override
        public List<PurchaseOrderStatistic> generatePurchaseOrder(PurchaseStatisticBaseDao statisticBaseDao,
                                                                  Timestamp statisticTime) {
            String statisticStr = DateUtil.formatTime(statisticTime, "yyyyMMdd");
            Integer statisticDate = Integer.valueOf(statisticStr);
            Timestamp begin = DateUtil.getBeginOfOneDay(statisticTime);
            Timestamp end = DateUtil.getEndOfOneDay(statisticTime);
            List<PurchaseOrderStatistic> result = initPruchaseOrderStatistic(statisticDate);

            //2.循环100张表
            for (int i = 1; i <= 100; i++) {
                String tempUserId = i + "";
                //2.1 下单总数和总金额
                List<Map> allOrderCount = statisticBaseDao.getAllOrderCountAndTotalAmount(tempUserId, begin, end);
                saveAllOrderCountAndAmount(result, allOrderCount);
                //2.2 下单人数
                List<Map> orderPersonCount = statisticBaseDao.getOrderPersonCount(tempUserId, begin, end);
                saveOrderPersonCount(result, orderPersonCount);
                //2.3 付款订单数
                List<Map> paySuccessOrderCount = statisticBaseDao.getPaySuccessOrderCount(tempUserId, begin, end);
                savePaySuccessOrderCount(result, paySuccessOrderCount);
                //2.4 付款成功人数
                List<Map> paySuccessPersonCount = statisticBaseDao.getPaySuccessPersonCount(tempUserId, begin, end);
                savePaySuccessPersonCount(result, paySuccessPersonCount);
                //2.5 首次下单人数和支付金额
                List<Map> fisrtOrderInfos = statisticBaseDao.getFisrtOrderPersons(tempUserId);
                List<Map<String, Integer>> tempFirst = filterAndSumFirstOrderInfs(fisrtOrderInfos, begin, end);
                saveFirstOrderInfos(result, tempFirst, PurchaseOrderConstant.FIRST_ORDER_COUNT_TYPE);
                //2.6首次支付人数
                List<Map> fisrtPayInfos = statisticBaseDao.getFisrtPayPersons(tempUserId);
                List<Map<String, Integer>> tempFirstPay = filterAndSumFirstOrderInfs(fisrtPayInfos, begin, end);
                saveFirstOrderInfos(result, tempFirstPay, PurchaseOrderConstant.FIRST_PAY_COUNT_TYPE);
            }
            return result;
        }

        private List<PurchaseOrderStatistic> initPruchaseOrderStatistic(Integer statisticDate) {
            List<PurchaseOrderStatistic> result = new ArrayList<>();
            List<VipPrice> prices = VipPriceCache.getAllRealMoneyPriceByClientType(CommonConstant
                    .CLIENT_TYPE_ANDRIOD, null);
            for (VipPrice price : prices) {
                PurchaseOrderStatistic temp = new PurchaseOrderStatistic();
                temp.setOrderClass(getClassType());
                temp.setOrderType(price.getVipPriceId());
                temp.setStatisticDate(statisticDate);
                result.add(temp);
            }
            return result;
        }
    };

    private static List<Map<String, Integer>> initFisrtOrderInfosMap() {
        List<Map<String, Integer>> result = new ArrayList<>();
        List<VipPrice> prices = VipPriceCache.getAllRealMoneyPriceByClientType(CommonConstant.CLIENT_TYPE_ANDRIOD, null);
        for (VipPrice price : prices) {
            Map temp = new HashMap();
            temp.put(PurchaseOrderConstant.TRANSACTION_DAYS, price.getVipDate());
            temp.put("FIRST_ORDER_COUNT", 0);
            temp.put("FIRST_PAY_COUNT", 0);
            result.add(temp);
        }
        return result;
    }

    /* 将查询出来的所有人第一次下单数据汇总*/
    private static List<Map<String, Integer>> filterAndSumFirstOrderInfs(List<Map> fisrtOrderInfos,
                                                                         Timestamp begin, Timestamp end) {
        List<Map<String, Integer>> result = initFisrtOrderInfosMap();

        for (Map tempOrderInfos : fisrtOrderInfos) {
            if (tempOrderInfos != null && tempOrderInfos.containsKey("create_time") && tempOrderInfos.get("create_time")
                    != null) {
                Timestamp createTime = (Timestamp) tempOrderInfos.get("create_time");
                Integer transactionDays = (Integer) tempOrderInfos.get(PurchaseOrderConstant.TRANSACTION_DAYS);

                if (DateUtil.compareDate(begin, createTime) && DateUtil.compareDate(createTime, end)) {
                    //循环给result赋值
                    for (Map<String, Integer> tempRes : result) {
                        Integer tempTransactionDay = tempRes.get(PurchaseOrderConstant.TRANSACTION_DAYS);
                        if (transactionDays.equals(tempTransactionDay)) {
                            Integer firstOrderCount = tempRes.get("FIRST_ORDER_COUNT") + 1;
                            tempRes.put("FIRST_ORDER_COUNT", firstOrderCount);
                        }
                    }
                }
            }
        }
        return result;
    }

    private static void saveFirstOrderInfos(List<PurchaseOrderStatistic> result, List<Map<String, Integer>>
            fisrtOrderInfos, Integer type) {
        for (PurchaseOrderStatistic temp : result) {
            VipPrice price = VipPriceCache.getVipPriceById(temp.getOrderType(), CommonConstant.CLIENT_TYPE_ANDRIOD);
            if (price == null || price.getVipDate() == null) {
                log.error("PurchaseOrderStatisticEnum ->saveFirstOrderInfos error price is null");
                continue;
            }

            Integer firstOrderCount = temp.getFirstOrderPersonCount();
            if (type == PurchaseOrderConstant.FIRST_PAY_COUNT_TYPE) {
                firstOrderCount = temp.getFisrtPayPersonCount();
            }
            for (Map<String, Integer> map : fisrtOrderInfos) {
                Integer transactionDate = map.get(PurchaseOrderConstant.TRANSACTION_DAYS);
                if (price.getVipDate().equals(transactionDate)) {
                    if (type == PurchaseOrderConstant.FIRST_ORDER_COUNT_TYPE) {
                        firstOrderCount += map.get("FIRST_ORDER_COUNT");
                    } else if (type == PurchaseOrderConstant.FIRST_PAY_COUNT_TYPE) {
                        firstOrderCount += map.get("FIRST_ORDER_COUNT");
                    }
                }
            }
            //设置值
            if (type == PurchaseOrderConstant.FIRST_ORDER_COUNT_TYPE) {
                temp.setFirstOrderPersonCount(firstOrderCount);
            } else if (type == PurchaseOrderConstant.FIRST_PAY_COUNT_TYPE) {
                temp.setFisrtPayPersonCount(firstOrderCount);
            }

        }
    }

    private static void savePaySuccessPersonCount(List<PurchaseOrderStatistic> result, List<Map>
            paySuccessPersonCount) {
        for (PurchaseOrderStatistic temp : result) {
            VipPrice price = VipPriceCache.getVipPriceById(temp.getOrderType(), CommonConstant.CLIENT_TYPE_ANDRIOD);
            if (price == null || price.getVipDate() == null) {
                log.error("PurchaseOrderStatisticEnum ->saveAllOrderCountAndAmount error price is null");
                continue;
            }

            Integer personCount = temp.getPayPersonCount();
            for (Map map : paySuccessPersonCount) {
                Integer transactionDate = (Integer) map.get(PurchaseOrderConstant.TRANSACTION_DAYS);
                if (price.getVipDate().equals(transactionDate)) {
                    if (map.get(PurchaseOrderConstant.PERSON_COUNT) != null) {
                        personCount += Integer.valueOf(map.get(PurchaseOrderConstant.PERSON_COUNT).toString());
                    }
                }
            }
            temp.setPayPersonCount(personCount);
        }
    }

    private static void savePaySuccessOrderCount(List<PurchaseOrderStatistic> result, List<Map> paySuccessOrderCount) {
        for (PurchaseOrderStatistic temp : result) {
            VipPrice price = VipPriceCache.getVipPriceById(temp.getOrderType(), CommonConstant.CLIENT_TYPE_ANDRIOD);
            if (price == null || price.getVipDate() == null) {
                log.error("PurchaseOrderStatisticEnum ->saveAllOrderCountAndAmount error price is null");
                continue;
            }

            Integer orderPayCount = temp.getPayCount();
            Integer orderTotalPayAmount = temp.getTotalPayAmount();
            for (Map map : paySuccessOrderCount) {
                Integer transactionDate = (Integer) map.get(PurchaseOrderConstant.TRANSACTION_DAYS);
                if (price.getVipDate().equals(transactionDate)) {
                    if (map.get(PurchaseOrderConstant.ORDER_PAY_COUNT) != null) {
                        orderPayCount += Integer.valueOf(map.get(PurchaseOrderConstant.ORDER_PAY_COUNT).toString());
                    }
                    if (map.get(PurchaseOrderConstant.ORDER_TOTAL_AMOUNT) != null) {
                        orderTotalPayAmount += Double.valueOf(map.get(PurchaseOrderConstant.ORDER_TOTAL_AMOUNT)
                                .toString()).intValue();
                    }
                }
            }
            temp.setPayCount(orderPayCount);
            temp.setTotalPayAmount(orderTotalPayAmount);
        }
    }

    private static void saveOrderPersonCount(List<PurchaseOrderStatistic> result, List<Map> orderPersonCount) {
        for (PurchaseOrderStatistic temp : result) {
            VipPrice price = VipPriceCache.getVipPriceById(temp.getOrderType(), CommonConstant.CLIENT_TYPE_ANDRIOD);
            if (price == null || price.getVipDate() == null) {
                log.error("PurchaseOrderStatisticEnum ->saveAllOrderCountAndAmount error price is null");
                continue;
            }

            Integer orderPersonCount1 = temp.getOrderPersonCount();
            for (Map map : orderPersonCount) {
                Integer transactionDate = (Integer) map.get(PurchaseOrderConstant.TRANSACTION_DAYS);
                if (price.getVipDate().equals(transactionDate)) {
                    if (map.get(PurchaseOrderConstant.PERSON_COUNT) != null) {
                        orderPersonCount1 += Integer.valueOf(map.get(PurchaseOrderConstant.PERSON_COUNT).toString());
                    }
                }
            }
            temp.setOrderPersonCount(orderPersonCount1);
        }
    }

    private static void saveAllOrderCountAndAmount(List<PurchaseOrderStatistic> result, List<Map> allOrderCount) {
        for (PurchaseOrderStatistic temp : result) {
            VipPrice price = VipPriceCache.getVipPriceById(temp.getOrderType(), CommonConstant.CLIENT_TYPE_ANDRIOD);
            if (price == null || price.getVipDate() == null) {
                log.error("PurchaseOrderStatisticEnum ->saveAllOrderCountAndAmount error price is null");
                continue;
            }
            Integer orderCount = temp.getOrderCount();
            Integer totalAmount = temp.getTotalOrderAmount();
            Integer orderPayAmount = temp.getTotalPayAmount();
            for (Map map : allOrderCount) {
                Integer transactionDate = (Integer) map.get(PurchaseOrderConstant.TRANSACTION_DAYS);
                if (price.getVipDate().equals(transactionDate)) {
                    if (map.get(PurchaseOrderConstant.ORDER_PAY_COUNT) != null) {
                        orderCount += Integer.valueOf(map.get(PurchaseOrderConstant.ORDER_PAY_COUNT).toString());
                    }
                    if (map.get(PurchaseOrderConstant.ORDER_TOTAL_AMOUNT) != null) {
                        totalAmount += Double.valueOf(map.get(PurchaseOrderConstant.ORDER_TOTAL_AMOUNT).toString())
                                .intValue();
                    }
                }
            }
            temp.setOrderCount(orderCount);
            temp.setTotalOrderAmount(totalAmount);
        }
    }

    private Integer classType;

    PurchaseOrderStatisticEnum(Integer classType) {
        this.classType = classType;
    }


    public Integer getClassType() {
        return classType;
    }

    private static final Logger log = CronEnum.PERIOD.getLogger();

    public List<PurchaseOrderStatistic> generatePurchaseOrder(PurchaseStatisticBaseDao statisticBaseDao, Timestamp
            statisticTime) {
        throw new AbstractMethodError("No method generatePurchaseOrder");
    }
}
