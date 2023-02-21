package com.mojieai.predict.service;

import com.mojieai.predict.entity.vo.UserTitleVo;

import java.util.List;
import java.util.Map;

public interface UserTitleService {

    Boolean distributeTitle2User(long gameId, Long userId, String titleEn, String dateStr, Integer date);

    Boolean checkUserTitle(long gameId, Long userId, String titleEn);

    List<String> getUserGodList(long gameId, Long userId, String versionCode);

    Map<String, Object> getUserTitleDetail(long gameId, Long userId);

    Boolean updateUserTitleAndInsertLog(long gameId, Long userId, Integer titleId, Integer date, String userTitleLogId);

    UserTitleVo refreshUserTitleRedis(long gameId, Long userId);

    void distributeTitleTiming();
}
