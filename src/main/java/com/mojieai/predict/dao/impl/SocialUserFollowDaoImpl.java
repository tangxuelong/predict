package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.SocialUserFollowDao;
import com.mojieai.predict.entity.bo.PaginationInfo;
import com.mojieai.predict.entity.bo.PaginationList;
import com.mojieai.predict.entity.po.SocialUserFollow;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tangxuelong on 2017/11/28.
 */
@Repository
public class SocialUserFollowDaoImpl extends BaseDao implements SocialUserFollowDao {
    @Override
    public Integer getUserFollowCount(Long userId, Integer followType) {
        Map params = new HashMap();
        params.put("userId", userId);
        params.put("followType", followType);
        return sqlSessionTemplate.selectOne("SocialUserFollow.getUserFollowCount", params);
    }

    @Override
    public PaginationList<SocialUserFollow> getFollowUserListByPage(Long userId, Integer followType, Integer page,
                                                                    Integer pageSize) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("followType", followType);
        PaginationInfo paginationInfo = new PaginationInfo(page, pageSize);
        return selectPaginationList("SocialUserFollow.getFollowUserListByPage", params, paginationInfo);
    }

    @Override
    public List<SocialUserFollow> getFollowUserIdList(Long userId, Integer followType, Integer count, Long
            lastFollowUserId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("count", count);
        params.put("followType", followType);
        params.put("lastFollowUserId", lastFollowUserId);
        return sqlSessionTemplate.selectList("SocialUserFollow.getFollowUserIdList", params);
    }

    @Override
    public SocialUserFollow getFollowUser(Long userId, Long followUserId, Integer followType, Boolean isLock) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("followUserId", followUserId);
        params.put("followType", followType);
        params.put("isLock", isLock);
        return sqlSessionTemplate.selectOne("SocialUserFollow.getFollowUser", params);
    }

    @Override
    public void insert(SocialUserFollow socialUserFollow) {
        sqlSessionTemplate.insert("SocialUserFollow.insert", socialUserFollow);
    }

    @Override
    public void update(SocialUserFollow socialUserFollow) {
        sqlSessionTemplate.update("SocialUserFollow.update", socialUserFollow);
    }
}
