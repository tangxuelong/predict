package com.mojieai.predict.entity.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class SocialLevelIntegralVo implements Serializable {
    private static final long serialVersionUID = -8807261200406612004L;

    private Integer levelId;
    private String titleName;
    private Long minIntegral;
    private String bigImg;
    private String smallImg;
}
