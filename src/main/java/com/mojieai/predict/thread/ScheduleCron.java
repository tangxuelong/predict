package com.mojieai.predict.thread;


import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.enums.CronEnum;
import com.mojieai.predict.service.cron.CronScheduleJobDetail;
import org.apache.logging.log4j.Logger;

public class ScheduleCron implements Runnable {
    private static final Logger log = LogConstant.commonLog;

    private Game game;
    private CronEnum cronEnum;
    private CronScheduleJobDetail cron;

    public ScheduleCron(Game game, CronEnum cronEnum, CronScheduleJobDetail cron) {
        this.game = game;
        this.cronEnum = cronEnum;
        this.cron = cron;
    }

    @Override
    public void run() {
        try {
            cronEnum.cron(game);
        } catch (Throwable e) {
            log.warn("error occurs while schedule " + cronEnum.getCron(), e);
        } finally {
            cron.startLoop(game, cronEnum);
        }
    }
}
