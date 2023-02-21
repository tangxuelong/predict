package com.mojieai.predict.service.goldcointask;

import com.mojieai.predict.constant.SocialEncircleKillConstant;
import com.mojieai.predict.dao.UserSocialTaskAwardDao;
import com.mojieai.predict.entity.bo.GoldTask;
import com.mojieai.predict.entity.po.UserSocialTaskAward;
import com.mojieai.predict.entity.vo.GoldCoinTaskVo;
import com.mojieai.predict.enums.GoldCoinTaskEnum;
import com.mojieai.predict.service.UserSportSocialRecommendService;
import com.mojieai.predict.thread.GoldCoinTask;
import com.mojieai.predict.thread.SocialTaskAwardTask;
import com.mojieai.predict.thread.ThreadPool;
import com.mojieai.predict.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

@Component
public class AddSportsRecommendTask extends AbstractTask {
    @Autowired
    private UserSportSocialRecommendService userSportSocialRecommendService;

    @Override
    public GoldCoinTaskVo getGoldCoinTaskVo(Long userId, GoldCoinTaskEnum goldCoinTaskEnum) {
        GoldCoinTaskVo result = new GoldCoinTaskVo();
        GoldTask recommendAward = getTaskAwardByType(goldCoinTaskEnum.getTaskType());
        Boolean recommendStatus = userSportSocialRecommendService.checkUserRecommend(userId, recommendAward
                .getTaskTimes(), DateUtil.getCurrentTimestamp());
        result.setTaskName("发" + recommendAward.getTaskTimes() + "次足彩预测");
        result.setTaskDate("");
        result.setTaskType(goldCoinTaskEnum.getTaskType());
        result.setTaskAward("+" + recommendAward.getTaskAward() + "金币");
        result.setTaskStatus(recommendStatus ? 1 : 0);
        result.setTaskStatusText(recommendStatus ? "已完成" : "去推荐");
        return result;
    }

    @Override
    public Boolean recordSocialTask(Long userId, GoldCoinTaskEnum goldCoinTaskEnum, String clientIp, Integer clientId) {
        return recordSportsSocialTask(userId, goldCoinTaskEnum, clientIp, clientId);
    }
}
