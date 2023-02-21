package com.mojieai.predict.service.manualpredicthistory;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.PredictConstant;
import com.mojieai.predict.util.PredictUtil;
import com.mojieai.predict.util.TrendUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SsqManualPredictNum extends ManualPredictNum {

    /*自动装配500注*/
    @Override
    public Set<String> autoFill500PredictNum(int redKillWrongCount, int blueKillWrongCount, List<String>
            redWinList, List<String> blueWinList, List<String> redList, List<String> blueList, String[] ruleArr) {
        Set<String> autoFillWinNum = new HashSet<>();
        StringBuffer restRedWinNum = new StringBuffer();
        redWinList.forEach(n -> restRedWinNum.append(n).append(CommonConstant.SPACE_SPLIT_STR));
        String redWinNumWithOutKillNum = restRedWinNum.toString();

        if (redKillWrongCount == 3 && blueKillWrongCount > 0) {//都错  人工不生效
            ruleArr = null;
            //无操作
        } else if (redKillWrongCount == 2 && blueKillWrongCount > 0) {//4+0红错二，蓝错 人工不生效
            ruleArr = null;
            //自动产生5等奖（325）
            autoFillWinNum.addAll(PredictUtil.ssqBaseGenerateFivePrize4And0(redWinNumWithOutKillNum, redList,
                    blueList.get(0), PredictConstant.SSQ_OPERATE_PREDICT_MAX_COUNT_FIVE_PRIZE));
        } else if (redKillWrongCount == 2 && blueKillWrongCount == 0) {//4+1红错二，蓝对 配置不生效
            ruleArr = null;
            //无操作
        } else if (redKillWrongCount == 1 && blueKillWrongCount > 0) {//5+0红错一，蓝错 配置不生效
            ruleArr = null;
            //产生四等奖(只有5个红球，25)
            autoFillWinNum.addAll(PredictUtil.ssqBaseGenerateFourPrize5And0(redWinNumWithOutKillNum, redList, blueList
                    .get(0), PredictConstant.SSQ_OPERATE_PREDICT_MAX_COUNT_FOUR_PRIZE));
            //五等奖(1500)
            autoFillWinNum.addAll(PredictUtil.ssqGenerateFivePrize5And0(redWinList, redList, blueList,
                    PredictConstant.SSQ_OPERATE_PREDICT_MAX_COUNT_FIVE_PRIZE));
        } else if (redKillWrongCount == 1 && blueKillWrongCount == 0) {//5+1红错一，蓝对
            if (ruleArr != null && ruleArr.length > 0) {
                ruleArr[0] = "0";
                ruleArr[1] = "0";
            }
        } else if (redKillWrongCount == 0 && blueKillWrongCount > 0) {//6+0红对，蓝错
            if (ruleArr != null && ruleArr.length > 0) {
                ruleArr[0] = "0";
                ruleArr[2] = "0";
            }
            //四等奖(144注)
            autoFillWinNum.addAll(PredictUtil.ssqGenerateFourPrize6And0(redWinList, redList, blueList,
                    PredictConstant.SSQ_OPERATE_PREDICT_MAX_COUNT_FOUR_PRIZE));
            //五等奖(4140)
            autoFillWinNum.addAll(PredictUtil.ssqGenerateFivePrize6And0(redWinList, redList, blueList,
                    PredictConstant.SSQ_OPERATE_PREDICT_MAX_COUNT_FIVE_PRIZE));
        }
        return autoFillWinNum;
    }

    @Override
    public Integer getRedKillBallType() {
        return PredictConstant.PREDICT_RED_BALL_STR_TYPE_KILL_THREE;
    }

    @Override
    public Integer getBlueKillBallType() {
        return PredictConstant.KILL_THREE_BLUE;
    }

    @Override
    public String generateRandomNum(List<String> redBallList, List<String> blueBallList) {
        Collections.shuffle(redBallList, new Random(System.currentTimeMillis()));
        Collections.shuffle(blueBallList, new Random(System.currentTimeMillis()));

        String randomNum = redBallList.get(0) + CommonConstant.SPACE_SPLIT_STR + redBallList.get(1) +
                CommonConstant.SPACE_SPLIT_STR + redBallList.get(2) + CommonConstant.SPACE_SPLIT_STR +
                redBallList.get(3) + CommonConstant.SPACE_SPLIT_STR + redBallList.get(4) + CommonConstant
                .SPACE_SPLIT_STR + redBallList.get(5) + CommonConstant.COMMON_COLON_STR + blueBallList.get(0);
        return TrendUtil.orderNum(randomNum);
    }

    /*生成指定奖级的大奖号*/
    @Override
    public String generateBigBonusByAwardLevel(String winningNum, int awardLevel, Set<String> predictNums,
                                               List<String> redList, List<String> blueList, List<String> redWinBalls,
                                               List<String> blueWinBalls) {
        StringBuffer result = new StringBuffer();
        String[] winningArr = winningNum.split(CommonConstant.COMMON_COLON_STR);
        if (awardLevel == 1) {//随机一注
            result.append(TrendUtil.orderNum(winningNum));
        } else if (awardLevel == 2) {
            result.append(winningArr[0]).append(CommonConstant.COMMON_COLON_STR);
            result.append(blueList.get(0));
        } else if (awardLevel == 3) {
            List<String> redWinNumList = Arrays.asList(winningArr[0].split(CommonConstant.SPACE_SPLIT_STR));
            Set<String> fourPrize = PredictUtil.ssqGenerateFouthPrizeOnlyRedBall(redWinNumList, redList, winningArr[1]);
            for (String temp : fourPrize) {
                if (!predictNums.contains(TrendUtil.orderNum(temp))) {
                    result = new StringBuffer(TrendUtil.orderNum(temp));
                    break;
                }
            }
        }
        if (StringUtils.isBlank(result)) {
            result.append(generateRandomNum(redList, blueList));
        }
        return TrendUtil.orderNum(result.toString());
    }
}
