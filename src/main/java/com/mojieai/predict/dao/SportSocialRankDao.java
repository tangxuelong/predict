package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.SportSocialRank;

import java.sql.Timestamp;
import java.util.List;

public interface SportSocialRankDao {
    List<SportSocialRank> getAllSportSocialRank();

    List<SportSocialRank> getAllSportSocialRankByType(Integer rankType);

    List<SportSocialRank> getAllSportSocialRankByPlayTypeNotUpdate(Integer rankType, Integer playType, Timestamp date);

    List<SportSocialRank> getAllSportSocialRankByPlayType(Integer rankType, Integer playType);

    SportSocialRank getUserSportSocialRankByType(Integer rankType, Integer playType, Long userId, Boolean isLock);

    void update(SportSocialRank sportSocialRank);

    void insert(SportSocialRank sportSocialRank);
}
