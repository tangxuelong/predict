package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class MatchTag implements Serializable {
    private static final long serialVersionUID = 1573642644539010382L;

    private Integer tagId;
    private String tagName;
    private Integer tagType;
    private Integer weight;
    private Integer status;

    public MatchTag(String tagName, Integer tagType, Integer weight) {
        this.tagName = tagName;
        this.tagType = tagType;
        this.weight = weight;
        this.status = 1;
    }
}
