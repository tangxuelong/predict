package com.mojieai.predict.service.cron;

import com.mojieai.predict.service.LoginService;
import com.mojieai.predict.thread.UpdateDeviceDispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by tangxuelong on 2017/7/20.
 */
@Component
public class UpdateDeviceCoordinator {
    public static AtomicBoolean SWITCH_ON = new AtomicBoolean(false);

    @Autowired
    private LoginService loginService;

    private final ExecutorService exec = Executors.newSingleThreadExecutor();

    @PostConstruct
    public void init() {
        if (SWITCH_ON.compareAndSet(false, true)) {
            exec.execute(new UpdateDeviceDispatcher(loginService));
        }
    }
}
