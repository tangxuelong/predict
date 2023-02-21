package com.mojieai.predict.thread;

import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.entity.po.CronTab;
import com.mojieai.predict.enums.ExecuteModeEnum;
import com.mojieai.predict.util.SpringContextHolder;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by qiwang
 */
public class TaskCron implements Runnable {
    private static final Logger log = LogConstant.commonLog;

    private CronTab cronTab;
    private ScheduledExecutorService scheduler;

    public TaskCron(CronTab cronTab, ScheduledExecutorService scheduler) {
        this.cronTab = cronTab;
        this.scheduler = scheduler;
    }

    @Override
    public void run() {
        try {
            log.info("TaskCron execute start." + cronTab.toString());
            Class clazz = SpringContextHolder.getBean(cronTab.getBeanName()).getClass();
            Method method = clazz.getDeclaredMethod(cronTab.getBeanMethod());
            method.invoke(SpringContextHolder.getBean(cronTab.getBeanName()));
            log.info("TaskCron execute end." + cronTab.toString() + "|getBeanName:" + cronTab.getBeanMethod());
        } catch (Exception e) {
            log.error("[TaskCron]cron method error." + cronTab.toString(), e);
        } finally {
            if (cronTab.getExecuteMode().equals(ExecuteModeEnum.CRON.getExecuteMode())) {
                Long delay = CronTab.getNextDelayTime(cronTab.getCron());
                scheduler.schedule(new TaskCron(cronTab, scheduler), delay, TimeUnit.MILLISECONDS);
            }
        }
    }
}
