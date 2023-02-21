package com.mojieai.predict.service.impl;

import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.constant.ResultConstant;
import com.mojieai.predict.dao.MatchTagDao;
import com.mojieai.predict.entity.po.MatchTag;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.MatchInfoService;
import com.mojieai.predict.service.MatchTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MatchTagServiceImpl implements MatchTagService {

    @Autowired
    private MatchTagDao matchTagDao;
    @Autowired
    private RedisService redisService;
    @Autowired
    private MatchInfoService matchInfoService;


    @Override
    public Map<String, Object> getAllMatchTagsForAdmin(Integer status) {
        Map<String, Object> result = new HashMap<>();

        List<MatchTag> tags = matchTagDao.getAllMatchTagIncludeEnable(status);
        result.put("tags", tags);
        return result;
    }

    @Override
    public Map<String, Object> addMatchTag(MatchTag matchTag) {
        Map<String, Object> result = new HashMap<>();
        Integer code = ResultConstant.ERROR;
        String msg = ResultConstant.SUCCESS_SAVE_ERROR;
        if (matchTag.getTagId() != null) {
            if (matchTagDao.getMatchTag(matchTag.getTagId()) != null) {
                matchTagDao.update(matchTag);
            }
        } else {
            if (matchTagDao.insert(matchTag) > 0) {
                code = ResultConstant.SUCCESS;
                msg = ResultConstant.SUCCESS_SAVE_MSG;
                Thread refreshThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        matchInfoService.buildNewMatchTagTimeLine(matchTag.getTagId());
                    }
                });
                refreshThread.start();
            }
        }
        //刷新tags
        String matchTagKey = RedisConstant.getMatchTagKey();
        redisService.del(matchTagKey);

        result.put("code", code);
        result.put("msg", msg);
        return result;
    }
}
