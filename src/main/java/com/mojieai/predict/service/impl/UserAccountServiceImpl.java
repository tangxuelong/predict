package com.mojieai.predict.service.impl;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.ResultConstant;
import com.mojieai.predict.dao.UserAccountDao;
import com.mojieai.predict.dao.UserWisdomCoinFlowDao;
import com.mojieai.predict.dao.WisdomFlowsequenceIdSequenceDao;
import com.mojieai.predict.entity.dto.Result;
import com.mojieai.predict.entity.po.UserAccount;
import com.mojieai.predict.entity.po.UserWisdomCoinFlow;
import com.mojieai.predict.service.PayService;
import com.mojieai.predict.service.UserAccountService;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.UserAccountUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserAccountServiceImpl implements UserAccountService {
    @Autowired
    private UserAccountDao userAccountDao;
    @Autowired
    private WisdomFlowsequenceIdSequenceDao wisdomFlowseqIdSeqDao;
    @Autowired
    private UserWisdomCoinFlowDao userWisdomCoinFlowDao;
    @Autowired
    private PayService payService;

    @Override
    public boolean checkUserBalance(Long userId, Integer accountType, Long payAmount) {
        UserAccount userAccount = userAccountDao.getUserAccountBalance(userId, accountType, false);
        if (userAccount != null && userAccount.getAccountBalance() != null && userAccount.getAccountBalance() >=
                payAmount) {
            return true;
        }
        return false;
    }

    @Override
    public Map<String, Object> getUserWithdrawBalanceCenter(Long userId) {
        Map<String, Object> res = new HashMap<>();
        UserAccount userAccount = userAccountDao.getUserAccountBalance(userId, CommonConstant.ACCOUNT_TYPE_BALANCE,
                false);
        String balanceFront = "0";
        String balanceBack = ".0";
        if (userAccount != null) {
            Long balance = userAccount.getAccountBalance();
//            String balanceStr = CommonUtil.removeZeroAfterPoint(CommonUtil.convertFen2Yuan(balance).toString());
//            String[] balanceArr = balanceStr.split(CommonConstant.COMMON_DOT_STR);
//            if (balanceArr.length > 0) {
//                balanceFront = balanceArr[0];
//            }
//            balanceBack = "." + (balanceArr.length > 1 ? balanceArr[1] : "0");
            if (balance > 0l) {
                balanceFront = CommonUtil.convertFen2Yuan(balance).intValue() + "";
                balanceBack = "." + String.valueOf(balance / 10 % 10);
            }
        }

        res.put("balanceFront", balanceFront);
        res.put("balanceBack", balanceBack);
        res.put("weiXinCode", CommonConstant.PLATE_WEI_XIN_CODE);
        res.put("balanceDesc", "可提现金额(元)");
        res.put("withdrawStep1Ad", "1、添加客服微信号：zhihuicp");
        res.put("withdrawStep2Ad", "2、向客服提供您的注册手机号，通过短信验证后可继续提现。");
        res.put("withdrawStep3Ad", "3、说明您的提现金额（每次不小于100元），客服完成转账。");
        res.put("statement", " 1、成为智慧预测师可提成酬金的50%；<br>2、单笔提现金额限制最低100元，如有变动以本公告为准；" +
                "<br>3、智慧客服上班时间为周一到周五10:00-18:00。");

        return res;
    }

    @Override
    public Result fillWisdom2UserAccount(Long userId, Long wisdomAmount, Long amount, Integer exchangeType, String
            mobileOperate) {
        Result result = new Result(ResultConstant.ERROR + "", "");
        String exchangeName = UserAccountUtil.getWisdomExchangeTypeCn(exchangeType);
        UserWisdomCoinFlow userWisdomCoinFlow = new UserWisdomCoinFlow();
        String flowId = CommonUtil.generateStrId(userId, CommonConstant.WISDOM_COIN_FLOW_ID_SQE, wisdomFlowseqIdSeqDao);
        userWisdomCoinFlow.initUserWisdomCoinFlow(flowId, userId, exchangeName, exchangeType, amount, wisdomAmount,
                null);
        Integer insertRes = userWisdomCoinFlowDao.insert(userWisdomCoinFlow);
        if (insertRes <= 0) {
            result.setDesc("流水创建失败");
            return result;
        }
        Map<String, Object> res = payService.fillAccount(userId, userWisdomCoinFlow.getFlowId(), wisdomAmount,
                CommonConstant.PAY_TYPE_WISDOM_COIN, null, wisdomAmount, exchangeName,
                mobileOperate, null);
        if (res == null) {
            result.setDesc("充值账户失败");
            return result;
        }
        if (res.containsKey("payStatus")) {
            Integer payStatus = Integer.valueOf(res.get("payStatus").toString());
            if (payStatus.equals(1) || payStatus.equals(ResultConstant.SUCCESS) || payStatus.equals(ResultConstant
                    .REPEAT_CODE)) {
                result.setCode(ResultConstant.SUCCESS + "");
            }
        }
        return result;
    }
}
