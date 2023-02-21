package com.mojieai.predict.enums;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.FilterConstant;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.redis.PeriodRedis;

import java.util.*;

/**
 * Created by tangxuelong on 2017/8/16.
 */
public enum FilterEnum {
    /* 胆码*/
    DAN(FilterConstant.FILTER_DAN) {
        @Override
        public String getFilterName(String gameEn) {
            return FilterConstant.FILTER_DAN_CN;
        }

        @Override
        public String getFilterType(String gameEn) {
            return FilterConstant.FILTER_DAN_TYPE;
        }

        @Override
        public String[] getFilterRange(String gameEn) {
            GameEnum gameEnum = GameEnum.getGameEnumByEn(gameEn);
            return gameEnum.getRedBalls();
        }

        @Override
        public String getFilterTitle(String gameEn) {
            GameEnum gameEnum = GameEnum.getGameEnumByEn(gameEn);
            return "选择1～" + (gameEnum.getSingleRedBallLength() - 1) + "个" + gameEnum.getRedBallName() + "胆码";
        }

        @Override
        public Map<String, String> getFilterLimit(String gameEn) {
            GameEnum gameEnum = GameEnum.getGameEnumByEn(gameEn);
            Map<String, String> filterLimit = new HashMap<>();
            filterLimit.put("min", "0");
            filterLimit.put("max", String.valueOf(gameEnum.getSingleRedBallLength() - 1));
            return filterLimit;
        }

        @Override
        public String[] getFilterCustom(String gameEn) {
            return null;
        }

        @Override
        public String getFilterRangeName(String gameEn) {
            return null;
        }

        @Override
        public String getFilterCustomName(String gameEn) {
            return null;
        }

        @Override
        public Boolean filterAction(List<Integer> redBalls, String action, String gameEn) {
            for (String redBall : action.split(CommonConstant.COMMA_SPLIT_STR)) {
                if (!redBalls.contains(Integer.valueOf(redBall))) {
                    return Boolean.FALSE;
                }
            }
            return Boolean.TRUE;
        }

        /* 生成推荐*/
        @Override
        public List<String> rebuildRecommend(String gameEn, List<GamePeriod> periods) {
            /* 胆码返回空*/
            return null;
        }

        /* 说明*/
        @Override
        public String getFilterIntroduction(String gameEn) {
            return "个人认为必出的红球号码。";
        }
    },
    /* 奇偶比*/
    SINGLE_DOUBLE(FilterConstant.FILTER_SINGLE_DOUBLE) {
        @Override
        public String getFilterName(String gameEn) {
            return FilterConstant.FILTER_SINGLE_DOUBLE_CN;
        }

        @Override
        public String getFilterType(String gameEn) {
            return FilterConstant.FILTER_SINGLE_DOUBLE_TYPE;
        }

        @Override
        public String[] getFilterRange(String gameEn) {
            String[] strings = new String[]{"首位奇数%f_s", "首位偶数%f_d", "末位奇数%l_s", "末位偶数%l_d"};
            return strings;
        }

        @Override
        public String getFilterTitle(String gameEn) {
            return "奇偶";
        }

        @Override
        public Map<String, String> getFilterLimit(String gameEn) {
            return null;
        }

        @Override
        public String[] getFilterCustom(String gameEn) {
            return getTwoDivStr(gameEn);
        }

        @Override
        public String getFilterRangeName(String gameEn) {
            return "首末尾奇偶";
        }

        @Override
        public String getFilterCustomName(String gameEn) {
            return "奇偶比";
        }

        @Override
        public Boolean filterAction(List<Integer> redBalls, String action, String gameEn) {
            /* 奇偶比*/
            Map<String, Boolean> fl = new HashMap<>();
            int singleCount = 0;
            int doubleCount = 0;
            for (int i = 0; i < redBalls.size(); i++) {
                if (redBalls.get(i) % 2 == 0) {
                    doubleCount++;
                    if (i == 0) {
                        fl.put("f_d", Boolean.TRUE);
                    }
                    if (i == redBalls.size() - 1) {
                        fl.put("l_d", Boolean.TRUE);
                    }
                } else {
                    singleCount++;
                    if (i == 0) {
                        fl.put("f_s", Boolean.TRUE);
                    }
                    if (i == redBalls.size() - 1) {
                        fl.put("l_s", Boolean.TRUE);
                    }
                }
            }
            String[] actionValueArr = action.split(CommonConstant.COMMA_SPLIT_STR);
            int index = 0;
            for (String actionValue : actionValueArr) {
                index++;
                if (actionValue.indexOf(CommonConstant.COMMON_SPLIT_STR) > -1) {
                    if (null == fl.get(actionValue)) {
                        return Boolean.FALSE;
                    }
                    if (fl.get(actionValue)) {
                        if (actionValueArr.length != index) {
                            continue;
                        }
                        return Boolean.TRUE;
                    }
                } else {
                    if ((singleCount + ":" + doubleCount).equals(actionValue)) {
                        return Boolean.TRUE;
                    }
                }
            }
            return Boolean.FALSE;
        }

        /* 生成推荐*/
        @Override
        public List<String> rebuildRecommend(String gameEn, List<GamePeriod> periods) {
            List<String> recommends = new ArrayList<>();
            /* 计算每个属性的个数*/
            String[] filterFirst = new String[]{"f_s", "f_d", "l_s", "l_d"};
            Map<String, Integer> filterFirstCount = getFilterIndexCountArr(filterFirst);

            String[] filterSecond = getTwoDivStr(gameEn);
            Map<String, Integer> filterSecondCount = getFilterIndexCountArr(filterSecond);

            List<String[]> filterClass = new ArrayList<>();
            filterClass.add(filterFirst);
            filterClass.add(filterSecond);

            List<Map<String, Integer>> filterIndexArr = new ArrayList<>();
            filterIndexArr.add(filterFirstCount);
            filterIndexArr.add(filterSecondCount);

            filterIndexArr = processFilterRecommend(periods, filterClass, FilterConstant.FILTER_SINGLE_DOUBLE,
                    filterIndexArr, gameEn);
            getRecommendByLength(recommends, 1, filterIndexArr.get(0));
            getRecommendByLength(recommends, 3, filterIndexArr.get(1));

            return recommends;
        }

        /* 说明*/
        @Override
        public String getFilterIntroduction(String gameEn) {
            return "不能被2整除的为奇数，能被2整除的为偶数，单注号码中红球奇偶个数比即为奇偶比。";
        }
    },
    /* 大小比*/
    BIG_SMALL(FilterConstant.FILTER_BIG_SMALL) {
        @Override
        public String getFilterName(String gameEn) {
            return FilterConstant.FILTER_BIG_SMALL_CN;
        }

        @Override
        public String getFilterType(String gameEn) {
            return FilterConstant.FILTER_AC_TYPE;
        }

        @Override
        public String[] getFilterRange(String gameEn) {
            return null;
        }

        @Override
        public String getFilterTitle(String gameEn) {
            return "大小比";
        }

        @Override
        public Map<String, String> getFilterLimit(String gameEn) {
            return null;
        }

        @Override
        public String[] getFilterCustom(String gameEn) {
            return getTwoDivStr(gameEn);
        }

        @Override
        public String getFilterRangeName(String gameEn) {
            return null;
        }

        @Override
        public String getFilterCustomName(String gameEn) {
            return null;
        }

        @Override
        public Boolean filterAction(List<Integer> redBalls, String action, String gameEn) {
            /* 大小比*/
            int bigCount = 0;
            int smallCount = 0;
            for (int i = 0; i < redBalls.size(); i++) {
                if (redBalls.get(i) < GameEnum.getGameEnumByEn(gameEn).getGameRedNumberMiddleLength()) {
                    smallCount++;
                } else {
                    bigCount++;
                }
            }

            for (String actionValue : action.split(CommonConstant.COMMA_SPLIT_STR)) {
                if ((bigCount + ":" + smallCount).equals(actionValue)) {
                    return Boolean.TRUE;
                }
            }
            return Boolean.FALSE;
        }

        /* 生成推荐*/
        @Override
        public List<String> rebuildRecommend(String gameEn, List<GamePeriod> periods) {
            List<String> recommends = new ArrayList<>();
            /* 计算每个属性的个数*/
            String[] filterSecond = getTwoDivStr(gameEn);
            Map<String, Integer> filterSecondCount = getFilterIndexCountArr(filterSecond);

            List<String[]> filterClass = new ArrayList<>();
            filterClass.add(filterSecond);

            List<Map<String, Integer>> filterIndexArr = new ArrayList<>();
            filterIndexArr.add(filterSecondCount);

            filterIndexArr = processFilterRecommend(periods, filterClass, FilterConstant.FILTER_BIG_SMALL,
                    filterIndexArr, gameEn);

            getRecommendByLength(recommends, 3, filterIndexArr.get(0));

            return recommends;
        }

        /* 说明*/
        @Override
        public String getFilterIntroduction(String gameEn) {
            return GameEnum.getGameEnumByEn(gameEn).getBigSmallIntroduction();
        }
    },
    /* 质合比*/
    PRIME_C(FilterConstant.FILTER_PRIME_C) {
        @Override
        public String getFilterName(String gameEn) {
            return FilterConstant.FILTER_PRIME_C_CN;
        }

        @Override
        public String getFilterType(String gameEn) {
            return FilterConstant.FILTER_SINGLE_DOUBLE_TYPE;
        }

        @Override
        public String[] getFilterRange(String gameEn) {
            String[] strings = new String[]{"首位质数%f_p", "首位合数%f_c", "末位质数%l_p", "末位合数%l_c"};
            return strings;
        }

        @Override
        public String getFilterTitle(String gameEn) {
            return "质合";
        }

        @Override
        public Map<String, String> getFilterLimit(String gameEn) {
            return null;
        }

        @Override
        public String[] getFilterCustom(String gameEn) {
            return getTwoDivStr(gameEn);
        }

        @Override
        public String getFilterRangeName(String gameEn) {
            return "首末尾质合";
        }

        @Override
        public String getFilterCustomName(String gameEn) {
            return "质合比";
        }

        @Override
        public Boolean filterAction(List<Integer> redBalls, String action, String gameEn) {
            /* 质合比*/
            Integer[] primeArr = {1, 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31};
            List<Integer> primeList = new ArrayList<>();
            Collections.addAll(primeList, primeArr);
            Map<String, Boolean> fl = new HashMap<>();
            int primeCount = 0;
            int cCount = 0;
            for (int i = 0; i < redBalls.size(); i++) {
                if (primeList.contains(redBalls.get(i))) {
                    primeCount++;
                    if (i == 0) {
                        fl.put("f_p", Boolean.TRUE);
                    }
                    if (i == redBalls.size() - 1) {
                        fl.put("l_p", Boolean.TRUE);
                    }
                } else {
                    cCount++;
                    if (i == 0) {
                        fl.put("f_c", Boolean.TRUE);
                    }
                    if (i == redBalls.size() - 1) {
                        fl.put("l_c", Boolean.TRUE);
                    }
                }
            }
            String[] actionValueArr = action.split(CommonConstant.COMMA_SPLIT_STR);
            int index = 0;
            for (String actionValue : actionValueArr) {
                index++;
                if (actionValue.indexOf(CommonConstant.COMMON_SPLIT_STR) > -1) {
                    if (null == fl.get(actionValue)) {
                        return Boolean.FALSE;
                    }
                    if (fl.get(actionValue)) {
                        if (actionValueArr.length != index) {
                            continue;
                        }
                        return Boolean.TRUE;
                    }
                } else {
                    if ((primeCount + ":" + cCount).equals(actionValue)) {
                        return Boolean.TRUE;
                    }
                }
            }
            return Boolean.FALSE;
        }

        /* 生成推荐*/
        @Override
        public List<String> rebuildRecommend(String gameEn, List<GamePeriod> periods) {
            List<String> recommends = new ArrayList<>();
            /* 计算每个属性的个数*/
            String[] filterFirst = new String[]{"f_p", "l_p", "f_c", "l_c"};
            Map<String, Integer> filterFirstCount = getFilterIndexCountArr(filterFirst);

            String[] filterSecond = getTwoDivStr(gameEn);
            Map<String, Integer> filterSecondCount = getFilterIndexCountArr(filterSecond);

            List<String[]> filterClass = new ArrayList<>();
            filterClass.add(filterFirst);
            filterClass.add(filterSecond);

            List<Map<String, Integer>> filterIndexArr = new ArrayList<>();
            filterIndexArr.add(filterFirstCount);
            filterIndexArr.add(filterSecondCount);

            filterIndexArr = processFilterRecommend(periods, filterClass, FilterConstant.FILTER_PRIME_C,
                    filterIndexArr, gameEn);

            getRecommendByLength(recommends, 1, filterIndexArr.get(0));
            getRecommendByLength(recommends, 3, filterIndexArr.get(1));

            return recommends;
        }

        /* 说明*/
        @Override
        public String getFilterIntroduction(String gameEn) {
            return "只能被1和它本身整除的数为质数，特别规定1是质数。单注号码中红球质数与合数的个数比即为质合比。";
        }
    },
    /* 三区比*/
    DIV_THREE(FilterConstant.FILTER_DIV_THREE) {
        @Override
        public String getFilterName(String gameEn) {
            return FilterConstant.FILTER_DIV_THREE_CN;
        }

        @Override
        public String getFilterType(String gameEn) {
            return FilterConstant.FILTER_AC_TYPE;
        }

        @Override
        public String[] getFilterRange(String gameEn) {
            return null;
        }

        @Override
        public String getFilterTitle(String gameEn) {
            return "三区比";
        }

        @Override
        public Map<String, String> getFilterLimit(String gameEn) {
            return null;
        }

        @Override
        public String[] getFilterCustom(String gameEn) {
            return getThreeDivStr(gameEn);
        }

        @Override
        public String getFilterRangeName(String gameEn) {
            return null;
        }

        @Override
        public String getFilterCustomName(String gameEn) {
            return null;
        }

        @Override
        public Boolean filterAction(List<Integer> redBalls, String action, String gameEn) {
            /* 三区*/
            int first = 0;
            int second = 0;
            int third = 0;
            for (int i = 0; i < redBalls.size(); i++) {
                if (redBalls.get(i) < GameEnum.getGameEnumByEn(gameEn).getGameRedNumberDiv1Length()) {
                    first++;
                } else if (redBalls.get(i) < GameEnum.getGameEnumByEn(gameEn).getGameRedNumberDiv2Length()) {
                    second++;
                } else {
                    third++;
                }
            }
            for (String actionValue : action.split(CommonConstant.COMMA_SPLIT_STR)) {
                if ((first + ":" + second + ":" + third).equals(actionValue)) {
                    return Boolean.TRUE;
                }
            }
            return Boolean.FALSE;
        }

        /* 生成推荐*/
        @Override
        public List<String> rebuildRecommend(String gameEn, List<GamePeriod> periods) {
            List<String> recommends = new ArrayList<>();
            /* 计算每个属性的个数*/
            String[] filterSecond = getThreeDivStr(gameEn);
            Map<String, Integer> filterSecondCount = getFilterIndexCountArr(filterSecond);

            List<String[]> filterClass = new ArrayList<>();
            filterClass.add(filterSecond);

            List<Map<String, Integer>> filterIndexArr = new ArrayList<>();
            filterIndexArr.add(filterSecondCount);

            filterIndexArr = processFilterRecommend(periods, filterClass, FilterConstant.FILTER_DIV_THREE,
                    filterIndexArr, gameEn);

            getRecommendByLength(recommends, 9, filterIndexArr.get(0));

            return recommends;
        }

        /* 说明*/
        @Override
        public String getFilterIntroduction(String gameEn) {
            return GameEnum.getGameEnumByEn(gameEn).getThreeDivIntroduction();
        }
    },
    /* 012路*/
    ZERO_ONE_TWO(FilterConstant.FILTER_ZERO_ONE_TWO) {
        @Override
        public String getFilterName(String gameEn) {
            return FilterConstant.FILTER_ZERO_ONE_TWO_CN;
        }

        @Override
        public String getFilterType(String gameEn) {
            return FilterConstant.FILTER_AC_TYPE;
        }

        @Override
        public String[] getFilterRange(String gameEn) {
            return null;
        }

        @Override
        public String getFilterTitle(String gameEn) {
            return "012路";
        }

        @Override
        public Map<String, String> getFilterLimit(String gameEn) {
            return null;
        }

        @Override
        public String[] getFilterCustom(String gameEn) {
            return getThreeDivStr(gameEn);
        }

        @Override
        public String getFilterRangeName(String gameEn) {
            return null;
        }

        @Override
        public String getFilterCustomName(String gameEn) {
            return null;
        }

        @Override
        public Boolean filterAction(List<Integer> redBalls, String action, String gameEn) {
            /* 012路*/
            int zero = 0;
            int one = 0;
            int two = 0;
            for (int i = 0; i < redBalls.size(); i++) {
                if ((redBalls.get(i) % 3) == 0) {
                    zero++;
                } else if ((redBalls.get(i) % 3) == 1) {
                    one++;
                } else if ((redBalls.get(i) % 3) == 2) {
                    two++;
                }
            }

            for (String actionValue : action.split(CommonConstant.COMMA_SPLIT_STR)) {
                if ((zero + ":" + one + ":" + two).equals(actionValue)) {
                    return Boolean.TRUE;
                }
            }
            return Boolean.FALSE;
        }

        /* 生成推荐*/
        @Override
        public List<String> rebuildRecommend(String gameEn, List<GamePeriod> periods) {
            List<String> recommends = new ArrayList<>();
            /* 计算每个属性的个数*/
            String[] filterSecond = getThreeDivStr(gameEn);
            Map<String, Integer> filterSecondCount = getFilterIndexCountArr(filterSecond);

            List<String[]> filterClass = new ArrayList<>();
            filterClass.add(filterSecond);

            List<Map<String, Integer>> filterIndexArr = new ArrayList<>();
            filterIndexArr.add(filterSecondCount);

            filterIndexArr = processFilterRecommend(periods, filterClass, FilterConstant.FILTER_ZERO_ONE_TWO,
                    filterIndexArr, gameEn);

            getRecommendByLength(recommends, 7, filterIndexArr.get(0));

            return recommends;
        }

        /* 说明*/
        @Override
        public String getFilterIntroduction(String gameEn) {
            return "除3余0的数字定义为0路，除3余1的数字定义为1路，除3余2的数字定义为2路。";
        }
    },
    /* 和值*/
    HEZHI(FilterConstant.FILTER_HEZHI) {
        @Override
        public String getFilterName(String gameEn) {
            return FilterConstant.FILTER_HEZHI_CN;
        }

        @Override
        public String getFilterType(String gameEn) {
            return FilterConstant.FILTER_HEZHI_TYPE;
        }

        @Override
        public String[] getFilterRange(String gameEn) {
            GameEnum gameEnum = GameEnum.getGameEnumByEn(gameEn);
            String[] strings = new String[]{gameEnum.getRedBallHeZhiMin(), gameEnum.getRedBallHeZhiMax()};
            return strings;
        }

        @Override
        public String getFilterTitle(String gameEn) {
            GameEnum gameEnum = GameEnum.getGameEnumByEn(gameEn);
            return "和值";
        }

        @Override
        public Map<String, String> getFilterLimit(String gameEn) {
            return null;
        }

        @Override
        public String[] getFilterCustom(String gameEn) {
            GameEnum gameEnum = GameEnum.getGameEnumByEn(gameEn);
            return new String[]{gameEnum.getRedBallHeZhiMin() + "-49", "50-59", "60-69", "70-79", "80-89", "90-99",
                    "100-109", "110-119", "120-129", "130-139", "140-" + gameEnum.getRedBallHeZhiMax()};
        }

        @Override
        public String getFilterRangeName(String gameEn) {
            return null;
        }

        @Override
        public String getFilterCustomName(String gameEn) {
            return null;
        }

        @Override
        public Boolean filterAction(List<Integer> redBalls, String action, String gameEn) {
            /* 和值*/
            Integer sum = 0;
            for (Integer ball : redBalls) {
                sum += ball;
            }
            for (String redBall : action.split(CommonConstant.COMMA_SPLIT_STR)) {
                String[] maxMin = redBall.split(CommonConstant.COMMON_DASH_STR);
                if (sum >= Integer.valueOf(maxMin[0]) && sum <= Integer.valueOf(maxMin[1])) {
                    return Boolean.TRUE;
                }
            }
            return Boolean.FALSE;
        }

        /* 生成推荐*/
        @Override
        public List<String> rebuildRecommend(String gameEn, List<GamePeriod> periods) {
            List<String> recommends = new ArrayList<>();
            /* 计算每个属性的个数*/
            String[] filterSecond = this.getFilterCustom(gameEn);
            Map<String, Integer> filterSecondCount = getFilterIndexCountArr(filterSecond);

            List<String[]> filterClass = new ArrayList<>();
            filterClass.add(filterSecond);

            List<Map<String, Integer>> filterIndexArr = new ArrayList<>();
            filterIndexArr.add(filterSecondCount);

            filterIndexArr = processFilterRecommend(periods, filterClass, FilterConstant.FILTER_HEZHI, filterIndexArr,
                    gameEn);

            getRecommendByLength(recommends, 4, filterIndexArr.get(0));

            return recommends;
        }

        /* 说明*/
        @Override
        public String getFilterIntroduction(String gameEn) {
            return GameEnum.getGameEnumByEn(gameEn).getHEZHIIntroduction();
        }
    },
    /* 跨度*/
    SPAN(FilterConstant.FILTER_SPAN) {
        @Override
        public String getFilterName(String gameEn) {
            return FilterConstant.FILTER_SPAN_CN;
        }

        @Override
        public String getFilterType(String gameEn) {
            return FilterConstant.FILTER_SPAN_TYPE;
        }

        @Override
        public String[] getFilterRange(String gameEn) {
            GameEnum gameEnum = GameEnum.getGameEnumByEn(gameEn);
            String[] strings = new String[]{gameEnum.SpanMin().toString(), gameEnum.SpanMax().toString()};
            return strings;
        }

        @Override
        public String getFilterTitle(String gameEn) {
            GameEnum gameEnum = GameEnum.getGameEnumByEn(gameEn);
            return "跨度";
        }

        @Override
        public Map<String, String> getFilterLimit(String gameEn) {
            return null;
        }

        @Override
        public String[] getFilterCustom(String gameEn) {
            GameEnum gameEnum = GameEnum.getGameEnumByEn(gameEn);
            String[] span = new String[gameEnum.SpanMax() - gameEnum.SpanMin() + 1];
            for (int i = gameEnum.SpanMin(); i <= gameEnum.SpanMax(); i++) {
                span[i - gameEnum.SpanMin()] = String.valueOf(i);
            }
            return span;
        }

        @Override
        public String getFilterRangeName(String gameEn) {
            return null;
        }

        @Override
        public String getFilterCustomName(String gameEn) {
            return null;
        }

        @Override
        public Boolean filterAction(List<Integer> redBalls, String action, String gameEn) {
            // 跨度
            int span = redBalls.get(redBalls.size() - 1) - redBalls.get(0);
            for (String spanAction : action.split(CommonConstant.COMMA_SPLIT_STR)) {
                if (spanAction.indexOf(CommonConstant.COMMON_DASH_STR) > -1) {
                    String[] spanActionArr = spanAction.split(CommonConstant.COMMON_DASH_STR);
                    if (span >= Integer.valueOf(spanActionArr[0]) && span <= Integer.valueOf(spanActionArr[1])) {
                        return Boolean.TRUE;
                    }
                } else {
                    if (Integer.valueOf(spanAction) == span) {
                        return Boolean.TRUE;
                    }
                }
            }
            return Boolean.FALSE;
        }

        /* 生成推荐*/
        @Override
        public List<String> rebuildRecommend(String gameEn, List<GamePeriod> periods) {
            List<String> recommends = new ArrayList<>();
            /* 计算每个属性的个数*/
            String[] filterSecond = this.getFilterCustom(gameEn);
            Map<String, Integer> filterSecondCount = getFilterIndexCountArr(filterSecond);

            List<String[]> filterClass = new ArrayList<>();
            filterClass.add(filterSecond);

            List<Map<String, Integer>> filterIndexArr = new ArrayList<>();
            filterIndexArr.add(filterSecondCount);

            filterIndexArr = processFilterRecommend(periods, filterClass, FilterConstant.FILTER_SPAN, filterIndexArr,
                    gameEn);

            getRecommendByLength(recommends, 10, filterIndexArr.get(0));

            return recommends;
        }

        /* 说明*/
        @Override
        public String getFilterIntroduction(String gameEn) {
            return GameEnum.getGameEnumByEn(gameEn).getSpanIntroduction();
        }
    },
    /* AC值*/
    AC(FilterConstant.FILTER_AC) {
        @Override
        public String getFilterName(String gameEn) {
            return FilterConstant.FILTER_AC_CN;
        }

        @Override
        public String getFilterType(String gameEn) {
            return FilterConstant.FILTER_AC_TYPE;
        }

        @Override
        public String[] getFilterRange(String gameEn) {
            GameEnum gameEnum = GameEnum.getGameEnumByEn(gameEn);
            String[] strings = new String[]{"0", gameEnum.AcMax().toString()};
            return strings;
        }

        @Override
        public String getFilterTitle(String gameEn) {
            return "AC值";
        }

        @Override
        public Map<String, String> getFilterLimit(String gameEn) {
            return null;
        }

        @Override
        public String[] getFilterCustom(String gameEn) {
            GameEnum gameEnum = GameEnum.getGameEnumByEn(gameEn);
            String[] AC = new String[gameEnum.AcMax() + 1];
            for (int i = 0; i <= gameEnum.AcMax(); i++) {
                AC[i] = String.valueOf(i);
            }
            return AC;
        }

        @Override
        public String getFilterRangeName(String gameEn) {
            return null;
        }

        @Override
        public String getFilterCustomName(String gameEn) {
            return null;
        }

        @Override
        public Boolean filterAction(List<Integer> redBalls, String action, String gameEn) {
            Set ACSet = new HashSet();//AC值
            for (int i = 0; i < (redBalls.size() - 1); i++) {
                for (int j = i + 1; j <= (redBalls.size() - 1); j++) {
                    ACSet.add(Math.abs(redBalls.get(j) - redBalls.get(i)));
                }
            }
            for (String ac : action.split(CommonConstant.COMMA_SPLIT_STR)) {
                if (ac.equals(String.valueOf(ACSet.size() - (redBalls.size() - 1)))) {
                    return Boolean.TRUE;
                }
            }
            return Boolean.FALSE;
        }

        /* 生成推荐*/
        @Override
        public List<String> rebuildRecommend(String gameEn, List<GamePeriod> periods) {
            List<String> recommends = new ArrayList<>();
            /* 计算每个属性的个数*/
            String[] filterSecond = this.getFilterCustom(gameEn);
            Map<String, Integer> filterSecondCount = getFilterIndexCountArr(filterSecond);

            List<String[]> filterClass = new ArrayList<>();
            filterClass.add(filterSecond);

            List<Map<String, Integer>> filterIndexArr = new ArrayList<>();
            filterIndexArr.add(filterSecondCount);

            filterIndexArr = processFilterRecommend(periods, filterClass, FilterConstant.FILTER_AC, filterIndexArr,
                    gameEn);

            getRecommendByLength(recommends, 3, filterIndexArr.get(0));

            return recommends;
        }

        /* 说明*/
        @Override
        public String getFilterIntroduction(String gameEn) {
            return "也称数字复杂值，AC值越大，表明号码算术级数越复杂，规律性越差，随机性越强。";
        }
    },
    /* 连号*/
    CONTINUITY(FilterConstant.FILTER_CONTINUITY) {
        @Override
        public String getFilterName(String gameEn) {
            return FilterConstant.FILTER_CONTINUITY_CN;
        }

        @Override
        public String getFilterType(String gameEn) {
            return FilterConstant.FILTER_AC_TYPE;
        }

        @Override
        public String[] getFilterRange(String gameEn) {
            return null;
        }

        @Override
        public String getFilterTitle(String gameEn) {
            return "连号";
        }

        @Override
        public Map<String, String> getFilterLimit(String gameEn) {
            return null;
        }

        @Override
        public String[] getFilterCustom(String gameEn) {
            String[] result = new String[]{"无连号%0", "一连号%1", "二连号%2", "三连号%3", "四连号%4", "五连号%5"};
            String[] resp = new String[GameEnum.getGameEnumByEn(gameEn).getSingleRedBallLength()];
            for (int i = 0; i < resp.length; i++) {
                resp[i] = result[i];
            }
            return resp;
        }


        @Override
        public String getFilterRangeName(String gameEn) {
            return null;
        }

        @Override
        public String getFilterCustomName(String gameEn) {
            return null;
        }

        @Override
        public Boolean filterAction(List<Integer> redBalls, String action, String gameEn) {
            /* 连号*/
            int lianhao = 0;
            for (int i = 1; i < redBalls.size(); i++) {
                if (redBalls.get(i) - redBalls.get(i - 1) == 1) {
                    lianhao++;
                }
            }

            for (String actionValue : action.split(CommonConstant.COMMA_SPLIT_STR)) {
                if (Integer.valueOf(actionValue) == lianhao) {
                    return Boolean.TRUE;
                }
            }
            return Boolean.FALSE;
        }

        /* 生成推荐*/
        @Override
        public List<String> rebuildRecommend(String gameEn, List<GamePeriod> periods) {
            List<String> recommends = new ArrayList<>();
            /* 计算每个属性的个数*/

            String[] filterSecond = new String[this.getFilterCustom(gameEn).length];
            for (int i = 0; i < this.getFilterCustom(gameEn).length; i++) {
                filterSecond[i] = this.getFilterCustom(gameEn)[i].split(CommonConstant.PERCENT_SPLIT_STR)[1];
            }
            Map<String, Integer> filterSecondCount = getFilterIndexCountArr(filterSecond);

            List<String[]> filterClass = new ArrayList<>();
            filterClass.add(filterSecond);

            List<Map<String, Integer>> filterIndexArr = new ArrayList<>();
            filterIndexArr.add(filterSecondCount);

            filterIndexArr = processFilterRecommend(periods, filterClass, FilterConstant.FILTER_CONTINUITY,
                    filterIndexArr, gameEn);

            getRecommendByLength(recommends, 3, filterIndexArr.get(0));

            return recommends;
        }

        /* 说明*/
        @Override
        public String getFilterIntroduction(String gameEn) {
            return "一注号码中相连的两个或者多个号码为连号。例如01 02为一连号，01 02 03为二连号。";
        }
    },
    /* 重码数*/
    REPEAT_LAST(FilterConstant.FILTER_REPEAT_LAST) {
        @Override
        public String getFilterName(String gameEn) {
            return FilterConstant.FILTER_REPEAT_LAST_CN;
        }

        @Override
        public String getFilterType(String gameEn) {
            return FilterConstant.FILTER_AC_TYPE;
        }

        @Override
        public String[] getFilterRange(String gameEn) {
            return null;
        }

        @Override
        public String getFilterTitle(String gameEn) {
            return "重码";
        }

        @Override
        public Map<String, String> getFilterLimit(String gameEn) {
            return null;
        }

        @Override
        public String[] getFilterCustom(String gameEn) {
            return getOneStr(gameEn);
        }

        @Override
        public String getFilterRangeName(String gameEn) {
            return null;
        }

        @Override
        public String getFilterCustomName(String gameEn) {
            return null;
        }

        @Override
        public Boolean filterAction(List<Integer> redBalls, String action, String gameEn) {
            int repeatCount = 0;
            GamePeriod lastOpenPeriod = PeriodRedis.getLastOpenPeriodByGameId(GameCache.getGame(gameEn).getGameId());
            String redWinningNumber = lastOpenPeriod.getWinningNumbers().split(CommonConstant.COMMON_COLON_STR)[0];
            for (String number : redWinningNumber.split(CommonConstant.SPACE_SPLIT_STR)) {
                if (redBalls.contains(Integer.valueOf(number))) {
                    repeatCount++;
                }
            }
            for (String actionValue : action.split(CommonConstant.COMMA_SPLIT_STR)) {
                if (String.valueOf(repeatCount).equals(actionValue)) {
                    return Boolean.TRUE;
                }
            }
            return Boolean.FALSE;
        }

        /* 生成推荐*/
        @Override
        public List<String> rebuildRecommend(String gameEn, List<GamePeriod> periods) {
            List<String> recommends = new ArrayList<>();
            /* 计算每个属性的个数*/
            String[] filterSecond = this.getFilterCustom(gameEn);
            Map<String, Integer> filterSecondCount = getFilterIndexCountArr(filterSecond);

            List<String[]> filterClass = new ArrayList<>();
            filterClass.add(filterSecond);

            List<Map<String, Integer>> filterIndexArr = new ArrayList<>();
            filterIndexArr.add(filterSecondCount);

            filterIndexArr = processFilterRecommend(periods, filterClass, FilterConstant.FILTER_REPEAT_LAST,
                    filterIndexArr, gameEn);

            getRecommendByLength(recommends, 2, filterIndexArr.get(0));

            return recommends;
        }

        /* 说明*/
        @Override
        public String getFilterIntroduction(String gameEn) {
            return "和上期红球号码相同的号码个数。";
        }
    },
    /* 邻码数*/
    NEIGHBOR(FilterConstant.FILTER_NEIGHBOR) {
        @Override
        public String getFilterName(String gameEn) {
            return FilterConstant.FILTER_NEIGHBOR_CN;
        }

        @Override
        public String getFilterType(String gameEn) {
            return FilterConstant.FILTER_AC_TYPE;
        }

        @Override
        public String[] getFilterRange(String gameEn) {
            return null;
        }

        @Override
        public String getFilterTitle(String gameEn) {
            return "邻码";
        }

        @Override
        public Map<String, String> getFilterLimit(String gameEn) {
            return null;
        }

        @Override
        public String[] getFilterCustom(String gameEn) {
            return getOneStr(gameEn);
        }

        @Override
        public String getFilterRangeName(String gameEn) {
            return null;
        }

        @Override
        public String getFilterCustomName(String gameEn) {
            return null;
        }

        @Override
        public Boolean filterAction(List<Integer> redBalls, String action, String gameEn) {
            int neighbor = 0;
            GamePeriod lastOpenPeriod = PeriodRedis.getLastOpenPeriodByGameId(GameCache.getGame(gameEn).getGameId());
            String redWinningNumber = lastOpenPeriod.getWinningNumbers().split(CommonConstant.COMMON_COLON_STR)[0];
            for (String number : redWinningNumber.split(CommonConstant.SPACE_SPLIT_STR)) {
                if (redBalls.contains((Integer.valueOf(number) + 1)) || redBalls.contains((Integer.valueOf(number) -
                        1))) {
                    neighbor++;
                }
            }
            for (String actionValue : action.split(CommonConstant.COMMA_SPLIT_STR)) {
                if (String.valueOf(neighbor).equals(actionValue)) {
                    return Boolean.TRUE;
                }
            }
            return Boolean.FALSE;
        }

        /* 生成推荐*/
        @Override
        public List<String> rebuildRecommend(String gameEn, List<GamePeriod> periods) {
            List<String> recommends = new ArrayList<>();
            /* 计算每个属性的个数*/
            String[] filterSecond = this.getFilterCustom(gameEn);
            Map<String, Integer> filterSecondCount = getFilterIndexCountArr(filterSecond);

            List<String[]> filterClass = new ArrayList<>();
            filterClass.add(filterSecond);

            List<Map<String, Integer>> filterIndexArr = new ArrayList<>();
            filterIndexArr.add(filterSecondCount);

            filterIndexArr = processFilterRecommend(periods, filterClass, FilterConstant.FILTER_NEIGHBOR,
                    filterIndexArr, gameEn);

            getRecommendByLength(recommends, 2, filterIndexArr.get(0));

            return recommends;
        }

        /* 说明*/
        @Override
        public String getFilterIntroduction(String gameEn) {
            return "和上期红球号码相邻的号码个数。";
        }
    },
    /* 孤码数*/
    ALONE(FilterConstant.FILTER_ALONE) {
        @Override
        public String getFilterName(String gameEn) {
            return FilterConstant.FILTER_ALONE_CN;
        }

        @Override
        public String getFilterType(String gameEn) {
            return FilterConstant.FILTER_AC_TYPE;
        }

        @Override
        public String[] getFilterRange(String gameEn) {
            return null;
        }

        @Override
        public String getFilterTitle(String gameEn) {
            return "孤码";
        }

        @Override
        public Map<String, String> getFilterLimit(String gameEn) {
            return null;
        }

        @Override
        public String[] getFilterCustom(String gameEn) {
            return getOneStr(gameEn);
        }

        @Override
        public String getFilterRangeName(String gameEn) {
            return null;
        }

        @Override
        public String getFilterCustomName(String gameEn) {
            return null;
        }

        @Override
        public Boolean filterAction(List<Integer> redBalls, String action, String gameEn) {
            int alone = 0;
            GamePeriod lastOpenPeriod = PeriodRedis.getLastOpenPeriodByGameId(GameCache.getGame(gameEn).getGameId());
            String redWinningNumber = lastOpenPeriod.getWinningNumbers().split(CommonConstant.COMMON_COLON_STR)[0];
            for (String number : redWinningNumber.split(CommonConstant.SPACE_SPLIT_STR)) {
                Integer numberInt = Integer.valueOf(number);
                if (!redBalls.contains(numberInt) && !redBalls.contains((numberInt + 1)) && !redBalls.contains
                        (numberInt - 1)) {
                    alone++;
                }
            }
            for (String actionValue : action.split(CommonConstant.COMMA_SPLIT_STR)) {
                if (String.valueOf(alone).equals(actionValue)) {
                    return Boolean.TRUE;
                }
            }
            return Boolean.FALSE;
        }

        /* 生成推荐*/
        @Override
        public List<String> rebuildRecommend(String gameEn, List<GamePeriod> periods) {
            List<String> recommends = new ArrayList<>();
            /* 计算每个属性的个数*/
            String[] filterSecond = this.getFilterCustom(gameEn);
            Map<String, Integer> filterSecondCount = getFilterIndexCountArr(filterSecond);

            List<String[]> filterClass = new ArrayList<>();
            filterClass.add(filterSecond);

            List<Map<String, Integer>> filterIndexArr = new ArrayList<>();
            filterIndexArr.add(filterSecondCount);

            filterIndexArr = processFilterRecommend(periods, filterClass, FilterConstant.FILTER_ALONE, filterIndexArr,
                    gameEn);

            getRecommendByLength(recommends, 2, filterIndexArr.get(0));

            return recommends;
        }

        /* 说明*/
        @Override
        public String getFilterIntroduction(String gameEn) {
            return "不是重码也不是临码的号码个数。";
        }
    };

