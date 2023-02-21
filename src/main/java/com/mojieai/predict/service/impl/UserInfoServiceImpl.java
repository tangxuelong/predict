package com.mojieai.predict.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.IniCache;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.IniConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.ResultConstant;
import com.mojieai.predict.dao.UserBankCardDao;
import com.mojieai.predict.dao.UserDao;
import com.mojieai.predict.dao.UserInfoDao;
import com.mojieai.predict.entity.bo.RealNameInfo;
import com.mojieai.predict.entity.dto.HttpParamDto;
import com.mojieai.predict.entity.po.User;
import com.mojieai.predict.entity.po.UserBankCard;
import com.mojieai.predict.entity.po.UserInfo;
import com.mojieai.predict.entity.vo.ResultVo;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.RongShuAutLogService;
import com.mojieai.predict.service.UserInfoService;
import com.mojieai.predict.service.beanself.BeanSelfAware;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.HttpServiceUtils;
import com.mojieai.predict.util.IdCardUtils;
import com.mojieai.predict.util.Md5Util;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class UserInfoServiceImpl implements UserInfoService, BeanSelfAware {
    private Logger log = LogConstant.commonLog;
    @Autowired
    private UserInfoDao userInfoDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private RedisService redisService;
    @Autowired
    private RongShuAutLogService rongShuAutLogService;
    @Autowired
    private UserBankCardDao userBankCardDao;

    private UserInfoService self;


    @Override
    public Map<String, Object> saveUserLotteryType(Long userId, Integer type) {
        Map<String, Object> res = new HashMap<>();
        Integer status = ResultConstant.ERROR;
        String msg = ResultConstant.SUCCESS_SAVE_ERROR;

        UserInfo userInfo = userInfoDao.getUserInfo(userId);
        String setRemark = dealWithUserInfoRemark(userInfo.getRemark(), type);

        if (updateUserInfoRemark(userInfo.getRemark(), setRemark, userId)) {
            status = ResultConstant.SUCCESS;
            msg = ResultConstant.SUCCESS_SAVE_MSG;
        }
        res.put("status", status);
        res.put("msg", msg);
        return res;
    }

    @Override
    public ResultVo saveUserWithdrawPwd(Long userId, String password) {
        ResultVo res = new ResultVo(ResultConstant.ERROR);
        UserInfo userInfo = userInfoDao.getUserInfo(userId);
        if (userInfo == null) {
            res.setMsg("用户不存在");
            return res;
        }
        if (StringUtils.isNotBlank(userInfo.getRemark())) {
            Map<String, Object> remarkMap = JSONObject.parseObject(userInfo.getRemark(), HashMap.class);
            if (remarkMap.containsKey("withdrawPwd") && StringUtils.isNotBlank(remarkMap.get("withdrawPwd").toString())) {
                res.setMsg("您已设置提现密码");
                return res;
            }
        }

        Map<String, Object> withdrawPwdMap = new HashMap<>();
        withdrawPwdMap.put("withdrawPwd", password);
        String setRemark = CommonUtil.appendKey2MapString(userInfo.getRemark(), withdrawPwdMap);

        if (updateUserInfoRemark(userInfo.getRemark(), setRemark, userId)) {
            res.setCode(ResultConstant.SUCCESS);
            res.setMsg(ResultConstant.SUCCESS_SAVE_MSG);
        }
        return res;
    }

    @Override
    public ResultVo updateUserWithdrawPwd(Long userId, String oldPassword, String newPassword) {
        ResultVo res = new ResultVo(ResultConstant.ERROR);

        UserInfo userInfo = userInfoDao.getUserInfo(userId);
        Map<String, Object> remarkMap = JSONObject.parseObject(userInfo.getRemark(), HashMap.class);
        if (!remarkMap.containsKey("withdrawPwd") || remarkMap.get("withdrawPwd") == null) {
            res.setMsg("未设置提现密码");
            return res;
        }
        if (!oldPassword.equals(remarkMap.get("withdrawPwd").toString())) {
            res.setMsg("旧密码不匹配");
            return res;
        }

        Map<String, Object> withdrawPwdMap = new HashMap<>();
        withdrawPwdMap.put("withdrawPwd", newPassword);
        String setRemark = CommonUtil.appendKey2MapString(userInfo.getRemark(), withdrawPwdMap);

        if (updateUserInfoRemark(userInfo.getRemark(), setRemark, userId)) {
            res.setCode(ResultConstant.SUCCESS);
            res.setMsg("修改成功");
        }
        return res;
    }

    private String dealWithUserInfoRemark(String remark, Integer type) {
        if (StringUtils.isEmpty(remark) && type == null) {
            return null;
        }
        Map<String, Object> resMap = new HashMap<>();
        if (StringUtils.isNotBlank(remark)) {
            resMap.putAll(JSONObject.parseObject(remark, HashMap.class));
        }
        if (type != null) {
            resMap.put("lotteryType", type);
        }

        return JSONObject.toJSONString(resMap);
    }

    @Transactional
    @Override
    public Boolean updateUserInfoRemark(String originRemark, String setRemark, Long userId) {
        boolean res = false;
        UserInfo userInfo = userInfoDao.getUserInfo(userId, true);
        if (userInfoDao.updateRemark(userId, setRemark, originRemark) > 0) {
            res = true;
        }
        return res;
    }

    @Override
    public Boolean checkUserReceivePush(Long userId, Integer pushType) {
        UserInfo userInfo = userInfoDao.getUserInfo(userId);
        if (StringUtils.isBlank(userInfo.getPushInfo())) {
            return true;
        }
        Map<Integer, Integer> pushInfo = JSONObject.parseObject(userInfo.getPushInfo(), HashMap.class);
        if (pushInfo.containsKey(pushType) && pushInfo.get(pushType).equals(1)) {
            return true;
        }
        return false;
    }

    @Override
    public Integer getUserLotteryType(Long userId) {
        Integer result = null;
        if (userId == null) {
            return result;
        }
        UserInfo userInfo = userInfoDao.getUserInfo(userId);
        if (userInfo == null || StringUtils.isBlank(userInfo.getRemark())) {
            return result;
        }
        String remark = userInfo.getRemark();
        Map<String, Object> remarkMap = JSONObject.parseObject(remark, HashMap.class);
        if (remarkMap.containsKey("lotteryType") && remarkMap.get("lotteryType") != null) {
            result = Integer.valueOf(remarkMap.get("lotteryType").toString());
        }
        return result;
    }

    @Override
    public Map<String, Object> getUserInfoByNickNameOrMobileFromOtter(String nickName, String mobile) {
        Map<String, Object> result = new HashMap<>();
        UserInfo userInfo = new UserInfo();
        User user = null;
        if (StringUtils.isNotBlank(nickName)) {
            List<UserInfo> userInfos = userInfoDao.getUserInfoByNickNameFromOtter(nickName);
            if (userInfos != null && userInfos.size() > 0) {
                userInfo = userInfos.get(0);
                user = userDao.getUserByUserId(userInfo.getUserId(), false);
            }
        } else {
            user = userDao.getUserByMobileFromOtter(mobile);
            if (user != null) {
                userInfo = userInfoDao.getUserInfo(user.getUserId());
            } else {
                user = new User();
            }
        }
        result.put("userInfo", userInfo);
        result.put("user", user);
        return result;
    }

    @Override
    public Boolean safeCheck(String deviceId, String mobile) {
        if (StringUtils.isNotBlank(deviceId)) {
            String key = Md5Util.getMD5String(deviceId, "UTF-8");
            List<String> mobiles = redisService.kryoGet(key, ArrayList.class);
            if (mobiles != null && mobiles.size() > 0) {
                if (mobiles.size() > CommonConstant.DEVICEID_ALLOW_MAX_MOBILE_LOGIN) {
                    log.error("有设备尝试登录多个账号,deviceId:" + deviceId + " 涉及手机号：" + JSONObject.toJSONString(mobiles));
                    return false;
                }
            } else {
                mobiles = new ArrayList<>();
                mobiles.add(mobile);
                redisService.kryoSetEx(key, 14400, mobiles);
            }
        }
        return true;
    }

    @Override
    public Map<String, Object> authenticateRealName(Long userId, String userName, String idCard) {
        Map<String, Object> result = new HashMap<>();
        int code = ResultConstant.ERROR;
        String msg = "";
        if (StringUtils.isBlank(userName) || StringUtils.isBlank(idCard)) {
            msg = "用户名称和身份证号不能为空";
            result.put("code", code);
            result.put("msg", msg);
            return result;
        }
        if (!IdCardUtils.validate(idCard)) {
            msg = "请输入正确的身份证号";
            result.put("code", code);
            result.put("msg", msg);
            return result;
        }
        UserInfo userInfo = userInfoDao.getUserInfo(userId);
        if (checkUserAuthenticateRealName(userInfo.getRemark())) {
            result.put("code", code);
            result.put("msg", "您已认证成功");
            return result;
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
            String url = rongShuMap.get("url").toString();
            String key = rongShuMap.get("key").toString();
            TreeMap<String, Object> params = new TreeMap<>();
            params.put("Appid", rongShuMap.get("appId").toString());
            params.put("Name", userName);
            params.put("IdCode", idCard);
            params.put("Sign", CommonUtil.getRongShuSign(params, key));

            //{"Address":"","Birthday":"","Gender":"","IdCode":"130203197607154331","Id_":"","MobilePhone":null,"Name":"韩涵忍","Nationality":"","Oid":"","Photo":null,"ResultCode":"1002","ResultMsg":"库中无此号","Seqno":"","Ssssxq":"","TrainChecked":false,"TransactionID":"1cfeb122-a07b-11e8-9057-00163e2e29d0"}
            String idCheckRes = HttpServiceUtils.sendPostRequest(url, JSONObject.toJSONString(params), HttpParamDto
                            .DEFAULT_CHARSET, HttpParamDto.DEFAULT_CONNECT_TIME_OUT, HttpParamDto.DEFAULT_READ_TIME_OUT,
                    "application/json", true);
            if (StringUtils.isNotBlank(idCheckRes)) {
                Map<String, Object> userReal = new HashMap<>();
                Integer authenticateCode = null;
                log.info("用户校验身份证信息：" + idCheckRes);
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
                userReal.put("realName", userName);
                userReal.put("idCard", idCard);
                userReal.put("authenticateCode", authenticateCode);
                if (updateUserInfoRemark(userInfo.getRemark(), CommonUtil.appendKey2MapString(userInfo.getRemark(),
                        userReal), userId)) {
                    code = ResultConstant.SUCCESS;
                    msg = "认证成功";
                }
                rongShuAutLogService.addRongShuAutLog(userId, CommonConstant.RONG_SHU_CHECK_USER, authenticateCode,
                        idCheckRes);
            }
        }
        result.put("code", code);
        result.put("msg", msg);
        return result;
    }

    @Override
    public Boolean checkUserIfAuthenticate(Long userId) {
        // TODO: 18/8/19 这里考虑缓存优化
        UserInfo userInfo = userInfoDao.getUserInfo(userId);
        if (userInfo == null || StringUtils.isBlank(userInfo.getRemark())) {
            return Boolean.FALSE;
        }

        return checkUserAuthenticateRealName(userInfo.getRemark());
    }

    @Override
    public Map<String, Object> getPersonalData(Long userId) {
        Map<String, Object> result = new HashMap<>();
        UserInfo userInfo = userInfoDao.getUserInfo(userId);
        User user = userDao.getUserByUserId(userId, false);
        String mobile = "";
        StringBuilder sb = new StringBuilder();
        if (user != null && StringUtils.isNotBlank(user.getMobile())) {
            sb.append(user.getMobile());
            mobile = sb.replace(3, 7, "****").toString();
        }
        String bankCard = "0";
        List<UserBankCard> bankCards = userBankCardDao.getUserAllBankCard(userId, CommonConstant.BANK_CARD_TYPE_DEBIT);
        if (bankCards != null) {
            bankCard = bankCards.size() + "";
        }
        RealNameInfo realNameInfo = CommonUtil.getUserRealNameInfo(userInfo.getRemark());
        String idCard = "";
        String realName = "";
        if (realNameInfo != null) {
            idCard = CommonUtil.hiddenNum(realNameInfo.getIdCard());
            realName = realNameInfo.getRealName();
        }

        Integer withdrawPwdStatus = 0;
        if (StringUtils.isNotBlank(userInfo.getRemark())) {
            Map<String, Object> remarkMap = JSONObject.parseObject(userInfo.getRemark(), HashMap.class);
            if (remarkMap.containsKey("withdrawPwd") && remarkMap.get("withdrawPwd") != null) {
                withdrawPwdStatus = 1;
            }
        }

        result.put("imgUrl", userInfo.getHeadImgUrl());
        result.put("userName", userInfo.getNickName());
        result.put("idCard", idCard);
        result.put("realName", realName);
        result.put("mobile", mobile);
        result.put("realNameAuthenticate", checkUserAuthenticateRealName(userInfo.getRemark()));
        result.put("bankCard", bankCard);
        result.put("withdrawPwdStatus", withdrawPwdStatus);
        return result;
    }

    private Boolean checkUserAuthenticateRealName(String userInfoRemark) {
        if (StringUtils.isNotBlank(userInfoRemark)) {
            Map<String, Object> remarkMap = JSONObject.parseObject(userInfoRemark, HashMap.class);
            if (remarkMap.containsKey("idCard") && remarkMap.get("idCard") != null) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    @Override
    public void setSelf(Object proxyBean) {
        self = (UserInfoService) proxyBean;
    }
}
