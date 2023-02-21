package com.mojieai.predict.service.predict;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.PredictConstant;
import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.po.PredictRedBall;
import com.mojieai.predict.enums.predict.PickNumEnum;
import com.mojieai.predict.enums.predict.PickNumPredict;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.service.UserSubscribeInfoService;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.PredictUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public abstract class AbstractPredictView extends AbstractPredict {

    @Autowired
    private UserSubscribeInfoService userSubscribeInfoService;

    public abstract Game getGame();

    public Game getGame(String gameEn) {
        return GameCache.getGame(gameEn);
    }

    public Map<String, String> getPredictRedBallNum(Integer type) {
        Game game = getGame();
        String redisKey = RedisConstant.getPredictTypeKey(game.getGameEn(), type);
        PickNumEnum pickNumEnum = PickNumEnum.getPickNumEnum(game.getGameEn());
        if (pickNumEnum == null) {
            return null;
        }
        PickNumPredict pickNumPredict = pickNumEnum.getGamePickNumEnum(type);
        Map<String, String> predictNumbers = redisService.kryoGet(redisKey, TreeMap.class);
        if (null == predictNumbers) {
            predictNumbers = rebuildMorePredictRedis(game, pickNumPredict);
        }
        return predictNumbers;
    }

    public Map<String, Object> getStatePredictNumView(PickNumPredict pickNumPredict, Long userId, long gameId) {
        Map<String, Object> res = new HashMap<>();
        List<String> resultList = new ArrayList<>();
        Map<String, String> predictRedBalls = getPredictRedBallNum(pickNumPredict.getStrType());

        for (Map.Entry<String, String> entry : predictRedBalls.entrySet()) {
            String periodId = entry.getKey();

            String partMissionId = PredictUtil.getPartKillStateRefundMissionId(gameId, Integer.valueOf(periodId),
                    pickNumPredict.getNumType(), pickNumPredict.getStrType(), userId);
            Integer isRefund = userSubscribeInfoService.checkIsRefund(partMissionId);
            // this
            resultList.add(periodId + "期" + CommonConstant.COMMON_COLON_STR + entry.getValue() + CommonConstant
                    .COMMON_COLON_STR + isRefund);
        }
        res.put("predictNum", resultList);
        res.put("titleText", pickNumPredict.getTitleText(getGame().getGameEn()));
        return res;
    }

    private Map<String, String> rebuildMorePredictRedis(Game game, PickNumPredict pickNumPredict) {
        if (pickNumPredict == null) {
            return null;
        }
        String redisKey = RedisConstant.getPredictTypeKey(game.getGameEn(), pickNumPredict.getStrType());

        //1.获取基础数据
        List<PredictRedBall> predictRedBall = predictRedBallDao.getPredictRedBalls(game.getGameId(), pickNumPredict
                .getStrType(), pickNumPredict.getPeriodShowCount());
        if (predictRedBall == null) {
            log.error(pickNumPredict.getPredictName() + "未查询到数据请及时处理");
            return null;
        }
        Map<String, String> predictMap = pickNumPredict.packPredictNum(predictRedBall);
        if (predictMap != null && !predictMap.isEmpty()) {
            //2.保存缓存
            redisService.kryoSet(redisKey, predictMap);
            GamePeriod period = PeriodRedis.getCurrentPeriod(game.getGameId());
            redisService.expire(redisKey, (int) DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(), DateUtil
                    .getIntervalSeconds(period.getAwardTime(), 3600)));
        }
        return predictMap;
    }
}
