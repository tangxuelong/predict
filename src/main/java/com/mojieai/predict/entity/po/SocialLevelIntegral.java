package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class SocialLevelIntegral implements Serializable {
    private static final long serialVersionUID = 8720632799397681592L;

    private Integer levelId;
    private Integer titleId;
    private Long minIntegral;
    private Integer enable;
    private String bigImgUrl;
    private String smallImgUrl;
}
