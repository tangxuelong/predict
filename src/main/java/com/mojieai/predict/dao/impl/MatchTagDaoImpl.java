package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.MatchTagDao;
import com.mojieai.predict.entity.po.MatchTag;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class MatchTagDaoImpl extends BaseDao implements MatchTagDao {

    @Override
    public List<MatchTag> getAllMatchTag() {
        return slaveSqlSessionTemplate.selectList("MatchTag.getAllMatchTag");
    }

    @Override
    public List<MatchTag> getAllMatchTagIncludeEnable(Integer status) {
        Map<String, Object> param = new HashMap<>();
        param.put("status", status);
        return slaveSqlSessionTemplate.selectList("MatchTag.getAllMatchTagIncludeEnable", param);
    }

    @Override
    public MatchTag getMatchTag(Integer tagId) {
        return slaveSqlSessionTemplate.selectOne("MatchTag.getMatchTag", tagId);
    }

    @Override
    public MatchTag getMatchTagByTagName(String matchName) {
        return slaveSqlSessionTemplate.selectOne("MatchTag.getMatchTagByTagName", matchName);
    }

    @Override
    public Integer update(MatchTag matchTag) {
        return sqlSessionTemplate.update("MatchTag.update", matchTag);
    }

    @Override
    public Integer insert(MatchTag matchTag) {
        return sqlSessionTemplate.insert("MatchTag.insert", matchTag);
    }
}
