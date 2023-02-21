package com.mojieai.predict.service.predict;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.GameConstant;
import com.mojieai.predict.constant.PredictConstant;
import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.po.PredictRedBall;
import com.mojieai.predict.enums.predict.PickNumEnum;
import com.mojieai.predict.enums.predict.PickNumPredict;
import com.mojieai.predict.enums.predict.SsqPickNumEnum;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.util.DateUtil;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SsqPredictView extends AbstractPredictView {
    private String gameEn = GameConstant.SSQ;

    @Override
    public Game getGame() {
        return GameCache.getGame(gameEn);
    }
}
