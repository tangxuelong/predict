package com.mojieai.predict.service.goldcointask;

import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.util.SpringContextHolder;

public class GoldCoinTaskFactory {
    private static GoldCoinTaskFactory instance = new GoldCoinTaskFactory();

    private GoldCoinTaskFactory() {
    }

    public static GoldCoinTaskFactory getInstance() {
        return instance;
    }

    public AbstractTask getTaskBean(String taskEn) {
        AbstractTask ag = SpringContextHolder.getBean(taskEn + "Task");
        if (ag == null) {
            throw new BusinessException("任务工厂中的对象不存在:" + taskEn);
        }
        return ag;
    }
}
