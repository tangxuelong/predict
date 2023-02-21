package com.mojieai.predict.service.impl;

import com.mojieai.predict.cache.VipPriceCache;
import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.dao.PurchaseOrderStatisticDao;
import com.mojieai.predict.dao.VipOperateFollowDao;
import com.mojieai.predict.entity.po.PurchaseOrderStatistic;
import com.mojieai.predict.entity.po.VipPrice;
import com.mojieai.predict.enums.PurchaseOrderStatisticEnum;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.PurchaseOrderStatisticService;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.VipUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.*;

@Service
public class PurchaseOrderStatisticServiceImpl implements PurchaseOrderStatisticService {

    @Autowired
    private PurchaseOrderStatisticDao purchaseOrderStatisticDao;
    @Autowired
    private VipOperateFollowDao vipOperateFollowDao;
    @Autowired
    private RedisService redisService;

    @Override
    public Map<String, Object> getPurchaseOrderStatisticInfo(Integer beginTime, Integer endTime, Integer orderClass) {
        Map<String, Object> res = new HashMap<>();
        List<PurchaseOrderStatistic> orderStatistics = purchaseOrderStatisticDao.getPurchaseOrderStatisticByTime
                (beginTime, endTime, orderClass);

        Map<String, Object> statisticData = new HashMap<>();
        Map<String, PurchaseOrderStatistic> dateSumOrderStatistic = new HashMap();
        PurchaseOrderStatistic statisticAllSum = null;
        for (PurchaseOrderStatistic orderStatistic : orderStatistics) {
            PurchaseOrderStatistic tempDateOrderStatistic = null;
            List<Map<String, Object>> someDateDatas = null;
            Map<String, Object> oneStatisticData = convertOrderStatic2Map(orderStatistic);
            if (oneStatisticData == null) {
                continue;
            }
            String key = String.valueOf(orderStatistic.getStatisticDate());
            //1.统计每一天的各个类型订单
            if (statisticData.isEmpty() || !statisticData.containsKey(key)) {
                someDateDatas = new ArrayList<>();
            } else {
                someDateDatas = (List<Map<String, Object>>) statisticData.get(key);
            }
            someDateDatas.add(oneStatisticData);
            statisticData.put(key, someDateDatas);
            //2.统计每一天综合
            if (dateSumOrderStatistic.isEmpty() || dateSumOrderStatistic.get(key) == null) {
                tempDateOrderStatistic = new PurchaseOrderStatistic();
                BeanUtils.copyProperties(orderStatistic, tempDateOrderStatistic);
                tempDateOrderStatistic = orderStatistic;
            } else {
                tempDateOrderStatistic = dateSumOrderStatistic.get(key);
                tempDateOrderStatistic.addOrderStatisOrderData(orderStatistic);
            }
            dateSumOrderStatistic.put(key, tempDateOrderStatistic);
            //3.统计所有的综合
            if (statisticAllSum == null) {
                statisticAllSum = new PurchaseOrderStatistic();
                BeanUtils.copyProperties(orderStatistic, statisticAllSum);
            } else {
                statisticAllSum.addOrderStatisOrderData(orderStatistic);
            }
        }
        //将map转为list
        List<Map> statisticDatas = new ArrayList<>();
        for (Map.Entry<String, Object> statisticMap : statisticData.entrySet()) {
            Map<String, Object> temp = new HashMap<>();
            temp.put("dateName", statisticMap.getKey());
            Map<String, Object> dateSum = convertOrderStatic2Map(dateSumOrderStatistic.get(statisticMap.getKey()));
            dateSum.put("goodName", statisticMap.getKey());
            temp.put("dateSum", dateSum);
            temp.put("data", statisticMap.getValue());
            statisticDatas.add(temp);
        }
        Collections.sort(statisticDatas, (n1, n2) -> Integer.valueOf(n2.get("dateName").toString()).compareTo
                (Integer.valueOf(n1.get("dateName").toString())));

        Map<String, Object> statisticSum = convertOrderStatic2Map(statisticAllSum);
        statisticSum.put("goodName", "合计");
        res.put("statisticSum", statisticSum);
        res.put("statisticDatas", statisticDatas);
        return res;
    }

