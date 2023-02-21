package com.mojieai.predict.service;


import com.mojieai.predict.entity.bo.PaginationList;
import com.mojieai.predict.entity.bo.SocialKillNumFilter;
import com.mojieai.predict.entity.po.ActivityIni;
import com.mojieai.predict.entity.po.Banner;
import com.mojieai.predict.entity.po.PredictNumbersOperate;
import com.mojieai.predict.entity.po.SocialEncircle;

import java.util.List;
import java.util.Map;

/**
 * Created by Ynght on 2016/11/25.
 */
public interface AdminService {
    List<Banner> queryBanner();

    String login(String mobile, String password);

    void setAdmin(String mobile);

    Banner bannerAdd(Banner banner, String isNew);

    List<ActivityIni> getAllActivityInis();

    void activityIniAddOrUpdate(ActivityIni ini);

    void updateActivityIni(Map<String, String> keyValueMap);

    Map<String, Object> setPeriodManualRule(String gameEn, String ruleStr);

    Map<String, Object> updateOperateStatus(long gameId, String periodId, Integer status);

    List<PredictNumbersOperate> getOpertePredict(Long gameId, String minPeriodId, String maxPeriodId, String
            manualFlag);

    String updateUserPredictNums(String mobile, Long gameId, Integer addNums);

    Integer getUserPredictNums(String mobile, Long gameId);

    PaginationList<SocialEncircle> getSocialEncircleList(long gameId, String periodId, Integer page, SocialKillNumFilter
            socialKillNumFilter);

    void sendPushSms(String typeStr,String msg);
}
