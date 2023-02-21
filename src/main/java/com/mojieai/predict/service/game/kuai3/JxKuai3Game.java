package com.mojieai.predict.service.game.kuai3;

import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.enums.GameEnum;
import com.mojieai.predict.enums.Kuai3GameEnum;
import org.springframework.stereotype.Component;

@Component
public class JxKuai3Game extends Kuai3Game {
    @Override
    public Game getGame() {
        return GameEnum.JXKUAI3.getGame();
    }

    @Override
    public String getWinningNumberPushUrl() {
        return null;
    }

    @Override
    public String[] getAllRedNums() {
        return new String[0];
    }

    @Override
    public Integer getDailyPeriod() {
        return Kuai3GameEnum.JXKUAI3_DAILY_PERIOD;
    }

    @Override
    public Integer getPeriodInterval() {
        return Kuai3GameEnum.JXKUAI3_TIME_INTERVAL;
    }

    @Override
    public String getInitPeriodFormat() {
        return Kuai3GameEnum.JXKUAI3_INIT_PERIOD_FORMAT;
    }
}
