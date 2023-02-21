package com.mojieai.predict.service.impl;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.dao.UserAccountFlowDao;
import com.mojieai.predict.entity.bo.GoldTask;
import com.mojieai.predict.entity.po.UserAccountFlow;
import com.mojieai.predict.service.GoldTaskAwardService;
import com.mojieai.predict.service.PayService;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class GoldTaskAwardServiceImpl implements GoldTaskAwardService {
    private static final Logger log = LogConstant.commonLog;

    @Autowired
    private PayService payService;
    @Autowired
    private UserAccountFlowDao userAccountFlowDao;

    @Override
    public void distributeAward(Long userId, String goldTaskType) {
        String payId = userId + GoldTask.getTaskEn(goldTaskType) + goldTaskType;
        //1.check follow是否已经发送
        UserAccountFlow userAccountFlow = userAccountFlowDao.getUserFlowCheck(payId, CommonConstant
                .PAY_TYPE_GOLD_COIN, userId, false);
        if (userAccountFlow != null && userAccountFlow.getStatus().equals(CommonConstant.PAY_STATUS_HANDLED)) {
            return;
        }

        //2.发放奖励
        GoldTask goldTask = payService.getTaskMap(goldTaskType);
        if (goldTask != null) {
            long totalAmount = goldTask.getTaskAward() == null ? 0 : goldTask.getTaskAward();
            try {
                String payDesc = GoldTask.getTaskName(goldTaskType);
                //2.1 通知派发奖励
                payService.fillAccount(userId, payId, totalAmount, CommonConstant.PAY_TYPE_GOLD_COIN, null,
                        totalAmount, payDesc, "", CommonConstant.CLIENT_TYPE_ANDRIOD);
            } catch (Exception e) {
                log.error("通知派发奖励异常" + userId, e);
            }
        }

    }
}
