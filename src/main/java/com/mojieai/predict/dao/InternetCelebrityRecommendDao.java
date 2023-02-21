package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.InternetCelebrityRecommend;

import java.util.List;

public interface InternetCelebrityRecommendDao {

    InternetCelebrityRecommend getRecentEnableRecommend(Long userId);

    List<Long> getAllCelebrityUser();

    List<InternetCelebrityRecommend> getHistoryLatestRecommend(Long userId, Integer count);

    List<InternetCelebrityRecommend> getAllShowCelebrities();

    Integer getCelebrityRecommendCount(String recommendId);

    Integer insert(InternetCelebrityRecommend internetCelebrityRecommend);

    Integer update(InternetCelebrityRecommend internetCelebrityRecommend);

    Integer updateRecommendLikeCount(String recommendId, Long setCount, Long originCount);

    List<InternetCelebrityRecommend> getAllInternetCelebrityLastRecommend();
}