    private String filterAction;

    FilterEnum(String filterAction) {
        this.filterAction = filterAction;
    }

    public static FilterEnum getFilterEnum(String filterAction) {
        for (FilterEnum filterEnum : values()) {
            if (filterEnum.getFilterAction().equals(filterAction)) {
                return filterEnum;
            }
        }
        return null;
    }

    public String getFilterAction() {
        return filterAction;
    }

    public String getFilterName(String gameEn) {
        throw new AbstractMethodError("getFilterName error");
    }

    public String getFilterType(String gameEn) {
        throw new AbstractMethodError("getFilterType error");
    }

    public String getFilterTitle(String gameEn) {
        throw new AbstractMethodError("getFilterTitle error");
    }

    public String[] getFilterRange(String gameEn) {
        throw new AbstractMethodError("getFilterRange error");
    }

    public Map<String, String> getFilterLimit(String gameEn) {
        throw new AbstractMethodError("getFilterLimit error");
    }

    public String[] getFilterCustom(String gameEn) {
        throw new AbstractMethodError("getFilterCustom error");
    }

    public String getFilterRangeName(String gameEn) {
        throw new AbstractMethodError("getFilterRangeName error");
    }

    public String getFilterCustomName(String gameEn) {
        throw new AbstractMethodError("getFilterCustomName error");
    }

