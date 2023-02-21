package com.mojieai.predict.thread;


import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.service.cron.BaseScheduler;
import org.apache.logging.log4j.Logger;

public class TaskProducer implements Runnable {
    private static final Logger log = LogConstant.commonLog;

    private BaseScheduler baseScheduler;

    public TaskProducer(BaseScheduler baseScheduler) {
        this.baseScheduler = baseScheduler;
    }

    @Override
    public void run() {
        log.info("[TaskProducer] start");
        while (true) {
            try {
                baseScheduler.schedule();
            } catch (Exception e) {
                log.error("[TaskProducer] aborted, pls check the reason! ", e);
            }
        }
    }
}
