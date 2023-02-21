package com.mojieai.predict.entity.po;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class Title implements Serializable {
    private static final long serialVersionUID = -3694666989385755672L;

    private Integer titleId;
    private String titleName;
    private String titleEn;
    private Integer enable;
    private Integer weight;
}
