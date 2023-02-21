package com.mojieai.predict.enums;

/**
 * Created by Singal
 */
public enum CommonStatusEnum {
    YES(1), NO(0);

    CommonStatusEnum(int status) {
        this.status = status;
    }

    private int status;

    public int getStatus() {
        return status;
    }
}