package com.mojieai.predict.thread;

import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.entity.po.UserDeviceInfo;
import com.mojieai.predict.service.LoginService;
import com.mojieai.predict.service.cron.UpdateDeviceCoordinator;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

/**
 * Created by tangxuelong on 2017/7/20.
 */
public class UpdateDeviceDispatcher implements Runnable {
    private static final Logger log = LogConstant.commonLog;

    @Autowired
    private UpdateDeviceCoordinator updateDeviceCoordinator;

    private LoginService loginService;

    public UpdateDeviceDispatcher(LoginService loginService) {
        this.loginService = loginService;
    }

    @Override
    public void run() {
        log.info("[update device Dispatcher] start");
        while (!Thread.currentThread().isInterrupted()) {
            try {
                UserDeviceInfo userDeviceInfo = LoginService.UPDATE_DEVICE_QUEUE.poll(5, TimeUnit.SECONDS);
                if (null != userDeviceInfo) {
                    ThreadPool.getInstance().getUpdateDeviceInfoExec().submit(new UpdateDeviceInfoTask(userDeviceInfo, loginService));
                }
            } catch (Exception e) {
                log.warn("[update device Dispatcher] error " + e.getMessage(), e);
            }
        }
        updateDeviceCoordinator.SWITCH_ON.compareAndSet(true, false);
        updateDeviceCoordinator.init();
        log.error("[update device Dispatcher] aborted, do not worry we will restart a new one, but pls check the reason! ");
    }
}
