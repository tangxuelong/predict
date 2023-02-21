package com.mojieai.predict.cache;


import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.dao.PayClientVersionControlDao;
import com.mojieai.predict.entity.po.PayClientVersionControl;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PayClientVersionControlCache {
    private static final Logger log = LogConstant.commonLog;

    private static Map<String, PayClientVersionControl> payClientVersionControlHashMap = new HashMap<>();

    @Autowired
    private PayClientVersionControlDao payClientVersionControlDao;

    private PayClientVersionControlCache() {
    }

    public void init() {
        log.info("init activityIni");
        refresh();
    }

    public void refresh() {
        List<PayClientVersionControl> payClientVersionControls = payClientVersionControlDao
                .getAllPayClientVersionControl();
        if (payClientVersionControls != null) {
            // clientId:channelId:versionControl
            for (PayClientVersionControl payClientVersionControl : payClientVersionControls) {
                payClientVersionControlHashMap.put(String.valueOf(payClientVersionControl.getClientId()) +
                        CommonConstant.COMMON_COLON_STR + String.valueOf(payClientVersionControl.getChannelId
                        ()) + CommonConstant.COMMON_COLON_STR + String.valueOf(payClientVersionControl.getVersionCode
                        ()), payClientVersionControl);
            }
        }
        log.info("refresh " + (payClientVersionControls == null ? 0 : payClientVersionControls.size()) + " " +
                "PayClientVersionControlCache");
    }

    public static PayClientVersionControl getActivityIniValue(String key) {
        if (!payClientVersionControlHashMap.containsKey(key)) {
            return null;
        }
        return payClientVersionControlHashMap.get(key);
    }
}