package com.mojieai.predict.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.constant.ResultConstant;
import com.mojieai.predict.dao.CouponConfigDao;
import com.mojieai.predict.dao.MatchScheduleDao;
import com.mojieai.predict.dao.MissionDao;
import com.mojieai.predict.entity.po.CouponConfig;
import com.mojieai.predict.entity.po.MatchSchedule;
import com.mojieai.predict.entity.po.Mission;
import com.mojieai.predict.service.MissionService;
import com.mojieai.predict.service.UserBuyRecommendService;
import com.mojieai.predict.service.UserCouponService;
import com.mojieai.predict.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class MissionServiceImpl implements MissionService {

    @Autowired
    private MissionDao missionDao;
    @Autowired
    private MatchScheduleDao matchScheduleDao;
    @Autowired
    private UserBuyRecommendService userBuyRecommendService;
    @Autowired
    private UserCouponService userCouponService;
    @Autowired
    private CouponConfigDao couponConfigDao;

    @Override
    public void userRecommendProfit2CashAccountTiming() {
        List<MatchSchedule> matchSchedules = matchScheduleDao.getNeedDealBuyRecommendMatch();
        if (matchSchedules == null || matchSchedules.size() == 0) {
            return;
        }

        for (MatchSchedule matchSchedule : matchSchedules) {
            String classId = matchSchedule.getLotteryCode() + ":" + matchSchedule.getMatchId();

            List<Mission> res = missionDao.getSlaveMissionByClassId(classId, Mission.MISSION_TYPE_FOOTBALL_WITHDRAW,
                    Mission.MISSION_STATUS_INTI);
            //1.todo 多线程去处理
            for (Mission mission : res) {
                userBuyRecommendService.updatePurchaseRecommendAwardStatus(mission);
                userBuyRecommendService.footballMatchEndUpdateWithdrawStatus(mission);
            }
            //2.check 所有该比赛下的方案是否都处理完
            Integer count = missionDao.getCountByClassIdAndStatus(classId, Mission.MISSION_TYPE_FOOTBALL_WITHDRAW,
                    Mission.MISSION_STATUS_INTI);
            if (count == 0) {
                matchScheduleDao.updateMatchStatus(matchSchedule.getMatchId(), matchSchedule.getLotteryCode(),
                        "IF_PURCHASE_LOG", "PURCHASE_LOG_TIME");
            }
        }
    }

    @Override
    public void withdrawUserAmount2CashAccount() {
        //1.取得所有待提现到现今账户的mission
        List<MatchSchedule> matchSchedules = matchScheduleDao.getNeedDealWithdrawMatch();
        if (matchSchedules == null || matchSchedules.size() == 0) {
            return;
        }
        for (MatchSchedule matchSchedule : matchSchedules) {
            String classId = matchSchedule.getLotteryCode() + ":" + matchSchedule.getMatchId();

            List<Mission> res = missionDao.getSlaveMissionByClassId(classId, Mission.MISSION_TYPE_FOOTBALL_WITHDRAW,
                    Mission.MISSION_STATUS_WITHDRAW_CASH_WAITE);
            for (Mission mission : res) {
                userBuyRecommendService.transferAccount2UserByMission(mission);
            }
            //2.check 所有该比赛下的方案是否都处理完
            Integer count = missionDao.getCountByClassIdAndStatus(classId, Mission.MISSION_TYPE_FOOTBALL_WITHDRAW,
                    Mission.MISSION_STATUS_WITHDRAW_CASH_WAITE);
            if (count == 0) {
                matchScheduleDao.updateMatchStatus(matchSchedule.getMatchId(), matchSchedule.getLotteryCode(),
                        "IF_WITHDRAW", "WITHDRAW_TIME");
            }
        }
    }

    @Override
    public void withdrawMoneyRefund2User() {
        List<Long> missions = missionDao.getSlaveMissionIdsByTaskType(Mission.MISSION_TYPE_FOOTBALL_WITHDRAW,
                Mission.MISSION_STATUS_WITHDRAW_WAIT_REFUND);
        if (missions == null || missions.size() == 0) {
            return;
        }

        for (Long missionId : missions) {
            Mission mission = missionDao.getSlaveBakMissionById(missionId);
            userBuyRecommendService.canceledMatchRefund2User(mission);
        }
    }

    @Override
    public void asynDistributeCoupon2UserTiming() {
        List<Long> missions = missionDao.getSlaveMissionIdsByTaskType(Mission.MISSION_TYPE_DISTRIBUTE_COUPON, Mission
                .MISSION_STATUS_INTI);
        if (missions == null || missions.size() == 0) {
            return;
        }
        for (Long missionId : missions) {
            Mission mission = missionDao.getSlaveBakMissionById(missionId);
            if (mission.getStatus().equals(Mission.MISSION_STATUS_COUPON_DISTRIBUTE)) {
                continue;
            }
            if (StringUtils.isNotBlank(mission.getRemark())) {
                Map<String, Object> remarkMap = JSONObject.parseObject(mission.getRemark());
                Long userId = Long.valueOf(remarkMap.get("userId").toString());
                Long couponConfigId = Long.valueOf(remarkMap.get("couponConfigId").toString());
                Timestamp beginTime = DateUtil.formatString(remarkMap.get("beginTime").toString(), "yyyyMMdd");

                CouponConfig couponConfig = couponConfigDao.getCouponConfigById(couponConfigId);
                Map<String, Object> distributeRes = userCouponService.distributeCoupon2UserByConfig(userId, mission
                        .getKeyInfo(), beginTime, couponConfig);
                if (distributeRes.get("status").toString().equals(ResultConstant.COUPON_DISTRIBUTE_SUCCESS_STATUS)) {
                    missionDao.updateMissionStatus(mission.getMissionId(), Mission.MISSION_STATUS_COUPON_DISTRIBUTE,
                            Mission.MISSION_STATUS_INTI);
                }
            }

        }
    }
}