    @Override
    public void statisticPurchaseOrderTiming() {
        String statisDateStr = DateUtil.getYesterday("yyyy-MM-dd HH:mm:ss");
        String key = RedisConstant.getStatisticPurchaseOrderKey();
        String lastStatisDateStr = redisService.kryoGet(key, String.class);
        if (lastStatisDateStr != null && lastStatisDateStr.equals(statisDateStr)) {
            return;
        }
        Timestamp statisticDate = DateUtil.formatToTimestamp(statisDateStr, "yyyy-MM-dd HH:mm:ss");
        if (rebuildOneDateStatistic(statisticDate)) {
            redisService.kryoSetEx(key, 17280, statisDateStr);
        }
    }

    @Override
    public boolean rebuildOneDateStatistic(Timestamp statisticDate) {
        int flag = 0;
        for (PurchaseOrderStatisticEnum orderStatisticEnum : PurchaseOrderStatisticEnum.values()) {
            List<PurchaseOrderStatistic> purchaseOrderStatistics = orderStatisticEnum.generatePurchaseOrder
                    (vipOperateFollowDao, statisticDate);
            for (PurchaseOrderStatistic temp : purchaseOrderStatistics) {
                try {
                    int res = purchaseOrderStatisticDao.insert(temp);
                    if (res <= 0) {
                        flag++;
                    }
                } catch (DuplicateKeyException e) {
                    continue;
                }
            }
        }
        if (flag == 0) {
            return true;
        }
        return false;
    }

    private Map<String, Object> convertOrderStatic2Map(PurchaseOrderStatistic orderStatistic) {
        if (orderStatistic == null) {
            return new HashMap<>();
        }
        Map<String, Object> res = new HashMap<>();

        VipPrice vipPrice = VipPriceCache.getVipPriceById(orderStatistic.getOrderType());
        String payRatio = calcuRatio(orderStatistic.getPayPersonCount(), orderStatistic.getOrderPersonCount());
        //首次下单
        String fisrtPayRatio = calcuRatio(orderStatistic.getFisrtPayPersonCount(), orderStatistic
                .getFirstOrderPersonCount());
        //人均消费
        String personAverageAmount = CommonUtil.divideByBigDecimal(orderStatistic.getTotalPayAmount(), orderStatistic
                .getPayPersonCount());

        res.put("goodName", VipUtils.convertDateShow(vipPrice.getVipDate()));
        res.put("orderPersonCount", orderStatistic.getOrderPersonCount());
        res.put("payPersonCount", orderStatistic.getPayPersonCount());
        res.put("payRatio", payRatio);
        res.put("firstOrderPersonCount", orderStatistic.getFirstOrderPersonCount());
        res.put("fisrtPayPersonCount", orderStatistic.getFisrtPayPersonCount());
        res.put("fisrtPayRatio", fisrtPayRatio);
        res.put("orderCount", orderStatistic.getOrderCount());
        res.put("payCount", orderStatistic.getPayCount());
        res.put("totalOrderAmount", CommonUtil.convertFen2Yuan(orderStatistic.getTotalOrderAmount()));
        res.put("totalPayAmount", CommonUtil.convertFen2Yuan(orderStatistic.getTotalPayAmount()));
        res.put("personAverageAmount", CommonUtil.convertFen2Yuan(personAverageAmount));
        return res;
    }

    private String calcuRatio(Integer div1, Integer div2) {
        String ratio = "0%";
        if (div2 != null && div2 != 0) {
            ratio = new BigDecimal(div1).divide(new BigDecimal(div2), 2, RoundingMode.HALF_UP).multiply(new
                    BigDecimal(100)).toString() + "%";
        }
        return ratio;
    }
}
