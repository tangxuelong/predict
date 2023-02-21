package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class AppVersion implements Serializable {
    private static final long serialVersionUID = 3937006121162677165L;

    private Integer versionId;
    private Integer clientId;
    private String clientName;
    private Integer versionCode;
    private String versionCodeName;
    private String upgradeDesc;
    private Integer forceUpgrade;
    private Timestamp createTime;
    private Timestamp updateTime;

    public AppVersion(Integer versionId, Integer clientId, String clientName, Integer versionCode, String
            versionCodeName, String upgradeDesc, Integer forceUpgrade) {
        this.versionId = versionId;
        this.clientId = clientId;
        this.clientName = clientName;
        this.versionCode = versionCode;
        this.versionCodeName = versionCodeName;
        this.upgradeDesc = upgradeDesc;
        this.forceUpgrade = forceUpgrade;
    }
}
