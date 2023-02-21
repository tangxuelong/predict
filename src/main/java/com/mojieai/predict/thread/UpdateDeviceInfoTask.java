package com.mojieai.predict.thread;

import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.entity.po.UserDeviceInfo;
import com.mojieai.predict.service.LoginService;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Callable;

/**
 * Created by tangxuelong on 2017/7/20.
 */
public class UpdateDeviceInfoTask implements Callable {
    private static final Logger log = LogConstant.commonLog;

    private UserDeviceInfo userDeviceInfo;
    private LoginService loginService;

    public UpdateDeviceInfoTask(UserDeviceInfo userDeviceInfo, LoginService loginService) {
        this.userDeviceInfo = userDeviceInfo;
        this.loginService = loginService;
    }

    @Override
    public Object call() throws Exception {
        try {
            // 更新设备信息
            loginService.updateDeviceInfo(userDeviceInfo);
        } catch (Exception e) {
            log.error("update deviceInfo error deviceId:" + userDeviceInfo.getDeviceId());
        }
        return null;
    }
}
