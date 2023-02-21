package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
public class SportRobotRecommend implements Serializable{
    private static final long serialVersionUID = -2360304839849589702L;
    private Integer robotId;
    private Long userId;
    private Integer recommendDate;
    private Integer recommendTimes;
    private Integer enable;

}