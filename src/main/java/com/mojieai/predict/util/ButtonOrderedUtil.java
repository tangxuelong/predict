package com.mojieai.predict.util;

import com.mojieai.predict.entity.po.ButtonOrdered;
import com.mojieai.predict.entity.vo.ButtonOrderVo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ButtonOrderedUtil {
    public static List<ButtonOrderVo> convertBtnListOrder2BtnVo(List<ButtonOrdered> buttonOrdereds, String
            versionCode) {
        if (buttonOrdereds == null || buttonOrdereds.size() <= 0) {
            return null;
        }
        List<ButtonOrderVo> btnVos = new ArrayList<>();
        for (ButtonOrdered button : buttonOrdereds) {
            if (button.getVersionCode() == null || Integer.valueOf(versionCode) >= button.getVersionCode()) {
                btnVos.add(convertBtnOrder2BtnVo(button));
            }
        }
        btnVos.sort(Comparator.comparing(ButtonOrderVo::getWeight));
        return btnVos;
    }

    public static ButtonOrderVo convertBtnOrder2BtnVo(ButtonOrdered buttonOrdered) {
        if (buttonOrdered == null) {
            return null;
        }
        ButtonOrderVo btnVo = new ButtonOrderVo();
        btnVo.setImg(buttonOrdered.getImg() == null ? "" : buttonOrdered.getImg());
        btnVo.setJumpUrl(buttonOrdered.getJumpUrl() == null ? "" : buttonOrdered.getJumpUrl());
        btnVo.setName(buttonOrdered.getName());
        btnVo.setUniqueStr(buttonOrdered.getUniqueStr());
        btnVo.setWeight(buttonOrdered.getWeight());
        return btnVo;
    }
}
