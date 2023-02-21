package com.mojieai.predict.enums.trend;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.TrendConstant;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.TrendUtil;

import java.util.Map;

public enum DltTrendEnum implements TrendEnumInterface {
    FRONT {
        @Override
        public String getTableName(Long gameId) {
            return "TB_DLT_FRONT_TREND";
        }

        @Override
        public String[] getBalls(String winningNumber) {
            return CommonUtil.getBalls(winningNumber, CommonConstant.ZERO);
        }

        @Override
        public String[] getBallColumns() {
            return new String[]{"FRONT_01", "FRONT_02", "FRONT_03", "FRONT_04", "FRONT_05", "FRONT_06",
                    "FRONT_07", "FRONT_08", "FRONT_09", "FRONT_10", "FRONT_11", "FRONT_12", "FRONT_13",
                    "FRONT_14", "FRONT_15", "FRONT_16", "FRONT_17", "FRONT_18", "FRONT_19", "FRONT_20",
                    "FRONT_21", "FRONT_22", "FRONT_23", "FRONT_24", "FRONT_25", "FRONT_26", "FRONT_27",
                    "FRONT_28", "FRONT_29", "FRONT_30", "FRONT_31", "FRONT_32", "FRONT_33", "FRONT_34",
                    "FRONT_35"};
        }

        @Override
        public void generateNewTrend(long gameId, String periodId, String winningNumber, Map<String, Object>
                lastTrend) {
            TrendUtil.processMissingNumber(periodId, getBalls(winningNumber), getBallColumns(), lastTrend);
        }
    }, BACK {
        @Override
        public String getTableName(Long gameId) {
            return "TB_DLT_BACK_TREND";
        }

        @Override
        public String[] getBalls(String winningNumber) {
            return CommonUtil.getBalls(winningNumber, CommonConstant.ONE);
        }

        @Override
        public String[] getBallColumns() {
            return new String[]{"BACK_01", "BACK_02", "BACK_03", "BACK_04", "BACK_05", "BACK_06", "BACK_07",
                    "BACK_08", "BACK_09", "BACK_10", "BACK_11", "BACK_12"};
        }

        @Override
        public void generateNewTrend(long gameId, String periodId, String winningNumber, Map<String, Object>
                lastTrend) {
            TrendUtil.processMissingNumber(periodId, getBalls(winningNumber), getBallColumns(), lastTrend);
        }
    }, LEADING_BALL {
        @Override
        public String getTableName(Long gameId) {
            return "TB_DLT_LEADING_FORM_TREND";
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
            return "TB_DLT_SWALLOWTAIL_FORM_TREND";
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
            return new String[]{"END_NUMBER"};
        }

        @Override
        public void generateNewTrend(long gameId, String periodId, String winningNumber, Map<String, Object>
                lastTrend) {
            TrendUtil.processFormBallMissingFormNumber(gameId, periodId, getBalls(winningNumber)[getBalls
                    (winningNumber).length - 1], getExtaColumn()[0], lastTrend);
        }
    }, JIOU {
        @Override
        public String getTableName(Long gameId) {
            return "TB_DLT_JIOU_TREND";
        }

        @Override
        public String[] getBalls(String winningNumber) {
            return CommonUtil.getBalls(winningNumber, CommonConstant.ZERO);
        }

        @Override
        public String[] getBallColumns() {
            return new String[]{"RED_1_S", "RED_1_D", "RED_2_S", "RED_2_D", "RED_3_S", "RED_3_D", "RED_4_S",
                    "RED_4_D", "RED_5_S", "RED_5_D", "RED_C_1", "RED_C_2", "RED_C_3", "RED_C_4", "RED_C_5", "RED_C_6"};
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
            return "TB_DLT_BIG_SMALL_TREND";
        }

        @Override
        public String[] getBalls(String winningNumber) {
            return CommonUtil.getBalls(winningNumber, CommonConstant.ZERO);
        }

        @Override
        public String[] getBallColumns() {
            return new String[]{"RED_1_S", "RED_1_D", "RED_2_S", "RED_2_D", "RED_3_S", "RED_3_D", "RED_4_S",
                    "RED_4_D", "RED_5_S", "RED_5_D", "RED_C_1", "RED_C_2", "RED_C_3", "RED_C_4", "RED_C_5", "RED_C_6"};
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
            return "TB_DLT_PRIME_TREND";
        }

        @Override
        public String[] getBalls(String winningNumber) {
            return CommonUtil.getBalls(winningNumber, CommonConstant.ZERO);
        }

        @Override
        public String[] getBallColumns() {
            return new String[]{"RED_1_S", "RED_1_D", "RED_2_S", "RED_2_D", "RED_3_S", "RED_3_D", "RED_4_S",
                    "RED_4_D", "RED_5_S", "RED_5_D", "RED_C_1", "RED_C_2", "RED_C_3", "RED_C_4", "RED_C_5", "RED_C_6"};
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
            return "TB_DLT_ZERO_ONE_TWO_WAY_TREND";
        }

        @Override
        public String[] getBalls(String winningNumber) {
            return CommonUtil.getBalls(winningNumber, CommonConstant.ZERO);
        }

        @Override
        public String[] getBallColumns() {
            return new String[]{"RED_1_Z", "RED_1_O", "RED_1_T", "RED_2_Z", "RED_2_O", "RED_2_T", "RED_3_Z",
                    "RED_3_O", "RED_3_T", "RED_4_Z", "RED_4_O", "RED_4_T", "RED_5_Z", "RED_5_O", "RED_5_T",
                    "RED_1_C", "RED_2_C", "RED_3_C", "RED_4_C", "RED_5_C", "RED_6_C", "RED_7_C",
                    "RED_8_C", "RED_9_C", "RED_10_C", "RED_11_C", "RED_12_C", "RED_13_C", "RED_14_C", "RED_15_C",
                    "RED_16_C", "RED_17_C", "RED_18_C", "RED_19_C", "RED_20_C", "RED_21_C"};
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
            return "TB_DLT_AC_VALUE_TREND";
        }

        @Override
        public String[] getBalls(String winningNumber) {
            return CommonUtil.getBalls(winningNumber, CommonConstant.ZERO);
        }

        @Override
        public String[] getBallColumns() {
            return new String[]{"AC_VALUE", "AC_SINGLE", "AC_DOUBLE", "AC_PRIME", "AC_C", "AC_VALUE_0", "AC_VALUE_1",
                    "AC_VALUE_2", "AC_VALUE_3", "AC_VALUE_4", "AC_VALUE_5", "AC_VALUE_6"};
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
            return "TB_DLT_SPAN_VALUE_TREND";
        }

        @Override
        public String[] getBalls(String winningNumber) {
            return CommonUtil.getBalls(winningNumber, CommonConstant.ZERO);
        }

        @Override
        public String[] getBallColumns() {
            return new String[]{"SPAN_VALUE", "SPAN_SINGLE", "SPAN_DOUBLE", "SPAN_PRIME", "SPAN_C", "SPAN_VALUE_4",
                    "SPAN_VALUE_5", "SPAN_VALUE_6", "SPAN_VALUE_7", "SPAN_VALUE_8", "SPAN_VALUE_9", "SPAN_VALUE_10",
                    "SPAN_VALUE_11", "SPAN_VALUE_12", "SPAN_VALUE_13", "SPAN_VALUE_14", "SPAN_VALUE_15",
                    "SPAN_VALUE_16", "SPAN_VALUE_17", "SPAN_VALUE_18", "SPAN_VALUE_19", "SPAN_VALUE_20",
                    "SPAN_VALUE_21", "SPAN_VALUE_22", "SPAN_VALUE_23", "SPAN_VALUE_24", "SPAN_VALUE_25",
                    "SPAN_VALUE_26", "SPAN_VALUE_27", "SPAN_VALUE_28", "SPAN_VALUE_29", "SPAN_VALUE_30",
                    "SPAN_VALUE_31", "SPAN_VALUE_32", "SPAN_VALUE_33", "SPAN_VALUE_34"};
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
            return "TB_DLT_HZ_TREND";
        }

        @Override
        public String[] getBalls(String winningNumber) {
            return CommonUtil.getBalls(winningNumber, CommonConstant.ZERO);
        }

        @Override
        public String[] getBallColumns() {
            return new String[]{"RED_HZ_49", "RED_HZ_59", "RED_HZ_69", "RED_HZ_79", "RED_HZ_89", "RED_HZ_99",
                    "RED_HZ_109", "RED_HZ_119", "RED_HZ_129", "RED_HZ_139", "RED_HZ_165"};
        }

        @Override
        public void generateNewTrend(long gameId, String periodId, String winningNumber, Map<String, Object>
                lastTrend) {
            TrendUtil.processHeZhiMissingNumber(gameId, periodId, getBalls(winningNumber), lastTrend, getBallColumns());
        }
    }, WEI_VALUE {
        @Override
        public String getTableName(Long gameId) {
            return "TB_DLT_WS_TREND";
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