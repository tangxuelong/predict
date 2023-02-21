package com.mojieai.predict.cache;


import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.dao.PayClientChannelDao;
import com.mojieai.predict.entity.po.PayClientChannel;
import com.mojieai.predict.exception.BusinessException;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PayClientChannelCache {
    private static final Logger log = LogConstant.commonLog;

    private static Map<String, PayClientChannel> payClientChannelHashMap = new HashMap<>();

    @Autowired
    private PayClientChannelDao payClientChannelDao;

    private PayClientChannelCache() {
    }

    public void init() {
        log.info("init PayClientChannelCache");
        refresh();
    }

    public void refresh() {
        List<PayClientChannel> payClientChannels = payClientChannelDao.getAllClientChannel();
        if (payClientChannels != null) {
            for (PayClientChannel payClientChannel : payClientChannels) {
                // clientID:channelID = key
                payClientChannelHashMap.put(String.valueOf(payClientChannel.getClientId()) + CommonConstant
                        .COMMON_COLON_STR + String.valueOf(payClientChannel.getChannelId()), payClientChannel);
            }
        }
        log.info("refresh " + (payClientChannels == null ? 0 : payClientChannels.size()) + " PayClientChannelCache");
    }

    public static PayClientChannel getClientChannel(String key) {
        if (!payClientChannelHashMap.containsKey(key)) {
            throw new BusinessException("no PayClientChannelCache found for :" + key);
        }
        return payClientChannelHashMap.get(key);
    }
}