package com.mojieai.predict.enums.predict;

import com.mojieai.predict.constant.GameConstant;

public enum PickNumEnum {
    SSQ_PICK_NUM(GameConstant.SSQ) {
        @Override
        public PickNumPredict getGamePickNumEnum(Integer type) {
            return SsqPickNumEnum.getPickNumEnumByStrType(type);
        }

        @Override
        public PickNumPredict[] getGamePickNumEnum() {
            return SsqPickNumEnum.values();
        }
    }, DLT_PICK_NUM(GameConstant.DLT) {
        @Override
        public PickNumPredict getGamePickNumEnum(Integer type) {
            return DltPickNumEnum.getPickNumEnumByStrType(type);
        }

        @Override
        public PickNumPredict[] getGamePickNumEnum() {
            return DltPickNumEnum.values();
        }
    };

    private String gameEn;

    PickNumEnum(String gameEn) {
        this.gameEn = gameEn;
    }

    public String getGameEn() {
        return gameEn;
    }

    public static PickNumEnum getPickNumEnum(String gameEn) {
        for (PickNumEnum pne : PickNumEnum.values()) {
            if (pne.getGameEn().equals(gameEn)) {
                return pne;
            }
        }
        return null;
    }

    public abstract PickNumPredict getGamePickNumEnum(Integer type);

    public abstract PickNumPredict[] getGamePickNumEnum();
}
