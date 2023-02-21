package com.mojieai.predict.enums;

import com.mojieai.predict.constant.FilterConstant;

/**
 * Created by tangxuelong on 2017/8/16.
 */
public enum SsqMatrixEnum {
    /* 中6保5*/
    Z6B5(FilterConstant.FILTER_Z6B5, FilterConstant.FILTER_Z6B5_CN_MSG) {
        @Override
        public String getMatrixName() {
            return FilterConstant.FILTER_Z6B5_CN;
        }

        @Override
        public Boolean matrixActionLimit(Integer redBallLength) {
            if (redBallLength >= 7 && redBallLength <= 33) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        }
    },
    /* 中6保4*/
    Z6B4(FilterConstant.FILTER_Z6B4, FilterConstant.FILTER_Z6B4_CN_MSG) {
        @Override
        public String getMatrixName() {
            return FilterConstant.FILTER_Z6B4_CN;
        }

        @Override
        public Boolean matrixActionLimit(Integer redBallLength) {
            if (redBallLength >= 9 && redBallLength <= 32) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        }
    },
    /* 中5保5*/
    Z5B5(FilterConstant.FILTER_Z5B5, FilterConstant.FILTER_Z5B5_CN_MSG) {
        @Override
        public String getMatrixName() {
            return FilterConstant.FILTER_Z5B5_CN;
        }

        @Override
        public Boolean matrixActionLimit(Integer redBallLength) {
            if (redBallLength >= 7 && redBallLength <= 31) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        }
    };

    private String matrixAction;

    private String errLimitMsg;

    SsqMatrixEnum(String matrixAction, String errLimitMsg) {
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

    public static SsqMatrixEnum getByMatrixAction(String matrixAction) {
        for (SsqMatrixEnum k : SsqMatrixEnum.values()) {
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
