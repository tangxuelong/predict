package com.mojieai.predict.cache;


import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.dao.PayChannelInfoDao;
import com.mojieai.predict.entity.po.PayChannelInfo;
import com.mojieai.predict.exception.BusinessException;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PayChannelInfoCache {
    private static final Logger log = LogConstant.commonLog;

    private static Map<Integer, PayChannelInfo> payChannelInfoMap = new HashMap<>();

    @Autowired
    private PayChannelInfoDao payChannelInfoDao;

    private PayChannelInfoCache() {
    }

    public void init() {
        log.info("init PayChannelInfoCache");
        refresh();
    }

    public void refresh() {
        List<PayChannelInfo> payChannelInfoList = payChannelInfoDao.getAllChannel();
        if (payChannelInfoList != null) {
            for (PayChannelInfo payChannelInfo : payChannelInfoList) {
                payChannelInfoMap.put(payChannelInfo.getChannelId(), payChannelInfo);
            }
        }
        log.info("refresh " + (payChannelInfoList == null ? 0 : payChannelInfoList.size()) + " PayChannelInfoCache");
    }

    public static PayChannelInfo getChannelInfo(Integer key) {
        if (!payChannelInfoMap.containsKey(key)) {
            throw new BusinessException("no payChannelInfoMap found for :" + key);
        }
        return payChannelInfoMap.get(key);
    }
}