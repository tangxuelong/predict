package com.mojieai.predict.service.filter;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.GameConstant;
import com.mojieai.predict.enums.DltMatrixEnum;
import com.mojieai.predict.enums.GameEnum;
import com.mojieai.predict.util.CommonUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tangxuelong on 2017/8/16.
 */
@Component
public class DltFilter extends Filter {

    @Override
    public List<Map<String, String>> getMatrixList() {
        List<Map<String, String>> matrixList = new ArrayList<>();
        for (DltMatrixEnum dltMatrixEnum : DltMatrixEnum.values()) {
            Map<String, String> matrixMap = new HashMap<>();
            matrixMap.put("matrixName", dltMatrixEnum.getMatrixName());
            matrixMap.put("matrixAction", dltMatrixEnum.getMatrixAction());
            matrixList.add(matrixMap);
        }
        return matrixList;
    }

    @Override
    public String[] getBlueCombine(String[] blueArr) {
        List<String[]> blueCombineArr = CommonUtil.combine(blueArr, 2);
        String[] result = new String[blueCombineArr.size()];
        for (int i = 0; i < blueCombineArr.size(); i++) {
            result[i] = blueCombineArr.get(i)[0] + CommonConstant.SPACE_SPLIT_STR + blueCombineArr.get(i)[1];
        }
        return result;
    }

    @Override
    public Integer getRedBallsCombine(String[] redArr, String[] blueArr) {
        int blueNum = CommonUtil.combine(blueArr.length, 2);
        return CommonUtil.combine(redArr.length, GameEnum.DLT.getGameRedNumberBLueLength()) * blueNum;
    }

    @Override
    public Integer getBallsCombine(Integer redCount, Integer blueCount) {
        return CommonUtil.combine(redCount, GameEnum.DLT.getGameRedNumberBLueLength()) * blueCount;
    }

    @Override
    public Integer[] bigMultipleFilter(List<String> resultList, String[] redBalls, Integer[] redBallsInt, String
            blueBall, String action) {
        int totalNum = 0;
        int resultNum = 0;
        for (int a = 0; a < redBalls.length - 4; a++) {
            for (int b = a + 1; b < redBalls.length - 3; b++) {
                for (int c = b + 1; c < redBalls.length - 2; c++) {
                    for (int d = c + 1; d < redBalls.length - 1; d++) {
                        for (int e = d + 1; e < redBalls.length; e++) {
                            totalNum++;
                            /* 红球*/
                            StringBuffer stringBuffer = new StringBuffer();
                            String number = stringBuffer.append(redBalls[a]).append(CommonConstant.SPACE_SPLIT_STR)
                                    .append(redBalls[b]).append(CommonConstant.SPACE_SPLIT_STR)
                                    .append(redBalls[c]).append(CommonConstant.SPACE_SPLIT_STR)
                                    .append(redBalls[d]).append(CommonConstant.SPACE_SPLIT_STR)
                                    .append(redBalls[e]).toString();

                            /* 过滤*/
                            List<Integer> redBall = new ArrayList<>();
                            redBall.add(redBallsInt[a]);
                            redBall.add(redBallsInt[b]);
                            redBall.add(redBallsInt[c]);
                            redBall.add(redBallsInt[d]);
                            redBall.add(redBallsInt[e]);
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
        Integer blueCount = getBlueCombine(blueBall.split(CommonConstant.SPACE_SPLIT_STR)).length;

        totalNum = totalNum * blueCount;
        resultNum = resultNum * blueCount;
        return new Integer[]{totalNum, resultNum};
    }

    @Override
    public String getGameEn() {
        return GameConstant.DLT;
    }

    @Override
    public String checkMatrixAction(String matrixAction, String lotteryNumber) {
        String[] redball = lotteryNumber.split(CommonConstant.COMMON_COLON_STR)[0].split(CommonConstant
                .SPACE_SPLIT_STR);
        if (!DltMatrixEnum.getByMatrixAction(matrixAction).matrixActionLimit(redball.length)) {
            return DltMatrixEnum.getByMatrixAction(matrixAction).getErrLimitMsg();
        }
        return null;
    }
}
