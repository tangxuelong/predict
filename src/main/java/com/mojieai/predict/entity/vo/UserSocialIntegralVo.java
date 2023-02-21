package com.mojieai.predict.entity.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class UserSocialIntegralVo implements Serializable {
    private static final long serialVersionUID = 4311963118725464008L;

    private String userId;
    private String integral;//当前积分
    private String upgradeIntegral;//下一级积分
    private Integer socialLevel;
    private String levelName;
    private String titleBigImg;
    private String titleSmallImg;
}
