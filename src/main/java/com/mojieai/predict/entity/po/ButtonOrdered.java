package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class ButtonOrdered implements Serializable {
    private static final long serialVersionUID = -3041878035053262026L;

    private Integer btnId;
    private Integer type;
    private String name;
    private String img;
    private String jumpUrl;
    private String uniqueStr;
    private Long gameId;
    private Integer weight;
    private String memo;
    private Integer enable;
    private Integer versionCode;
}
