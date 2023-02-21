package com.mojieai.predict.entity.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VipGoldMallInfo {
    private String title;
    private String descAd;
    private String imgUrl;
    private String areaKey;

    public VipGoldMallInfo(String title, String descAd, String imgUrl, String areaKey) {
        this.title = title;
        this.descAd = descAd;
        this.imgUrl = imgUrl;
        this.areaKey = areaKey;
    }


}
