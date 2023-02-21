package com.mojieai.predict.enums.resonance;

import com.mojieai.predict.constant.GameConstant;

public enum ResonanceEnum {
    SSQ_RESONANCE(GameConstant.SSQ) {
        @Override
        public GameResonance[] getGameResonance() {
            return SsqResonanceEnum.values();
        }
    }, DLT_RESONANCE(GameConstant.DLT) {
        @Override
        public GameResonance[] getGameResonance() {
            return DltResonanceEnum.values();
        }
    };

    private String gameEn;

    ResonanceEnum(String gameEn) {
        this.gameEn = gameEn;
    }

    public abstract GameResonance[] getGameResonance();

    public String getGameEn() {
        return gameEn;
    }
}
