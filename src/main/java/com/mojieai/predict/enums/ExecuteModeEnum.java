package com.mojieai.predict.enums;

/**
 * Created by qiwang
 */
public enum ExecuteModeEnum {
    CRON(1),
    INTERVAL(2);

    private Integer executeMode;

    ExecuteModeEnum(int executeMode) {
        this.executeMode = executeMode;
    }

    public Integer getExecuteMode() {
        return executeMode;
    }

    public static ExecuteModeEnum getEnum(int mode) {
        for (ExecuteModeEnum e : ExecuteModeEnum.values()) {
            if (e.getExecuteMode() == mode) {
                return e;
            }
        }
        return null;
    }
}
