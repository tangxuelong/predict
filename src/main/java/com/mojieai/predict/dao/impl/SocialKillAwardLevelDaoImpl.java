package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.SocialKillAwardLevelDao;
import com.mojieai.predict.entity.po.SocialKillAwardLevel;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tangxuelong on 2017/10/16.
 */
@Repository
public class SocialKillAwardLevelDaoImpl extends BaseDao implements SocialKillAwardLevelDao {
    @Override
    public void insert(SocialKillAwardLevel socialKillAwardLevel) {
        sqlSessionTemplate.insert("SocialKillAwardLevel.insert", socialKillAwardLevel);
    }

    @Override
    public List<SocialKillAwardLevel> getSocialKillAwardLevel(Long gameId, Integer ballType) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("ballType", ballType);
        return sqlSessionTemplate.selectList("SocialKillAwardLevel.getSocialKillAwardLevel", params);
    }

    @Override
    public int updateSocialKillAwardLevel(Integer levelId, Integer rankScore) {
        Map<String, Object> params = new HashMap<>();
        params.put("levelId", levelId);
        params.put("rankScore", rankScore);
        return sqlSessionTemplate.update("SocialKillAwardLevel.updateSocialKillAwardLevel", params);
    }
}
