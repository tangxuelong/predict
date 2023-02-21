package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.SportSocialRankDao;
import com.mojieai.predict.entity.po.SportSocialRank;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class SportSocialRankDaoImpl extends BaseDao implements SportSocialRankDao {

    @Override
    public List<SportSocialRank> getAllSportSocialRank() {
        return sqlSessionTemplate.selectList("SportSocialRank.getAllSportSocialRank");
    }

    @Override
    public List<SportSocialRank> getAllSportSocialRankByType(Integer rankType) {
        Map<String, Object> params = new HashMap<>();
        params.put("rankType", rankType);
        return sqlSessionTemplate.selectList("SportSocialRank.getAllSportSocialRankByType", params);

    }

    @Override
    public List<SportSocialRank> getAllSportSocialRankByPlayTypeNotUpdate(Integer rankType, Integer playType,
                                                                          Timestamp date) {
        Map<String, Object> params = new HashMap<>();
        params.put("rankType", rankType);
        params.put("playType", playType);
        params.put("date", date);
        return sqlSessionTemplate.selectList("SportSocialRank.getAllSportSocialRankByPlayTypeNotUpdate", params);

    }

    @Override
    public List<SportSocialRank> getAllSportSocialRankByPlayType(Integer rankType, Integer playType) {
        Map<String, Object> params = new HashMap<>();
        params.put("rankType", rankType);
        params.put("playType", playType);
        return sqlSessionTemplate.selectList("SportSocialRank.getAllSportSocialRankByPlayType", params);

    }

    @Override
    public SportSocialRank getUserSportSocialRankByType(Integer rankType, Integer playType, Long userId, Boolean
            isLock) {
        Map<String, Object> params = new HashMap<>();
        params.put("rankType", rankType);
        params.put("playType", playType);
        params.put("userId", userId);
        params.put("isLock", isLock);
        return sqlSessionTemplate.selectOne("SportSocialRank.getUserSportSocialRankByType", params);

    }

    @Override
    public void update(SportSocialRank sportSocialRank) {
        sqlSessionTemplate.update("SportSocialRank.update", sportSocialRank);
    }

    @Override
    public void insert(SportSocialRank sportSocialRank) {
        sqlSessionTemplate.insert("SportSocialRank.insert", sportSocialRank);
    }
}
