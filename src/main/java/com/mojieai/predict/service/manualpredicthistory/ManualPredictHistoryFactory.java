package com.mojieai.predict.service.manualpredicthistory;

import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.util.SpringContextHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class ManualPredictHistoryFactory {
    private static ManualPredictHistoryFactory manualPredictHistoryFactory = new ManualPredictHistoryFactory();

    private ManualPredictHistoryFactory() {
    }

    public static ManualPredictHistoryFactory getInstance() {
        return manualPredictHistoryFactory;
    }

    public ManualPredictNum getManualPredictNum(String gameEn){
        if (StringUtils.isBlank(gameEn)) {
            throw new IllegalArgumentException("blank gameEn!");
        }
        ManualPredictNum manualPredictNum = SpringContextHolder.getBean(gameEn + "ManualPredictNum");
        if (manualPredictNum == null) {
            throw new BusinessException("游戏工厂中的对象不存在:" + gameEn);
        }
        return manualPredictNum;
    }
}
