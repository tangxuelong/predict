package com.mojieai.predict.service.game.d11;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.entity.po.AwardInfo;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.vo.AwardInfoVo;
import com.mojieai.predict.enums.D11GameEnum;
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

public abstract class D11Game extends AbstractGame {

    @Override
    public int[] analyseBidAwardLevels(String bidBalls, GamePeriod period) {
        return new int[0];
    }

    @Override
    public String getPeriodDateFormat() {
        return DateUtil.DATE_FORMAT_YYMMDD;
    }

    @Override
    protected int getWinningNumberLength() {
        return 5;
    }

    @Override
    protected String getWinningNumberRegexp() {
        return "^((01|02|03|04|05|06|07|08|09|10|11)\\s){4}(01|02|03|04|05|06|07|08|09|10|11)$";
    }

    @Override
    public AwardInfoVo calcWinningNumber(GamePeriod gamePeriod) {
        Map<String, Object> resultMap = new HashMap<>();
        String winningNumbers = gamePeriod.getWinningNumbers();
        Integer awardStatus = 0;
        if (!StringUtils.isBlank(winningNumbers)) {
            awardStatus = 1;
            winningNumbers = winningNumbers.trim();
            int odd = 0;
            int even = 0;
            int big = 0;
            int small = 0;
            int sum = 0;
            int[] numbers = CommonUtil.numberStr2IntArray(winningNumbers, CommonConstant.SPACE_SPLIT_STR);
            for (int i = 0; i < 5; i++) {
                if (numbers[i] % 2 == 0) {
                    even = even + 1;
                } else {
                    odd = odd + 1;
                }
                if (numbers[i] > 5) {
                    big = big + 1;
                } else {
                    small = small + 1;
                }
                sum = sum + numbers[i];
            }
            resultMap.put("odd", odd);
            resultMap.put("even", even);
            resultMap.put("big", big);
            resultMap.put("small", small);
            resultMap.put("sum", sum);
        } else {
            resultMap.put("awardStatusCn", "等待开奖");
        }
        AwardInfoVo awardInfoVo = new AwardInfoVo(gamePeriod.getPeriodId(), winningNumbers, resultMap, GameCache.getGame
                (gamePeriod.getGameId()).getGameName(), GameCache.getGame(gamePeriod.getGameId()).getGameEn(),
                awardStatus, DateUtil.formatTime(gamePeriod.getAwardTime(), DateUtil.DATE_FORMAT_YYYYMMDD_HHMM));
        return awardInfoVo;
    }

    @Override
    public List<AwardInfo> getAwardInfoList(RedisService redisService, String periodId) {
        return D11GameEnum.D11_AWARD_INFO_LIST;
    }

    @Override
    public Timestamp getOfficialStartTime(GamePeriod gamePeriod) {
        Timestamp endTime = gamePeriod.getEndTime();
        Timestamp startTime = DateUtil.getIntervalMinutes(endTime, -10);
        return startTime;
    }

    @Override
    public Integer getAwardInfoSize() {
        return D11GameEnum.D11_AWARD_INFO_LIST.size();
    }

    @Override
    public List<AwardInfo> getDefaultAwardInfoList() {
        return D11GameEnum.D11_AWARD_INFO_LIST;
    }
}
