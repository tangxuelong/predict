package com.mojieai.predict.service;

import com.mojieai.predict.entity.bo.SocialKillNumFilter;
import com.mojieai.predict.entity.bo.UserEncircleInfo;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.po.SocialEncircle;
import com.mojieai.predict.entity.po.SocialUserFollow;
import com.mojieai.predict.entity.vo.MyEncircleVo;

import java.util.List;
import java.util.Map;

public interface SocialEncircleCodeService {

    Long generateEncircleCodeId();

    void checkSocialKillNumList();

    void updateKilledEncircleList();

    void rebuildKillNumListRedis(Long gameId, Integer count, String periodId);

    List<MyEncircleVo> rebuildPeriodHotEncircle(long gameId, String periodId);

    Integer getPeriodEncircleStatus(long gameId, String periodId);

    Map<String, Object> addEncircleCode(long gameId, String periodId, long userId, String encircleNums, Integer
            encircleCount, String killCount, String versionCode, String clientIp, Integer clientId);

    Map<String, Object> getMyEncircle(long gameId, long userId, Integer page, Integer socialCodeType, Integer
            lastIndexId);

    Map<String, Object> getEncircleIndex(long gameId, Integer encircleType, Long userId);

    Map<String, Object> getKillNumList(long gameId, Integer page, Integer partakeCounts, String encircleCounts, String
            killNumCount, Long killNumUserId, String encircleListType);

    Map<String, Object> getKillNumListByPeriodId(long gameId, Integer lastIndex, String periodId, Long userId,
                                                 SocialKillNumFilter socialKillNumFilter, String versionCode);

    void updateKillNumList(long gameId, String periodId);

    Integer addEncircleNumAndIndexUserSocial(SocialEncircle socialEncircle, String taskId);

    SocialEncircle getSocialEncircleByEncircleId(long gameId, String periodId, Long encircleId);

    void reCalculateIndexUserSocialRightNums(long gameId, String beginPeriod, String endPeriod);

    Map<String, Object> getMyEncirclesV2_3(long gameId, Long userId, Long lookUpUserId, Integer lastIndex);

    List<UserEncircleInfo> getFollowsCurrentEncircleList(GamePeriod gamePeriod, List<SocialUserFollow> follows,
                                                         Long userId);

    UserEncircleInfo convertSocialEncircle2EncircleInfo(SocialEncircle socialEncircle, Map<String, Integer>
            socialKillAwardLevel);

    Integer updateHotEncircleType(Long gameId, String periodId, Long encircleId, Integer isHot);

    List<UserEncircleInfo> saveUserEncircleInfo2Redis(SocialEncircle socialEncircle, Map<String, Integer>
            socialKillAwardLevel);

    List<SocialEncircle> getAllEncircle(long gameId, String periodId);

    MyEncircleVo packageMyEncircleVo(SocialEncircle socialEncircle, Map<String, Integer> socialAwardLevel);

    Map<String, Object> getSocialBigData(long gameId);

    void packageEncircleInfo(long gameId, List<UserEncircleInfo> userEncircleInfos, Long userId);

    Map<String, Object> userSocialRecords(long gameId, Long userId, Long lookUpUserId, String
            versionCode, Integer type, Boolean enHasNext, Boolean killHasNext, Integer enLastIndex, Integer
                                                  killLastIndex);

}
