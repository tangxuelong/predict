package com.mojieai.predict.service.impl;

import com.mojieai.predict.cache.SocialLevelIntegralCache;
import com.mojieai.predict.entity.vo.SocialLevelIntegralVo;
import com.mojieai.predict.service.SocialLevelIntegralService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SocialLevelIntegralServiceImpl implements SocialLevelIntegralService {

    @Override
    public List<Map> getSocialLevelIntegrals() {
        List<Map> result = new ArrayList<>();
        List<SocialLevelIntegralVo> socialLevels = SocialLevelIntegralCache.getAllSocialLevels();
        for (SocialLevelIntegralVo temp : socialLevels) {
            Map tempMap = convertSocialLevelIntegralVo2Map(temp);
            if (tempMap != null) {
                result.add(tempMap);
            }
        }
        return result;
    }

    private Map convertSocialLevelIntegralVo2Map(SocialLevelIntegralVo temp) {
        Map result = new HashMap();
        result.put("levelImg", temp.getBigImg());
        result.put("titleName", temp.getTitleName());
        result.put("score", temp.getMinIntegral());
        return result;
    }
}
