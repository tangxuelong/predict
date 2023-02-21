package com.mojieai.predict.thread;

import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.entity.bo.Task;
import com.mojieai.predict.service.cron.BaseScheduler;
import org.apache.logging.log4j.Logger;

public class TaskDispatcher implements Runnable {
    private static final Logger log = LogConstant.commonLog;

    private BaseScheduler baseScheduler;

    public TaskDispatcher(BaseScheduler baseScheduler) {
        this.baseScheduler = baseScheduler;
    }

    @Override
    public void run() {
        log.info("[TaskDispatcher] start");
        while (true) {
            try {
                Task task = baseScheduler.getExecutorQueue().take();//阻塞等待
                if (null != task) {
                    ThreadPool.getInstance().getTaskExecutor(task.getGameId()).submit(new TaskConsumer(baseScheduler,
                            task));
                }
            } catch (Exception e) {
                log.error("[TaskDispatcher] aborted, pls check the reason! ", e);
            }
        }
    }
}
