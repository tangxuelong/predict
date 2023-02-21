package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * 推送记录
 *
 * @author tangxuelong
 */
@Data
@NoArgsConstructor
public class PushTrigger {
    private Integer triggerId;
    private Integer pushType;
    private String pushTitle;
    private String pushText;
    private String pushUrl;
    private Timestamp pushTime;
    private String pushTarget;
    private Integer isPushed;
    private String remark;
    private Timestamp createTime;
    private Timestamp updateTime;

    @Override
    public String toString() {
        return "PushTrigger{" +
                "triggerId=" + triggerId +
                ", pushType=" + pushType +
                ", pushTitle='" + pushTitle + '\'' +
                ", pushText='" + pushText + '\'' +
                ", pushUrl='" + pushUrl + '\'' +
                ", pushTime=" + pushTime +
                ", pushTarget='" + pushTarget + '\'' +
                ", isPushed=" + isPushed +
                ", remark='" + remark + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}