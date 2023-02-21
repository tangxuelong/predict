package com.mojieai.predict.service.goldcointask;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.dao.UserAccountFlowDao;
import com.mojieai.predict.entity.bo.GoldTask;
import com.mojieai.predict.entity.po.UserAccountFlow;
import com.mojieai.predict.entity.vo.GoldCoinTaskVo;
import com.mojieai.predict.entity.vo.UserLoginVo;
import com.mojieai.predict.enums.GoldCoinTaskEnum;
import com.mojieai.predict.service.LoginService;
import com.mojieai.predict.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

@Component
public class UpHeadImgTask extends AbstractTask {
    @Autowired
    private UserAccountFlowDao userAccountFlowDao;
    @Autowired
    private LoginService loginService;

    @Override
    public GoldCoinTaskVo getGoldCoinTaskVo(Long userId, GoldCoinTaskEnum goldCoinTaskEnum) {
        GoldCoinTaskVo result = new GoldCoinTaskVo();

        GoldTask task = getTaskAwardByType(goldCoinTaskEnum.getTaskType());
        result.setTaskAward("+" + task.getTaskAward() + "金币");

        Timestamp taskDate = DateUtil.getCurrentTimestamp();
        String payId = userId + goldCoinTaskEnum.getTaskEn() + goldCoinTaskEnum.getTaskType();
        UserAccountFlow userAccountFlow = userAccountFlowDao.getUserFlowCheck(payId, CommonConstant
                .PAY_TYPE_GOLD_COIN, userId, false);
        if (userAccountFlow != null && userAccountFlow.getCreateTime() != null) {
            taskDate = userAccountFlow.getCreateTime();
        }
        result.setTaskDate(DateUtil.formatDate(taskDate, DateUtil.formatTab[DateUtil.FMT_DATE_YYYYMMDD]));

        UserLoginVo userLoginVo = loginService.getUserLoginVo(userId);
        boolean taskStatus = false;
        if (userLoginVo != null && StringUtils.isNotBlank(userLoginVo.getHeadImgUrl())) {
            taskStatus = !userLoginVo.getHeadImgUrl().equals(CommonConstant.DEFAULT_HEAD_IMG_URL);
        }

        result.setTaskStatus(taskStatus ? 1 : 0);
        result.setTaskStatusText(taskStatus ? "已完善" : "去完善");
        result.setTaskName("上传头像");
        result.setTaskType(goldCoinTaskEnum.getTaskType());
        return result;
    }
}
