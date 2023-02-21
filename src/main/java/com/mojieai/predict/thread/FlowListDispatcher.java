package com.mojieai.predict.thread;

import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.entity.po.UserAccountFlow;
import com.mojieai.predict.service.PayService;
import com.mojieai.predict.service.cron.FlowListCoordinator;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

/**
 * Created by tangxuelong on 2017/7/20.
 */
public class FlowListDispatcher implements Runnable {
    private static final Logger log = LogConstant.commonLog;

    @Autowired
    private FlowListCoordinator flowListCoordinator;

    private PayService payService;

    public FlowListDispatcher(PayService payService) {
        this.payService = payService;
    }

    @Override
    public void run() {
        log.info("[flow list Dispatcher] start");
        while (!Thread.currentThread().isInterrupted()) {
            try {
                UserAccountFlow userAccountFlow = PayService.FLOW_LIST_QUEUE.poll(5, TimeUnit.SECONDS);
                if (null != userAccountFlow) {
                    ThreadPool.getInstance().getUpdateDeviceInfoExec().submit(new FlowListTask(userAccountFlow,
                            payService));
                }
            } catch (Exception e) {
                log.warn("[update device Dispatcher] error " + e.getMessage(), e);
            }
        }
        flowListCoordinator.SWITCH_ON.compareAndSet(true, false);
        flowListCoordinator.init();
        log.error("[update device Dispatcher] aborted, do not worry we will restart a new one, but pls check the " +
                "reason! ");
    }
}
