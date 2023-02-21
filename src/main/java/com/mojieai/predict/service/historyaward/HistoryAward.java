package com.mojieai.predict.service.historyaward;

import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.GamePeriod;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class HistoryAward {
    protected final Logger log = LogConstant.commonLog;

    /**
     * 单式复式胆拖玩法,计算所有投注号码的历史中奖信息。计算中奖次数，而不是中奖注数
     * 如果号码都是单式玩法，中奖次数=中奖注数
     *
     * @param lotteryNumber 多行号码，以逗号分隔。去页面投注下就知道了
     * @param periodNum     查询近多少期
     */
    public abstract Map<String, Object[]> getAllAwardRecords(String lotteryNumber, int periodNum);


    public abstract List<Map<String, String>> getNumberProperties(String lotteryNumber, String gameEn);

    public abstract Map<String, HashMap> buildLastNumberBehave(String gameEn);

    public abstract String getShowText(GamePeriod period);

    public abstract String getAwardTitle(String gameEn);

    public abstract List<Integer> getOmitNumber(GamePeriod period, String key);

    public abstract List<Map<String, String>> getLastNumberBehave(String lotteryNumber, Integer ifValidType, String
            gameEn);

    public Map<String, Object> getHistoryAwardContent(String gameEn, String lotteryNumber, int limit) {
        throw new AbstractMethodError("no getHistoryAwardContent method " + gameEn);
    }

    public List<Map<String, Object>> getNumHistoryOmit(String lotteryNumber, String gameEn) {
        throw new AbstractMethodError("no getNumHistoryOmit method " + gameEn);
    }

    public Map<String, HashMap> buildHistoryOmit(Game game) {
        throw new AbstractMethodError("no buildHistoryOmit method " + game.getGameId());
    }

    /**
     * 号码位图法表示
     */
    protected long generateByBalls(String[] balls) {
        long result = 0l;
        for (String ball : balls) {
            result |= (1l << (Integer.parseInt(ball) - 1));
        }
        return result;
    }

    protected int calOneNumber(long num) {
        int count = 0;
        while (num != 0) {
            count++;
            num &= (num - 1);
        }
        return count;
    }
}
