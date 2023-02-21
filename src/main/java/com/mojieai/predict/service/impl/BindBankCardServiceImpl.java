package com.mojieai.predict.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.cache.BankCache;
import com.mojieai.predict.cache.BannerCache;
import com.mojieai.predict.cache.IniCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.SysCardBinDao;
import com.mojieai.predict.dao.UserBankCardDao;
import com.mojieai.predict.dao.UserInfoDao;
import com.mojieai.predict.entity.bo.BankInfo;
import com.mojieai.predict.entity.bo.RealNameInfo;
import com.mojieai.predict.entity.dto.HttpParamDto;
import com.mojieai.predict.entity.po.SysCardBin;
import com.mojieai.predict.entity.po.UserBankCard;
import com.mojieai.predict.entity.po.UserInfo;
import com.mojieai.predict.service.BindBankCardService;
import com.mojieai.predict.service.RongShuAutLogService;
import com.mojieai.predict.service.UserInfoService;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.HttpServiceUtils;
import com.mojieai.predict.util.JDDefray.JDDefrayCodeConst;
import com.mojieai.predict.util.WithdrawDefrayUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 榕树三方绑卡
 * 没有用这块
 */

@Service
public class BindBankCardServiceImpl implements BindBankCardService {
    protected Logger log = LogConstant.commonLog;

    @Autowired
    private UserInfoDao userInfoDao;
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private UserBankCardDao userBankCardDao;
    @Autowired
    private RongShuAutLogService rongShuAutLogService;
    @Autowired
    private SysCardBinDao sysCardBinDao;

    @Override
    public Map<String, Object> getBindBankCardDetail(Long userId) {
        UserInfo userInfo = userInfoDao.getUserInfo(userId);
        Map<String, Object> result = new HashMap<>();
        String realName = "";
        String idCard = "";
        Integer status = 0;
        if (userInfo != null) {
            RealNameInfo realNameInfo = CommonUtil.getUserRealNameInfo(userInfo.getRemark());
            if (realNameInfo != null) {
                status = 1;
                idCard = realNameInfo.getIdCard();
                realName = realNameInfo.getRealName();
            }
        }

        result.put("realName", realName);
        result.put("idCard", CommonUtil.hiddenNum(idCard));
        result.put("realNameAuthenticateStatus", status);
        return result;
    }

