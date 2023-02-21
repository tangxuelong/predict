package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class AppChannel implements Serializable{
    private static final long serialVersionUID = 8819338806933188682L;

    private Integer channelId;
    private String channelName;
    private Timestamp createTime;
    private Timestamp updateTime;
}
