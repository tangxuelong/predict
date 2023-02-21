package com.mojieai.predict.service.goldcointask;

import com.mojieai.predict.constant.SocialEncircleKillConstant;
import com.mojieai.predict.dao.UserSocialTaskAwardDao;
import com.mojieai.predict.entity.bo.GoldTask;
import com.mojieai.predict.entity.po.UserSocialTaskAward;
import com.mojieai.predict.entity.vo.GoldCoinTaskVo;
import com.mojieai.predict.enums.GoldCoinTaskEnum;
import com.mojieai.predict.service.UserBuyRecommendService;
import com.mojieai.predict.thread.SocialTaskAwardTask;
import com.mojieai.predict.thread.ThreadPool;
import com.mojieai.predict.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

@Component
public class PurchaseSportsRecommendTask extends AbstractTask {
    @Autowired
    private UserBuyRecommendService userBuyRecommendService;

    @Override
    public GoldCoinTaskVo getGoldCoinTaskVo(Long userId, GoldCoinTaskEnum goldCoinTaskEnum) {
        GoldCoinTaskVo result = new GoldCoinTaskVo();
        GoldTask taskAward = getTaskAwardByType(goldCoinTaskEnum.getTaskType());
        Boolean purchaseStatus = userBuyRecommendService.checkUserPurchaseTaskStatus(userId, taskAward.getTaskTimes()
                , DateUtil.getCurrentTimestamp());
        result.setTaskName("买" + taskAward.getTaskTimes() + "单足彩预测");
        result.setTaskDate("");
        result.setTaskType(goldCoinTaskEnum.getTaskType());
        result.setTaskAward("+" + taskAward.getTaskAward() + "金币");
        result.setTaskStatus(purchaseStatus ? 1 : 0);
        result.setTaskStatusText(purchaseStatus ? "已完成" : "去购买");
        return result;
    }

    @Override
    public Boolean recordSocialTask(Long userId, GoldCoinTaskEnum goldCoinTaskEnum, String clientIp, Integer clientId) {
        return recordSportsSocialTask(userId, goldCoinTaskEnum, clientIp, clientId);
    }
}
