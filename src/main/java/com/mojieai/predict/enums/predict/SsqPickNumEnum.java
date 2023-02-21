package com.mojieai.predict.enums.predict;

import com.mojieai.predict.constant.PredictConstant;
import com.mojieai.predict.entity.po.PredictRedBall;
import com.mojieai.predict.service.predict.AbstractPredictDb;
import com.mojieai.predict.service.predict.AbstractPredictView;
import com.mojieai.predict.util.PredictUtil;

import java.util.List;
import java.util.Map;

public enum SsqPickNumEnum implements PickNumPredict {
    BASE_STATE_KILL_THREE("基础态杀3", PredictConstant.PREDICT_RED_BALL_STR_TYPE_KILL_THREE, 3, PredictConstant
            .PREDICT_NUM_TYPE_RED_BALL) {
        @Override
        public Map<String, String> packPredictNum(List<PredictRedBall> predictRedBalls) {
            return PredictUtil.packMorePredictNum(predictRedBalls);
        }

        @Override
        public Boolean generatePredictNum(AbstractPredictDb predictDb, String periodId) {
            return true;//todo 将预测1w 中的杀3 注抽离处理啊
        }
    }, COLD_STATE_KILL_THREE_RED_100("冷态杀3", PredictConstant.COLD_STATE_KILL_THREE_RED_100, 3, PredictConstant
            .PREDICT_NUM_TYPE_RED_BALL) {
        @Override
        public Map<String, String> packPredictNum(List<PredictRedBall> predictRedBalls) {
            return PredictUtil.packMorePredictNum(predictRedBalls);
        }

        @Override
        public Boolean generatePredictNum(AbstractPredictDb predictDb, String periodId) {
            return predictDb.generateRedStateKill(this, periodId);
        }

        @Override
        public Integer getColdHotStateNum() {
            return 100;
        }

        @Override
        public String generateNewByNumModel(Long gameId, String periodId, String numModel) {
            return PredictUtil.generateColdStateKill3(this, periodId, numModel, PredictConstant
                    .SSQ_RED_HOT_STATE_OPTION_SIZE);
        }

        @Override
        public String getGenerateNewPredictModel(AbstractPredictDb predictDb, long gameId, String periodId) {
            return predictDb.getPeriodNumModel(this, gameId, periodId, PredictConstant.PREDICT_NUM_TYPE_RED_BALL);
        }
    }, HOT_STATE_KILL_THREE_RED_100("热态杀3", PredictConstant.HOT_STATE_KILL_THREE_RED_100, 3, PredictConstant
            .PREDICT_NUM_TYPE_RED_BALL) {
        @Override
        public Map<String, String> packPredictNum(List<PredictRedBall> predictRedBalls) {
            return PredictUtil.packMorePredictNum(predictRedBalls);
        }

        @Override
        public Boolean generatePredictNum(AbstractPredictDb predictDb, String periodId) {
            return predictDb.generateRedStateKill(this, periodId);
        }

        @Override
        public Integer getColdHotStateNum() {
            return 100;
        }

        @Override
        public String generateNewByNumModel(Long gameId, String periodId, String numModel) {
            return PredictUtil.generateHotStateKill3(this, periodId, numModel, PredictConstant
                    .SSQ_RED_HOT_STATE_OPTION_SIZE);
        }

        @Override
        public String getGenerateNewPredictModel(AbstractPredictDb predictDb, long gameId, String periodId) {
            return predictDb.getPeriodNumModel(this, gameId, periodId, PredictConstant.PREDICT_NUM_TYPE_RED_BALL);
        }
    }, CALL_BACK_STATE_KILL_THREE_RED_100("回补态杀3", PredictConstant.CALL_BACK_STATE_KILL_THREE_RED_100, 3,
            PredictConstant.PREDICT_NUM_TYPE_RED_BALL) {
        @Override
        public Map<String, String> packPredictNum(List<PredictRedBall> predictRedBalls) {
            return PredictUtil.packMorePredictNum(predictRedBalls);
        }

        @Override
        public Boolean generatePredictNum(AbstractPredictDb predictDb, String periodId) {
            return predictDb.generateRedStateKill(this, periodId);
        }

        @Override
        public Integer getColdHotStateNum() {
            return 100;
        }

        @Override
        public String generateNewByNumModel(Long gameId, String periodId, String numModel) {
            return PredictUtil.generateCallBackStateKill3(this, gameId, periodId, numModel, PredictConstant
                    .SSQ_RED_HOT_STATE_OPTION_SIZE);
        }

        @Override
        public String getGenerateNewPredictModel(AbstractPredictDb predictDb, long gameId, String periodId) {
            return predictDb.getLastPeriodNumModel(this, gameId, periodId, PredictConstant.PREDICT_NUM_TYPE_RED_BALL);
        }
    }, COLD_STATE_KILL_THREE_RED_200("冷态杀3", PredictConstant.COLD_STATE_KILL_THREE_RED_200, 3, PredictConstant
            .PREDICT_NUM_TYPE_RED_BALL) {
        @Override
        public Map<String, String> packPredictNum(List<PredictRedBall> predictRedBalls) {
            return PredictUtil.packMorePredictNum(predictRedBalls);
        }

        @Override
        public Boolean generatePredictNum(AbstractPredictDb predictDb, String periodId) {
            return predictDb.generateRedStateKill(this, periodId);
        }

        @Override
        public Integer getColdHotStateNum() {
            return 200;
        }

        @Override
        public String generateNewByNumModel(Long gameId, String periodId, String numModel) {
            return PredictUtil.generateColdStateKill3(this, periodId, numModel, PredictConstant
                    .SSQ_RED_HOT_STATE_OPTION_SIZE);
        }

        @Override
        public String getGenerateNewPredictModel(AbstractPredictDb predictDb, long gameId, String periodId) {
            return predictDb.getPeriodNumModel(this, gameId, periodId, PredictConstant.PREDICT_NUM_TYPE_RED_BALL);
        }
    }, HOT_STATE_KILL_THREE_RED_200("热态杀3", PredictConstant.HOT_STATE_KILL_THREE_RED_200, 3, PredictConstant
            .PREDICT_NUM_TYPE_RED_BALL) {
        @Override
        public Map<String, String> packPredictNum(List<PredictRedBall> predictRedBalls) {
            return PredictUtil.packMorePredictNum(predictRedBalls);
        }

        @Override
        public Boolean generatePredictNum(AbstractPredictDb predictDb, String periodId) {
            return predictDb.generateRedStateKill(this, periodId);
        }

        @Override
        public Integer getColdHotStateNum() {
            return 200;
        }

        @Override
        public String generateNewByNumModel(Long gameId, String periodId, String numModel) {
            return PredictUtil.generateHotStateKill3(this, periodId, numModel, PredictConstant
                    .SSQ_RED_HOT_STATE_OPTION_SIZE);
        }

        @Override
        public String getGenerateNewPredictModel(AbstractPredictDb predictDb, long gameId, String periodId) {
            return predictDb.getPeriodNumModel(this, gameId, periodId, PredictConstant.PREDICT_NUM_TYPE_RED_BALL);
        }
    }, CALL_BACK_STATE_KILL_THREE_RED_200("回补态杀3", PredictConstant.CALL_BACK_STATE_KILL_THREE_RED_200, 3,
            PredictConstant.PREDICT_NUM_TYPE_RED_BALL) {
        @Override
        public Map<String, String> packPredictNum(List<PredictRedBall> predictRedBalls) {
            return PredictUtil.packMorePredictNum(predictRedBalls);
        }

        @Override
        public Boolean generatePredictNum(AbstractPredictDb predictDb, String periodId) {
            return predictDb.generateRedStateKill(this, periodId);
        }

        @Override
        public Integer getColdHotStateNum() {
            return 200;
        }

        @Override
        public String generateNewByNumModel(Long gameId, String periodId, String numModel) {
            return PredictUtil.generateCallBackStateKill3(this, gameId, periodId, numModel, PredictConstant
                    .SSQ_RED_HOT_STATE_OPTION_SIZE);
        }

        @Override
        public String getGenerateNewPredictModel(AbstractPredictDb predictDb, long gameId, String periodId) {
            return predictDb.getLastPeriodNumModel(this, gameId, periodId, PredictConstant.PREDICT_NUM_TYPE_RED_BALL);
        }
    }, COLD_STATE_KILL_THREE_RED_500("冷态杀3", PredictConstant.COLD_STATE_KILL_THREE_RED_500, 3, PredictConstant
            .PREDICT_NUM_TYPE_RED_BALL) {
        @Override
        public Map<String, String> packPredictNum(List<PredictRedBall> predictRedBalls) {
            return PredictUtil.packMorePredictNum(predictRedBalls);
        }

        @Override
        public Boolean generatePredictNum(AbstractPredictDb predictDb, String periodId) {
            return predictDb.generateRedStateKill(this, periodId);
        }

        @Override
        public Integer getColdHotStateNum() {
            return 500;
        }

        @Override
        public String generateNewByNumModel(Long gameId, String periodId, String numModel) {
            return PredictUtil.generateColdStateKill3(this, periodId, numModel, PredictConstant
                    .SSQ_RED_HOT_STATE_OPTION_SIZE);
        }

        @Override
        public String getGenerateNewPredictModel(AbstractPredictDb predictDb, long gameId, String periodId) {
            return predictDb.getPeriodNumModel(this, gameId, periodId, PredictConstant.PREDICT_NUM_TYPE_RED_BALL);
        }
    }, HOT_STATE_KILL_THREE_RED_500("热态杀3", PredictConstant.HOT_STATE_KILL_THREE_RED_500, 3, PredictConstant
            .PREDICT_NUM_TYPE_RED_BALL) {
        @Override
        public Map<String, String> packPredictNum(List<PredictRedBall> predictRedBalls) {
            return PredictUtil.packMorePredictNum(predictRedBalls);
        }

        @Override
        public Boolean generatePredictNum(AbstractPredictDb predictDb, String periodId) {
            return predictDb.generateRedStateKill(this, periodId);
        }

        @Override
        public Integer getColdHotStateNum() {
            return 500;
        }

        @Override
        public String generateNewByNumModel(Long gameId, String periodId, String numModel) {
            return PredictUtil.generateHotStateKill3(this, periodId, numModel, PredictConstant
                    .SSQ_RED_HOT_STATE_OPTION_SIZE);
        }

        @Override
        public String getGenerateNewPredictModel(AbstractPredictDb predictDb, long gameId, String periodId) {
            return predictDb.getPeriodNumModel(this, gameId, periodId, PredictConstant.PREDICT_NUM_TYPE_RED_BALL);
        }
    }, CALL_BACK_STATE_KILL_THREE_RED_500("回补态杀3", PredictConstant.CALL_BACK_STATE_KILL_THREE_RED_500, 3,
            PredictConstant.PREDICT_NUM_TYPE_RED_BALL) {
        @Override
        public Map<String, String> packPredictNum(List<PredictRedBall> predictRedBalls) {
            return PredictUtil.packMorePredictNum(predictRedBalls);
        }

        @Override
        public Boolean generatePredictNum(AbstractPredictDb predictDb, String periodId) {
            return predictDb.generateRedStateKill(this, periodId);
        }

        @Override
        public Integer getColdHotStateNum() {
            return 500;
        }

        @Override
        public String generateNewByNumModel(Long gameId, String periodId, String numModel) {
            return PredictUtil.generateCallBackStateKill3(this, gameId, periodId, numModel, PredictConstant
                    .SSQ_RED_HOT_STATE_OPTION_SIZE);
        }

        @Override
        public String getGenerateNewPredictModel(AbstractPredictDb predictDb, long gameId, String periodId) {
            return predictDb.getLastPeriodNumModel(this, gameId, periodId, PredictConstant.PREDICT_NUM_TYPE_RED_BALL);
        }
    }, BASE_STATE_KILL_BLUE_THREE("基础态杀3", PredictConstant.KILL_THREE_BLUE, 3, PredictConstant
            .PREDICT_NUM_TYPE_BLUE_BALL) {
        @Override
        public Map<String, String> packPredictNum(List<PredictRedBall> predictRedBalls) {
            return PredictUtil.packMorePredictNum(predictRedBalls);
        }

        @Override
        public Boolean generatePredictNum(AbstractPredictDb predictDb, String periodId) {
            return true;//todo 将预测1w 中的杀3 注抽离处理啊
        }
    }, COLD_STATE_KILL_THREE_BLUE_100("冷态杀3", PredictConstant.COLD_STATE_KILL_THREE_BLUE_100, 3, PredictConstant
            .PREDICT_NUM_TYPE_BLUE_BALL) {
        @Override
        public Map<String, String> packPredictNum(List<PredictRedBall> predictRedBalls) {
            return PredictUtil.packMorePredictNum(predictRedBalls);
        }

        @Override
        public Boolean generatePredictNum(AbstractPredictDb predictDb, String periodId) {
            return predictDb.generateRedStateKill(this, periodId);
        }

        @Override
        public Integer getColdHotStateNum() {
            return 100;
        }

        @Override
        public String generateNewByNumModel(Long gameId, String periodId, String numModel) {
            return PredictUtil.generateColdStateKill3(this, periodId, numModel, PredictConstant
                    .SSQ_BLUE_HOT_STATE_OPTION_SIZE);
        }

        @Override
        public String getGenerateNewPredictModel(AbstractPredictDb predictDb, long gameId, String periodId) {
            return predictDb.getPeriodNumModel(this, gameId, periodId, PredictConstant.PREDICT_NUM_TYPE_BLUE_BALL);
        }
    }, HOT_STATE_KILL_THREE_BLUE_100("热态杀3", PredictConstant.HOT_STATE_KILL_THREE_BLUE_100, 3, PredictConstant
            .PREDICT_NUM_TYPE_BLUE_BALL) {
        @Override
        public Map<String, String> packPredictNum(List<PredictRedBall> predictRedBalls) {
            return PredictUtil.packMorePredictNum(predictRedBalls);
        }

        @Override
        public Boolean generatePredictNum(AbstractPredictDb predictDb, String periodId) {
            return predictDb.generateRedStateKill(this, periodId);
        }

        @Override
        public Integer getColdHotStateNum() {
            return 100;
        }

        @Override
        public String generateNewByNumModel(Long gameId, String periodId, String numModel) {
            return PredictUtil.generateHotStateKill3(this, periodId, numModel, PredictConstant
                    .SSQ_BLUE_HOT_STATE_OPTION_SIZE);
        }

        @Override
        public String getGenerateNewPredictModel(AbstractPredictDb predictDb, long gameId, String periodId) {
            return predictDb.getPeriodNumModel(this, gameId, periodId, PredictConstant.PREDICT_NUM_TYPE_BLUE_BALL);
        }
    }, CALL_BACK_STATE_KILL_THREE_BLUE_100("回补态杀3", PredictConstant.CALL_BACK_STATE_KILL_THREE_BLUE_100, 3,
            PredictConstant.PREDICT_NUM_TYPE_BLUE_BALL) {
        @Override
        public Map<String, String> packPredictNum(List<PredictRedBall> predictRedBalls) {
            return PredictUtil.packMorePredictNum(predictRedBalls);
        }

        @Override
        public Boolean generatePredictNum(AbstractPredictDb predictDb, String periodId) {
            return predictDb.generateRedStateKill(this, periodId);
        }

        @Override
        public Integer getColdHotStateNum() {
            return 100;
        }

        @Override
        public String generateNewByNumModel(Long gameId, String periodId, String numModel) {
            return PredictUtil.generateCallBackStateKill3(this, gameId, periodId, numModel, PredictConstant
                    .SSQ_BLUE_HOT_STATE_OPTION_SIZE);
        }

        @Override
        public String getGenerateNewPredictModel(AbstractPredictDb predictDb, long gameId, String periodId) {
            return predictDb.getLastPeriodNumModel(this, gameId, periodId, PredictConstant.PREDICT_NUM_TYPE_BLUE_BALL);
        }
    }, COLD_STATE_KILL_THREE_BLUE_200("冷态杀3", PredictConstant.COLD_STATE_KILL_THREE_BLUE_200, 3, PredictConstant
            .PREDICT_NUM_TYPE_BLUE_BALL) {
        @Override
        public Map<String, String> packPredictNum(List<PredictRedBall> predictRedBalls) {
            return PredictUtil.packMorePredictNum(predictRedBalls);
        }

        @Override
        public Boolean generatePredictNum(AbstractPredictDb predictDb, String periodId) {
            return predictDb.generateRedStateKill(this, periodId);
        }

        @Override
        public Integer getColdHotStateNum() {
            return 200;
        }

        @Override
        public String generateNewByNumModel(Long gameId, String periodId, String numModel) {
            return PredictUtil.generateColdStateKill3(this, periodId, numModel, PredictConstant
                    .SSQ_BLUE_HOT_STATE_OPTION_SIZE);
        }

        @Override
        public String getGenerateNewPredictModel(AbstractPredictDb predictDb, long gameId, String periodId) {
            return predictDb.getPeriodNumModel(this, gameId, periodId, PredictConstant.PREDICT_NUM_TYPE_BLUE_BALL);
        }
    }, HOT_STATE_KILL_THREE_BLUE_200("热态杀3", PredictConstant.HOT_STATE_KILL_THREE_BLUE_200, 3, PredictConstant
            .PREDICT_NUM_TYPE_BLUE_BALL) {
        @Override
        public Map<String, String> packPredictNum(List<PredictRedBall> predictRedBalls) {
            return PredictUtil.packMorePredictNum(predictRedBalls);
        }

        @Override
        public Boolean generatePredictNum(AbstractPredictDb predictDb, String periodId) {
            return predictDb.generateRedStateKill(this, periodId);
        }

        @Override
        public Integer getColdHotStateNum() {
            return 200;
        }

        @Override
        public String generateNewByNumModel(Long gameId, String periodId, String numModel) {
            return PredictUtil.generateHotStateKill3(this, periodId, numModel, PredictConstant
                    .SSQ_BLUE_HOT_STATE_OPTION_SIZE);
        }

        @Override
        public String getGenerateNewPredictModel(AbstractPredictDb predictDb, long gameId, String periodId) {
            return predictDb.getPeriodNumModel(this, gameId, periodId, PredictConstant.PREDICT_NUM_TYPE_BLUE_BALL);
        }
    }, CALL_BACK_STATE_KILL_THREE_BLUE_200("回补态杀3", PredictConstant.CALL_BACK_STATE_KILL_THREE_BLUE_200, 3,
            PredictConstant.PREDICT_NUM_TYPE_BLUE_BALL) {
        @Override
        public Map<String, String> packPredictNum(List<PredictRedBall> predictRedBalls) {
            return PredictUtil.packMorePredictNum(predictRedBalls);
        }

        @Override
        public Boolean generatePredictNum(AbstractPredictDb predictDb, String periodId) {
            return predictDb.generateRedStateKill(this, periodId);
        }

        @Override
        public Integer getColdHotStateNum() {
            return 200;
        }

        @Override
        public String generateNewByNumModel(Long gameId, String periodId, String numModel) {
            return PredictUtil.generateCallBackStateKill3(this, gameId, periodId, numModel, PredictConstant
                    .SSQ_BLUE_HOT_STATE_OPTION_SIZE);
        }

        @Override
        public String getGenerateNewPredictModel(AbstractPredictDb predictDb, long gameId, String periodId) {
            return predictDb.getLastPeriodNumModel(this, gameId, periodId, PredictConstant.PREDICT_NUM_TYPE_BLUE_BALL);
        }
    }, COLD_STATE_KILL_THREE_BLUE_500("冷态杀3", PredictConstant.COLD_STATE_KILL_THREE_BLUE_500, 3, PredictConstant
            .PREDICT_NUM_TYPE_BLUE_BALL) {
        @Override
        public Map<String, String> packPredictNum(List<PredictRedBall> predictRedBalls) {
            return PredictUtil.packMorePredictNum(predictRedBalls);
        }

        @Override
        public Boolean generatePredictNum(AbstractPredictDb predictDb, String periodId) {
            return predictDb.generateRedStateKill(this, periodId);
        }

        @Override
        public Integer getColdHotStateNum() {
            return 500;
        }

        @Override
        public String generateNewByNumModel(Long gameId, String periodId, String numModel) {
            return PredictUtil.generateColdStateKill3(this, periodId, numModel, PredictConstant
                    .SSQ_BLUE_HOT_STATE_OPTION_SIZE);
        }

        @Override
        public String getGenerateNewPredictModel(AbstractPredictDb predictDb, long gameId, String periodId) {
            return predictDb.getPeriodNumModel(this, gameId, periodId, PredictConstant.PREDICT_NUM_TYPE_BLUE_BALL);
        }
    }, HOT_STATE_KILL_THREE_BLUE_500("热态杀3", PredictConstant.HOT_STATE_KILL_THREE_BLUE_500, 3, PredictConstant
            .PREDICT_NUM_TYPE_BLUE_BALL) {
        @Override
        public Map<String, String> packPredictNum(List<PredictRedBall> predictRedBalls) {
            return PredictUtil.packMorePredictNum(predictRedBalls);
        }

        @Override
        public Boolean generatePredictNum(AbstractPredictDb predictDb, String periodId) {
            return predictDb.generateRedStateKill(this, periodId);
        }

        @Override
        public Integer getColdHotStateNum() {
            return 500;
        }

        @Override
        public String generateNewByNumModel(Long gameId, String periodId, String numModel) {
            return PredictUtil.generateHotStateKill3(this, periodId, numModel, PredictConstant
                    .SSQ_BLUE_HOT_STATE_OPTION_SIZE);
        }

        @Override
        public String getGenerateNewPredictModel(AbstractPredictDb predictDb, long gameId, String periodId) {
            return predictDb.getPeriodNumModel(this, gameId, periodId, PredictConstant.PREDICT_NUM_TYPE_BLUE_BALL);
        }
    }, CALL_BACK_STATE_KILL_THREE_BLUE_500("回补态杀3", PredictConstant.CALL_BACK_STATE_KILL_THREE_BLUE_500, 3,
            PredictConstant.PREDICT_NUM_TYPE_BLUE_BALL) {
        @Override
        public Map<String, String> packPredictNum(List<PredictRedBall> predictRedBalls) {
            return PredictUtil.packMorePredictNum(predictRedBalls);
        }

        @Override
        public Boolean generatePredictNum(AbstractPredictDb predictDb, String periodId) {
            return predictDb.generateRedStateKill(this, periodId);
        }

        @Override
        public Integer getColdHotStateNum() {
            return 500;
        }

        @Override
        public String generateNewByNumModel(Long gameId, String periodId, String numModel) {
            return PredictUtil.generateCallBackStateKill3(this, gameId, periodId, numModel, PredictConstant
                    .SSQ_BLUE_HOT_STATE_OPTION_SIZE);
        }

        @Override
        public String getGenerateNewPredictModel(AbstractPredictDb predictDb, long gameId, String periodId) {
            return predictDb.getLastPeriodNumModel(this, gameId, periodId, PredictConstant.PREDICT_NUM_TYPE_BLUE_BALL);
        }
    };

