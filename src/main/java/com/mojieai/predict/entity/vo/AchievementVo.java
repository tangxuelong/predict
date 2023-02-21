package com.mojieai.predict.entity.vo;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.util.CommonUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Comparator;

@Data
@NoArgsConstructor
public class AchievementVo implements Comparable {
    private String achieveName;
    private String achieveDesc;
    private Integer ifHighLight = 0;

    public AchievementVo(String achieveName, String achieveDesc, Integer ifHighLight) {
        this.achieveName = achieveName;
        this.achieveDesc = achieveDesc;
        this.ifHighLight = ifHighLight;
    }

    @Override
    public int compareTo(Object o) {
        AchievementVo achievementVo = (AchievementVo) o;
        String achieveComNum = this.getAchieveName().replaceAll("^\\d", "");
        String paramVoNum = achievementVo.getAchieveName().replaceAll("^\\d", "");
        if (CommonUtil.isNumeric(achieveComNum) && CommonUtil.isNumeric(paramVoNum)) {
            if (Integer.valueOf(achieveComNum) > 100) {
                if (Integer.valueOf(achieveComNum) > Integer.valueOf(paramVoNum)) {
                    return -1;
                } else if (Integer.valueOf(achieveComNum) < Integer.valueOf(paramVoNum)) {
                    return 1;
                }
            } else {
                if (Integer.valueOf(achieveComNum) > Integer.valueOf(paramVoNum)) {
                    return 1;
                } else if (Integer.valueOf(achieveComNum) < Integer.valueOf(paramVoNum)) {
                    return -1;
                }
            }
        }
        return 0;
    }
}
