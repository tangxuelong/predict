package com.mojieai.predict.service;

import com.mojieai.predict.entity.po.MatchSchedule;

import java.util.List;

public interface MatchScheduleService {

    void getMatchTiming();

    void expireMatchTiming();

    List<MatchSchedule> getMatchByMatchStatus(Integer status);

    void cancelMatchTiming();
}
