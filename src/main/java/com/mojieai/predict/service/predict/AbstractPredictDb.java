package com.mojieai.predict.service.predict;

import com.mojieai.predict.constant.PredictConstant;
import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.po.PredictRedBall;
import com.mojieai.predict.enums.predict.PickNumPredict;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.service.PredictColdHotModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;

public abstract class AbstractPredictDb extends AbstractPredict {

    @Autowired
    protected PredictColdHotModelService predictColdHotModelService;

    abstract Game getGame();

    public String getPeriodNumModel(PickNumPredict pickNumEnum, long gameId, String periodId, Integer numType) {
        return predictColdHotModelService.getColdHotModel(gameId, periodId, pickNumEnum.getColdHotStateNum(), numType);
    }

    public String getLastPeriodNumModel(PickNumPredict pickNumEnum, long gameId, String periodId, Integer numType) {
        GamePeriod lastPeriod = PeriodRedis.getLastPeriodByGameIdAndPeriodId(gameId, periodId);
        return predictColdHotModelService.getColdHotModel(gameId, lastPeriod.getPeriodId(), pickNumEnum
                .getColdHotStateNum(), numType);
    }

    /* 冷 热 回滚态杀三*/
    public Boolean generateRedStateKill(PickNumPredict pickNumPredict, String periodId) {
        Game game = getGame();

        GamePeriod lastPeriod = PeriodRedis.getLastPeriodByGameIdAndPeriodId(game.getGameId(), periodId);
        //1.生产某一期杀3
        String numModel = pickNumPredict.getGenerateNewPredictModel(this, game.getGameId(), periodId);
        String predictNum = pickNumPredict.generateNewByNumModel(game.getGameId(), periodId, numModel);
        //2.保存数据库
        saveStateNum2Db(game.getGameId(), periodId, pickNumPredict.getStrType(), predictNum);
        //3.给上期算奖
        Boolean historyPrize = calculateHistoryPrize(game.getGameId(), lastPeriod.getPeriodId(), pickNumPredict
                .getStrType(), pickNumPredict.getNumType());
        String redSubscribeIndexKey = RedisConstant.getSubscribeIndexKey(game.getGameId(), PredictConstant
                .PREDICT_STATE_PROGRAM_TYPE_RED);
        String firstRedSubscribeIndexKey = RedisConstant.getSubscribeIndexKey(game.getGameId(), PredictConstant
                .PREDICT_STATE_PROGRAM_TYPE_RED_FIRST);
        String blueSubscribeIndexKey = RedisConstant.getSubscribeIndexKey(game.getGameId(), PredictConstant
                .PREDICT_STATE_PROGRAM_TYPE_BLUE);
        String firstBlueSubscribeIndexKey = RedisConstant.getSubscribeIndexKey(game.getGameId(), PredictConstant
                .PREDICT_STATE_PROGRAM_TYPE_RED_FIRST);
        redisService.del(redSubscribeIndexKey);
        redisService.del(blueSubscribeIndexKey);
        redisService.del(firstRedSubscribeIndexKey);
        redisService.del(firstBlueSubscribeIndexKey);
        String redisKey = RedisConstant.getPredictTypeKey(game.getGameEn(), pickNumPredict.getStrType());
        redisService.del(redisKey);
        return historyPrize;
    }

    private void saveStateNum2Db(long gameId, String periodId, Integer strType, String numStr) {
        PredictRedBall predictRedBall = new PredictRedBall(gameId, periodId, strType, numStr);
        try {
            predictRedBallDao.insert(predictRedBall);
        } catch (DuplicateKeyException e) {
            return;
        }
    }
}
