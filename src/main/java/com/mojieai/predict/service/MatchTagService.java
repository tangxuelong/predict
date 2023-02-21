package com.mojieai.predict.service;

import com.mojieai.predict.entity.po.MatchTag;

import java.util.Map;

public interface MatchTagService {

    Map<String, Object> getAllMatchTagsForAdmin(Integer status);

    Map<String, Object> addMatchTag(MatchTag matchTag);
}
