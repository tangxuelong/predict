package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.ProgramSaleStats;

import java.util.List;

public interface ProgramSaleStatsDao {
    List<ProgramSaleStats> getStatsByDate(Integer orderDateMin, Integer orderDateMax);

    void insert(ProgramSaleStats programSaleStats);
}
