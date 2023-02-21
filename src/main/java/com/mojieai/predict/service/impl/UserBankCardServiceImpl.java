package com.mojieai.predict.service.impl;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.ResultConstant;
import com.mojieai.predict.dao.SysCardBinDao;
import com.mojieai.predict.dao.UserBankCardDao;
import com.mojieai.predict.entity.po.SysCardBin;
import com.mojieai.predict.entity.po.UserBankCard;
import com.mojieai.predict.entity.vo.ResultVo;
import com.mojieai.predict.enums.CommonStatusEnum;
import com.mojieai.predict.service.UserBankCardService;
import com.mojieai.predict.util.JDDefray.JDDefrayCodeConst;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserBankCardServiceImpl implements UserBankCardService {
    protected Logger log = LogConstant.commonLog;

    @Autowired
    private UserBankCardDao userBankCardDao;
    @Autowired
    private SysCardBinDao sysCardBinDao;

    @Override
    public Boolean checkUserIfBankCard(Long userId) {
        Integer userBankCardCount = userBankCardDao.getUserBankCardCount(userId);
        if (userBankCardCount != null && userBankCardCount > 0) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public ResultVo addUserBankCardWithOutAuth(Long userId, String realName, String bankCardNo, String mobile) {
        ResultVo result = new ResultVo(ResultConstant.ERROR, "");
        SysCardBin sysCardBin = sysCardBinDao.getSysCardBinByBankCardPrefix(bankCardNo.substring(0, 6));
        String bankCn = "";
        if (sysCardBin != null) {
            if (!sysCardBin.getCardType().equals(CommonConstant.BANK_CARD_TYPE_DEBIT)) {
                result.setMsg("提现不支持信用卡");
                return result;
            }
            bankCn = sysCardBin.getIssuerBankName();
        }
        UserBankCard userBankCard = userBankCardDao.getUserBankCardByBankNo(userId, bankCardNo);
        if (userBankCard != null) {
            if (userBankCard.getStatus().equals(CommonStatusEnum.YES.getStatus())) {
                result.setMsg("已绑定该卡");
                return result;
            }
            int updateRes = userBankCardDao.updateBankCard(userId, userBankCard.getBankId(), realName,
                    CommonStatusEnum.YES.getStatus());
            if (updateRes > 0) {
                result.setCode(ResultConstant.SUCCESS);
                result.setMsg("绑卡成功");
            }
            return result;
        }
        String bankName = "";
        Integer cardType = CommonConstant.BANK_CARD_TYPE_DEBIT;
        if (sysCardBin != null) {
            cardType = sysCardBin.getCardType();
            bankName = StringUtils.isBlank(sysCardBin.getIssuerBankName()) ? "" : sysCardBin.getIssuerBankName();
        }

        String jdBankType = JDDefrayCodeConst.getBankCardType(bankName);
        userBankCard = new UserBankCard(bankCardNo, cardType, userId, realName, mobile, bankCn, "",
                null, jdBankType, 1);
        try {
            Integer res = userBankCardDao.insert(userBankCard);
            if (res > 0) {
                result.setCode(ResultConstant.SUCCESS);
            }
        } catch (Exception e) {
            log.error("绑卡异常", e);
            result.setMsg("绑卡失败");
        }
        return result;
    }

    @Override
    public ResultVo unbindUserBankCard(Long userId, Integer bankId) {
        ResultVo resultVo = new ResultVo(ResultConstant.ERROR);
        UserBankCard userBankCard = userBankCardDao.getUserBankCardById(userId, bankId);
        if (userBankCard == null) {
            resultVo.setMsg("卡信息不存在");
            return resultVo;
        }
        if (userBankCardDao.updateBankCardStatus(userId, bankId, CommonStatusEnum.NO.getStatus()) > 0) {
            resultVo.setCode(ResultConstant.SUCCESS);
            resultVo.setMsg("解绑成功");
        }

        return resultVo;
    }
}
