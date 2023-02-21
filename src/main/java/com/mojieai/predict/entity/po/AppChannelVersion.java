package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class AppChannelVersion implements Serializable {
    private static final long serialVersionUID = -1911218762075875191L;

    private Integer versionId;
    private Integer channelId;
    private String appUrl;
    private String upgradeDesc;
    private Integer forceUpgrade;
    private Integer status;
    private Timestamp createTime;
    private Timestamp updateTime;

    public AppChannelVersion(Integer channelId, Integer versionId, String appUrl, String upgradeDesc, Integer
            forceUpgrade) {
        this.channelId = channelId;
        this.versionId = versionId;
        this.appUrl = appUrl;
        this.upgradeDesc = upgradeDesc;
        this.forceUpgrade = forceUpgrade;
    }
}
