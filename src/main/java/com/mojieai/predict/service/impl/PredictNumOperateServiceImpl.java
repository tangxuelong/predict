package com.mojieai.predict.service.impl;

import com.mojieai.predict.cache.IniCache;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.IniConstant;
import com.mojieai.predict.constant.PredictConstant;
import com.mojieai.predict.dao.PredictNumbersOperateDao;
import com.mojieai.predict.enums.CronEnum;
import com.mojieai.predict.service.PredictNumOperateService;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;

@Service
public class PredictNumOperateServiceImpl implements PredictNumOperateService {
    private static final Logger log = CronEnum.PERIOD.getLogger();

    @Autowired
    private PredictNumbersOperateDao predictNumbersOperateDao;

    @Override
    public Integer getPredictNumsCount(long gameId, String periodId) {
        //当前产生9500+random(10)个号码
        StringBuffer sb = new StringBuffer(gameId + "").append(periodId).append(IniCache.getIniValue(IniConstant
                .RANDOM_CODE, CommonConstant.RANDOM_CODE));
        Random random = new Random(new Long((long) sb.toString().hashCode()));
        Integer randNum = random.nextInt(10);
        Integer result = PredictConstant.PREDICT_NUMBERS_COUNT + randNum;
//        Integer maxOperateNum = 9000;
//        Map predictNumbersOperate = predictNumbersOperateDao.getPredictNumsByGameIdAndPeriodId(gameId, periodId);
//        if (predictNumbersOperate != null && Integer.valueOf(predictNumbersOperate.get("STATUS").toString()) == 1) {
//            //解析role
//            String[] ruleArr = predictNumbersOperate.get("RULE_STR").toString().split(CommonConstant
// .COMMON_COLON_STR);
//            if (ruleArr != null && ruleArr.length > 0) {
//                for (String temp : ruleArr) {
//                    result = result - Integer.valueOf(temp);
//                }
//                if (result < maxOperateNum) {
//                    log.error("gameId:" + gameId + "periodId:" + periodId + " 预测号码个数为：" + result + "请确认");
//                }
//            }
//        }
        return result;
    }

    @Override
    public Map<String, Object> getOperatePredictNumRule(long gameId, String periodId) {
        Map predictNumbersOperate = predictNumbersOperateDao.getPredictNumsByGameIdAndPeriodId(gameId, periodId);

        if (predictNumbersOperate != null && Integer.valueOf(predictNumbersOperate.get("STATUS").toString()) != 1) {
            predictNumbersOperate = null;
        }
        return predictNumbersOperate;
    }
}