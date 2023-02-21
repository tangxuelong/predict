package com.mojieai.predict.enums.trend;

import com.mojieai.predict.constant.TrendConstant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Singal
 */
public enum StatisticsEnum {
    //出现次数
    APPEAR(TrendConstant.TREND_STATISTICS_APPEAR) {
        @Override
        public List<Integer> processStatList(List<Map<String, Object>> subChartList) {
            List<Integer> statList = null;
            for (Map<String, Object> statMap : subChartList) {
                List<Integer> omitList = (List<Integer>) statMap.get(TrendConstant.KEY_TREND_OMIT_NUM);
                if (statList == null) {
                    statList = new ArrayList<>(Collections.nCopies(omitList.size(), 0));
                }
                for (int index = 0; index < omitList.size(); index++) {
                    if (omitList.get(index) <= 0) {
                        statList.set(index, statList.get(index) + 1);
                    }
                }
            }
            return statList;
        }
    },
    //平均遗漏
    AVERAGE_OMIT(TrendConstant.TREND_STATISTICS_AVERAGE_OMIT) {
        @Override
        public List<Integer> processStatList(List<Map<String, Object>> subChartList) {
            List<Integer> omitStatList = null;
            List<Integer> appearList = null;
            for (int m = 0; m < subChartList.size(); m++) {
                List<Integer> omitList = (List<Integer>) subChartList.get(m).get(TrendConstant.KEY_TREND_OMIT_NUM);
                if (omitStatList == null || appearList == null) {
                    omitStatList = new ArrayList<>(Collections.nCopies(omitList.size(), 0));
                    appearList = new ArrayList<>(Collections.nCopies(omitList.size(), 0));
                }
                for (int index = 0; index < omitList.size(); index++) {
                    if (omitList.get(index) > 0) {
                        if (m == 0) {
                            omitStatList.set(index, omitList.get(index));
                        } else {
                            omitStatList.set(index, omitStatList.get(index) + 1);
                        }
                    } else {
                        appearList.set(index, appearList.get(index) + 1);
                    }
                }
            }
            List<Integer> averageList = new ArrayList<>();
            for (int n = 0; n < omitStatList.size(); n++) {
                averageList.add(omitStatList.get(n) / (appearList.get(n)+1));
            }
            return averageList;
        }
    },
    //最大遗漏
    MAX_OMIT(TrendConstant.TREND_STATISTICS_MAX_OMIT) {
        @Override
        public List<Integer> processStatList(List<Map<String, Object>> subChartList) {
            List<Integer> statList = null;
            for (Map<String, Object> statMap : subChartList) {
                List<Integer> omitList = (List<Integer>) statMap.get(TrendConstant.KEY_TREND_OMIT_NUM);
                if (statList == null) {
                    statList = new ArrayList<>(Collections.nCopies(omitList.size(), 0));
                }
                for (int index = 0; index < omitList.size(); index++) {
                    if (omitList.get(index) > statList.get(index)) {
                        statList.set(index, omitList.get(index));
                    }
                }
            }
            return statList;
        }
    },
    //最大连出
    MAX_CONTINUOUS(TrendConstant.TREND_STATISTICS_MAX_CONTINUOUS) {
        @Override
        public List<Integer> processStatList(List<Map<String, Object>> subChartList) {
            List<Integer> statList = null;
            List<Integer> tempList = null;
            for (Map<String, Object> statMap : subChartList) {
                List<Integer> omitList = (List<Integer>) statMap.get(TrendConstant.KEY_TREND_OMIT_NUM);
                if (statList == null || tempList == null) {
                    statList = new ArrayList<>(Collections.nCopies(omitList.size(), 0));
                    tempList = new ArrayList<>(Collections.nCopies(omitList.size(), 0));
                }
                for (int index = 0; index < omitList.size(); index++) {
                    if (omitList.get(index) <= 0) {
                        tempList.set(index, tempList.get(index) + 1);
                    } else {
                        if (tempList.get(index) > statList.get(index)) {
                            statList.set(index, tempList.get(index));
                        }
                        tempList.set(index, 0);
                    }
                }
            }
            return statList;
        }
    };

    private String statisticsCn;

    StatisticsEnum(String statisticsCn) {
        this.statisticsCn = statisticsCn;
    }

    public String getStatisticsCn() {
        return statisticsCn;
    }

    abstract public List<Integer> processStatList(List<Map<String, Object>> subChartList);
}