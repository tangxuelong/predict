package com.mojieai.predict.entity.dto;

import lombok.Data;

import java.util.Map;

/**
 * Created by tangxuelong on 2017/7/21.
 */
@Data
public class PushDto {
    private String title;
    private String text;
    private String url;
    private Map<String, String> content;

    public PushDto(String title, String text, String url, Map<String, String> content) {
        this.title = title;
        this.text = text;
        this.url = url;
        this.content = content;
    }
}
