package com.mojieai.predict.service;

import java.util.List;
import java.util.Map;

public interface IndexUserSocialCodeService {

    List<Map> getRecentOpenedSocialIndex(Long gameId, Long userId, Integer recentPeriodCount, Integer socialType);
}
