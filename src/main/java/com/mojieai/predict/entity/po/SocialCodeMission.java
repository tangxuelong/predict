package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * MISSION
 *
 * @author tangxuelong
 */
@Data
@NoArgsConstructor
public class SocialCodeMission {
    private Long missionId;
    private String keyInfo;
    private Integer missionType;
    private String remark;
    private Integer status;
    private Timestamp createTime;
    private Timestamp updateTime;
    //唯一键约束KEY_INFO , TASK_TYPE

    public static int MISSION_TYPE_ENCIRCLE = 0;//围号
    public static int MISSION_TYPE_KILL = 1;//杀号

    public SocialCodeMission(String keyInfo, Integer missionType, Integer status, Timestamp createTime, String remark) {
        this.keyInfo = keyInfo;
        this.missionType = missionType;
        this.status = status;
        this.createTime = createTime;
        this.remark = remark;
    }

}