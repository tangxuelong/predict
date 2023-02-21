package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * Created by tangxuelong on 2017/7/8.
 */
@Data
@NoArgsConstructor
public class UserDeviceInfo {
    private Long userId;
    private String deviceId;
    private String deviceImei;
    private String deviceName;
    private Integer clientType;
    private String clientId;
    private String pushGameEns;
    private String channel;
    private Integer pushType;
    private Timestamp createTime;
    private Timestamp updateTime;

    public UserDeviceInfo(Long userId, String deviceId, String deviceImei, String deviceName, Integer clientType,
                          String clientId, String pushGameEns, String channel, Integer pushType, Timestamp createTime,
                          Timestamp updateTime) {
        this.userId = userId;
        this.deviceId = deviceId;
        this.deviceImei = deviceImei;
        this.deviceName = deviceName;
        this.clientType = clientType;
        this.clientId = clientId;
        this.pushGameEns = pushGameEns;
        this.channel = channel;
        this.pushType = pushType;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

}
