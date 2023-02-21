package com.mojieai.predict.cache;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.dao.SocialLevelIntegralDao;
import com.mojieai.predict.entity.po.SocialLevelIntegral;
import com.mojieai.predict.entity.po.Title;
import com.mojieai.predict.entity.vo.SocialLevelIntegralVo;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SocialLevelIntegralCache {

    private static List<SocialLevelIntegralVo> socialLevelIntegralVos = new ArrayList<>();
    private static Map<Integer, String> levelColorMap = new HashMap<>();
    private static Map<Integer, String> edgeLevelColorMap = new HashMap<>();

    static {
        levelColorMap.put(1, "#8AD765");
        levelColorMap.put(2, "#70D6E0");
        levelColorMap.put(3, "#70D6E0");
        levelColorMap.put(4, "#6F91FF");
        levelColorMap.put(5, "#6F91FF");
        levelColorMap.put(6, "#6F91FF");
        levelColorMap.put(7, "#FFAB2A");
        levelColorMap.put(8, "#FFAB2A");
        levelColorMap.put(9, "#FFAB2A");
        levelColorMap.put(10, "#FF8127");
        levelColorMap.put(11, "#FF8127");
        levelColorMap.put(12, "#FF8127");
        levelColorMap.put(13, "#FA5E5E");
        levelColorMap.put(14, "#FA5E5E");
        levelColorMap.put(15, "#FA5E5E");
        edgeLevelColorMap.put(1, "#83C963");
        edgeLevelColorMap.put(2, "#59BBC5");
        edgeLevelColorMap.put(3, "#59BBC5");
        edgeLevelColorMap.put(4, "#4D6ED6");
        edgeLevelColorMap.put(5, "#4D6ED6");
        edgeLevelColorMap.put(6, "#4D6ED6");
        edgeLevelColorMap.put(7, "#F29A14");
        edgeLevelColorMap.put(8, "#F29A14");
        edgeLevelColorMap.put(9, "#F29A14");
        edgeLevelColorMap.put(10, "#EF751E");
        edgeLevelColorMap.put(11, "#EF751E");
        edgeLevelColorMap.put(12, "#EF751E");
        edgeLevelColorMap.put(13, "#ED4747");
        edgeLevelColorMap.put(14, "#ED4747");
        edgeLevelColorMap.put(15, "#ED4747");
    }

    @Autowired
    private SocialLevelIntegralDao socialLevelIntegralDao;

    public SocialLevelIntegralCache() {
    }

    public void init() {
        refresh();
    }

    public void refresh() {
        List<SocialLevelIntegral> socialLevelIntegrals = socialLevelIntegralDao.getAllSocialLevelIntegral();
        for (SocialLevelIntegral temp : socialLevelIntegrals) {
            SocialLevelIntegralVo socialLevelIntegralVo = new SocialLevelIntegralVo();
            socialLevelIntegralVo.setLevelId(temp.getLevelId());
            socialLevelIntegralVo.setMinIntegral(temp.getMinIntegral());
            Title title = TitleCache.getTitleById(temp.getTitleId());
            socialLevelIntegralVo.setTitleName(title.getTitleName());
            socialLevelIntegralVo.setBigImg(temp.getBigImgUrl());
            socialLevelIntegralVo.setSmallImg(temp.getSmallImgUrl());
            socialLevelIntegralVos.add(socialLevelIntegralVo);
        }
    }

    /**
     * 通过用户积分获取等级信息
     *
     * @param integeral
     * @param type      0:当前等级 1下一级
     * @return
     */
    public static SocialLevelIntegralVo getUserLevelVoByIntegralByScore(Long integeral, Integer type) {
        SocialLevelIntegralVo currentLevel = null;
        SocialLevelIntegralVo nextLevel = null;

        if (socialLevelIntegralVos == null) {
            return null;
        }

        if (integeral == null || integeral == 0l) {
            currentLevel = socialLevelIntegralVos.get(0);
            nextLevel = socialLevelIntegralVos.get(1);
        } else if (integeral > socialLevelIntegralVos.get(socialLevelIntegralVos.size() - 1).getMinIntegral()) {
            currentLevel = socialLevelIntegralVos.get(socialLevelIntegralVos.size() - 1);
        } else {
            for (int i = 0; i < socialLevelIntegralVos.size(); i++) {
                SocialLevelIntegralVo temp = socialLevelIntegralVos.get(i);
                if (integeral < temp.getMinIntegral()) {
                    currentLevel = socialLevelIntegralVos.get(i - 1);
                    nextLevel = socialLevelIntegralVos.get(i);
                    break;
                }
            }
        }

        if (CommonConstant.SOCIAL_INTEGERAL_NEXT_LEVEL.equals(type)) {
            return nextLevel;
        }
        return currentLevel;
    }

    public static List<SocialLevelIntegralVo> getAllSocialLevels() {
        return socialLevelIntegralVos;
    }

    public static String getColorByLevel(Integer socialLevel) {
        if (socialLevel > levelColorMap.size()) {
            return "#8AD765";
        }
        return levelColorMap.get(socialLevel);
    }

    public static String getEdgeColorByLevel(int userLevel) {
        if (userLevel > edgeLevelColorMap.size()) {
            return "#83C963";
        }
        return edgeLevelColorMap.get(userLevel);
    }
}
