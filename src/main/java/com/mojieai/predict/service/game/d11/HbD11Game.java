package com.mojieai.predict.service.game.d11;

import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.enums.D11GameEnum;
import com.mojieai.predict.enums.GameEnum;
import org.springframework.stereotype.Component;

@Component
public class HbD11Game extends D11Game {
    @Override
    public Game getGame() {
        return GameEnum.HBD11.getGame();
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
    public Integer getDailyPeriod() { return D11GameEnum.HBD11_DAILY_PERIOD;}

    @Override
    public Integer getPeriodInterval(){
        return D11GameEnum.HBD11_TIME_INTERVAL;
    }

    @Override
    public String getInitPeriodFormat(){
        return D11GameEnum.HBD11_INIT_PERIOD_FORMAT;
    }
}
