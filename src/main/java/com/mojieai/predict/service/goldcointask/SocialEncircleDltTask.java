package com.mojieai.predict.service.goldcointask;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.GameConstant;
import com.mojieai.predict.constant.SocialEncircleKillConstant;
import com.mojieai.predict.entity.bo.GoldTask;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.vo.GoldCoinTaskVo;
import com.mojieai.predict.enums.GoldCoinTaskEnum;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.service.UserSocialTaskAwardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SocialEncircleDltTask extends AbstractTask {
    @Autowired
    private UserSocialTaskAwardService userSocialTaskAwardService;

    @Override
    public GoldCoinTaskVo getGoldCoinTaskVo(Long userId, GoldCoinTaskEnum goldCoinTaskEnum) {
        Game game = GameCache.getGame(GameConstant.DLT);
        GoldCoinTaskVo result = new GoldCoinTaskVo();
        GamePeriod currentPeriod = PeriodRedis.getCurrentPeriod(game.getGameId());
        GoldTask encircleAward = getTaskAwardByType(goldCoinTaskEnum.getTaskType());

        Integer taskStatus = userSocialTaskAwardService.checkUserFinishTask(currentPeriod.getGameId(), currentPeriod
                .getPeriodId(), userId, Integer.valueOf(goldCoinTaskEnum.getTaskType())) ? 1 : 0;

        result.setTaskName(encircleAward.getTaskTimes() + "次围号(" + game.getGameName() + ")");
        result.setTaskDate(currentPeriod.getPeriodId() + "期");
        result.setTaskType(goldCoinTaskEnum.getTaskType());
        result.setTaskAward("+" + encircleAward.getTaskAward() + "金币");
        result.setTaskStatus(taskStatus);
        result.setTaskStatusText(taskStatus == 1 ? "已完成" : "去围号");
        return result;
    }
}
