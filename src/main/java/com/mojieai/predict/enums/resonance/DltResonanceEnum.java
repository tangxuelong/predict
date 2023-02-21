package com.mojieai.predict.enums.resonance;

/**
 * Created by tangxuelong on 2018/3/23.
 */
public enum DltResonanceEnum implements GameResonance {
    EN_SIX_HOURS(0, 0) {
        @Override
        public Integer getResonanceTypeTime() {
            return 60 * 6;
        }
    },
    EN_TWELVE_HOURS(0, 1) {
        @Override
        public Integer getResonanceTypeTime() {
            return 60 * 12;
        }
    },
    EN_TWENTY_FOUR_HOURS(0, 2) {
        @Override
        public Integer getResonanceTypeTime() {
            return 60 * 24;
        }
    },
    EN_ALL_PERIOD(0, 3) {
        @Override
        public Integer getResonanceTypeTime() {
            return 0;
        }
    },
    KILL_SIX_HOURS(1, 0) {
        @Override
        public Integer getResonanceTypeTime() {
            return 60 * 6;
        }
    },
    KILL_TWELVE_HOURS(1, 1) {
        @Override
        public Integer getResonanceTypeTime() {
            return 60 * 12;
        }
    },
    KILL_TWENTY_FOUR_HOURS(1, 2) {
        @Override
        public Integer getResonanceTypeTime() {
            return 60 * 24;
        }
    },
    KILL_ALL_PERIOD(1, 3) {
        @Override
        public Integer getResonanceTypeTime() {
            return 0;
        }
    };

    private Integer socialType;
    private Integer resonanceType;

    DltResonanceEnum(Integer socialType, Integer resonanceType) {
        this.socialType = socialType;
        this.resonanceType = resonanceType;
    }

    public Integer getSocialType() {
        return this.socialType;
    }

    public Integer getResonanceType() {
        return this.resonanceType;
    }

    public Integer getResonanceTypeTime() {
        throw new AbstractMethodError("matrixActionLimit error");
    }

    public static DltResonanceEnum getByType(String socialType, Integer resonanceType) {
        for (DltResonanceEnum k : DltResonanceEnum.values()) {
            if (socialType.equals(k.getSocialType()) && resonanceType.equals(k.getResonanceType())) {
                return k;
            }
        }
        return null;
    }
}
