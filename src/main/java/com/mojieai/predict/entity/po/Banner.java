package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * Created by Ynght on 2016/10/24.
 */
@Data
@NoArgsConstructor
public class Banner {
    private Integer bannerId;
    private String title;
    private String imgUrl;
    private String detailUrl;
    private Timestamp startTime;
    private Timestamp endTime;
    private Integer isDel;
    private Integer weight;
    private Long gameId;
    private Integer actionType;//0 : wap  //1:native
    private Integer positionType;
    private String exclusiveClientId;

    public Banner(Integer bannerId, String title, String imgUrl, String detailUrl, Timestamp startTime, Timestamp
            endTime, Integer isDel, Integer weight, Long gameId, Integer actionType, Integer positionType, String
                          exclusiveClientId) {
        this.bannerId = bannerId;
        this.title = title;
        this.imgUrl = imgUrl;
        this.detailUrl = detailUrl;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isDel = isDel;
        this.weight = weight;
        this.gameId = gameId;
        this.actionType = actionType;
        this.positionType = positionType;
        this.exclusiveClientId = exclusiveClientId;
    }
}
