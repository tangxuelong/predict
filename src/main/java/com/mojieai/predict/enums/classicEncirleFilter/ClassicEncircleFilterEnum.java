package com.mojieai.predict.enums.classicEncirleFilter;

import com.mojieai.predict.constant.GameConstant;

public enum ClassicEncircleFilterEnum {
    SSQ_CLASSIC_FILTER(GameConstant.SSQ) {
        @Override
        public EncircleClaissicFilter[] getFilterEnum() {
            return SsqClassicEncircleFilterEnum.values();
        }
    },
    DLT_CLASSIC_FILTER(GameConstant.DLT) {
        @Override
        public EncircleClaissicFilter[] getFilterEnum() {
            return DltClassicEncircleFilterEnum.values();
        }
    };

    private String gameEn;

    ClassicEncircleFilterEnum(String gameEn) {
        this.gameEn = gameEn;
    }

    public String getGameEn() {
        return this.gameEn;
    }

    public static ClassicEncircleFilterEnum getClassicEncircleFilterEnum(String gameEn) {
        for (ClassicEncircleFilterEnum classicEncircleFilterEnum : ClassicEncircleFilterEnum.values()) {
            if (classicEncircleFilterEnum.getGameEn().equals(gameEn)) {
                return classicEncircleFilterEnum;
            }
        }
        return null;
    }

    public abstract EncircleClaissicFilter[] getFilterEnum();
}
