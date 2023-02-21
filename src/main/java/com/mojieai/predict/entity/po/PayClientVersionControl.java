package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * 全局配置表对应的ini
 *
 * @author Singal
 */
@Data
@NoArgsConstructor
public class PayClientVersionControl {
    private Integer clientId;
    private Integer channelId;
    private Integer versionCode;
    private Integer isDelete;
    private Timestamp createTime;
    private Timestamp updateTime;
}