    public String getFilterIntroduction(String gameEn) {
        throw new AbstractMethodError("getFilterIntroduction error");
    }

    public String getIsMultipleChoose(String gameEn) {
        return "(可多选)";
    }

    /* 推荐*/
    public List<String> rebuildRecommend(String gameEn, List<GamePeriod> periods) {
        throw new AbstractMethodError("rebuildRecommend error");
    }

    public Boolean filterAction(List<Integer> redBalls, String action, String gameEn) {
        throw new AbstractMethodError("filterAction error");
    }

    public String[] getOneStr(String gameEn) {
        GameEnum gameEnum = GameEnum.getGameEnumByEn(gameEn);
        String[] arr = new String[gameEnum.getSingleRedBallLength() + 1];
        for (int i = 0; i <= gameEnum.getSingleRedBallLength(); i++) {
            arr[i] = String.valueOf(i);
        }
        return arr;
    }

    public String[] getTwoDivStr(String gameEn) {
        GameEnum gameEnum = GameEnum.getGameEnumByEn(gameEn);
        String[] arr = new String[gameEnum.getSingleRedBallLength() + 1];
        for (int i = 0; i <= gameEnum.getSingleRedBallLength(); i++) {
            arr[i] = i + ":" + (gameEnum.getSingleRedBallLength() - i);
        }
        return arr;
    }

