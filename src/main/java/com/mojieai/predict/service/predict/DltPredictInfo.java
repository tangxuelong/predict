package com.mojieai.predict.service.predict;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.cache.IniCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.po.PredictSchedule;
import com.mojieai.predict.enums.DltGameEnum;
import com.mojieai.predict.util.PredictUtil;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DltPredictInfo extends PredictInfo {
    private String gameEn = GameConstant.DLT;

    /*预测一万注*/
    @Override
    public Map<String, Object> generatePredictNums(GamePeriod period) {
        Map<String, Object> result = new HashMap<>();
        Set<String> numberList = new HashSet<>();
        List<String> redList = new ArrayList<>(DltGameEnum.DLT_FRONT_NUMBERS);
        List<String> blueList = new ArrayList<>(DltGameEnum.DLT_BACK_NUMBERS);

        /* 预测号码中去掉蓝球杀1码*/
        String blueKillThreeKey = RedisConstant.getPredictTypeKey(GameCache.getGame(period.getGameId()).getGameEn(),
                PredictConstant.KILL_ONE_BLUE);
        Map<String, String> killThreeBlueNumbers = redisService.kryoGet(blueKillThreeKey, TreeMap.class);
        if (null != killThreeBlueNumbers && killThreeBlueNumbers.size() > 0) {
            for (String k : killThreeBlueNumbers.get(period.getPeriodId()).split(CommonConstant.SPACE_SPLIT_STR)) {
                blueList.remove(k);
            }
        }

        StringBuffer sb = new StringBuffer(gameEn).append(period.getPeriodId()).append(IniCache.getIniValue(IniConstant
                .RANDOM_CODE, CommonConstant.RANDOM_CODE));
        Collections.shuffle(redList, new Random(new Long((long) sb.toString().hashCode())));
        String allNumber = redList.get(0);
        for (int i = 1; i < 32; i++) {
            allNumber = allNumber + CommonConstant.SPACE_SPLIT_STR + redList.get(i);
        }
        String killNum = redList.get(32) + CommonConstant.SPACE_SPLIT_STR + redList.get(33) + CommonConstant
                .SPACE_SPLIT_STR + redList.get(34);

        List<String> tempList = getPredictIndexFromFile(period, allNumber, GameConstant.DLT, "dlt32z5b4.txt");
        List<String> tempList1 = getPredictIndexFromFile(period, allNumber, GameConstant.DLT, "dlt32z4b4.txt");


        Set<String> tempSet = new HashSet<>();
        tempSet.addAll(tempList);
        tempSet.addAll(tempList1);

        List<Integer> orderList = new ArrayList<>();
        for (int i = 0; i < tempList.size(); i++) {
            orderList.add(i);
        }
        Collections.shuffle(orderList, new Random(System.currentTimeMillis()));
        for (String num : tempSet) {
            Collections.shuffle(blueList, new Random(System.currentTimeMillis()));
            numberList.add(num + CommonConstant.COMMON_COLON_STR + blueList.get(0) + CommonConstant.SPACE_SPLIT_STR
                    + blueList.get(1));
        }

        //过滤号码
        List<String> listAllNum = new ArrayList<>();
        listAllNum.addAll(numberList);
        int predictCount = predictNumOperateService.getPredictNumsCount(period.getGameId(), period.getPeriodId());
        numberList = PredictUtil.filterPredictNum(period.getGameId(), period.getPeriodId(), listAllNum, predictCount);

        result.put("killNum", killNum);
        result.put("numberList", numberList);
        return result;
    }

    @Override
    public void killBluePredict(Long gameId, String periodId, PredictSchedule predictScheduleDirty) {
        killBluePredictCal(gameId, periodId, predictScheduleDirty, PredictConstant.KILL_ONE_BLUE, 1);
    }

    @Override
    public String getShowText(String gameEn) {
        return PredictUtil.getShowTextCal(gameEn, PredictConstant.KILL_ONE_BLUE_EXPIRE_MSG, PredictConstant
                .KILL_ONE_BLUE_MSG);
    }

}
