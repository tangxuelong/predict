package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * Created by tangxuelong on 2017/7/8.
 */
@Data
@NoArgsConstructor
public class UserInfo {
    private Long userId;
    private String nickName;
    private String headImgUrl;
    private String channelType;
    private String deviceId;
    private String footballIntroduce;
    private Integer isReMaster;
    private String pushInfo;
    private String remark;
    private Timestamp createTime;
    private Timestamp updateTime;

    public UserInfo(Long userId, String nickName, String headImgUrl, String channelType, String deviceId, Timestamp
            createTime, Timestamp updateTime) {
        this.userId = userId;
        this.nickName = nickName;
        this.headImgUrl = headImgUrl;
        this.channelType = channelType;
        this.deviceId = deviceId;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public String getHeadImgUrl(){
        if (headImgUrl.contains("ovqsyejql.bkt.clouddn.com")) {
            headImgUrl = headImgUrl.replace("ovqsyejql.bkt.clouddn.com", "sportsimg.mojieai.com");
        }
        return headImgUrl;
    }
}
