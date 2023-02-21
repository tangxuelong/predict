package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class SocialRobot {
    private Integer robotId;

    private Long robotUserId;

    private Date createTime;

    private Integer isEnable;

    private Integer robotType;

}