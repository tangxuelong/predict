package com.mojieai.predict.service.historyaward;

import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.util.SpringContextHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class HistoryAwardFactory {

    private static HistoryAwardFactory instance = new HistoryAwardFactory();

    private HistoryAwardFactory() {
    }

    public static HistoryAwardFactory getInstance() {
        return instance;
    }

    public HistoryAward getHistoryAward(String gameEn) {
        if (StringUtils.isBlank(gameEn)) {
            throw new IllegalArgumentException("blank gameEn!");
        }
        HistoryAward historyAward = SpringContextHolder.getBean(gameEn + "HistoryAward");
        if (historyAward == null) {
            throw new BusinessException("游戏工厂中的对象不存在:" + gameEn);
        }
        return historyAward;
    }
}