    public String[] getThreeDivStr(String gameEn) {
        GameEnum gameEnum = GameEnum.getGameEnumByEn(gameEn);
        List<String> list = new ArrayList<>();
        for (int i = 0; i <= gameEnum.getSingleRedBallLength(); i++) {
            for (int j = 0; j <= gameEnum.getSingleRedBallLength() - i; j++) {
                list.add(i + ":" + j + ":" + (gameEnum.getSingleRedBallLength() - i - j));
            }
        }
        String[] l = new String[list.size()];

        return list.toArray(l);
    }

    public static String getResultShowText(Integer[] resultNum) {
        return "缩水前" + resultNum[0] + "注，缩水后" + resultNum[1] + "注，缩水率" + String.format("%.2f", (100 - (float)
                resultNum[1] / (float) resultNum[0] * 100)) + "%";
    }

    public static Integer getNextPage(Integer resultNum, Integer pageCount, Integer pageIndex) {
        if (resultNum > ((pageIndex + 1) * pageCount)) {
            pageIndex++;
        }
        return pageIndex;
    }

    public static Map<String, Integer> getFilterIndexCountArr(String[] filterArr) {
        Map<String, Integer> filterCountMap = new HashMap<>();
        for (int i = 0; i < filterArr.length; i++) {
            filterCountMap.put(filterArr[i], 0);
        }
        return filterCountMap;
    }

