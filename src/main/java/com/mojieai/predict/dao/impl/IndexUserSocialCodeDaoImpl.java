package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.IndexUserSocialCodeDao;
import com.mojieai.predict.entity.bo.PaginationInfo;
import com.mojieai.predict.entity.bo.PaginationList;
import com.mojieai.predict.entity.po.IndexUserSocialCode;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class IndexUserSocialCodeDaoImpl extends BaseDao implements IndexUserSocialCodeDao {
    @Override
    public Integer insert(IndexUserSocialCode indexUserSocialCode) {
        return sqlSessionTemplate.insert("IndexUserSocialCode.insert", indexUserSocialCode);
    }

    @Override
    public PaginationList<IndexUserSocialCode> getIndexUserSocialCodeByGameIdAndUserIdByPage(Long gameId, Long
            userId, Integer page, Integer socialCodeType, Integer pageSize) {
        if (pageSize == null) {
            pageSize = PaginationInfo.defaultRecordPerPage;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("userId", userId);
        params.put("socialCodeType", socialCodeType);
        PaginationInfo paginationInfo = new PaginationInfo(page, pageSize);
        return selectPaginationList("IndexUserSocialCode.getIndexUserSocialCodeByGameIdAndUserIdByPage", params,
                paginationInfo);
    }

    @Override
    public IndexUserSocialCode getUserMaxScoreEncircleByTime(long gameId, Long userId, Timestamp beginTime, Timestamp
            endTime) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("userId", userId);
        params.put("socialCodeType", 0);
        params.put("beginTime", beginTime);
        params.put("endTime", endTime);
        return sqlSessionTemplate.selectOne("IndexUserSocialCode.getUserMaxScoreEncircleByTime", params);
    }

    @Override
    public Boolean ifUserKillEncircle(Long gameId, Long userId, Long encircleId, Integer socialCodeType) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("userId", userId);
        params.put("socialCodeId", encircleId);
        params.put("socialCodeType", socialCodeType);
        IndexUserSocialCode res = sqlSessionTemplate.selectOne("IndexUserSocialCode.ifUserKillEncircle", params);
        return res == null ? false : true;
    }

    @Override
    public List<IndexUserSocialCode> getUserPartTakePeriodId(Integer periodCount, Integer socialCodeType, Long
            userId, Integer indexId) {
        Map<String, Object> params = new HashMap<>();
        params.put("periodCount", periodCount);
        params.put("socialCodeType", socialCodeType);
        params.put("userId", userId);
        params.put("indexId", indexId);

        return sqlSessionTemplate.selectList("IndexUserSocialCode.getUserPartTakePeriodId", params);
    }

    @Override
    public List<IndexUserSocialCode> getIndexSocialByCondition(Long gameId, String periodId, Long userId, Integer
            socialCodeType) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("userId", userId);
        params.put("periodId", periodId);
        params.put("socialCodeType", socialCodeType);

        return sqlSessionTemplate.selectList("IndexUserSocialCode.getIndexSocialByCondition", params);
    }

    @Override
    public List<Map> getUserAwardIndexSocials(Long gameId, Long userId, Integer total, Integer socialType, String
            periodId) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("userId", userId);
        params.put("socialType", socialType);
        params.put("total", total);
        params.put("periodId", periodId);
        return sqlSessionTemplate.selectList("IndexUserSocialCode.getUserAwardIndexSocials", params);
    }

    @Override
    public void updateRightNums(long gameId, Long userId, String periodId, Integer socialCodeType, Long socialCodeId,
                                Integer socialRightCount, Integer socialCount) {
        Map params = new HashMap();
        params.put("gameId", gameId);
        params.put("userId", userId);
        params.put("periodId", periodId);
        params.put("socialCodeType", socialCodeType);
        params.put("socialCodeId", socialCodeId);
        params.put("socialRightCount", socialRightCount);
        params.put("socialCount", socialCount);
        sqlSessionTemplate.update("IndexUserSocialCode.updateRightNums", params);
    }
}
