package com.mojieai.predict.service.impl;

import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.dao.PredictColdHotModelDao;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.po.PredictColdHotModel;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.PredictColdHotModelService;
import com.mojieai.predict.util.PredictUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PredictColdHotModelServiceImpl implements PredictColdHotModelService {

    @Autowired
    private PredictColdHotModelDao predictColdHotModelDao;
    @Autowired
    private RedisService redisService;


    @Override
    public String getColdHotModel(long gameId, String periodId, Integer periodCount, Integer numType) {
        String res = null;
        PredictColdHotModel coldHotModel = predictColdHotModelDao.getColdHotModelByPk(gameId, periodId, periodCount,
                numType);
        if (coldHotModel == null) {
            coldHotModel = rebuildColdHotModel(gameId, periodId, periodCount, numType);
        }
        if (coldHotModel != null && StringUtils.isNotBlank(coldHotModel.getNums())) {
            res = coldHotModel.getNums();
        }
        return res;
    }

    private PredictColdHotModel rebuildColdHotModel(long gameId, String periodId, Integer periodCount, Integer
            numType) {
        String chartName = PredictUtil.getColdHotChartNameByNumType(gameId, numType);
        //1.从上一期中获取冷热数据
        GamePeriod lastPeriod = PeriodRedis.getLastPeriodByGameIdAndPeriodId(gameId, periodId);
        String chartKey = RedisConstant.getCurrentChartKey(gameId, lastPeriod.getPeriodId(), chartName, null);
        List<Map<String, Object>> numColdHotData = redisService.kryoGet(chartKey, ArrayList.class);
        if (numColdHotData == null || numColdHotData.size() == 0) {
            return null;
        }

        String numModel = PredictUtil.extractOnePhaseHotColdModel(numColdHotData, periodCount);
        //2.保存冷热态模版
        PredictColdHotModel predictColdHotModel = null;
        try {
            predictColdHotModel = new PredictColdHotModel(gameId, periodId, periodCount, numType, numModel);
            predictColdHotModelDao.insert(predictColdHotModel);
        } catch (DuplicateKeyException e) {
            predictColdHotModel = predictColdHotModelDao.getColdHotModelByPk(gameId, periodId, periodCount, numType);
        }
        return predictColdHotModel;
    }
}
