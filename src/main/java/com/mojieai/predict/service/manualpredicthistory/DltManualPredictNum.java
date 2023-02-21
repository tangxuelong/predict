package com.mojieai.predict.service.manualpredicthistory;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.PredictConstant;
import com.mojieai.predict.util.PredictUtil;
import com.mojieai.predict.util.TrendUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DltManualPredictNum extends ManualPredictNum {

    @Override
    public String generateBigBonusByAwardLevel(String winningNum, int awardLevel, Set<String> predictNums,
                                               List<String> redList, List<String> blueList, List<String> redWinBalls,
                                               List<String> blueWinBalls) {
        String result = "";
        if (awardLevel == 1) {//随机一注
            result = TrendUtil.orderNum(winningNum);
        } else if (awardLevel == 2) {
            List<String> secoundPrize = PredictUtil.dltBaseGenerateSecoundPrize(winningNum, blueList);
            Collections.shuffle(secoundPrize);
            result = secoundPrize.get(0);
        } else if (awardLevel == 3) {
            List<String> thridPrize = new ArrayList<>();
            if (blueWinBalls.size() == 2) {
                thridPrize.addAll(PredictUtil.dltBaseGenerateThridPrize4And2(redWinBalls, blueWinBalls, redList));
            }
            thridPrize.addAll(PredictUtil.dltBaseGenerateThridPrize5And0(winningNum, blueList));
            Collections.shuffle(thridPrize);
            result = thridPrize.get(0);
        }
        if (StringUtils.isBlank(result)) {
            result = generateRandomNum(redList, blueList);
        }
        return TrendUtil.orderNum(result.toString());
    }

    @Override
    public Set<String> autoFill500PredictNum(int redKillWrongCount, int blueKillWrongCount, List<String>
            redWinList, List<String> blueWinList, List<String> redList, List<String> blueList, String[] ruleArr) {
        Set<String> autoFillWinNum = new HashSet<>();
        StringBuffer restRedWinNum = new StringBuffer();
        redWinList.forEach(n -> restRedWinNum.append(n).append(CommonConstant.SPACE_SPLIT_STR));
        String redWinNumWithOutKillNum = restRedWinNum.toString();

        if (redKillWrongCount == 3 && blueKillWrongCount > 0) {//都错2+1 六等奖 自动装入六等奖
            ruleArr = null;
            //六等奖4060
            autoFillWinNum.addAll(PredictUtil.dltBaseGenerateSixPrize2And1(redWinList, blueWinList, redList, blueList));
        } else if (redKillWrongCount == 3 && blueKillWrongCount == 0) {//红错2 蓝对 2+2  五六等奖
            ruleArr = null;
            //五等奖(378)
            autoFillWinNum.addAll(PredictUtil.dltBaseGenerateFivePrize2And2(redWinList, blueWinList, redList));
            //六等奖(1261)
            autoFillWinNum.addAll(PredictUtil.dltGenerateSixPrize2And2(redWinList, blueWinList, redList, blueList));
        } else if (redKillWrongCount == 2 && blueKillWrongCount > 0) { //3+1  五六等奖
            ruleArr = null;
            //五等奖(406)
            autoFillWinNum.addAll(PredictUtil.dltBaseGenerateFivePrize3And1(redWinList, blueWinList, redList,
                    blueList));
            //六等奖(3654)
            autoFillWinNum.addAll(PredictUtil.dltGenerateSixPrize3And1(redWinList, blueWinList, redList, blueList));
        } else if (redKillWrongCount == 2 && blueKillWrongCount == 0) {//3+2 四 五六
            ruleArr = null;
            //四(434)
            autoFillWinNum.addAll(PredictUtil.dltBaseGenerateFourPrize3And2(redWinList, blueWinList, redList,
                    blueList));
            //五(757)
            autoFillWinNum.addAll(PredictUtil.dltGenerateFivePrize3And2(redWinList, blueWinList, redList, blueList));
            //六等奖(4032)
            autoFillWinNum.addAll(PredictUtil.dltGenerateSixPrize3And2(redWinList, blueWinList, redList, blueList));
        } else if (redKillWrongCount == 1 && blueKillWrongCount > 0) {//4+1 四 五 六
            ruleArr = null;
        } else if (redKillWrongCount == 1 && blueKillWrongCount == 0) {//4+2 三四五六
            ruleArr[0] = "0";
            ruleArr[1] = "0";
        } else if (redKillWrongCount == 0 && blueKillWrongCount > 0) {//5+1  二三四五六
            ruleArr[0] = "0";
        }

        return autoFillWinNum;
    }

    @Override
    public Integer getRedKillBallType() {
        return PredictConstant.PREDICT_RED_BALL_STR_TYPE_KILL_THREE;
    }

    @Override
    public Integer getBlueKillBallType() {
        return PredictConstant.KILL_ONE_BLUE;
    }

    @Override
    public String generateRandomNum(List<String> redBallList, List<String> blueBallList) {
        Collections.shuffle(redBallList, new Random(System.currentTimeMillis()));
        Collections.shuffle(blueBallList, new Random(System.currentTimeMillis()));

        String randomNum = redBallList.get(0) + CommonConstant.SPACE_SPLIT_STR + redBallList.get(1) +
                CommonConstant.SPACE_SPLIT_STR + redBallList.get(2) + CommonConstant.SPACE_SPLIT_STR +
                redBallList.get(3) + CommonConstant.SPACE_SPLIT_STR + redBallList.get(4) + CommonConstant
                .COMMON_COLON_STR + blueBallList.get(0) + CommonConstant.SPACE_SPLIT_STR + blueBallList.get(1);
        return TrendUtil.orderNum(randomNum);
    }
}
