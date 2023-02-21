package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.SocialUserFansDao;
import com.mojieai.predict.entity.bo.PaginationInfo;
import com.mojieai.predict.entity.bo.PaginationList;
import com.mojieai.predict.entity.po.SocialUserFans;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tangxuelong on 2017/11/28.
 */
@Repository
public class SocialUserFansDaoImpl extends BaseDao implements SocialUserFansDao {
    @Override
    public PaginationList<SocialUserFans> getUserFansListByPage(Long userId, Integer fansType, Integer page, Integer
            pageSize) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("fansType", fansType);
        PaginationInfo paginationInfo = new PaginationInfo(page, pageSize);
        return selectPaginationList("SocialUserFans.getUserFansListByPage", params, paginationInfo);
    }

    @Override
    public SocialUserFans getUserFans(Long userId, Long fansUserId, Integer fansType) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("fansUserId", fansUserId);
        params.put("fansType", fansType);
        return sqlSessionTemplate.selectOne("SocialUserFans.getUserFans", params);
    }

    @Override
    public List<Long> getUserFansUserId(Long userId, Integer fansType) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("fansType", fansType);
        return sqlSessionTemplate.selectList("SocialUserFans.getUserFansUserId", params);
    }

    @Override
    public void insert(SocialUserFans socialUserFans) {
        sqlSessionTemplate.insert("SocialUserFans.insert", socialUserFans);
    }

    @Override
    public void update(SocialUserFans socialUserFans) {
        sqlSessionTemplate.update("SocialUserFans.update", socialUserFans);
    }
}
