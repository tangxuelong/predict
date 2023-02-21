package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.SocialClassicEncircleCodeDao;
import com.mojieai.predict.entity.po.SocialClassicEncircle;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class SocialClassicEncircleCodeDaoImpl extends BaseDao implements SocialClassicEncircleCodeDao {

    @Override
    public Integer insert(SocialClassicEncircle socialEncircle) {

        return sqlSessionTemplate.insert("SocialClassicEncircle.insert", socialEncircle);
    }

    @Override
    public List<SocialClassicEncircle> getSocialClassicEncircleByCondition(Long gameId, String periodId, Long
            encircleId, Long userId, Integer codeType) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        params.put("encircleId", encircleId);
        params.put("userId", userId);
        params.put("codeType", codeType);
        return sqlSessionTemplate.selectList("SocialClassicEncircle.getSocialClassicEncircleByCondition", params);
    }
}
