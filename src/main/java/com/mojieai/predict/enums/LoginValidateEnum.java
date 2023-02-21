package com.mojieai.predict.enums;

import com.mojieai.predict.cache.IniCache;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.IniConstant;
import com.mojieai.predict.constant.RedisConstant;

/**
 * Created by tangxuelong on 2017/7/16.
 */
public enum LoginValidateEnum {
    SMS(CommonConstant.SMS) {
        @Override
        public String getValidateType() {
            return RedisConstant.PREFIX_VERIFY_CODE_TIMES;
        }

        @Override
        public Integer getValidateMaxTimes() {
            return IniCache.getIniIntValue(IniConstant.SMS_VERIFY_TIMES, 5);
        }

        @Override
        public Integer getMaxTimesExpireTime() {
            return IniCache.getIniIntValue(IniConstant.MAX_TIMES_EXPIRE_TIME, 60);
        }
    },
    PASSWORD(CommonConstant.PASSWORD) {
        @Override
        public String getValidateType() {
            return RedisConstant.PREFIX_PASSWORD_TIMES;
        }

        @Override
        public Integer getValidateMaxTimes() {
            return IniCache.getIniIntValue(IniConstant.PASSWORD_TIMES, 5);
        }

        @Override
        public Integer getMaxTimesExpireTime() {
            return IniCache.getIniIntValue(IniConstant.MAX_TIMES_EXPIRE_TIME, 60);
        }
    };
    private String type;

    LoginValidateEnum(String type) {
        this.type = type;
    }

    public String getLoginValidateEnum() {
        return type;
    }

    public static LoginValidateEnum getLoginValidateEnum(String type) {
        for (LoginValidateEnum loginValidateEnum : values()) {
            if (loginValidateEnum.getLoginValidateEnum().equals(type)) {
                return loginValidateEnum;
            }
        }
        return null;
    }

    abstract public String getValidateType();

    abstract public Integer getValidateMaxTimes();

    abstract public Integer getMaxTimesExpireTime();
}
