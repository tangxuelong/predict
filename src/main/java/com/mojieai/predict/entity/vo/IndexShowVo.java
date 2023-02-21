package com.mojieai.predict.entity.vo;

import lombok.Data;

/**
 * Created by tangxuelong on 2017/7/24.
 */
@Data
public class IndexShowVo {
    private String url;
    private String showText;
    private Integer isShow;

    public IndexShowVo(String url, String showText, Integer isShow) {
        this.url = url;
        this.showText = showText;
        this.isShow = isShow;
    }
}
