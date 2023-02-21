package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.SocialKillCodeDao;
import com.mojieai.predict.entity.bo.PaginationInfo;
import com.mojieai.predict.entity.bo.PaginationList;
import com.mojieai.predict.entity.po.SocialKillCode;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class SocialKillCodeDaoImpl extends BaseDao implements SocialKillCodeDao {
    @Override
    public Integer insert(SocialKillCode socialKillCode) {
        return sqlSessionTemplate.insert("SocialKillCode.insert", socialKillCode);
    }

    @Override
    public PaginationList<SocialKillCode> getKillNumsByEncircleIdByPage(long gameId, String periodId, Long
            encircleId, Long userId, Integer page, Integer isDistribute) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        params.put("encircleId", encircleId);
        params.put("userId", userId);
        params.put("isDistribute", isDistribute);
        PaginationInfo paginationInfo = new PaginationInfo(page, PaginationInfo.defaultRecordPerPage);
        return selectPaginationList("SocialKillCode.getkillNumsByEncircleIdByPage", params, paginationInfo);
    }

    @Override
    public SocialKillCode getKillNumsByEncircleIdAndUserId(long gameId, String periodId, Long encircleId, Long userId) {
        SocialKillCode socialKillCode = new SocialKillCode();
        List<SocialKillCode> socialKillCodes = getKillNumsByCondition(gameId, periodId, encircleId, userId);
        if (socialKillCodes != null && socialKillCodes.size() > 0) {
            socialKillCode = socialKillCodes.get(0);
        }
        return socialKillCode;
    }

    @Override
    public List<SocialKillCode> getKillNumsByPeriodId(long gameId, String periodId) {

        List<SocialKillCode> socialKillCodes = getKillNumsByCondition(gameId, periodId, null, null);
        return socialKillCodes;
    }

    @Override
    public SocialKillCode getKillNumsByKillCodeId(long killCodeId, String periodId) {
        Map<String, Object> params = new HashMap<>();
        params.put("killCodeId", killCodeId);
        params.put("periodId", periodId);
        return sqlSessionTemplate.selectOne("SocialKillCode.getKillNumsByKillCodeId", params);
    }

    @Override
    public List<SocialKillCode> getKillNumsByCondition(long gameId, String periodId, Long encircleId, Long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        params.put("encircleId", encircleId);
        params.put("userId", userId);
        return sqlSessionTemplate.selectList("SocialKillCode.getKillNumsByCondition", params);
    }

    @Override
    public int updateRightNums(Long killCodeId, Integer rightNums) {
        Map<String, Object> params = new HashMap<>();
        params.put("rightNums", rightNums);
        params.put("killCodeId", killCodeId);
        return sqlSessionTemplate.update("SocialKillCode.updateRightNums", params);
    }

    @Override
    public int updateUserScore(Long killCodeId, Integer userAwardScore) {
        Map<String, Object> params = new HashMap<>();
        params.put("userAwardScore", userAwardScore);
        params.put("killCodeId", killCodeId);
        return sqlSessionTemplate.update("SocialKillCode.updateUserScore", params);
    }

    @Override
    public int updateSocialKillCode(SocialKillCode socialKillCode) {
        return sqlSessionTemplate.update("SocialKillCode.updateSocialKillCode", socialKillCode);
    }

    @Override
    public List<SocialKillCode> getUnDistributeKillNums(long gameId, String periodId) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        return sqlSessionTemplate.selectList("SocialKillCode.getUnDistributeKillNums", params);
    }

    @Override
    public int updateToDistribute(Long killCodeId, String periodId) {
        Map<String, Object> params = new HashMap<>();
        params.put("killCodeId", killCodeId);
        params.put("periodId", periodId);
        return sqlSessionTemplate.update("SocialKillCode.updateToDistribute", params);
    }
}
