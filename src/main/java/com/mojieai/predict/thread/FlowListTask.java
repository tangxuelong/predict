package com.mojieai.predict.thread;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.entity.po.UserAccountFlow;
import com.mojieai.predict.enums.PayChannelEnum;
import com.mojieai.predict.service.PayService;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Callable;

/**
 * Created by tangxuelong on 2017/7/20.
 */
public class FlowListTask implements Callable {
    private static final Logger log = LogConstant.commonLog;

    private UserAccountFlow userAccountFlow;
    private PayService payService;

    public FlowListTask(UserAccountFlow userAccountFlow, PayService payService) {
        this.userAccountFlow = userAccountFlow;
        this.payService = payService;
    }

    @Override
    public Object call() throws Exception {
        try {
            // 主动查询
            if (userAccountFlow.getPayType().equals(CommonConstant.ACCOUNT_TYPE_CASH)) {
                payService.wxOrderQuery(userAccountFlow);
            }
        } catch (Exception e) {
            log.error("orderQuery error flowId:" + userAccountFlow.getFlowId());
        }
        return null;
    }
}