    @Override
    public Map<String, Object> userBindBankCard(Long userId, String userName, String idCard, String mobile,
                                                String bankNo) {
        Map<String, Object> result = new HashMap<>();
        Integer code = ResultConstant.ERROR;
        String msg = "";
        UserBankCard userBankCard = userBankCardDao.getUserBankCardByBankNo(userId, bankNo);
        if (userBankCard != null) {
            result.put("code", code);
            result.put("msg", "已绑定该卡");
            return result;
        }
        UserInfo userInfo = userInfoDao.getUserInfo(userId);
        Boolean realNameAuth = Boolean.FALSE;
        RealNameInfo realNameInfo = CommonUtil.getUserRealNameInfo(userInfo.getRemark());
        if (realNameInfo != null && StringUtils.isNotBlank(realNameInfo.getIdCard())) {
            realNameAuth = Boolean.TRUE;
            userName = realNameInfo.getRealName();
            idCard = realNameInfo.getIdCard();
        }

        //三方校验
        String rongShu = IniCache.getIniValue(IniConstant.RONG_SHU_ID_CHECK);
        if (StringUtils.isBlank(rongShu)) {
            log.error("榕树配置不存在，请及时审核");
            result.put("code", code);
            result.put("msg", "系统校验失败，请联系客服");
            return result;
        }
        Map<String, Object> rongShuMap = JSONObject.parseObject(rongShu, HashMap.class);
        if (rongShuMap != null) {
            String url = rongShuMap.get("autBankUrl").toString();
            String key = rongShuMap.get("key").toString();
            TreeMap<String, Object> params = new TreeMap<>();
            params.put("Appid", rongShuMap.get("appId").toString());
            params.put("Name", userName);
            params.put("IdCode", idCard);
            params.put("Mobile", mobile);
            params.put("CardNo", bankNo);
            params.put("Sign", CommonUtil.getRongShuSign(params, key));

            //{"Address":"","Birthday":"","Gender":"","IdCode":"130203197607154331","Id_":"","MobilePhone":null,"Name":"韩涵忍","Nationality":"","Oid":"","Photo":null,"ResultCode":"1002","ResultMsg":"库中无此号","Seqno":"","Ssssxq":"","TrainChecked":false,"TransactionID":"1cfeb122-a07b-11e8-9057-00163e2e29d0"}
            String idCheckRes = HttpServiceUtils.sendPostRequest(url, JSONObject.toJSONString(params), HttpParamDto
                            .DEFAULT_CHARSET, HttpParamDto.DEFAULT_CONNECT_TIME_OUT, HttpParamDto.DEFAULT_READ_TIME_OUT,
                    "application/json", true);
            if (StringUtils.isNotBlank(idCheckRes)) {
                Map<String, Object> userReal = new HashMap<>();
                Integer authenticateCode = null;
                log.info("绑卡校验：" + idCheckRes);
                Map<String, Object> checkResMap = JSONObject.parseObject(idCheckRes, HashMap.class);
                if (checkResMap.containsKey("ResultCode")) {
                    Integer resultCode = Integer.valueOf(checkResMap.get("ResultCode").toString());
                    if (resultCode.equals(CommonConstant.RONG_SHU_ID_CHECK_REFUSE) || resultCode.equals(CommonConstant
                            .RONG_SHU_ID_CHECK_ERROR)) {
                        result.put("code", code);
                        result.put("msg", "姓名或身份证号不正确");
                        return result;
                    }
                    authenticateCode = resultCode;
                }
                if (!realNameAuth) {
                    userReal.put("realName", userName);
                    userReal.put("idCard", idCard);
                    userReal.put("authenticateCode", authenticateCode);
                    Boolean saveUserRealNameRes = userInfoService.updateUserInfoRemark(userInfo.getRemark(),
                            CommonUtil.appendKey2MapString(userInfo.getRemark(), userReal), userId);
                    if (saveUserRealNameRes) {
                        realNameAuth = true;
                    }
                }
                if (realNameAuth) {
                    SysCardBin sysCardBin = sysCardBinDao.getSysCardBinByBankCardPrefix(bankNo.substring(0, 6));
                    String jdBankType = JDDefrayCodeConst.getBankCardType(sysCardBin.getCardName());
                    userBankCard = new UserBankCard(bankNo, sysCardBin.getCardType(), userId, userName, mobile, "",
                            authenticateCode + "", CommonConstant.BANK_AUTHENTICATE_RONG_SHU, jdBankType, 1);
                    try {
                        userBankCardDao.insert(userBankCard);
                        code = ResultConstant.SUCCESS;
                        msg = "绑卡成功";
                    } catch (DuplicateKeyException e) {
                    }
                }
                rongShuAutLogService.addRongShuAutLog(userId, CommonConstant.RONG_SHU_CHECK_BANK, authenticateCode,
                        idCheckRes);
            }
        }

        result.put("code", code);
        result.put("msg", msg);
        return result;
    }

    @Override
    public Map<String, Object> getUserBankList(Long userId) {
        Map<String, Object> result = new HashMap<>();

        List<Map<String, Object>> bankList = new ArrayList<>();
        List<UserBankCard> userBankCardList = userBankCardDao.getUserAllBankCard(userId, CommonConstant.BANK_CARD_TYPE_DEBIT);
        if (userBankCardList != null && userBankCardList.size() > 0) {
            for (UserBankCard userBankCard : userBankCardList) {
                Map<String, Object> temp = convertUserBankCard2Map(userBankCard);
                if (temp != null && !temp.isEmpty()) {
                    bankList.add(temp);
                }
            }
        }

        result.put("bankList", bankList);
        result.put("supportBankList", BankCache.getJDSupportBank());
        return result;
    }

    @Override
    public Map<String, Object> getUserBankCardDetail(Long userId, Integer bankId) {
        Map<String, Object> result = new HashMap<>();
        String mobile = "";
        UserBankCard userBankCard = userBankCardDao.getUserBankCardById(userId, bankId);
        if (userBankCard != null) {
            if (StringUtils.isNotBlank(userBankCard.getMobile())) {
                StringBuffer mobileStr = new StringBuffer(userBankCard.getMobile());
                mobile = mobileStr.replace(3, 7, "****").toString();
            }
        }
        result.put("mobile", mobile);
        result.put("bankName", userBankCard.getBankCn());
        result.put("charge", "手续费全免");
        return result;
    }

    private Map<String, Object> convertUserBankCard2Map(UserBankCard userBankCard) {
        Map<String, Object> result = new HashMap<>();

        result.put("bankId", userBankCard.getBankId());
        result.put("bankImg", WithdrawDefrayUtil.getBankCardImg(userBankCard.getBankCn()));
        result.put("bankName", CommonUtil.packageBankName(userBankCard.getBankCn(), userBankCard.getBankCard()));
        result.put("bankCardType", CommonUtil.getCardTypeCn(userBankCard.getCardType()));
        return result;
    }
}
