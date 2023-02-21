package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.InternetCelebrityRecommendDao;
import com.mojieai.predict.entity.po.InternetCelebrityRecommend;
import com.mojieai.predict.util.DateUtil;
import com.yeepay.shade.com.google.common.collect.Maps;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class InternetCelebrityRecommendDaoImpl extends BaseDao implements InternetCelebrityRecommendDao {

    @Override
    public InternetCelebrityRecommend getRecentEnableRecommend(Long userId) {
        Map<String, Object> param = new HashMap<>();
        param.put("userId", userId);
        param.put("matchTime", DateUtil.getCurrentTimestamp());
        return sqlSessionTemplate.selectOne("InternetCelebrityRecommend.getRecentEnableRecommend", param);
    }

    @Override
    public List<Long> getAllCelebrityUser() {
        return sqlSessionTemplate.selectList("InternetCelebrityRecommend.getAllCelebrityUser");
    }

    @Override
    public List<InternetCelebrityRecommend> getHistoryLatestRecommend(Long userId, Integer count) {
        Map<String, Object> param = new HashMap<>();
        param.put("userId", userId);
        param.put("count", count);
        return sqlSessionTemplate.selectList("InternetCelebrityRecommend.getHistoryLatestRecommend", param);
    }

    @Override
    public List<InternetCelebrityRecommend> getAllShowCelebrities() {
        return slaveSqlSessionTemplate.selectList("InternetCelebrityRecommend.getAllShowCelebrities");
    }

    @Override
    public Integer getCelebrityRecommendCount(String recommendId) {
        Map<String, Object> param = new HashMap<>();
        param.put("recommendId", recommendId);
        return sqlSessionTemplate.selectOne("InternetCelebrityRecommend.getCelebrityRecommendCount", param);
    }

    @Override
    public Integer insert(InternetCelebrityRecommend internetCelebrityRecommend) {
        return sqlSessionTemplate.insert("InternetCelebrityRecommend.insert", internetCelebrityRecommend);
    }

    @Override
    public Integer update(InternetCelebrityRecommend internetCelebrityRecommend) {
        return sqlSessionTemplate.update("InternetCelebrityRecommend.update", internetCelebrityRecommend);
    }

    @Override
    public Integer updateRecommendLikeCount(String recommendId, Long setCount, Long originCount) {
        Map<String, Object> param = new HashMap<>();
        param.put("recommendId", recommendId);
        param.put("setCount", setCount);
        param.put("originCount", originCount);
        return sqlSessionTemplate.update("InternetCelebrityRecommend.updateRecommendLikeCount", param);
    }

    @Override
    public List<InternetCelebrityRecommend> getAllInternetCelebrityLastRecommend() {
        return sqlSessionTemplate.selectList("InternetCelebrityRecommend.getAllInternetCelebrityLastRecommend");
    }
}
