package com.mojieai.predict.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.*;
import com.mojieai.predict.entity.bo.PaginationInfo;
import com.mojieai.predict.entity.bo.PaginationList;
import com.mojieai.predict.entity.bo.WithdrawMerchantBalanceResponse;
import com.mojieai.predict.entity.po.*;
import com.mojieai.predict.entity.vo.ResultVo;
import com.mojieai.predict.entity.vo.WithdrawStatusVo;
import com.mojieai.predict.enums.WithdrawEnum;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.PayService;
import com.mojieai.predict.service.UserWithdrawFlowService;
import com.mojieai.predict.service.beanself.BeanSelfAware;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.WithdrawDefrayUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.sql.Timestamp;
import java.util.*;


@Service
public class UserWithdrawFlowServiceImpl implements UserWithdrawFlowService, BeanSelfAware {
    private Logger log = LogConstant.commonLog;

    @Autowired
    private UserWithdrawIdSequenceDao userWithdrawIdSequenceDao;
    @Autowired
    private UserWithdrawFlowDao userWithdrawFlowDao;
    @Autowired
    private UserAccountDao userAccountDao;
    @Autowired
    private UserAccountFlowDao userAccountFlowDao;
    @Autowired
    private PayService payService;
    @Autowired
    private MissionDao missionDao;
    @Autowired
    private UserBankCardDao userBankCardDao;
    @Autowired
    private RedisService redisService;
    @Autowired
    private UserInfoDao userInfoDao;

    private UserWithdrawFlowService self;

