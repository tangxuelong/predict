package com.mojieai.predict.entity.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by tangxuelong on 2017/6/30.
 */
@Data
@NoArgsConstructor
public class BannerVo {
    private Integer bannerId;
    private String title;
    private String imgUrl;
    private String detailUrl;
    private Integer weight;
    private Long gameId;
    private Integer actionType;

    public BannerVo(Integer bannerId, String title, String imgUrl, String detailUrl, Integer weight, Long gameId,
                    Integer actionType) {
        this.bannerId = bannerId;
        this.title = title;
        this.imgUrl = imgUrl;
        this.detailUrl = detailUrl;
        this.weight = weight;
        this.gameId = gameId;
        this.actionType = actionType;
    }
}
