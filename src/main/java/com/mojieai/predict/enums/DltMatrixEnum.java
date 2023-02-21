package com.mojieai.predict.enums;

import com.mojieai.predict.constant.FilterConstant;

/**
 * Created by tangxuelong on 2017/8/16.
 */
public enum DltMatrixEnum {
    /* 中5保4*/
    Z5B4(FilterConstant.FILTER_Z5B4, FilterConstant.FILTER_Z5B4_CN_MSG) {
        @Override
        public String getMatrixName() {
            return FilterConstant.FILTER_Z5B4_CN;
        }
        @Override
        public Boolean matrixActionLimit(Integer redBallLength) {
            if (redBallLength >= 7 && redBallLength <= 32) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        }
    },
    /* 中5保3*/
    Z5B3(FilterConstant.FILTER_Z5B3, FilterConstant.FILTER_Z5B3_CN_MSG) {
        @Override
        public String getMatrixName() {
            return FilterConstant.FILTER_Z5B3_CN;
        }
        @Override
        public Boolean matrixActionLimit(Integer redBallLength) {
            if (redBallLength >= 10 && redBallLength <= 22 && redBallLength != 16) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        }
    },
    /* 中4保4*/
    Z4B4(FilterConstant.FILTER_Z4B4, FilterConstant.FILTER_Z4B4_CN_MSG) {
        @Override
        public String getMatrixName() {
            return FilterConstant.FILTER_Z4B4_CN;
        }
        @Override
        public Boolean matrixActionLimit(Integer redBallLength) {
            if (redBallLength >= 6 && redBallLength <= 32) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        }
    };

    private String matrixAction;

    private String errLimitMsg;

    DltMatrixEnum(String matrixAction, String errLimitMsg) {
        this.matrixAction = matrixAction;
        this.errLimitMsg = errLimitMsg;
    }

    public String getMatrixAction() {
        return matrixAction;
    }

    public String getErrLimitMsg() {
        return errLimitMsg;
    }

    public String getMatrixName() {
        throw new AbstractMethodError("getMatrixName");
    }

    public static DltMatrixEnum getByMatrixAction(String matrixAction) {
        for (DltMatrixEnum k : DltMatrixEnum.values()) {
            if (matrixAction.equals(k.getMatrixAction())) {
                return k;
            }
        }
        return null;
    }

    public Boolean matrixActionLimit(Integer redBallLength) {
        throw new AbstractMethodError("matrixActionLimit error");
    }

}