    public static void getRecommendByLength(List<String> recommends, Integer length, Map<String, Integer> filterMap) {
        int index = 0;
        for (Map.Entry<String, Integer> entry : filterMap.entrySet()) {
            if (index < length) {
                recommends.add(entry.getKey());
            }
            index++;
        }
    }

    public static List<Map<String, Integer>> processFilterRecommend(List<GamePeriod> periods, List<String[]>
            filterClass, String
                                                                            filterAction, List<Map<String, Integer>>
            filterIndexArr, String gameEn) {
        for (GamePeriod period : periods) {
            String[] redBalls = period.getWinningNumbers().split(CommonConstant.COMMON_COLON_STR)[0].split
                    (CommonConstant.SPACE_SPLIT_STR);
            List<Integer> list = new ArrayList<>();
            for (String ball : redBalls) {
                list.add(Integer.valueOf(ball));
            }
            for (int i = 0; i < filterClass.size(); i++) {
                for (int j = 0; j < filterClass.get(i).length; j++) {
                    if (FilterEnum.getFilterEnum(filterAction).filterAction(list, filterClass.get(i)[j], gameEn)) {
                        filterIndexArr.get(i).put(filterClass.get(i)[j], filterIndexArr.get(i).get(filterClass
                                .get(i)[j]) + 1);
                    }
                }
            }
        }
        List<Map<String, Integer>> list = new ArrayList<>();
        for (int i = 0; i < filterClass.size(); i++) {

            Map<String, Integer> result = new LinkedHashMap<>();

            //sort by value, and reserve, 10,9,8,7,6...
            filterIndexArr.get(i).entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .forEachOrdered(x -> result.put(x.getKey(), x.getValue()));
            list.add(result);
        }
        return list;
    }
}
