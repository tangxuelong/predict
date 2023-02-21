package com.mojieai.predict.enums.trend;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.TrendConstant;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.TrendUtil;

import java.util.Map;

public enum SsqTrendEnum implements TrendEnumInterface {
    RED {
        @Override
        public String getTableName(Long gameId) {
            return "TB_SSQ_RED_TREND";
        }

        @Override
        public String[] getBalls(String winningNumber) {
            return CommonUtil.getBalls(winningNumber, CommonConstant.ZERO);
        }

        @Override
        public String[] getBallColumns() {
            return new String[]{"RED_01", "RED_02", "RED_03", "RED_04", "RED_05", "RED_06", "RED_07", "RED_08",
                    "RED_09", "RED_10", "RED_11", "RED_12", "RED_13", "RED_14", "RED_15", "RED_16", "RED_17",
                    "RED_18", "RED_19", "RED_20", "RED_21", "RED_22", "RED_23", "RED_24", "RED_25", "RED_26",
                    "RED_27", "RED_28", "RED_29", "RED_30", "RED_31", "RED_32", "RED_33"};
        }

        @Override
        public void generateNewTrend(long gameId, String periodId, String winningNumber, Map<String, Object>
                lastTrend) {
            TrendUtil.processMissingNumber(periodId, getBalls(winningNumber), getBallColumns(), lastTrend);
        }
    }, BLUE {
        @Override
        public String getTableName(Long gameId) {
            return "TB_SSQ_BLUE_TREND";
        }

        @Override
        public String[] getBalls(String winningNumber) {
            return CommonUtil.getBalls(winningNumber, CommonConstant.ONE);
        }

        @Override
        public String[] getBallColumns() {
            return new String[]{"BLUE_01", "BLUE_02", "BLUE_03", "BLUE_04", "BLUE_05", "BLUE_06", "BLUE_07", "BLUE_08",
                    "BLUE_09", "BLUE_10", "BLUE_11", "BLUE_12", "BLUE_13", "BLUE_14", "BLUE_15", "BLUE_16"};
        }

        @Override
        public void generateNewTrend(long gameId, String periodId, String winningNumber, Map<String, Object>
                lastTrend) {
            TrendUtil.processMissingNumber(periodId, getBalls(winningNumber), getBallColumns(), lastTrend);
        }
    }, LEADING_BALL {
        @Override
        public String getTableName(Long gameId) {
            return "TB_SSQ_LEADING_FORM_TREND";
        }

        @Override
        public String[] getBalls(String winningNumber) {
            return CommonUtil.getBalls(winningNumber, CommonConstant.ZERO);
        }

        @Override
        public String[] getBallColumns() {
            return new String[]{"AREA_1", "AREA_2", "AREA_3", "ODD", "EVEN", "ROUTE_0", "ROUTE_1",
                    "ROUTE_2", "PRIME", "COMPOSITE"};
        }

        @Override
        public String[] getExtaColumn() {
            return new String[]{"LEADING_NUMBER"};
        }

        @Override
        public void generateNewTrend(long gameId, String periodId, String winningNumber, Map<String, Object>
                lastTrend) {
            TrendUtil.processFormBallMissingFormNumber(gameId, periodId, getBalls(winningNumber)[0], "LEADING_NUMBER",
                    lastTrend);
        }
    }, SWALLOW_TAIL {
        @Override
        public String getTableName(Long gameId) {
            return "TB_SSQ_SWALLOWTAIL_FORM_TREND";
        }

        @Override
        public String[] getBalls(String winningNumber) {
            return CommonUtil.getBalls(winningNumber, CommonConstant.ZERO);
        }

        @Override
        public String[] getBallColumns() {
            return new String[]{"AREA_1", "AREA_2", "AREA_3", "ODD", "EVEN", "ROUTE_0", "ROUTE_1",
                    "ROUTE_2", "PRIME", "COMPOSITE"};
        }

        @Override
        public void generateNewTrend(long gameId, String periodId, String winningNumber, Map<String, Object>
                lastTrend) {
            TrendUtil.processFormBallMissingFormNumber(gameId, periodId, getBalls(winningNumber)[getBalls
                    (winningNumber).length - 1], getExtaColumn()[0], lastTrend);
        }

        @Override
        public String[] getExtaColumn() {
            return new String[]{"END_NUMBER"};
        }
    }, BLUE_FORM {
        @Override
        public String getTableName(Long gameId) {
            return "TB_SSQ_BLUE_FORM_TREND";
        }

        @Override
        public String[] getBalls(String winningNumber) {
            return CommonUtil.getBalls(winningNumber, CommonConstant.ONE);
        }

        @Override
        public String[] getBallColumns() {
            return new String[]{"SMALL", "BIG", "ODD", "EVEN", "ROUTE_0", "ROUTE_1",
                    "ROUTE_2", "PRIME", "COMPOSITE"};
        }

        @Override
        public void generateNewTrend(long gameId, String periodId, String winningNumber, Map<String, Object>
                lastTrend) {
            TrendUtil.processFormBallMissingFormNumber(gameId, periodId, getBalls(winningNumber)[getBalls
                    (winningNumber).length - 1], getExtaColumn()[0], lastTrend);
        }

        @Override
        public String[] getExtaColumn() {
            return new String[]{"BLUE_NUMBER"};
        }
    }, JIOU {
        @Override
        public String getTableName(Long gameId) {
            return "TB_SSQ_JIOU_TREND";
        }

        @Override
        public String[] getBalls(String winningNumber) {
            return CommonUtil.getBalls(winningNumber, CommonConstant.ZERO);
        }

        @Override
        public String[] getBallColumns() {
            return new String[]{"RED_1_S", "RED_1_D", "RED_2_S", "RED_2_D", "RED_3_S", "RED_3_D", "RED_4_S",
                    "RED_4_D", "RED_5_S", "RED_5_D", "RED_6_S", "RED_6_D", "RED_C_1", "RED_C_2", "RED_C_3", "RED_C_4",
                    "RED_C_5", "RED_C_6", "RED_C_7"};
        }

        @Override
        public void generateNewTrend(long gameId, String periodId, String winningNumber, Map<String, Object>
                lastTrend) {
            String[] winningNumberRed = getBalls(winningNumber);
            String[] ballColumns = getBallColumns();
            TrendUtil.processJiOuMissingNumber(gameId, periodId, winningNumber, lastTrend, winningNumberRed,
                    ballColumns, TrendConstant.TREND_TYPE_JIOU);
        }
    }, BIG_SMALL {
        @Override
        public String getTableName(Long gameId) {
            return "TB_SSQ_BIG_SMALL_TREND";
        }

        @Override
        public String[] getBalls(String winningNumber) {
            return CommonUtil.getBalls(winningNumber, CommonConstant.ZERO);
        }

        @Override
        public String[] getBallColumns() {
            return new String[]{"RED_1_S", "RED_1_D", "RED_2_S", "RED_2_D", "RED_3_S", "RED_3_D", "RED_4_S",
                    "RED_4_D", "RED_5_S", "RED_5_D", "RED_6_S", "RED_6_D", "RED_C_1", "RED_C_2", "RED_C_3", "RED_C_4",
                    "RED_C_5", "RED_C_6", "RED_C_7"};
        }

        @Override
        public void generateNewTrend(long gameId, String periodId, String winningNumber, Map<String, Object>
                lastTrend) {
            String[] winningNumberRed = getBalls(winningNumber);
            String[] ballColumns = getBallColumns();
            TrendUtil.processJiOuMissingNumber(gameId, periodId, winningNumber, lastTrend, winningNumberRed,
                    ballColumns, TrendConstant.TREND_TYPE_BIG_SMALL);
        }
    }, PRIME {
        @Override
        public String getTableName(Long gameId) {
            return "TB_SSQ_PRIME_TREND";
        }

        @Override
        public String[] getBalls(String winningNumber) {
            return CommonUtil.getBalls(winningNumber, CommonConstant.ZERO);
        }

        @Override
        public String[] getBallColumns() {
            return new String[]{"RED_1_S", "RED_1_D", "RED_2_S", "RED_2_D", "RED_3_S", "RED_3_D", "RED_4_S",
                    "RED_4_D", "RED_5_S", "RED_5_D", "RED_6_S", "RED_6_D", "RED_C_1", "RED_C_2", "RED_C_3", "RED_C_4",
                    "RED_C_5", "RED_C_6", "RED_C_7"};
        }

        @Override
        public void generateNewTrend(long gameId, String periodId, String winningNumber, Map<String, Object>
                lastTrend) {
            String[] winningNumberRed = getBalls(winningNumber);
            String[] ballColumns = getBallColumns();
            TrendUtil.processJiOuMissingNumber(gameId, periodId, winningNumber, lastTrend, winningNumberRed,
                    ballColumns, TrendConstant.TREND_TYPE_PRIME);
        }
    }, ZERO_ONE_TWO_WAY {
        @Override
        public String getTableName(Long gameId) {
            return "TB_SSQ_ZERO_ONE_TWO_WAY_TREND";
        }

        @Override
        public String[] getBalls(String winningNumber) {
            return CommonUtil.getBalls(winningNumber, CommonConstant.ZERO);
        }

        @Override
        public String[] getBallColumns() {
            return new String[]{"RED_1_Z", "RED_1_O", "RED_1_T", "RED_2_Z", "RED_2_O", "RED_2_T", "RED_3_Z",
                    "RED_3_O", "RED_3_T", "RED_4_Z", "RED_4_O", "RED_4_T", "RED_5_Z", "RED_5_O", "RED_5_T",
                    "RED_6_Z", "RED_6_O", "RED_6_T", "RED_1_C", "RED_2_C", "RED_3_C", "RED_4_C", "RED_5_C",
                    "RED_6_C", "RED_7_C", "RED_8_C", "RED_9_C", "RED_10_C", "RED_11_C", "RED_12_C", "RED_13_C",
                    "RED_14_C", "RED_15_C", "RED_16_C", "RED_17_C", "RED_18_C", "RED_19_C", "RED_20_C", "RED_21_C",
                    "RED_22_C", "RED_23_C", "RED_24_C", "RED_25_C", "RED_26_C", "RED_27_C", "RED_28_C"};
        }

        @Override
        public void generateNewTrend(long gameId, String periodId, String winningNumber, Map<String, Object>
                lastTrend) {
            String[] winningNumberRed = getBalls(winningNumber);
            String[] ballColumns = getBallColumns();
            TrendUtil.processZeroOneTwoMissingNumber(gameId, periodId, winningNumber, lastTrend, winningNumberRed,
                    ballColumns, TrendConstant.TREND_TYPE_ZERO_ONE_TWO);
        }
    }, AC_VALUE {
        @Override
        public String getTableName(Long gameId) {
            return "TB_SSQ_AC_VALUE_TREND";
        }

        @Override
        public String[] getBalls(String winningNumber) {
            return CommonUtil.getBalls(winningNumber, CommonConstant.ZERO);
        }

        @Override
        public String[] getBallColumns() {
            return new String[]{"AC_VALUE", "AC_SINGLE", "AC_DOUBLE", "AC_PRIME", "AC_C", "AC_VALUE_0", "AC_VALUE_1",
                    "AC_VALUE_2", "AC_VALUE_3", "AC_VALUE_4", "AC_VALUE_5", "AC_VALUE_6", "AC_VALUE_7", "AC_VALUE_8",
                    "AC_VALUE_9", "AC_VALUE_10"};
        }

        @Override
        public void generateNewTrend(long gameId, String periodId, String winningNumber, Map<String, Object>
                lastTrend) {
            String[] winningNumberRed = getBalls(winningNumber);
            String[] ballColumns = getBallColumns();
            TrendUtil.processACMissingNumber(gameId, periodId, winningNumber, lastTrend, winningNumberRed,
                    ballColumns, TrendConstant.TREND_TYPE_AC_VALUE);
        }
    }, SPAN_VALUE {
        @Override
        public String getTableName(Long gameId) {
            return "TB_SSQ_SPAN_VALUE_TREND";
        }

        @Override
        public String[] getBalls(String winningNumber) {
            return CommonUtil.getBalls(winningNumber, CommonConstant.ZERO);
        }

        @Override
        public String[] getBallColumns() {
            return new String[]{"SPAN_VALUE", "SPAN_SINGLE", "SPAN_DOUBLE", "SPAN_PRIME", "SPAN_C", "SPAN_VALUE_5",
                    "SPAN_VALUE_6", "SPAN_VALUE_7", "SPAN_VALUE_8", "SPAN_VALUE_9", "SPAN_VALUE_10", "SPAN_VALUE_11",
                    "SPAN_VALUE_12", "SPAN_VALUE_13", "SPAN_VALUE_14", "SPAN_VALUE_15", "SPAN_VALUE_16",
                    "SPAN_VALUE_17", "SPAN_VALUE_18", "SPAN_VALUE_19", "SPAN_VALUE_20", "SPAN_VALUE_21",
                    "SPAN_VALUE_22", "SPAN_VALUE_23", "SPAN_VALUE_24", "SPAN_VALUE_25", "SPAN_VALUE_26",
                    "SPAN_VALUE_27", "SPAN_VALUE_28", "SPAN_VALUE_29", "SPAN_VALUE_30", "SPAN_VALUE_31",
                    "SPAN_VALUE_32"};
        }

        @Override
        public void generateNewTrend(long gameId, String periodId, String winningNumber, Map<String, Object>
                lastTrend) {
            String[] winningNumberRed = getBalls(winningNumber);
            String[] ballColumns = getBallColumns();
            TrendUtil.processACMissingNumber(gameId, periodId, winningNumber, lastTrend, winningNumberRed,
                    ballColumns, TrendConstant.TREND_TYPE_SPAN_VALUE);
        }
    }, HEZHI {
        @Override
        public String getTableName(Long gameId) {
            return "TB_SSQ_HZ_TREND";
        }

        @Override
        public String[] getBalls(String winningNumber) {
            return CommonUtil.getBalls(winningNumber, CommonConstant.ZERO);
        }

        @Override
        public String[] getBallColumns() {
            return new String[]{"RED_HZ_49", "RED_HZ_59", "RED_HZ_69", "RED_HZ_79", "RED_HZ_89", "RED_HZ_99",
                    "RED_HZ_109", "RED_HZ_119", "RED_HZ_129", "RED_HZ_139", "RED_HZ_183"};
        }

        @Override
        public void generateNewTrend(long gameId, String periodId, String winningNumber, Map<String, Object>
                lastTrend) {
            TrendUtil.processHeZhiMissingNumber(gameId, periodId, getBalls(winningNumber), lastTrend, getBallColumns());
        }
    }, WEI_VALUE {
        @Override
        public String getTableName(Long gameId) {
            return "TB_SSQ_WS_TREND";
        }

        @Override
        public String[] getBalls(String winningNumber) {
            return CommonUtil.getBalls(winningNumber, CommonConstant.ZERO);
        }

        @Override
        public String[] getBallColumns() {
            return new String[]{"WS_0", "WS_1", "WS_2", "WS_3", "WS_4", "WS_5", "WS_6", "WS_7", "WS_8", "WS_9"};
        }

        @Override
        public void generateNewTrend(long gameId, String periodId, String winningNumber, Map<String, Object>
                lastTrend) {
            TrendUtil.processWeiShuMissingNumber(periodId, getBalls(winningNumber), lastTrend, getBallColumns());
        }
    };

    @Override
    public String[] getExtaColumn() {
        return null;
    }
}