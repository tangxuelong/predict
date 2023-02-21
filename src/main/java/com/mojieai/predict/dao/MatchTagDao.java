package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.MatchTag;

import java.util.List;

public interface MatchTagDao {

    List<MatchTag> getAllMatchTag();

    List<MatchTag> getAllMatchTagIncludeEnable(Integer status);

    MatchTag getMatchTag(Integer tagId);

    MatchTag getMatchTagByTagName(String matchName);

    Integer update(MatchTag matchTag);

    Integer insert(MatchTag matchTag);
}
