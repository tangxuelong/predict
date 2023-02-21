package com.mojieai.predict.service.predict;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.cache.IniCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.po.PredictSchedule;
import com.mojieai.predict.enums.SsqGameEnum;
import com.mojieai.predict.util.PredictUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

@Service
public class SsqPredictInfo extends PredictInfo {
    private String gameEn = GameConstant.SSQ;

    /*预测一万注*/
    @Override
    public Map<String, Object> generatePredictNums(GamePeriod period) {
        Map<String, Object> result = new HashMap<>();
        Set<String> numberList = new HashSet<>();
        List<String> redList = new ArrayList<>(SsqGameEnum.SSQ_RED_NUMBERS);
        List<String> blueList = new ArrayList<>(SsqGameEnum.SSQ_BLUE_NUMBERS);

        /* 预测号码中去掉蓝球杀三码*/
        String blueKillThreeKey = RedisConstant.getPredictTypeKey(GameCache.getGame(period.getGameId()).getGameEn(),
                PredictConstant.KILL_THREE_BLUE);
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
        for (int i = 1; i < 30; i++) {
            allNumber = allNumber + CommonConstant.SPACE_SPLIT_STR + redList.get(i);
        }
        String killNum = redList.get(30) + CommonConstant.SPACE_SPLIT_STR + redList.get(31) + CommonConstant
                .SPACE_SPLIT_STR + redList.get(32);
        String[] redBalls = allNumber.split(CommonConstant.SPACE_SPLIT_STR);
        String urlSeparator30 = String.valueOf(File.separatorChar);
        String path30 = getClass().getResource(CommonConstant.COMMON_DOT_STR).getPath();
        String subPath30 = path30.substring(0, path30.indexOf("WEB-INF") + 8);
        String rootPath30 = subPath30.substring(0, subPath30.lastIndexOf(urlSeparator30));
        rootPath30 = rootPath30 + File.separatorChar + "classes" + File.separatorChar + "filterfile" + File
                .separatorChar + GameConstant.SSQ + File.separatorChar + "ssq30z6b5.txt";
        File file30 = new File(rootPath30);
        List<String> tempList = new ArrayList<>();
        try {
            InputStreamReader read = new InputStreamReader(new FileInputStream(file30));
            BufferedReader bufferedReader = new BufferedReader(read);
            String lineTxt;
            while (StringUtils.isNotBlank(lineTxt = bufferedReader.readLine())) {
                Collections.shuffle(blueList, new Random(System.currentTimeMillis()));
                String[] placeArray = lineTxt.split(CommonConstant.SPACE_SPLIT_STR);
                String number30 = "";
                for (String place : placeArray) {
                    number30 = number30 + redBalls[Integer.parseInt(place) - 1] + CommonConstant.SPACE_SPLIT_STR;
                }
                tempList.add(number30.substring(0, number30.length() - 1) + CommonConstant.COMMON_COLON_STR +
                        blueList.get(0));
            }
            read.close();
        } catch (Throwable e) {
            log.error("read 30 file error" + period, e);
        }
        //只保留1w注
        int predictCount = predictNumOperateService.getPredictNumsCount(period.getGameId(), period.getPeriodId());
        numberList = PredictUtil.filterPredictNum(period.getGameId(), period.getPeriodId(), tempList, predictCount);

        result.put("killNum", killNum);
        result.put("numberList", numberList);
        return result;
    }

    @Override
    public void killBluePredict(Long gameId, String periodId, PredictSchedule predictScheduleDirty) {
        killBluePredictCal(gameId, periodId, predictScheduleDirty, PredictConstant.KILL_THREE_BLUE, 3);
    }

    @Override
    public String getShowText(String gameEn) {
        return PredictUtil.getShowTextCal(gameEn, PredictConstant.KILL_THREE_BLUE_EXPIRE_MSG, PredictConstant
                .KILL_THREE_BLUE_MSG);
    }
}
