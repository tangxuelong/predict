package com.mojieai.predict.service.historyaward;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.cache.IniCache;
import com.mojieai.predict.constant.GameConstant;
import com.mojieai.predict.constant.IniConstant;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.enums.SsqGameEnum;
import com.mojieai.predict.redis.base.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 双色球
 */
@Service
public class SsqHistoryAward extends RedBlueGameHistoryAward {
    @Autowired
    private RedisService redisService;

    @Override
    public int getTotalBlueCount() {
        return SsqGameEnum.SSQ_BLUE_NUMBERS.size();
    }

    @Override
    public int[][] getLevelInfos() {
        return SsqGameEnum.LEVEL_INFO;
    }

    @Override
    public int getBetRedBallNum() {
        return SsqGameEnum.RED_BALL_NUM;
    }

    @Override
    public int getBetBlueBallNum() {
        return SsqGameEnum.BLUE_BALL_NUM;
    }

    @Override
    public boolean noBingGo(int matchRedNumber, int matchBlueNumber) {
        if (matchBlueNumber == 0 && matchRedNumber < 4) { //铁定不中奖的情况
            return true;
        }
        return false;
    }

    @Override
    public int getHistoryAwardDetailDisplayNum() {
        return IniCache.getIniIntValue(IniConstant.HISTORY_AWARD_DISPLAY_NUM + GameConstant.SSQ, 10); //默认只展示近10条中奖明细
    }

    @Override
    public Game getGame() {
        return GameCache.getGame(GameConstant.SSQ);
    }
}
