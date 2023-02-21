package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.SocialEncircleCodeDao;
import com.mojieai.predict.entity.bo.PaginationInfo;
import com.mojieai.predict.entity.bo.PaginationList;
import com.mojieai.predict.entity.po.SocialEncircle;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class SocialEncircleCodeDaoImpl extends BaseDao implements SocialEncircleCodeDao {
    @Override
    public Integer insert(SocialEncircle socialEncircle) {
        return sqlSessionTemplate.insert("SocialEncircle.insert", socialEncircle);
    }

    @Override
    public SocialEncircle getSocialEncircleByEncircleId(Long gameId, String periodId, Long encircleId) {
        SocialEncircle socialEncircle = null;
        List<SocialEncircle> socialEncircles = getSocialEncircleByCondition(gameId, periodId, encircleId, null,
                null, null);
        if (socialEncircles != null && socialEncircles.size() > 0) {
            socialEncircle = socialEncircles.get(0);
        }
        return socialEncircle;
    }

    @Override
    public List<SocialEncircle> getSocialEncircleByPeriodId(Long gameId, String periodId) {
        return getSocialEncircleByCondition(gameId, periodId, null, null, null, null);
    }

    @Override
    public List<SocialEncircle> getPeriodHotEncircle(Long gameId, String periodId, Integer isHot, Integer socialType) {
        return getSocialEncircleByCondition(gameId, periodId, null, null, socialType, isHot);
    }

    @Override
    public int updateRightNums(Long encircleCodeId, Integer rightNums) {
        Map<String, Object> params = new HashMap<>();
        params.put("rightNums", rightNums);
        params.put("encircleCodeId", encircleCodeId);
        return sqlSessionTemplate.update("SocialEncircle.updateRightNums", params);
    }

    @Override
    public int updateUserScore(Long encircleCodeId, Integer userAwardScore) {
        Map<String, Object> params = new HashMap<>();
        params.put("userAwardScore", userAwardScore);
        params.put("encircleCodeId", encircleCodeId);
        return sqlSessionTemplate.update("SocialEncircle.updateUserScore", params);
    }

    @Override
    public int updateSocialEncircle(SocialEncircle socialEncircle) {
        return sqlSessionTemplate.update("SocialEncircle.updateSocialEncircle", socialEncircle);
    }

    @Override
    public int updateUserRankByencircleId(Long gameId, String periodId, Long encircleId, Integer followKillNums) {

        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        params.put("encircleCodeId", encircleId);
        params.put("followKillNums", followKillNums);
        return sqlSessionTemplate.update("SocialEncircle.updateUserRankByencircleId", params);
    }

    @Override
    public int setEncircleIsHot(long gameId, String periodId, Long encircleId, Integer isHot) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        params.put("encircleCodeId", encircleId);
        params.put("isHot", isHot);
        return sqlSessionTemplate.update("SocialEncircle.setEncircleIsHot", params);
    }

    @Override
    public List<SocialEncircle> getUnDistributeSocialEncircle(Long gameId, String periodId) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        return sqlSessionTemplate.selectList("SocialEncircle.getUnDistributeSocialEncircle", params);
    }


    @Override
    public int updateToDistribute(Long encircleCodeId, String periodId) {
        Map<String, Object> params = new HashMap<>();
        params.put("encircleCodeId", encircleCodeId);
        params.put("periodId", periodId);
        return sqlSessionTemplate.update("SocialEncircle.updateToDistribute", params);
    }

    @Override
    public PaginationList<SocialEncircle> getSocialEncircleByPage(long gameId, String periodId, Integer page) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        PaginationInfo paginationInfo = new PaginationInfo(page, PaginationInfo.defaultRecordPerPage);
        return selectPaginationList("SocialEncircle.getSocialEncircleByPage", params, paginationInfo);
    }

    @Override
    public List<SocialEncircle> getSocialEncircleByCondition(Long gameId, String periodId, Long encircleId, Long
            userId, Integer codeType, Integer isHot) {

        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        params.put("encircleCodeId", encircleId);
        params.put("userId", userId);
        params.put("codeType", codeType);
        params.put("isHot", isHot);
        return sqlSessionTemplate.selectList("SocialEncircle.getSocialEncircleByCondition", params);
    }


}
