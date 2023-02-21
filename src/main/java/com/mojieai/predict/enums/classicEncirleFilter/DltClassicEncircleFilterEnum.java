package com.mojieai.predict.enums.classicEncirleFilter;

public enum DltClassicEncircleFilterEnum implements EncircleClaissicFilter {
    CLASSIC_5_5(5, 5), CLASSIC_5_4(5, 4), CLASSIC_10_5(10, 5), CLASSIC_10_4(10, 4), CLASSIC_15_5
            (15, 5), CLASSIC_20_5(20, 5);

    private Integer encircleCount;
    private Integer rightCount;

    DltClassicEncircleFilterEnum(Integer encircleCount, Integer rightCount) {
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
