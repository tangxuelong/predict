package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.SocialEncircleAwardLevelDao;
import com.mojieai.predict.entity.po.SocialEncircleAwardLevel;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tangxuelong on 2017/10/16.
 */
@Repository
public class SocialEncircleAwardLevelDaoImpl extends BaseDao implements SocialEncircleAwardLevelDao {
    @Override
    public void insert(SocialEncircleAwardLevel socialEncircleAwardLevel) {
        sqlSessionTemplate.insert("SocialEncircleAwardLevel.insert", socialEncircleAwardLevel);
    }

    @Override
    public List<SocialEncircleAwardLevel> getSocialEncircleAwardLevel(Long gameId, Integer ballType) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("ballType", ballType);
        return sqlSessionTemplate.selectList("SocialEncircleAwardLevel.getSocialEncircleAwardLevel", params);
    }

    @Override
    public int updateSocialEncircleAwardLevel(Integer levelId, Integer rankScore) {
        Map<String, Object> params = new HashMap<>();
        params.put("levelId", levelId);
        params.put("rankScore", rankScore);
        return sqlSessionTemplate.update("SocialEncircleAwardLevel.updateSocialEncircleAwardLevel", params);
    }
}
