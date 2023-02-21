package com.mojieai.predict.enums.classicEncirleFilter;

public enum SsqClassicEncircleFilterEnum implements EncircleClaissicFilter {
    CLASSIC_5_5(5, 5), CLASSEIC_5_4(5, 4), CLASSIC_5_3(5, 3), CLASSIC_10_6(10, 6), CLASSIC_10_5(10, 5), CLASSIC_15_6
            (15, 6), CLASSIC_15_5(15, 5), CLASSIC_20_6(20, 6);

    private Integer encircleCount;
    private Integer rightCount;

    SsqClassicEncircleFilterEnum(Integer encircleCount, Integer rightCount) {
        this.encircleCount = encircleCount;
        this.rightCount = rightCount;
    }

    public Integer getEncircleCount() {
        return this.encircleCount;
    }

    public Integer getRightCount() {
        return this.rightCount;
    }

    @Override
    public Boolean enable() {
        return true;
    }

    @Override
    public Boolean filterClassicEncircle(Integer encircleCount, Integer rightCount) {
        if (this.encircleCount.equals(encircleCount) && this.rightCount.equals(rightCount)) {
            return true;
        }
        return false;
    }

}
