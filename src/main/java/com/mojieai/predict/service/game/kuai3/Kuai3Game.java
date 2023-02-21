package com.mojieai.predict.service.game.kuai3;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.entity.po.AwardInfo;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.vo.AwardInfoVo;
import com.mojieai.predict.enums.Kuai3GameEnum;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.game.AbstractGame;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Kuai3Game extends AbstractGame {

    @Override
    public int[] analyseBidAwardLevels(String bidBalls, GamePeriod period) {
        return new int[0];
    }

    @Override
    public String getPeriodDateFormat() {
        return DateUtil.DATE_FORMAT_YYYYMMDD;
    }

    @Override
    protected int getWinningNumberLength() {
        return 3;
    }

    @Override
    protected String getWinningNumberRegexp() {
        return "^([1-6]\\s){2}([1-6])$";
    }

    @Override
    public AwardInfoVo calcWinningNumber(GamePeriod gamePeriod) {
        Map<String, Object> resultMap = new HashMap<>();
        int sum;
        Integer awardStatus = 0;
        String winningNumbers = gamePeriod.getWinningNumbers();
        if (!StringUtils.isBlank(winningNumbers) && winningNumbers.length() == 5) {
            winningNumbers = winningNumbers.trim();
            awardStatus = 1;
            int[] numberArray = CommonUtil.numberStr2IntArray(winningNumbers, CommonConstant.SPACE_SPLIT_STR);
            sum = numberArray[0] + numberArray[1] + numberArray[2];
            resultMap.put("hezhi", sum);
            if (sum <= 10) {
                resultMap.put("awardStyle1", "小");
            } else {
                resultMap.put("awardStyle1", "大");
            }
            if (sum % 2 == 0) {
                resultMap.put("awardStyle2", "双");
            } else {
                resultMap.put("awardStyle2", "单");
            }
            if (numberArray[0] != numberArray[1] && numberArray[1] != numberArray[2]) {
                resultMap.put("awardShape", "三不同号");
                if (numberArray[1] - numberArray[0] == 1 && numberArray[2] - numberArray[1] == 1) {
                    resultMap.put("awardShape", "三连号");
                }
            } else if (numberArray[0] == numberArray[1] && numberArray[1] == numberArray[2]) {
                resultMap.put("awardShape", "三同号");
            } else if (numberArray[0] == numberArray[1] && numberArray[1] != numberArray[2]) {
                resultMap.put("awardShape", "二同号");
            } else if (numberArray[0] != numberArray[1] && numberArray[1] == numberArray[2]) {
                resultMap.put("awardShape", "二同号");
            }
        } else {
            resultMap.put("awardStatusCn", "等待开奖");
        }
        AwardInfoVo awardInfoVo = new AwardInfoVo(gamePeriod.getPeriodId(), winningNumbers, resultMap, GameCache
                .getGame(gamePeriod.getGameId()).getGameName(), GameCache.getGame(gamePeriod.getGameId()).getGameEn()
                , awardStatus, DateUtil.formatTime(gamePeriod.getAwardTime(), DateUtil.DATE_FORMAT_YYYYMMDD_HHMM));
        return awardInfoVo;
    }

    @Override
    public List<AwardInfo> getAwardInfoList(RedisService redisService, String periodId) {
        return Kuai3GameEnum.KUAI3_AWARD_INFO_LIST;
    }

    @Override
    public Map<String, Object> getPlayTypeAndBonus(RedisService redisService, String periodId, String gameEn) {
        Map<String, Object> map = new HashMap<>();
        map.put("awardInfoList", Kuai3GameEnum.KUAI3_PLAY_TYPE_BONUS);
        return map;
    }

    @Override
    public Timestamp getOfficialStartTime(GamePeriod gamePeriod) {
        Timestamp endTime = gamePeriod.getEndTime();
        Timestamp startTime = DateUtil.getIntervalMinutes(endTime, -10);
        return startTime;
    }

    @Override
    public Integer getAwardInfoSize() {
        return Kuai3GameEnum.KUAI3_AWARD_INFO_LIST.size();
    }

    @Override
    public List<AwardInfo> getDefaultAwardInfoList() {
        return Kuai3GameEnum.KUAI3_AWARD_INFO_LIST;
    }
}
