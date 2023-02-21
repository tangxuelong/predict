package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.ProgramSaleStatsDao;
import com.mojieai.predict.entity.po.ProgramSaleStats;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ProgramSaleStatsDaoImpl extends BaseDao implements ProgramSaleStatsDao {
    @Override
    public List<ProgramSaleStats> getStatsByDate(Integer orderDateMin, Integer orderDateMax) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderDateMin", orderDateMin);
        params.put("orderDateMax", orderDateMax);
        return sqlSessionTemplate.selectList("ProgramSaleStats.getStatsByDate", params);
    }

    @Override
    public void insert(ProgramSaleStats programSaleStats) {
        sqlSessionTemplate.insert("ProgramSaleStats.insert", programSaleStats);
    }
}
