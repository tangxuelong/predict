package com.mojieai.predict.service;

import com.mojieai.predict.entity.po.ActivityAwardLevel;
import com.mojieai.predict.entity.po.ActivityDateUserInfo;
import com.mojieai.predict.entity.po.ActivityUserInfo;
import com.mojieai.predict.entity.po.GamePeriod;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by tangxuelong on 2017/11/7.
 */
@Service
public interface ActivityBetService {
    // 投注
    Integer betMatchItem(Long userId, String matchId, String betItem, Integer betAmount, Integer betType, Integer isAward);

    // 预约世界杯
    void userAppointment(Long userId,Integer activityId);

    // 世界杯抽中奖品记录
    Integer confirmAwardGoods(Long userId,Integer activityId,Integer levelId,String uniqueId);
}
