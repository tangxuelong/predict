package com.mojieai.predict.thread;

import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.entity.bo.Task;
import com.mojieai.predict.service.cron.BaseScheduler;
import org.apache.logging.log4j.Logger;

public class TaskConsumer implements Runnable {
    private static final Logger log = LogConstant.commonLog;

    private BaseScheduler baseScheduler;
    private Task task;

    public TaskConsumer(BaseScheduler baseScheduler, Task task) {
        this.baseScheduler = baseScheduler;
        this.task = task;
    }

    @Override
    public void run() {
        if (task == null) {
            return;
        }
        baseScheduler.execute(task);
    }
}
