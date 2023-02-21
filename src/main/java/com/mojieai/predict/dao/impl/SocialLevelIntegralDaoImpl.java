package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.SocialLevelIntegralDao;
import com.mojieai.predict.entity.po.SocialLevelIntegral;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class SocialLevelIntegralDaoImpl extends BaseDao implements SocialLevelIntegralDao {

    @Override
    public List<SocialLevelIntegral> getAllSocialLevelIntegral() {
        return sqlSessionTemplate.selectList("SocialLevelIntegral.getAllSocialLevelIntegral");
    }

    @Override
    public Integer updateSocialLevelIntegralEnable(Integer levelId, Integer enable) {
        Map param = new HashMap<>();
        param.put("levelId", levelId);
        param.put("enable", enable);
        return sqlSessionTemplate.update("SocialLevelIntegral.updateSocialLevelIntegralEnable", param);
    }

    @Override
    public Integer insert(SocialLevelIntegral socialLevelIntegral) {
        return sqlSessionTemplate.insert("SocialLevelIntegral.insert", socialLevelIntegral);
    }


}