    @Override
    public Map<String, Object> getUserWithdrawOrder(Long userId, Integer page) {
        Map<String, Object> result = new HashMap<>();
        TreeMap<Integer, Object> dateOrderMap = new TreeMap<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2.compareTo(o1);
            }
        });
        if (page == null) {
            page = 1;
        }
        Boolean hasNext = Boolean.FALSE;
        Integer pageCount = 20;
        PaginationList<UserWithdrawFlow> paginationList = userWithdrawFlowDao.getUserWithdrawFlow(userId, pageCount, page);
        if (paginationList != null) {
            PaginationInfo pageInfo = paginationList.getPaginationInfo();
            if (pageInfo.getTotalPage() != null && pageInfo.getCurrentPage() < pageInfo.getTotalPage()) {
                hasNext = Boolean.TRUE;
                page = pageInfo.getCurrentPage() + 1;
            }
            List<UserWithdrawFlow> userWithdrawFlows = paginationList.getList();
            if (userWithdrawFlows != null && userWithdrawFlows.size() > 0) {
                for (UserWithdrawFlow withdrawFlow : userWithdrawFlows) {
                    List<Map<String, Object>> dateOrder = null;
                    Integer createTime = Integer.valueOf(DateUtil.formatTime(withdrawFlow.getCreateTime(), "yyyyMMdd"));
                    if (dateOrderMap.containsKey(createTime)) {
                        dateOrder = (List<Map<String, Object>>) dateOrderMap.get(createTime);
                    } else {
                        dateOrder = new ArrayList<>();
                    }

                    Map<String, Object> temp = convertWithdrawFlow2Map(withdrawFlow);
                    if (temp != null) {
                        dateOrder.add(temp);
                        dateOrderMap.put(createTime, dateOrder);
                    }
                }
            }
        }

        List<Map<String, Object>> orderList = new ArrayList<>();
        for (Integer key : dateOrderMap.keySet()) {
            Map<String, Object> temp = new HashMap<>();
            temp.put("date", DateUtil.formatTime(DateUtil.formatString(key + "", "yyyyMMdd"), "yyyy年MM月dd日"));
            temp.put("orderes", dateOrderMap.get(key));
            orderList.add(temp);
        }

        result.put("page", page);
        result.put("hasNext", hasNext);
        result.put("orderList", orderList);
        return result;
    }

    @Override
    public Map<String, Object> getUserWithdrawDetail(String withdrawOrderId, Long userId) {
        Map<String, Object> result = new HashMap<>();
        UserWithdrawFlow withdrawFlow = userWithdrawFlowDao.getUserWithdrawFlowById(userId, withdrawOrderId, false);
        if (withdrawFlow == null || !withdrawFlow.getUserId().equals(userId)) {
            throw new BusinessException("流水记录不存在");
        }
        String failReason = "";
        if (withdrawFlow.getWithdrawStatus().equals(CommonConstant.WITHDRAW_STATUS_FAIL) && StringUtils.isNotBlank
                (withdrawFlow.getRemark())) {
            Map<String, Object> remarkMap = JSONObject.parseObject(withdrawFlow.getRemark(), HashMap.class);
            if (remarkMap != null && remarkMap.containsKey("failReason")) {
                failReason = remarkMap.get("failReason").toString();
            }
        }

        result.put("serviceCharge", "该笔手续费由我们承担");
        result.put("createTime", DateUtil.formatTime(DateUtil.getCurrentTimestamp(), "yyyy-MM-dd HH:mm:ss"));
        result.put("bankNo", CommonUtil.hiddenNum(withdrawFlow.getBankCard()));
        result.put("orderId", withdrawFlow.getWithdrawId());
        result.put("status", getUserWithdrawDetailStatus(withdrawFlow.getWithdrawStatus()));
        result.put("withdrawAmount", CommonUtil.convertFen2Yuan(withdrawFlow.getWithdrawAmount()).toString());
        result.put("failReason", failReason);
        return result;
    }

    @Override
    public Map<String, Object> getApplyWithdrawIndex(Long userId) {
        Map<String, Object> result = new HashMap<>();

        UserAccount userAccount = userAccountDao.getUserAccountBalance(userId, CommonConstant.ACCOUNT_TYPE_BALANCE,
                false);
        Long userBalance = 0l;
        if (userAccount != null && userAccount.getAccountBalance() != null) {
            userBalance = userAccount.getAccountBalance();
        }
        List<UserBankCard> bankCards = userBankCardDao.getUserAllBankCard(userId, CommonConstant.BANK_CARD_TYPE_DEBIT);
        List<Map<String, Object>> bankList = new ArrayList<>();

        if (bankCards != null && bankCards.size() > 0) {
            for (UserBankCard userBankCard : bankCards) {
                if (bankList.size() > 0) {
                    break;
                }
                Map<String, Object> temp = new HashMap<>();
                temp.put("bankId", userBankCard.getBankId());
                temp.put("bankCn", CommonUtil.packageBankName(userBankCard.getBankCn(), userBankCard.getBankCard()));
                temp.put("bankCardType", CommonUtil.getCardTypeCn(userBankCard.getCardType()));
                temp.put("bankImg", WithdrawDefrayUtil.getBankCardImg(userBankCard.getBankCn()));
                bankList.add(temp);
            }
        }
        UserInfo userInfo = userInfoDao.getUserInfo(userId);
        Integer hasWithdrawPwd = 0;
        if (userInfo != null && StringUtils.isNotBlank(userInfo.getRemark())) {
            Map<String, Object> remarkMap = JSONObject.parseObject(userInfo.getRemark(), HashMap.class);
            if (remarkMap.containsKey("withdrawPwd") && remarkMap.get("withdrawPwd") != null) {
                hasWithdrawPwd = 1;
            }
        }

        String minWithdrawMoney = ActivityIniCache.getActivityIniValue(ActivityIniConstant.MIN_WITHDRAW_MONEY, "100");

        result.put("maxWithdrawMoneyDesc", "本月还可提现：" + CommonUtil.convertFen2Yuan(getUserCurrentMonthMaxWithDrawMoney
                (userId)).toString());
        result.put("minWithdrawMoney", minWithdrawMoney);
        result.put("bankList", bankList);
        result.put("hasWithdrawPwd", hasWithdrawPwd);
        result.put("userBalance", CommonUtil.convertFen2Yuan(userBalance));
        result.put("withdrawDesc", "预计1～2个工作日到账");
        result.put("balanceDes", "帐户余额：" + CommonUtil.convertFen2Yuan(userBalance) + CommonConstant
                .CASH_MONETARY_UNIT_YUAN);
        return result;
    }

    @Override
    public List<Map<String, Object>> adminGetAllWaitConfirmWithdrawOrder() {
        List<Map<String, Object>> result = new ArrayList<>();

        List<UserWithdrawFlow> orders = userWithdrawFlowDao.getAllWithdrawOrderByStatusFromOtter(CommonConstant
                .WITHDRAW_STATUS_WAIT_CONFIRM);
        if (orders != null && orders.size() > 0) {
            for (UserWithdrawFlow flow : orders) {
                Map<String, Object> temp = new HashMap<>();
                temp.put("orderId", flow.getWithdrawId());
                temp.put("withdrawAmount", flow.getWithdrawAmount());
                temp.put("bankNum", flow.getBankCard());
                temp.put("createTime", flow.getCreateTime());
                result.add(temp);
            }
        }
        return result;
    }

    @Override
    public Long getUserCurrentMonthMaxWithDrawMoney(Long userId) {
        Long result = 0l;
        String maxWithdrawAmount = ActivityIniCache.getActivityIniValue(ActivityIniConstant
                .MANUAL_MAX_WITHDRAW_AMOUNT, "1000");
        if (StringUtils.isBlank(maxWithdrawAmount)) {
            log.error("未设置当月最大提现额度");
            return 0l;
        }
        result = CommonUtil.convertYuan2Fen(maxWithdrawAmount).longValue();
        Timestamp beginTime = DateUtil.getBeginOfCurrentMonth();
        Timestamp endTime = DateUtil.getBeginOfNextMonth();
        Long userWithdrawMoney = userWithdrawFlowDao.getUserWithdrawSumByTime(userId, beginTime, endTime);
        if (userWithdrawMoney != null) {
            result = result - userWithdrawMoney;
        }
        return result;
    }

    // 0 申请提现 1:审核中 2 银行处理 4一到帐  5 失败
    private List<WithdrawStatusVo> getUserWithdrawDetailStatus(Integer withdrawStatus) {
        List<WithdrawStatusVo> withdrawStatusVos = new ArrayList<>();
        WithdrawStatusVo initStatus = new WithdrawStatusVo("申请提现", 1, 1);
        WithdrawStatusVo checkStatus = new WithdrawStatusVo("审核中", 0, 0);
        WithdrawStatusVo bankDealStatus = new WithdrawStatusVo("银行处理", 0, 0);
        WithdrawStatusVo successStatus = new WithdrawStatusVo("已到账", 0, -1);
        WithdrawStatusVo failStatus = new WithdrawStatusVo("提现失败", 2, -1);

        withdrawStatusVos.add(initStatus);
        withdrawStatusVos.add(checkStatus);
        withdrawStatusVos.add(bankDealStatus);
        withdrawStatusVos.add(successStatus);
        if (withdrawStatus.equals(CommonConstant.WITHDRAW_STATUS_INIT) || withdrawStatus.equals(CommonConstant
                .WITHDRAW_STATUS_WAIT_CONFIRM) || withdrawStatus.equals(CommonConstant
                .WITHDRAW_STATUS_CONFIRM_THROUGH)) {
            checkStatus.setStatus(1);
            checkStatus.setLineStatus(1);
        } else if (withdrawStatus.equals(CommonConstant.WITHDRAW_STATUS_ACCEPT)) {
            checkStatus.setStatus(1);
            checkStatus.setLineStatus(1);
            bankDealStatus.setStatus(1);
            bankDealStatus.setLineStatus(1);
        } else if (withdrawStatus.equals(CommonConstant.WITHDRAW_STATUS_FAIL)) {
            withdrawStatusVos.remove(bankDealStatus);
            withdrawStatusVos.remove(successStatus);
            checkStatus.setStatus(1);
            checkStatus.setLineStatus(1);
            withdrawStatusVos.add(failStatus);
        } else if (withdrawStatus.equals(CommonConstant.WITHDRAW_STATUS_FINISH)) {
            checkStatus.setStatus(1);
            checkStatus.setLineStatus(1);
            bankDealStatus.setStatus(1);
            bankDealStatus.setLineStatus(1);
            successStatus.setStatus(1);
        }

        return withdrawStatusVos;
    }

    @Override
    public Map<String, Object> createWithdrawOrder(Long userId, Integer bankId, Long withdrawAmount, Integer clientId, String
            clientIp, String password) {
        Map<String, Object> result = new HashMap<>();
        Integer code = ResultConstant.ERROR;
        String msg = "";
        result.put("code", code);
        result.put("actionType", 0);

        UserInfo userInfo = userInfoDao.getUserInfo(userId);
        Map<String, Object> remarkMap = JSONObject.parseObject(userInfo.getRemark(), HashMap.class);
        String key = RedisConstant.getUserWithdrawTimes(DateUtil.getCurrentDay(), userId);
        String oldTimes = redisService.get(key);
        if (StringUtils.isNotBlank(oldTimes) && Integer.valueOf(oldTimes) >= 5) {
            msg = "您已输入错5次密码，请24小时后再尝试或寻求客服帮助<font color='#3C72C4'>在线客服</font>";
            result.put("actionType", 1);
            result.put("msg", msg);
            return result;
        }
        if (remarkMap.containsKey("withdrawPwd") && StringUtils.isNotBlank(remarkMap.get("withdrawPwd").toString())) {
            if (!remarkMap.get("withdrawPwd").equals(password)) {
                Long times = redisService.incr(key);
                redisService.expire(key, 172800);
                long rightTimes = 5l - times;
                if (times >= 5) {
                    msg = "您已输入错5次密码，请24小时后再尝试或寻求客服帮助<font color='#3C72C4'>在线客服</font>";
                    result.put("actionType", 1);
                    result.put("msg", msg);
                    return result;
                }
                rightTimes = rightTimes > 0l ? rightTimes : 0l;
                result.put("msg", "请输入正确密码，您还有" + rightTimes + "次输入密码机会");
                return result;
            }
            if (StringUtils.isNotBlank(oldTimes) && Integer.valueOf(oldTimes) > 0) {
                redisService.del(key);
            }
        }

        UserAccount userAccount = userAccountDao.getUserAccountBalance(userId, CommonConstant.ACCOUNT_TYPE_BALANCE,
                false);
        if (userAccount == null || userAccount.getAccountBalance() == null || userAccount.getAccountBalance() < withdrawAmount) {
            result.put("msg", "余额不足");
            return result;
        }

        if (!checkWithdrawBalance(withdrawAmount)) {
            log.error("京东提现不足，请及时处理");
            result.put("msg", "请稍后重试");
            return result;
        }

        UserBankCard userBankCard = userBankCardDao.getUserBankCardById(userId, bankId);
        if (userBankCard == null || !userBankCard.getCardType().equals(CommonConstant.BANK_CARD_TYPE_DEBIT)) {
            result.put("msg", "请绑定提现银行卡");
            return result;
        }

        UserWithdrawFlow userWithdrawFlow = initUserWithdrawFlow(userId, userBankCard, withdrawAmount);
        if (userWithdrawFlow == null) {
            result.put("msg", "暂不支持此银行卡，请参考所支持银行卡列表");
            return result;
        }

        UserAccountFlow userAccountFlow = new UserAccountFlow(payService.generateFlowId(userId), userId,
                userWithdrawFlow.getWithdrawId(), withdrawAmount, CommonConstant.PAY_TYPE_BALANCE, null,
                withdrawAmount, "提现", clientIp, clientId, null, CommonConstant.PAY_OPERATE_TYPE_DEC);

        try {
            ResultVo resultVo = self.createWithdrawOrderAndDescAccount(userId, userWithdrawFlow, userAccountFlow);
            result.put("code", resultVo.getCode());
            result.put("msg", resultVo.getMsg());
        } catch (BusinessException e) {
            log.error(e);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    private boolean checkWithdrawBalance(Long withdrawAmount) {
        String key = RedisConstant.getWithdrawMerchantBalance(WithdrawEnum.JING_DONG_WITHDRAW.getCode());
        Long balance = redisService.kryoGet(key, Long.class);
        if (balance == null) {
            WithdrawMerchantBalanceResponse balanceResponse = WithdrawEnum.JING_DONG_WITHDRAW.queryBalance();
            if (balanceResponse.getCode().equals(ResultConstant.SUCCESS)) {
                balance = refreshWithdrawBalanceRedis(balanceResponse, WithdrawEnum.JING_DONG_WITHDRAW.getCode());
            } else {
                balance = 0l;
            }
        }
        return balance > withdrawAmount;
    }

    private UserWithdrawFlow initUserWithdrawFlow(Long userId, UserBankCard userBankCard, Long withdrawAmount) {
        if (userBankCard == null || StringUtils.isBlank(userBankCard.getBankCard()) || withdrawAmount == null ||
                withdrawAmount == 0) {
            return null;
        }
        //目前只有京东代付所以必需要有remark 找银行简码
        String bankRemark = userBankCard.getRemark();
        if (StringUtils.isBlank(bankRemark)) {
            return null;
        }
        Map<String, Object> bankRemarkMap = JSONObject.parseObject(bankRemark, HashMap.class);
        Map<String, Object> remarkMap = new HashMap<>();
        remarkMap.put("jdBankCardEn", bankRemarkMap.get("jdBankCardEn"));

        UserWithdrawFlow userWithdrawFlow = new UserWithdrawFlow();
        userWithdrawFlow.setBankCard(userBankCard.getBankCard());
        userWithdrawFlow.setServiceCharge(null);
        userWithdrawFlow.setUserId(userId);
        userWithdrawFlow.setUserName(userBankCard.getAccountName());
        userWithdrawFlow.setWithdrawAmount(withdrawAmount);
        userWithdrawFlow.setWithdrawId(CommonUtil.generateStrId(userId, "WITHDRAW", userWithdrawIdSequenceDao));
        userWithdrawFlow.setRemark(JSONObject.toJSONString(remarkMap));

        String maxWithdrawAmount = ActivityIniCache.getActivityIniValue(ActivityIniConstant
                .MANUAL_CONFIRM_WITHDRAW_AMOUNT_UPPER_LIMIT, "1000");
        Integer withdrawStatus = CommonConstant.WITHDRAW_STATUS_INIT;
        if (maxWithdrawAmount != null && CommonUtil.multiply(maxWithdrawAmount, "100").longValue() <= withdrawAmount) {
            withdrawStatus = CommonConstant.WITHDRAW_STATUS_WAIT_CONFIRM;
        }

        userWithdrawFlow.setWithdrawStatus(withdrawStatus);
        return userWithdrawFlow;
    }

    @Transactional
    @Override
    public ResultVo createWithdrawOrderAndDescAccount(Long userId, UserWithdrawFlow userWithdrawFlow, UserAccountFlow
            userAccountFlow) {
        ResultVo resultVo = new ResultVo(ResultConstant.ERROR, "");
        UserAccount userAccount = userAccountDao.getUserAccountBalance(userId, CommonConstant.ACCOUNT_TYPE_BALANCE,
                true);
        if (userAccount == null || userAccount.getAccountBalance() == null || userWithdrawFlow.getWithdrawAmount() ==
                null || userAccount.getAccountBalance() < userWithdrawFlow.getWithdrawAmount()) {
            resultVo.setMsg("余额不足");
            return resultVo;
        }
        Long oldBalance = userAccount.getAccountBalance();
        Long newBalance = oldBalance - userWithdrawFlow.getWithdrawAmount();
        Integer updateBalanceRes = userAccountDao.updateUserBalance(userId, CommonConstant.ACCOUNT_TYPE_BALANCE,
                newBalance, oldBalance);
        if (updateBalanceRes <= 0) {
            log.error("更新用余额异常:" + userId);
            throw new BusinessException("扣款异常，请重试");
        }

        if (userAccountFlowDao.insert(userAccountFlow) <= 0) {
            log.error("新增提现流水异常" + userId);
            throw new BusinessException("扣款异常，请重试");
        }

        if (userWithdrawFlowDao.insert(userWithdrawFlow) <= 0) {
            log.error("新增提现记录异常:" + userId);
            throw new BusinessException("扣款异常，请重试");
        }

        if (userWithdrawFlow.getWithdrawStatus().equals(CommonConstant.WITHDRAW_STATUS_INIT)) {
            Mission mission = new Mission(userWithdrawFlow.getWithdrawId(), Mission.MISSION_TYPE_BANK_WITHDRAW,
                    Mission.MISSION_STATUS_INTI, DateUtil.getCurrentTimestamp(), null, null);
            if (missionDao.insert(mission) <= 0) {
                log.error("新增Mission记录异常:" + userId);
                throw new BusinessException("扣款异常，请重试");
            }
        }

        resultVo.setCode(ResultConstant.SUCCESS);
        resultVo.setMsg("申请成功");
        return resultVo;
    }

    @Override
    public ResultVo threePartyWithdrawSuccessHandler(String withdrawFlowId) {
        ResultVo resultVo = new ResultVo(ResultConstant.ERROR);
        Long userIdSuffix = CommonUtil.getUserIdSuffix(withdrawFlowId);
        UserWithdrawFlow userWithdrawFlow = userWithdrawFlowDao.getUserWithdrawFlowById(userIdSuffix, withdrawFlowId, false);
        if (userWithdrawFlow == null) {
            log.error("提现订单不存在:" + withdrawFlowId + " 请核实");
            resultVo.setMsg("订单不存在");
            return resultVo;
        }
        Integer status = userWithdrawFlow.getWithdrawStatus();
        if (status.equals(CommonConstant.WITHDRAW_STATUS_FINISH) || status.equals(CommonConstant.WITHDRAW_STATUS_FAIL)) {
            resultVo.setCode(ResultConstant.SUCCESS);
            return resultVo;
        }

        UserAccountFlow accountFlow = userAccountFlowDao.getUserFlowCheck(withdrawFlowId, CommonConstant
                .ACCOUNT_TYPE_BALANCE, userWithdrawFlow.getUserId(), false);
        if (accountFlow == null) {
            log.error("提现对应的account 流水不存在.payId" + withdrawFlowId);
            resultVo.setMsg("account flow 不存在");
            return resultVo;
        }

        if (status.equals(CommonConstant.WITHDRAW_STATUS_CONFIRM_THROUGH) || status.equals(CommonConstant
                .WITHDRAW_STATUS_INIT) || status.equals(CommonConstant.WITHDRAW_STATUS_ACCEPT)) {
            try {
                resultVo = self.withdrawSuccessHandler(userWithdrawFlow.getUserId(), withdrawFlowId);
            } catch (Exception e) {
                log.error("", e);
            }
        }
        return resultVo;
    }

    @Transactional
    @Override
    public ResultVo withdrawSuccessHandler(Long userId, String withdrawFlowId) {
        ResultVo resultVo = new ResultVo(ResultConstant.ERROR);
        UserWithdrawFlow withdrawFlow = userWithdrawFlowDao.getUserWithdrawFlowById(userId, withdrawFlowId, true);

        if (userWithdrawFlowDao.updateWithdrawFlowStatus(userId, withdrawFlowId, CommonConstant
                .WITHDRAW_STATUS_FINISH, withdrawFlow.getWithdrawStatus(), Boolean.TRUE) <= 0) {
            throw new BusinessException("更新提现流水失败.withdrawFlowId:" + withdrawFlowId);
        }
        UserAccountFlow accountFlow = userAccountFlowDao.getUserFlowCheck(withdrawFlowId, CommonConstant
                .ACCOUNT_TYPE_BALANCE, userId, false);
        if (userAccountFlowDao.updateFlowStatus(userId, accountFlow.getFlowId(), CommonConstant.PAY_STATUS_HANDLED,
                accountFlow.getStatus()) <= 0) {
            throw new BusinessException("提现更新帐户流水失败.flowId:" + accountFlow.getFlowId());
        }

        Mission mission = missionDao.getMissionByKeyInfo(withdrawFlowId, Mission.MISSION_TYPE_BANK_WITHDRAW);
        if (missionDao.updateMissionStatus(mission.getMissionId(), Mission.MISSION_STATUS_WITHDRAW_FINISH, mission
                .getStatus()) <= 0) {
            throw new BusinessException("更新提现mission表异常.missionId:" + mission.getMissionId());
        }
        resultVo.setCode(ResultConstant.SUCCESS);
        return resultVo;
    }

    @Override
    public ResultVo threePartyWithdrawErrorHandler(String withdrawFlowId, String errorMsg) {
        ResultVo resultVo = new ResultVo(ResultConstant.ERROR);
        Long userIdSuffix = CommonUtil.getUserIdSuffix(withdrawFlowId);
        UserWithdrawFlow userWithdrawFlow = userWithdrawFlowDao.getUserWithdrawFlowById(userIdSuffix, withdrawFlowId, false);
        if (userWithdrawFlow == null) {
            log.error("提现订单不存在:" + withdrawFlowId + " 请核实");
            resultVo.setMsg("订单不存在");
            return resultVo;
        }
        Integer status = userWithdrawFlow.getWithdrawStatus();
        if (status.equals(CommonConstant.WITHDRAW_STATUS_FINISH) || status.equals(CommonConstant.WITHDRAW_STATUS_FAIL)) {
            resultVo.setCode(ResultConstant.SUCCESS);
            return resultVo;
        }

        try {
            resultVo = self.withdrawErrorHandler(userWithdrawFlow.getUserId(), withdrawFlowId, errorMsg, true);
            log.info("");
        } catch (Exception e) {
            log.error("", e);
        }

        return resultVo;
    }

    @Transactional
    @Override
    public ResultVo withdrawErrorHandler(Long userId, String withdrawFlowId, String errorMsg, Boolean missionFlag) {
        ResultVo resultVo = new ResultVo(ResultConstant.ERROR);
        UserAccount userAccount = userAccountDao.getUserAccountBalance(userId, CommonConstant.ACCOUNT_TYPE_BALANCE,
                true);
        UserWithdrawFlow withdrawFlow = userWithdrawFlowDao.getUserWithdrawFlowById(userId, withdrawFlowId, false);
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("failReason", errorMsg);
        String remark = CommonUtil.appendKey2MapString(withdrawFlow.getRemark(), errorMap);
        if (userWithdrawFlowDao.updateWithdrawFlowStatusAndRemark(userId, withdrawFlowId, CommonConstant
                .WITHDRAW_STATUS_FAIL, withdrawFlow.getWithdrawStatus(), remark, Boolean.TRUE) <= 0) {
            throw new BusinessException("更新提现流水异常.withdrawId:" + withdrawFlowId);
        }
        Long oldBalance = userAccount.getAccountBalance() == null ? 0l : userAccount.getAccountBalance();
        Long newBalance = withdrawFlow.getWithdrawAmount() + oldBalance;
        if (userAccountDao.updateUserBalance(userId, CommonConstant.ACCOUNT_TYPE_BALANCE, newBalance, oldBalance) <= 0) {
            throw new BusinessException("回充提现金额异常.withdrawId:" + withdrawFlowId);
        }
        if (missionFlag) {
            Mission mission = missionDao.getMissionByKeyInfo(withdrawFlowId, Mission.MISSION_TYPE_BANK_WITHDRAW);
            if (mission != null && missionDao.updateMissionStatus(mission.getMissionId(), Mission
                    .MISSION_STATUS_WITHDRAW_FAIL, mission.getStatus()) <= 0) {
                throw new BusinessException("更新异常.withdrawId:" + withdrawFlowId);
            }
        }
        resultVo.setCode(ResultConstant.SUCCESS);
        return resultVo;
    }

    @Override
    public ResultVo adminManualConfirmLargeWithdrawOrder(String withdrawFlowId, Integer orderStatus, String
            failReason) {
        ResultVo resultVo = new ResultVo(ResultConstant.ERROR, "");
        Long userIdFix = CommonUtil.getUserIdSuffix(withdrawFlowId);
        UserWithdrawFlow withdrawFlow = userWithdrawFlowDao.getUserWithdrawFlowById(userIdFix, withdrawFlowId, false);
        if (withdrawFlow == null) {
            resultVo.setMsg("提现订单不存在");
            return resultVo;
        }
        if (!withdrawFlow.getWithdrawStatus().equals(CommonConstant.WITHDRAW_STATUS_WAIT_CONFIRM)) {
            resultVo.setMsg("订单已确认,或不需要确认");
            return resultVo;
        }
        String remark = null;
        if (orderStatus.equals(CommonConstant.WITHDRAW_STATUS_FAIL)) {
            Map<String, Object> errorMsg = new HashMap<>();
            errorMsg.put("failReason", failReason);
            remark = CommonUtil.appendKey2MapString(withdrawFlow.getRemark(), errorMsg);
            return withdrawErrorHandler(withdrawFlow.getUserId(), withdrawFlowId, failReason, false);
        }

        resultVo = self.confirmLargeWithdrawOrderAndAddMission(withdrawFlow.getUserId(), withdrawFlowId, orderStatus,
                remark);
        return resultVo;
    }

    @Transactional
    @Override
    public ResultVo confirmLargeWithdrawOrderAndAddMission(Long userId, String withdrawFlowId, Integer orderStatus,
                                                           String remark) {
        ResultVo resultVo = new ResultVo(ResultConstant.SUCCESS, "确认成功");
        if (orderStatus.equals(CommonConstant.WITHDRAW_STATUS_CONFIRM_THROUGH)) {
            remark = null;
        }
        Integer updateRes = userWithdrawFlowDao.updateWithdrawFlowStatusAndRemark(userId, withdrawFlowId, orderStatus,
                CommonConstant.WITHDRAW_STATUS_WAIT_CONFIRM, remark, Boolean.FALSE);
        if (updateRes <= 0) {
            log.error("人工确认大额提现异常" + userId + ":" + withdrawFlowId);
            throw new BusinessException("人工确认异常");
        }
        Mission mission = new Mission(withdrawFlowId, Mission.MISSION_TYPE_BANK_WITHDRAW, Mission
                .MISSION_STATUS_INTI, DateUtil.getCurrentTimestamp(), null, null);
        try {
            if (missionDao.insert(mission) <= 0) {
                log.error("新增Mission记录异常:" + userId);
                throw new BusinessException("扣款异常，请重试");
            }
        } catch (DuplicateKeyException e) {
        }
        return resultVo;
    }

    @Override
    public void thirdPartyPaymentTiming() {
        Integer thirdPartWithdrawSwitch = ActivityIniCache.getActivityIniIntValue(ActivityIniConstant
                .THIRD_PARTY_WITHDRAW_SWITCH, 1);
        if (thirdPartWithdrawSwitch.equals(0)) {
            return;
        }
        List<Long> missionIds = missionDao.getSlaveMissionIdsByTaskType(Mission.MISSION_TYPE_BANK_WITHDRAW, Mission
                .MISSION_STATUS_INTI);
        if (missionIds == null || missionIds.size() <= 0) {
            return;
        }
        for (Long missionId : missionIds) {
            Mission mission = missionDao.getSlaveBakMissionById(missionId);
            if (mission == null || !mission.getStatus().equals(Mission.MISSION_STATUS_INTI)) {
                continue;
            }
            Long userPrefix = CommonUtil.getUserIdSuffix(mission.getKeyInfo());
            UserWithdrawFlow userWithdrawFlow = userWithdrawFlowDao.getUserWithdrawFlowById(userPrefix, mission
                    .getKeyInfo(), false);
            if (userWithdrawFlow == null) {
                continue;
            }
            Integer withdrawStatus = userWithdrawFlow.getWithdrawStatus();
            if (!withdrawStatus.equals(CommonConstant.WITHDRAW_STATUS_INIT) && !withdrawStatus.equals(CommonConstant
                    .WITHDRAW_STATUS_CONFIRM_THROUGH)) {
                continue;
            }
            //todo 考虑多线程
            Map<String, Object> orderMap = WithdrawEnum.JING_DONG_WITHDRAW.createWithdrawOrder(userWithdrawFlow);
            if (orderMap != null && orderMap.containsKey("threePartyStatus")) {
                Integer threePartyStatus = Integer.valueOf(orderMap.get("threePartyStatus").toString());
                if (threePartyStatus.equals(CommonConstant.THREE_PARTY_WITHDRAW_ORDER_SUCCESS)) {
                    threePartyWithdrawSuccessHandler(mission.getKeyInfo());
                } else if (threePartyStatus.equals(CommonConstant.THREE_PARTY_WITHDRAW_ORDER_ACCEPT)) {
                    // 已受理 等待回调
                    missionDao.updateMissionStatus(mission.getMissionId(), Mission.MISSION_STATUS_WITHDRAW_ACCEPTED,
                            mission.getStatus());
                } else if (threePartyStatus.equals(CommonConstant.THREE_PARTY_WITHDRAW_ORDER_UNKNOWN)) {
                    Map<String, Object> queryRes = WithdrawEnum.JING_DONG_WITHDRAW.queryOrder(userWithdrawFlow);
                    Integer queryStatus = Integer.valueOf(queryRes.get("threePartyStatus").toString());
                    if (queryStatus.equals(CommonConstant.THREE_PARTY_WITHDRAW_ORDER_SUCCESS)) {
                        threePartyWithdrawSuccessHandler(mission.getKeyInfo());
                    } else if (queryStatus.equals(CommonConstant.THREE_PARTY_WITHDRAW_ORDER_FAIL)) {
                        Map<String, Object> resMap = (Map<String, Object>) queryRes.get("resMap");
                        String errorMsg = resMap.get("trade_respmsg").toString();
                        threePartyWithdrawErrorHandler(mission.getKeyInfo(), errorMsg);
                    }
                } else if (threePartyStatus.equals(CommonConstant.THREE_PARTY_WITHDRAW_ORDER_FAIL)) {
                    Map<String, Object> resMap = (Map<String, Object>) orderMap.get("resMap");
                    String errorMsg = resMap.get("trade_respmsg").toString();
                    threePartyWithdrawErrorHandler(mission.getKeyInfo(), errorMsg);
                } else if (threePartyStatus.equals(CommonConstant.THREE_PARTY_WITHDRAW_ORDER_PROCESSING)) {
                    //todo 处理中 不用操作
                }
            }
            log.info(JSONObject.toJSONString(orderMap));
        }
    }

    @Override
    public void monitorWithdrawBalanceTiming() {
        for (WithdrawEnum withdrawEnum : WithdrawEnum.values()) {
            WithdrawMerchantBalanceResponse balanceResponse = withdrawEnum.queryBalance();
            if (balanceResponse.getCode().equals(ResultConstant.SUCCESS)) {
                refreshWithdrawBalanceRedis(balanceResponse, withdrawEnum.getCode());
            }
        }
    }

    private Long refreshWithdrawBalanceRedis(WithdrawMerchantBalanceResponse balanceResponse, Integer withdrawCode) {
        String key = RedisConstant.getWithdrawMerchantBalance(withdrawCode);

        redisService.del(key);
        Long balance = balanceResponse.getMerchantBalance() == null ? 0l : balanceResponse.getMerchantBalance();
        redisService.kryoSetEx(key, 604800, balance);
        return balance;
    }

    private Map<String, Object> convertWithdrawFlow2Map(UserWithdrawFlow withdrawFlow) {
        Map<String, Object> result = new HashMap<>();

        String amountColor = "";
        String status = CommonUtil.packageColorHtmlTag2Str("审核中", CommonConstant.COMMON_COLOR_ORIGIN_1);
        if (withdrawFlow.getWithdrawStatus().equals(CommonConstant.WITHDRAW_STATUS_ACCEPT)) {
            status = CommonUtil.packageColorHtmlTag2Str("银行处理", CommonConstant.COMMON_COLOR_ORIGIN_1);
        } else if (withdrawFlow.getWithdrawStatus().equals(CommonConstant.WITHDRAW_STATUS_FINISH)) {
            status = "已到账";
        } else if (withdrawFlow.getWithdrawStatus().equals(CommonConstant.WITHDRAW_STATUS_FAIL)) {
            status = "订单失败";
            amountColor = "#999999";
        }
        String withdrawAmount = CommonUtil.convertFen2Yuan(withdrawFlow.getWithdrawAmount()) + CommonConstant
                .CASH_MONETARY_UNIT_YUAN;

        result.put("status", status);
        result.put("withdrawOrderId", withdrawFlow.getWithdrawId());
        result.put("createTime", DateUtil.formatTime(withdrawFlow.getCreateTime(), "HH时mm分"));
        result.put("amount", CommonUtil.packageColorHtmlTag2Str(withdrawAmount, amountColor));
        return result;
    }

    @Override
    public void setSelf(Object proxyBean) {
        self = (UserWithdrawFlowService) proxyBean;
    }
}
