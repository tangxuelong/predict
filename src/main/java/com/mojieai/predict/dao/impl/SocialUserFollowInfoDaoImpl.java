package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.SocialUserFollowInfoDao;
import com.mojieai.predict.entity.po.SocialUserFollowInfo;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tangxuelong on 2017/11/28.
 */
@Repository
public class SocialUserFollowInfoDaoImpl extends BaseDao implements SocialUserFollowInfoDao {
    @Override
    public SocialUserFollowInfo getUserFollowInfo(Long userId, Integer followType) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("followType", followType);
        return sqlSessionTemplate.selectOne("SocialUserFollowInfo.getUserFollowInfo", params);
    }

    @Override
    public void insert(SocialUserFollowInfo socialUserFollowInfo) {
        sqlSessionTemplate.insert("SocialUserFollowInfo.insert", socialUserFollowInfo);
    }

    @Override
    public void update(SocialUserFollowInfo socialUserFollowInfo) {
        sqlSessionTemplate.update("SocialUserFollowInfo.update", socialUserFollowInfo);
    }
}
