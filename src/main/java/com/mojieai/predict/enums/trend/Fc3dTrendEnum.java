package com.mojieai.predict.enums.trend;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.TrendConstant;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.TrendUtil;

import java.util.Map;

public enum Fc3dTrendEnum implements TrendEnumInterface {
    HUNDRED {
        @Override
        public String getTableName(Long gameId) {
            return "TB_FC3D_HUNDRED_TREND";
        }

        @Override
        public String[] getBalls(String winningNumber) {
            return CommonUtil.getFc3dBall(winningNumber, CommonConstant.FC_HUNDRED_DIGIT);
        }

        @Override
        public String[] getBallColumns() {
            return getFc3dDigitColumn();
        }

        @Override
        public void generateNewTrend(long gameId, String periodId, String winningNumber, Map<String, Object>
                lastTrend) {
            TrendUtil.processMissingNumber(periodId, getBalls(winningNumber), getBallColumns(), lastTrend);
        }
    }, TEN {
        @Override
        public String getTableName(Long gameId) {
            return "TB_FC3D_TEN_TREND";
        }

        @Override
        public String[] getBalls(String winningNumber) {
            return CommonUtil.getFc3dBall(winningNumber, CommonConstant.FC_TEN_DIGIT);
        }

        @Override
        public String[] getBallColumns() {
            return getFc3dDigitColumn();
        }

        @Override
        public void generateNewTrend(long gameId, String periodId, String winningNumber, Map<String, Object>
                lastTrend) {
            TrendUtil.processMissingNumber(periodId, getBalls(winningNumber), getBallColumns(), lastTrend);
        }
    }, ONE {
        @Override
        public String getTableName(Long gameId) {
            return "TB_FC3D_ONE_TREND";
        }

        @Override
        public String[] getBalls(String winningNumber) {
            return CommonUtil.getFc3dBall(winningNumber, CommonConstant.FC_ONE_DIGIT);
        }

        @Override
        public String[] getBallColumns() {
            return getFc3dDigitColumn();
        }

        @Override
        public void generateNewTrend(long gameId, String periodId, String winningNumber, Map<String, Object>
                lastTrend) {
            TrendUtil.processMissingNumber(periodId, getBalls(winningNumber), getBallColumns(), lastTrend);
        }
    }, INDISTINCT_LOCATION {//不分位

        @Override
        public String getTableName(Long gameId) {
            return "TB_FC3D_INDISTINCT_LOCATION_TREND";
        }

        @Override
        public String[] getBalls(String winningNumber) {
            return CommonUtil.getFc3dBall(winningNumber, CommonConstant.FC_ALL_DIGIT);
        }

        @Override
        public String[] getBallColumns() {
            return getFc3dDigitColumn();
        }

        @Override
        public void generateNewTrend(long gameId, String periodId, String winningNumber, Map<String, Object>
                lastTrend) {
            TrendUtil.processMissingNumber(periodId, getBalls(winningNumber), getBallColumns(), lastTrend);
        }
    }, BIG_SMALL_RATIO {
        @Override
        public String getTableName(Long gameId) {
            return "TB_FC3D_BIG_SMALL_RATIO_TREND";
        }

        @Override
        public String[] getBalls(String winningNumber) {
            return CommonUtil.getFc3dBall(winningNumber, CommonConstant.FC_ALL_DIGIT);
        }

        @Override
        public String[] getBallColumns() {
            return getFc3dRatioColumn();
        }

        @Override
        public void generateNewTrend(long gameId, String periodId, String winningNumber, Map<String, Object>
                lastTrend) {
            TrendUtil.processOddEvenRatioMissingNumber(gameId, periodId, getBalls(winningNumber), lastTrend,
                    getBallColumns(), TrendConstant.TREND_TYPE_BIG_SMALL);
        }
    }, ODD_EVEN_RATIO {
        @Override
        public String getTableName(Long gameId) {
            return "TB_FC3D_ODD_EVEN_RATIO_TREND";
        }

        @Override
        public String[] getBalls(String winningNumber) {
            return CommonUtil.getFc3dBall(winningNumber, CommonConstant.FC_ALL_DIGIT);
        }

        @Override
        public String[] getBallColumns() {
            return getFc3dRatioColumn();
        }

        @Override
        public void generateNewTrend(long gameId, String periodId, String winningNumber, Map<String, Object>
                lastTrend) {
            TrendUtil.processOddEvenRatioMissingNumber(gameId, periodId, getBalls(winningNumber), lastTrend,
                    getBallColumns(), TrendConstant.TREND_TYPE_JIOU);
        }
    }, PRIME_COMPOSITE_RATIO {
        @Override
        public String getTableName(Long gameId) {
            return "TB_FC3D_PRIME_COMPOSITE_RATIO_TREND";
        }

        @Override
        public String[] getBalls(String winningNumber) {
            return CommonUtil.getFc3dBall(winningNumber, CommonConstant.FC_ALL_DIGIT);
        }

        @Override
        public String[] getBallColumns() {
            return getFc3dRatioColumn();
        }

        @Override
        public void generateNewTrend(long gameId, String periodId, String winningNumber, Map<String, Object>
                lastTrend) {
            TrendUtil.processOddEvenRatioMissingNumber(gameId, periodId, getBalls(winningNumber), lastTrend,
                    getBallColumns(), TrendConstant.TREND_TYPE_PRIME);
        }
    }, HUNDRED_SHAPE {
        @Override
        public String getTableName(Long gameId) {
            return "TB_FC3D_HUNDRED_SHAPE_TREND";
        }

        @Override
        public String[] getBalls(String winningNumber) {
            return CommonUtil.getFc3dBall(winningNumber, CommonConstant.FC_HUNDRED_DIGIT);
        }

        @Override
        public String[] getBallColumns() {
            return getFc3dShapeColumn();
        }

        @Override
        public void generateNewTrend(long gameId, String periodId, String winningNumber, Map<String, Object>
                lastTrend) {
            String hundredBall = getBalls(winningNumber)[0];
            TrendUtil.processShapeMissingNumber(gameId, periodId, hundredBall, lastTrend);
        }
    }, TEN_SHAPE {
        @Override
        public String getTableName(Long gameId) {
            return "TB_FC3D_TEN_SHAPE_TREND";
        }

        @Override
        public String[] getBalls(String winningNumber) {
            return CommonUtil.getFc3dBall(winningNumber, CommonConstant.FC_TEN_DIGIT);
        }

        @Override
        public String[] getBallColumns() {
            return getFc3dShapeColumn();
        }

        @Override
        public void generateNewTrend(long gameId, String periodId, String winningNumber, Map<String, Object>
                lastTrend) {
            String hundredBall = getBalls(winningNumber)[0];
            TrendUtil.processShapeMissingNumber(gameId, periodId, hundredBall, lastTrend);
        }
    }, ONE_SHAPE {
        @Override
        public String getTableName(Long gameId) {
            return "TB_FC3D_ONE_SHAPE_TREND";
        }

        @Override
        public String[] getBalls(String winningNumber) {
            return CommonUtil.getFc3dBall(winningNumber, CommonConstant.FC_ONE_DIGIT);
        }

        @Override
        public String[] getBallColumns() {
            return getFc3dShapeColumn();
        }

        @Override
        public void generateNewTrend(long gameId, String periodId, String winningNumber, Map<String, Object>
                lastTrend) {
            String hundredBall = getBalls(winningNumber)[0];
            TrendUtil.processShapeMissingNumber(gameId, periodId, hundredBall, lastTrend);
        }
    };

    @Override
    public String[] getExtaColumn() {
        return new String[0];
    }

    private static String[] getFc3dDigitColumn() {
        return new String[]{"BALL_0", "BALL_1", "BALL_2", "BALL_3", "BALL_4", "BALL_5", "BALL_6", "BALL_7", "BALL_8",
                "BALL_9"};
    }

    private static String[] getFc3dRatioColumn() {
        return new String[]{"RATIO_0_3", "RATIO_1_2", "RATIO_2_1", "RATIO_3_0"};
    }

    private static String[] getFc3dShapeColumn() {
        return new String[]{"BIG", "SMALL", "ODD", "EVEN", "PRIME", "COMPOSITE", "ROUTE_0", "ROUTE_1", "ROUTE_2"};
    }
}
