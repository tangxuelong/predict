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
public class PayClientChannel {
    private Integer clientId;
    private Integer channelId;
    private String payKeyStr;
    private Timestamp createTime;
    private Timestamp updateTime;
}