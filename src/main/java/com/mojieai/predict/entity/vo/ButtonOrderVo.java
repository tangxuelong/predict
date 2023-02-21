package com.mojieai.predict.entity.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class ButtonOrderVo implements Serializable {
    private static final long serialVersionUID = -6585512707278385236L;

    private String name = "";
    private String img = "";
    private String jumpUrl = "";
    private String uniqueStr = "";
    private Integer weight = 0;
}
