package com.mojieai.predict.service.predict;

import com.mojieai.predict.constant.PredictConstant;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.util.SpringContextHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class PredictFactory {
    private static PredictFactory predictFactory = new PredictFactory();

    private PredictFactory() {
    }

    public static PredictFactory getInstance() {
        return predictFactory;
    }

    public PredictInfo getPredictInfo(String gameEn) {
        if (StringUtils.isBlank(gameEn)) {
            throw new IllegalArgumentException("blank gameEn!");
        }
        PredictInfo predictInfo = SpringContextHolder.getBean(gameEn + PredictConstant.PREDICT_FACTORY_GET_INFO);
        if (predictInfo == null) {
            throw new BusinessException("游戏工厂中的对象不存在:" + gameEn);
        }
        return predictInfo;
    }

    public AbstractPredictDb getPredictDb(String gameEn) {
        if (StringUtils.isBlank(gameEn)) {
            throw new IllegalArgumentException("blank gameEn!");
        }
        AbstractPredictDb predictInfo = SpringContextHolder.getBean(gameEn + PredictConstant
                .PREDICT_FACTORY_CALCUlATE_2DB);
        if (predictInfo == null) {
            throw new BusinessException("游戏工厂中的对象不存在:" + gameEn);
        }
        return predictInfo;
    }

    public AbstractPredictView getPredictView(String gameEn) {
        if (StringUtils.isBlank(gameEn)) {
            throw new IllegalArgumentException("blank gameEn!");
        }
        AbstractPredictView predictView = SpringContextHolder.getBean(gameEn + PredictConstant
                .PREDICT_FACTORY_PREDICT_VIEW);
        if (predictView == null) {
            throw new BusinessException("游戏工厂中的对象不存在:" + gameEn);
        }
        return predictView;
    }

}
