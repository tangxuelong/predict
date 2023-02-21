package com.mojieai.predict.dao;

import com.mojieai.predict.entity.bo.PaginationList;
import com.mojieai.predict.entity.po.IndexUserSocialCode;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public interface IndexUserSocialCodeDao {

    Integer insert(IndexUserSocialCode indexUserSocialCode);

    PaginationList<IndexUserSocialCode> getIndexUserSocialCodeByGameIdAndUserIdByPage(Long gameId, Long userId,
                                                                                      Integer page, Integer
                                                                                              socialCodeType, Integer
                                                                                              pageSize);

    IndexUserSocialCode getUserMaxScoreEncircleByTime(long gameId, Long userId, Timestamp beginTime, Timestamp endTime);

    Boolean ifUserKillEncircle(Long gameId, Long userId, Long encircleId, Integer socialCodeType);

    List<IndexUserSocialCode> getUserPartTakePeriodId(Integer periodCount, Integer socialCodeType, Long userId,
                                                      Integer indexId);

    List<IndexUserSocialCode> getIndexSocialByCondition(Long gameId, String periodId, Long userId, Integer
            socialCodeType);

    List<Map> getUserAwardIndexSocials(Long gameId, Long userId, Integer total, Integer socialType, String periodId);

    void updateRightNums(long gameId, Long userId, String periodId, Integer socialCodeType, Long socialCodeId,
                         Integer rightNums, Integer socialCount);
}
