package com.mojieai.predict.service.impl;

import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.MarqueeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MarqueeServiceImpl implements MarqueeService {

    @Autowired
    private RedisService redisService;

    @Override
    public List<Map<String, Object>> getRecentMarqueeInfo() {
        String zqMarqueeKey = RedisConstant.getFootballMarqueeKey();
        List res = redisService.kryoLRange(zqMarqueeKey, -50l, -1l, HashMap.class);
        return res;
    }

    @Override
    public void saveContent2Marquee(String marqueeTitle, String pushUrl) {
        if (StringUtils.isNotBlank(marqueeTitle)) {
            String zqMarqueeKey = RedisConstant.getFootballMarqueeKey();
            Map<String, Object> temp = new HashMap();
            temp.put("desc", marqueeTitle);
            temp.put("pushUrl", pushUrl);
            redisService.kryoRPush(zqMarqueeKey, temp);
            if (redisService.llen(zqMarqueeKey) > 100) {
                redisService.kryoLPop(zqMarqueeKey, HashMap.class);
            }
        }
    }


}