    private Integer strType;
    private String predictName;
    private Integer numCount;
    private Integer numType;

    SsqPickNumEnum(String predictName, Integer strType, Integer numCount, Integer numType) {
        this.strType = strType;
        this.predictName = predictName;
        this.numCount = numCount;
        this.numType = numType;
    }

    public Integer getStrType() {
        return this.strType;
    }

    public String getPredictName() {
        return this.predictName;
    }

    public Integer getNumCount() {
        return numCount;
    }

    public Integer getNumType() {
        return numType;
    }

    public static SsqPickNumEnum getPickNumEnumByStrType(Integer strType) {
        if (strType == null) {
            return null;
        }
        for (SsqPickNumEnum pne : SsqPickNumEnum.values()) {
            if (pne.getStrType().equals(strType)) {
                return pne;
            }
        }
        return null;
    }

    @Override
    public Integer getColdHotStateNum() {
        return -1;//表示全期
    }

    @Override
    public Integer getPeriodShowCount() {
        return 100;
    }

    @Override
    public Map<String, Object> getPredictInfo(AbstractPredictView predictView, Long userId) {
        return predictView.getStatePredictNumView(this, userId, predictView.getGame().getGameId());
    }

    @Override
    public String getTitleText(String gameEn) {
        String msg = PredictConstant.KILL_THREE_RED_MSG;
        if (this.getNumType().equals(1)) {
            msg = PredictConstant.KILL_THREE_BLUE_MSG;
        }
        return PredictUtil.getShowTextCal(gameEn, PredictConstant.KILL_THREE_BLUE_EXPIRE_MSG, msg);
    }

    public String generateNewByNumModel(Long gameId, String periodId, String numModel) {
        return null;
    }

    public String getGenerateNewPredictModel(AbstractPredictDb predictDb, long gameId, String periodId) {
        return null;
    }
}
