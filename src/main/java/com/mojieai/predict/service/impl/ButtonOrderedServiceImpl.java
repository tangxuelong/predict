package com.mojieai.predict.service.impl;

import com.mojieai.predict.cache.BannerCache;
import com.mojieai.predict.cache.ButtonOrderCache;
import com.mojieai.predict.constant.ButtonOrderedConstant;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.entity.po.ButtonOrdered;
import com.mojieai.predict.entity.vo.BannerVo;
import com.mojieai.predict.entity.vo.ButtonOrderVo;
import com.mojieai.predict.service.ButtonOrderedService;
import com.mojieai.predict.util.ButtonOrderedUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ButtonOrderedServiceImpl implements ButtonOrderedService {

    @Override
    public Map<String, Object> getToolsIndexButtons(Long gameId, String versionCode, Integer clientType) {
        Map<String, Object> result = new HashMap<>();

        //1.banners
        List<BannerVo> bannerVos = BannerCache.getBannerVosV2(BannerCache.POSITION_TYPE_TOOL, Integer.valueOf
                (versionCode), clientType);
        List<BannerVo> bannerVoList = new ArrayList<>();
        for (BannerVo bannerVo : bannerVos) {
            if (bannerVo.getGameId().equals(gameId)) {
                bannerVoList.add(bannerVo);
            }
        }
        if (StringUtils.isBlank(versionCode)) {
            versionCode = String.valueOf(CommonConstant.VERSION_CODE_3_2);
        }
        //2.横向
        List<ButtonOrdered> buttonOrdered = ButtonOrderCache.getButtonOrdered(gameId, ButtonOrderedConstant
                .BUTTON_TYPE_TRANSVERSE);
        List<ButtonOrderVo> transverse = ButtonOrderedUtil.convertBtnListOrder2BtnVo(buttonOrdered, versionCode);
        //3.纵向
        List<ButtonOrderVo> portrait = new ArrayList<>();
        portrait.add(new ButtonOrderVo());
        List<ButtonOrdered> btnOrdPortrait1 = ButtonOrderCache.getButtonOrdered(gameId, ButtonOrderedConstant
                .BUTTON_TYPE_PROTRAIT_1);
        List<ButtonOrderVo> portrait1 = ButtonOrderedUtil.convertBtnListOrder2BtnVo(btnOrdPortrait1, versionCode);
        portrait.addAll(portrait1);

        //4.添加剩余部分
        List<ButtonOrdered> btnOrdPortrait2 = ButtonOrderCache.getButtonOrdered(gameId, ButtonOrderedConstant
                .BUTTON_TYPE_PROTRAIT_2);
        if (btnOrdPortrait2 != null) {
            //4.添加空代表横杠
            portrait.add(new ButtonOrderVo());
            List<ButtonOrderVo> portrait2 = ButtonOrderedUtil.convertBtnListOrder2BtnVo(btnOrdPortrait2, versionCode);
            portrait.addAll(portrait2);
        }

        //5.添加剩余部分
        List<ButtonOrdered> btnOrdPortrait3 = ButtonOrderCache.getButtonOrdered(gameId, ButtonOrderedConstant
                .BUTTON_TYPE_PROTRAIT_3);
        if (btnOrdPortrait3 != null) {
            //5.添加空代表横杠
            portrait.add(new ButtonOrderVo());
            List<ButtonOrderVo> portrait3 = ButtonOrderedUtil.convertBtnListOrder2BtnVo(btnOrdPortrait3, versionCode);
            portrait.addAll(portrait3);
        }


        result.put("banners", bannerVoList);
        result.put("transverse", transverse);
        result.put("portrait", portrait);
        return result;
    }
}
