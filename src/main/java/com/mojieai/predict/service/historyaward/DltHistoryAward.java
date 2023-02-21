package com.mojieai.predict.service.historyaward;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.cache.IniCache;
import com.mojieai.predict.constant.GameConstant;
import com.mojieai.predict.constant.IniConstant;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.enums.DltGameEnum;
import com.mojieai.predict.redis.base.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 针对红蓝双色球的彩种,例如双色球和大乐透。其他彩种是否支持未知。
 */
@Service
public class DltHistoryAward extends RedBlueGameHistoryAward {
    @Autowired
    private RedisService redisService;

    @Override
    public int getTotalBlueCount() {
        return DltGameEnum.DLT_BACK_NUMBERS.size();
    }

    @Override
    public int[][] getLevelInfos() {
        return DltGameEnum.LEVEL_INFO;
    }

    @Override
    public int getBetRedBallNum() {
        return DltGameEnum.FRONT_BALL_NUM;
    }

    @Override
    public int getBetBlueBallNum() {
        return DltGameEnum.BACK_BALL_NUM;
    }

    @Override
    public boolean noBingGo(int matchRedNumber, int matchBlueNumber) {
        //铁定不中奖的情况
        if ((matchBlueNumber == 0 && matchRedNumber < 3) || (matchBlueNumber == 1 && matchRedNumber < 2)) {
            return true;
        }
        return false;
    }

    @Override
    public int getHistoryAwardDetailDisplayNum() {
        return IniCache.getIniIntValue(IniConstant.HISTORY_AWARD_DISPLAY_NUM + GameConstant.DLT, 10); //默认只展示近10条中奖明细
    }

    @Override
    public Game getGame() {
        return GameCache.getGame(GameConstant.DLT);
    }
}
