package com.mojieai.predict.service;

import com.mojieai.predict.entity.po.UserSocialRecord;
import com.mojieai.predict.entity.vo.AchievementVo;

import java.util.List;
import java.util.Map;


public interface UserSocialRecordService {

    List<UserSocialRecord> getUserLastestSocialRecords(long gameId, Long userId, Integer socialType);

    Map<String, List<AchievementVo>> getUserAchievementVo(long gameId, Long userId);
}
