package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.SocialClassicEncircle;

import java.util.List;

public interface SocialClassicEncircleCodeDao {
    Integer insert(SocialClassicEncircle socialEncircle);

    List<SocialClassicEncircle> getSocialClassicEncircleByCondition(Long gameId, String periodId, Long encircleId,
                                                                    Long userId, Integer codeType);
}
