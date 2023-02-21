package com.mojieai.predict.service.impl;

import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.*;
import com.mojieai.predict.entity.po.*;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.PayService;
import com.mojieai.predict.service.ProgramService;
import com.mojieai.predict.service.UserProgramService;
import com.mojieai.predict.service.VipMemberService;
import com.mojieai.predict.service.beanself.BeanSelfAware;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.ProgramUtil;
import com.mojieai.predict.util.TrendUtil;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserProgramServiceImpl implements UserProgramService, BeanSelfAware {
    private static final Logger log = LogConstant.commonLog;

    @Autowired
    private UserProgramDao userProgramDao;
    @Autowired
    private ProgramDao programDao;
    @Autowired
    private UserProgramIdSequenceDao userProgramIdSequenceDao;
    @Autowired
    private PayService payService;
    @Autowired
    private VipMemberService vipMemberService;
    @Autowired
    private UserAccountFlowDao userAccountFlowDao;
    @Autowired
    private RedisService redisService;
    @Autowired
    private ProgramService programService;
    @Autowired
    private WisdomFlowsequenceIdSequenceDao wisdomFlowseqIdSeqDao;
    @Autowired
    private UserWisdomCoinFlowDao userWisdomCoinFlowDao;
    @Autowired
    private MissionDao missionDao;

    private UserProgramService self;

    @Override
    public Map<String, Object> getUserProgram(long gameId, String lastPeriodId, Long userId) {
        boolean hasNext = false;
        Integer pageSize = 10;
        String maxPeriodId = lastPeriodId;
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> programAwardList = new ArrayList<>();
        List<String> periodIds = userProgramDao.getUserProgramPagePeriodId(gameId, lastPeriodId, userId, pageSize + 1);
        if (periodIds.size() > 0) {
            if (periodIds.size() > 10) {
                hasNext = true;
                lastPeriodId = periodIds.get(9);
            } else {
                lastPeriodId = periodIds.get(periodIds.size() - 1);
            }
            List<UserProgram> userPrograms = userProgramDao.getUserProgramsByLastPeriodId(gameId, maxPeriodId,
                    lastPeriodId, userId);
            Map<String, List<Map<String, Object>>> programMap = new HashMap<>();
            for (UserProgram userProgram : userPrograms) {
                List<Map<String, Object>> programDataList = null;
                if (programMap.containsKey(userProgram.getPeriodId())) {
                    programDataList = programMap.get(userProgram.getPeriodId());
                } else {
                    programDataList = new ArrayList<>();
                }
                Program program = programDao.getProgramById(userProgram.getProgramId(), false);
                if (program != null) {
                    Map<String, Object> tempMap = ProgramUtil.convertProgram2Map(program, userProgram.getIsReturnCoin
                            ());
                    if (tempMap != null && !tempMap.isEmpty()) {
                        programDataList.add(tempMap);
                    }
                    programMap.put(program.getPeriodId(), programDataList);
                }
            }

            //包装成list
            if (programMap.size() > 0) {
                for (Map.Entry<String, List<Map<String, Object>>> temp : programMap.entrySet()) {
                    Map<String, Object> programDateMap = new HashMap<>();
                    programDateMap.put("periodText", "双色球" + temp.getKey() + "期");
                    programDateMap.put("periodId", temp.getKey());
                    programDateMap.put("programDataList", temp.getValue());
                    programAwardList.add(programDateMap);
                }

                programAwardList.sort((p1, p2) -> Integer.valueOf(p2.get("periodId").toString()).compareTo(Integer
                        .valueOf(p1.get("periodId").toString())));
            }
        }

        result.put("hasNext", hasNext);
        result.put("lastPeriodId", lastPeriodId);
        result.put("programAwardList", programAwardList);
        return result;
    }

    @Override
    public Map<String, Object> cashPurchaseProgram(Long userId, String programId, Integer channelId, Integer bankId,
                                                   String clientIp, Integer clientId) {
        Map<String, Object> res = new HashMap<>();
        Program program = programDao.getProgramById(programId, false);
        long price = program.getPrice();
        if (vipMemberService.checkUserIsVip(userId, VipMemberConstant.VIP_MEMBER_TYPE_DIGIT)) {
            price = ProgramUtil.getVipPrice(program.getPrice(), program.getVipDiscount()).longValue();
            if (program.getVipPrice() != null) {
                price = program.getVipPrice();
            }
        }
        //1.购买方案
        UserProgram userProgram = produceUserProgram(userId, program, price);

        //2.支付
        String payDesc = ProgramUtil.getProgramTypeCn(program.getProgramType()) + "，智慧指数 " + program.getWisdomScore();
        Map<String, Object> payMap = payService.payCreateFlow(userId, userProgram.getUserProgramId(), price,
                CommonConstant.PAY_TYPE_CASH, channelId, price, payDesc, clientIp, clientId, CommonConstant
                        .PROGRAM_PURCHASE_CALL_BACK_METHOD, CommonConstant.PAY_OPERATE_TYPE_DEC, bankId);
        if (payMap != null) {
            Integer payCode = 0;
            if (payMap.containsKey("payStatus")) {
                payCode = Integer.valueOf(payMap.get("payStatus").toString());
            }
            if (payCode == ResultConstant.ERROR) {
                res.put("flag", -1);
                res.put("msg", payMap.get("payMsg"));
                return res;
            }
            res.put("flowId", payMap.get("flowId"));
            res.putAll((Map<? extends String, ?>) payMap.get("payForToken"));
            res.put("msg", "购买成功");
        }
        return res;
    }

    @Override
    public Map<String, Object> wisdomCoinPurchaseProgram(Long userId, String programId, Integer channelId) {
        Map<String, Object> res = new HashMap<>();
        res.put("flag", -1);
        Program program = programDao.getProgramById(programId, false);
        long price = program.getPrice();
        if (vipMemberService.checkUserIsVip(userId, VipMemberConstant.VIP_MEMBER_TYPE_DIGIT)) {
            price = ProgramUtil.getVipPrice(program.getPrice(), program.getVipDiscount()).longValue();
        }
        //1.初始化用户方案
        UserProgram userProgram = produceUserProgram(userId, program, price);
        if (userProgram.getIsPay().equals(CommonConstant.PROGRAM_IS_PAY_YES)) {
            res.put("flag", 0);
            res.put("msg", "方案已购买");
            return res;
        }

        // 2.支付
        String payDesc = ProgramUtil.getProgramTypeCn(program.getProgramType()) + "，智慧指数 " + program.getWisdomScore();
        Map payInfo = payService.payCreateFlow(userId, userProgram.getUserProgramId(), price, CommonConstant
                .ACCOUNT_TYPE_WISDOM_COIN, channelId, price, payDesc, null, null, null, CommonConstant
                .PAY_OPERATE_TYPE_DEC, null);
        // 3.支付失败
        Integer payCode = 0;
        if (payInfo.containsKey("payStatus")) {
            payCode = Integer.valueOf(payInfo.get("payStatus").toString());
        }
        if (payCode == ResultConstant.ERROR) {
            res.put("flag", ResultConstant.PAY_FAILED_CODE);
            res.put("msg", payInfo.get("payMsg"));
            return res;
        }
        // 4.支付成功 更新用户方案
        Boolean updateRes = self.updateUserProgramPayed(programId, userProgram.getUserProgramId());
        if (updateRes) {
            // 业务处理成功
            String flowId = String.valueOf(payInfo.get("flowId"));
            payService.handledFlow(flowId);
            saveUserPurchaseProgram2Redis(program.getGameId(), program.getPeriodId(), userId, programId);
            programService.rebuildSaleProgramList(program.getGameId(), program.getPeriodId(), program.getProgramType());
            res.put("flag", 0);
            res.put("msg", "方案购买成功");
        } else {
            res.put("msg", "方案购买失败");
        }
        return res;
    }

    @Override
    public UserProgram produceUserProgram(Long userId, Program program, Long payAmount) {
        //1.check流水是否已经存在
        UserProgram userProgram = userProgramDao.getUserProgramByProgramId(userId, program.getProgramId(), program
                .getPrice());
        if (userProgram != null) {
            return userProgram;
        }
        //2.创建新的
        userProgram = new UserProgram();
        String userProgramId = CommonUtil.generateStrId(userId, "USERPROGRAM", userProgramIdSequenceDao);
        userProgram.initUserProgram(userProgramId, userId, program, payAmount, program.getPrice());
        try {
            userProgramDao.insert(userProgram);
        } catch (DuplicateKeyException e) {
            userProgram = userProgramDao.getUserProgramByProgramId(userId, program.getProgramId(), program.getPrice());
        }
        return userProgram;
    }

    private void saveUserPurchaseProgram2Redis(long gameId, String periodId, Long userId, String programId) {
        try {
            GamePeriod period = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, periodId);
            String key = RedisConstant.getUserPurchaseProgramKey(gameId, periodId, userId, programId);
            int expireTime = TrendUtil.getExprieSecond(period.getAwardTime(), 100);
            redisService.kryoSetEx(key, expireTime, userId);
        } catch (Exception e) {
            log.error("refresh user purchase program error userId:" + userId + " programId:" + programId, e);
        }
    }

    @Override
    public Boolean callBackMakeProgramEffective(String userProgramId, String exchangeFlowId) {
        Boolean res = Boolean.FALSE;
        //1.check流水
        Long userIdSuffix = Long.valueOf(exchangeFlowId.substring(exchangeFlowId.length() - 2, exchangeFlowId.length
                ()));
        UserAccountFlow userAccountFlow = userAccountFlowDao.getUserFlowByShardType(exchangeFlowId, userIdSuffix,
                false);
        if (userAccountFlow == null || userAccountFlow.getStatus() != CommonConstant.PAY_STATUS_FINISH) {
            Integer status = userAccountFlow == null ? -1 : userAccountFlow.getStatus();
            log.error("流水id:" + exchangeFlowId + " 异常." + "流水状态为:" + status);
            return false;
        }
        //2.验证vip流水是否已经置位
        UserProgram userProgram = userProgramDao.getUserProgramByUserProgramId(userProgramId, false);
        if (userProgram == null) {
            log.error("异常方案购买回调 userProgramId:" + userProgramId + "payExchangeId:" + exchangeFlowId);
            return false;
        }
        //3.校验流水金额支付前后是否一样
        if (!userProgram.getPayPrice().equals(userAccountFlow.getPayAmount())) {
            log.error("方案订单金额不一致.userProgramId:" + userProgramId + " 金额为" + userProgram.getPayPrice() + " " +
                    "userAccountFlow:" + exchangeFlowId + " 金额为:" + userAccountFlow.getPayAmount());
            return false;
        }
        if (userProgram.getIsPay().equals(1)) {
            return true;
        }
        //4.更新用户方案状态
        res = self.updateUserProgramPayed(userProgram.getProgramId(), userProgram.getUserProgramId());
        if (res) {
            saveUserPurchaseProgram2Redis(userProgram.getGameId(), userProgram.getPeriodId(), userProgram.getUserId(),
                    userProgram.getProgramId());
            Program program = programDao.getProgramById(userProgram.getProgramId(), false);
            programService.rebuildSaleProgramList(userProgram.getGameId(), userProgram.getPeriodId(), program
                    .getProgramType());
        }
        return res;
    }

    @Transactional
    @Override
    public Boolean updateUserProgramPayed(String programId, String userProgramId) {
        Boolean res = Boolean.FALSE;
        Program program = programDao.getProgramById(programId, true);
        UserProgram userProgram = userProgramDao.getUserProgramByUserProgramId(userProgramId, false);
        if (userProgram.getIsPay() == 0) {
            Integer saleCount = program.getSaleCount() == null ? 1 : program.getSaleCount() + 1;
            program.setSaleCount(saleCount);
            int updateRes = programDao.update(program);
            if (updateRes > 0) {
                int updateUserPro = userProgramDao.updateUserProgramPayStatus(userProgramId, CommonConstant
                        .PROGRAM_IS_PAY_YES);
                if (updateUserPro > 0) {
                    res = Boolean.TRUE;
                }
            }
        }
        // 此处已经支付成功 插入方案赔付
        if (program.getBuyType().equals(CommonConstant.PROGRAM_BUY_TYPE_COMPENSATE)) {
            Mission mission = new Mission(userProgramId, Mission.MISSION_TYPE_REFUND, 0, DateUtil.getCurrentTimestamp
                    (), "", program.getProgramId());
            missionDao.insert(mission);
        }
        return res;
    }

    @Override
    public Boolean updateUserSubscribeInfoAfterPayed(Long userId, Program program, String userProgramId) {
        Boolean res = Boolean.FALSE;
        //1.更新用户购买db
        Boolean updateRes = self.updateUserProgramPayed(program.getProgramId(), userProgramId);
        if (updateRes) {
            //2.刷新各种缓存
            saveUserPurchaseProgram2Redis(program.getGameId(), program.getPeriodId(), userId, program.getProgramId());
            programService.rebuildSaleProgramList(program.getGameId(), program.getPeriodId(), program.getProgramType());
            res = Boolean.TRUE;
        }
        return res;
    }

    @Override
    public void refundWisdomCoin() {
        List<Long> missionList = missionDao.getSlaveMissionIdsByTaskType(Mission.MISSION_TYPE_REFUND, 0);
        if (null == missionList || missionList.size() <= 0) {
            return;
        }
        List<String> programIds = new ArrayList<>();
        for (Long missionId : missionList) {
            Mission mission = missionDao.getSlaveBakMissionById(missionId);
            UserProgram userProgram = userProgramDao.getUserProgramByUserProgramId(mission.getKeyInfo(), Boolean.FALSE);
            Program program = programDao.getProgramById(userProgram.getProgramId(), Boolean.FALSE);
            if (program.getRefundStatus().equals(CommonConstant.PROGRAM_IS_RETURN_COIN_WAIT)) {
                // 退款
                refundWisdomCoin2User(userProgram);
                // 更新mission
                missionDao.updateMissionStatus(missionId, 1, 0);
                programIds.add(program.getProgramId());
            }
            if (program.getRefundStatus().equals(CommonConstant.PROGRAM_IS_RETURN_COIN_NO)) {
                // 更新mission
                missionDao.updateMissionStatus(missionId, 2, 0);
            }
        }
        // 退智慧币已经在上面完成，这里只更新方案状态
        List<Long> missionListS = missionDao.getSlaveMissionIdsByTaskType(Mission.MISSION_TYPE_REFUND, 0);
        if (null != missionListS && missionListS.size() > 0) {
            return;
        }
        for (String programId : programIds) {
            //3.更新方案退款标志位
            Program program = programDao.getProgramById(programId, Boolean.FALSE);
            if (program.getRefundStatus().equals(CommonConstant.PROGRAM_IS_RETURN_COIN_WAIT)) {
                programDao.updateProgramRefundStatus(programId, CommonConstant.PROGRAM_IS_RETURN_COIN_YES);
            }
        }
        List<String> programIdByRefundType = programDao.getProgramIdByRefundType(CommonConstant
                .PROGRAM_IS_RETURN_COIN_WAIT);
        if (programIdByRefundType.size() > 0) {
            log.error("Mission退款完成，方案状态未更新完毕");
        }
    }

    @Override
    public void programRefundMonitor() {
        List<Long> missionList = missionDao.getMonitorRefundMission();
        if (null == missionList || missionList.size() <= 0) {
            return;
        }
        for (Long missionId : missionList) {
            Mission mission = missionDao.getSlaveBakMissionById(missionId);
            UserProgram userProgram = userProgramDao.getUserProgramByUserProgramId(mission.getKeyInfo(), Boolean.FALSE);
            GamePeriod gamePeriod = PeriodRedis.getPeriodByGameIdAndPeriod(userProgram.getGameId(), userProgram
                    .getPeriodId());
            //如果开奖内两小时继续监控
            if (DateUtil.getDiffSeconds(gamePeriod.getAwardTime(), DateUtil.getCurrentTimestamp()) < 7200) {
                continue;
            }
            if (userProgram.getIsReturnCoin().equals(CommonConstant.PROGRAM_IS_RETURN_COIN_INIT) || userProgram
                    .getIsReturnCoin().equals(CommonConstant.PROGRAM_IS_RETURN_COIN_WAIT)) {
                log.error("方案退款延时过长：方案Id:" + userProgram.getUserProgramId() + " userId:" + userProgram.getUserId());
            }
        }

    }

    private boolean refundWisdomCoin2User(UserProgram userProgram) {
        boolean res = false;
        //1.退款
        Map<String, Object> resMap = payService.fillAccount(userProgram.getUserId(), "REFUND" + userProgram
                        .getUserProgramId(), userProgram.getPayPrice(), CommonConstant.PAY_TYPE_WISDOM_COIN, null,
                userProgram.getPayPrice(), "方案赔付", null, null);
        if (resMap != null && resMap.containsKey("payStatus")) {
            Integer code = Integer.valueOf(resMap.get("payStatus").toString());
            if (code.equals(ResultConstant.REPEAT_CODE) || code.equals(ResultConstant.PAY_SUCCESS_CODE)) {
                //2.插入wisdom流水
                String flowId = CommonUtil.generateStrId(userProgram.getUserId(), CommonConstant
                        .WISDOM_COIN_FLOW_ID_SQE, wisdomFlowseqIdSeqDao);
                UserWisdomCoinFlow userWisdomCoinFlow = new UserWisdomCoinFlow();
                userWisdomCoinFlow.initUserWisdomCoinFlow(flowId, userProgram.getUserId(), "方案赔付",
                        UserAccountConstant.WISDOM_COIN_EXCHANGE_TYPE_PROGRAM_COMPENSATE, userProgram.getPayPrice(),
                        userProgram.getPayPrice(), null);
                int insertRes = 0;
                try {
                    insertRes = userWisdomCoinFlowDao.insert(userWisdomCoinFlow);
                } catch (DuplicateKeyException e) {
                    insertRes = 1;
                }

                //3.更新用户方案状态
                if (insertRes > 0) {
                    userProgramDao.updateUserProgramRefundStatus(userProgram.getUserProgramId(), CommonConstant
                            .PROGRAM_IS_RETURN_COIN_YES);
                    res = true;
                }
            }
        }
        return res;
    }

    @Override
    public void setSelf(Object proxyBean) {
        self = (UserProgramService) proxyBean;
    }
}
