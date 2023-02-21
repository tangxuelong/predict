package com.mojieai.predict.service.filter;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.GameConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.enums.GameEnum;
import com.mojieai.predict.enums.SsqMatrixEnum;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.util.CommonUtil;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tangxuelong on 2017/8/16.
 */
@Component
public class SsqFilter extends Filter {

    protected Logger log = LogConstant.commonLog;

    @Autowired
    private RedisService redisService;

    @Override
    public List<Map<String, String>> getMatrixList() {
        List<Map<String, String>> matrixList = new ArrayList<>();
        for (SsqMatrixEnum ssqMatrixEnum : SsqMatrixEnum.values()) {
            Map<String, String> matrixMap = new HashMap<>();
            matrixMap.put("matrixName", ssqMatrixEnum.getMatrixName());
            matrixMap.put("matrixAction", ssqMatrixEnum.getMatrixAction());
            matrixList.add(matrixMap);
        }
        return matrixList;
    }

    @Override
    public String[] getBlueCombine(String[] blueArr) {
        return blueArr;
    }

    @Override
    public Integer getRedBallsCombine(String[] redArr, String[] blueArr) {
        return CommonUtil.combine(redArr.length, GameEnum.SSQ.getGameRedNumberBLueLength()) * blueArr.length;
    }

    @Override
    public Integer getBallsCombine(Integer redCount, Integer blueCount) {
        return CommonUtil.combine(redCount, GameEnum.SSQ.getGameRedNumberBLueLength()) * blueCount;
    }

    @Override
    public String getGameEn() {
        return GameConstant.SSQ;
    }

    @Override
    public Integer[] bigMultipleFilter(List<String> resultList, String[] redBalls, Integer[] redBallsInt, String
            blueBall, String action) {
            int totalNum = 0;
            int resultNum = 0;
            for (int a = 0; a < redBalls.length - 5; a++) {
                for (int b = a + 1; b < redBalls.length - 4; b++) {
                    for (int c = b + 1; c < redBalls.length - 3; c++) {
                        for (int d = c + 1; d < redBalls.length - 2; d++) {
                            for (int e = d + 1; e < redBalls.length - 1; e++) {
                                for (int f = e + 1; f < redBalls.length; f++) {
                                    totalNum++;
                                    /* 红球*/
                                    StringBuffer stringBuffer = new StringBuffer();
                                    String number = stringBuffer.append(redBalls[a]).append(CommonConstant
                                            .SPACE_SPLIT_STR)
                                            .append(redBalls[b]).append(CommonConstant.SPACE_SPLIT_STR)
                                            .append(redBalls[c]).append(CommonConstant.SPACE_SPLIT_STR)
                                            .append(redBalls[d]).append(CommonConstant.SPACE_SPLIT_STR)
                                            .append(redBalls[e]).append(CommonConstant.SPACE_SPLIT_STR)
                                            .append(redBalls[f]).toString();

                                    /* 过滤*/
                                    List<Integer> redBall = new ArrayList<>();
                                    redBall.add(redBallsInt[a]);
                                    redBall.add(redBallsInt[b]);
                                    redBall.add(redBallsInt[c]);
                                    redBall.add(redBallsInt[d]);
                                    redBall.add(redBallsInt[e]);
                                    redBall.add(redBallsInt[f]);
                                    if (!filterAction(redBall, action)) {
                                        continue;
                                    }
                                    resultNum++;
                                    /* 红球和篮球组合*/
                                    resultList.add(number);
                                }
                            }
                        }
                    }
                }
            }
            Integer blueCount = getBlueCombine(blueBall.split(CommonConstant.SPACE_SPLIT_STR)).length;
            totalNum = totalNum * blueCount;
            resultNum = resultNum * blueCount;
            return new Integer[]{totalNum, resultNum};
    }

    @Override
    public String checkMatrixAction(String matrixAction, String lotteryNumber) {
        String[] redball = lotteryNumber.split(CommonConstant.COMMON_COLON_STR)[0].split(CommonConstant
                .SPACE_SPLIT_STR);
        if (!SsqMatrixEnum.getByMatrixAction(matrixAction).matrixActionLimit(redball.length)) {
            return SsqMatrixEnum.getByMatrixAction(matrixAction).getErrLimitMsg();
        }
        return null;
    }
}
