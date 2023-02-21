package com.mojieai.predict.enums;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.ResultConstant;
import com.mojieai.predict.entity.bo.JDDefrayConfig;
import com.mojieai.predict.entity.bo.WithdrawMerchantBalanceResponse;
import com.mojieai.predict.entity.po.UserWithdrawFlow;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.WithdrawDefrayUtil;
import com.mojieai.predict.util.JDDefray.Contants;
import com.mojieai.predict.util.JDDefray.JDDefrayCodeConst;
import com.mojieai.predict.util.JDDefray.RequestUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public enum WithdrawEnum {
    JING_DONG_WITHDRAW("京东", 0) {
        @Override
        public WithdrawMerchantBalanceResponse queryBalance() {
            WithdrawMerchantBalanceResponse result = new WithdrawMerchantBalanceResponse(ResultConstant.ERROR);

            JDDefrayConfig defrayConfig = WithdrawDefrayUtil.getJDDefrayConfig();
            if (defrayConfig == null) {
                log.error(getName() + "代付ini未配置基础参数");
                result.setMsg("代付ini未配置基础参数");
                return result;
            }

            RequestUtil demoUtil = new RequestUtil();
            String responseText = "";
            try {
                //请求
                responseText = demoUtil.tradeRequestSSL(getQueryJDDefrayParam(defrayConfig.getCustomerNo()),
                        defrayConfig.getQueryBalanceUrl(), null, defrayConfig.getFilePwd());
                //验证数据
                Map<String, String> map = demoUtil.verifySingReturnData(responseText);
                if (map == null) {
                    log.info("验证签名不成功");
                    result.setMsg("验证签名不成功");
                    return result;
                }
                String response_code = map.get("response_code");
                if (JDDefrayCodeConst.SUCCESS.equals(response_code)) {
                    String account_amount = map.get("account_amount") == null ? "" : map.get("account_amount").toString();
                    String frozen_amount = map.get("frozen_amount") == null ? "" : map.get("frozen_amount").toString();
                    result.setMerchantBalance(Long.parseLong("".equals(account_amount) ? "0" : account_amount));
                    result.setMerchantFrozenAmount(Long.parseLong("".equals(frozen_amount) ? "0" : frozen_amount));
                    result.setCode(ResultConstant.SUCCESS);
                } else {
                    result.setMsg(response_code);
                    log.error("查询失败 描述：" + response_code + " " + map.get("response_message"));
                }
            } catch (Exception e) {
                log.error("代付发生异常", e);
            }
            return result;
        }

        @Override
        public Map<String, Object> createWithdrawOrder(UserWithdrawFlow userWithdrawFlow) {
            Map<String, Object> result = new HashMap<>();

            RequestUtil requestUtil = new RequestUtil();
            String responseText = "";
            JDDefrayConfig jdDefrayConfig = WithdrawDefrayUtil.getJDDefrayConfig();
            if (jdDefrayConfig == null) {
                log.error("ini京东代付配置异常");
                throw new BusinessException("ini京东代付配置异常");
            }
            Map<String, String> paramMap = initDefrayPayParam(userWithdrawFlow, jdDefrayConfig.getCustomerNo(),
                    jdDefrayConfig.getCallBackUrl());
            Integer threePartyStatus = CommonConstant.THREE_PARTY_WITHDRAW_ORDER_UNKNOWN;
            //创建请求业务数据
            try {
                //请求
                responseText = requestUtil.tradeRequestSSL(paramMap, jdDefrayConfig.getDefrayPayUrl(), Contants
                        .encryptType_RSA, jdDefrayConfig.getFilePwd());

                //验证数据
                Map<String, String> map = requestUtil.verifySingReturnData(responseText);
                if (map == null) {
                    log.error("验证签名不成功");
                    result.put("code", ResultConstant.ERROR);
                    result.put("msg", "验证签名不成功");
                    return result;
                }
                threePartyStatus = rescode(map);//处理返回数据
                result.put("resMap", map);
            } catch (Exception e) {
                log.error("生成提现订单异常", e);
            }
            result.put("threePartyStatus", threePartyStatus);
            return result;
        }

        @Override
        public Map<String, Object> queryOrder(UserWithdrawFlow userWithdrawFlow) {
            Map<String, Object> result = new HashMap<>();
            Map<String, String> paramMap = new HashMap<>();
            JDDefrayConfig jdDefrayConfig = WithdrawDefrayUtil.getJDDefrayConfig();
            if (jdDefrayConfig == null) {
                log.error("ini京东代付配置异常");
                throw new BusinessException("ini京东代付配置异常");
            }
            paramMap.put("customer_no", jdDefrayConfig.getCustomerNo());//提交者会员号
            paramMap.put("request_datetime", WithdrawDefrayUtil.getJDFormatDateStr(DateUtil.getCurrentTimestamp()));
            paramMap.put("out_trade_no", userWithdrawFlow.getWithdrawId());//商户订单号

            RequestUtil demoUtil = new RequestUtil();
            String responseText = "";
            Integer threePartyStatus = CommonConstant.THREE_PARTY_WITHDRAW_ORDER_UNKNOWN;
            try {
                //请求
                responseText = demoUtil.tradeRequestSSL(paramMap, jdDefrayConfig.getTradeQueryUrl(), null,
                        jdDefrayConfig.getFilePwd());
                //验证数据
                Map<String, String> map = demoUtil.verifySingReturnData(responseText);
//                System.out.println(map);
                if (map == null) {
                    log.error("验证签名不成功");
                    result.put("code", ResultConstant.ERROR);
                    result.put("msg", "验证签名不成功");
                    return result;
                }
                threePartyStatus = rescode(map);//处理返回数据
                result.put("resMap", map);
            } catch (Exception e) {
                e.printStackTrace();
            }
            result.put("threePartyStatus", threePartyStatus);
            return result;
        }
    };

    private static Integer rescode(Map<String, String> map) {
        Integer result = null;
        String response_code = map.get("response_code");
        if (JDDefrayCodeConst.SUCCESS.equals(response_code)) {//如果response_code返回0000，表示请求逻辑正常，进一步判断订单状态
            result = WithdrawDefrayUtil.tradeJDCode(map);
        } else if (!JDDefrayCodeConst.isContainCode(response_code)) {//返回编码不包含在配置中的
            if (map.get("trade_status") == null || StringUtils.isEmpty(map.get("trade_status"))) {
                // 返回编码不包含在配置中的,未知处理
                result = CommonConstant.THREE_PARTY_WITHDRAW_ORDER_UNKNOWN;
            } else {//如果有trade_status，按trade_status状态判断
                result = WithdrawDefrayUtil.tradeJDCode(map);
            }
        } else if (JDDefrayCodeConst.SYSTEM_ERROR.equals(response_code) || JDDefrayCodeConst.RETURN_PARAM_NULL.equals(response_code)) {
            //需查询交易获取结果或等待通知结果
            result = CommonConstant.THREE_PARTY_WITHDRAW_ORDER_UNKNOWN;
        } else if (JDDefrayCodeConst.OUT_TRADE_NO_EXIST.equals(response_code)) {
            //System.out.println("外部交易号已经存在");
            //需查询交易获取结果或等待通知结果
            result = CommonConstant.THREE_PARTY_WITHDRAW_ORDER_UNKNOWN;
        } else if (JDDefrayCodeConst.ACCOUNT_BALANCE_NOT_ENOUGH.equals(response_code)) {
            log.error("京东代付余额不足请及时冲值");
            result = CommonConstant.THREE_PARTY_WITHDRAW_ORDER_PROCESSING;
        } else {
            // 失败处理逻辑
            result = CommonConstant.THREE_PARTY_WITHDRAW_ORDER_FAIL;
        }
        return result;
    }

    private static Map<String, String> initDefrayPayParam(UserWithdrawFlow userWithdrawFlow, String customerNo,
                                                          String callBackUrl) {
        Map<String, String> map = new HashMap<>();
        Timestamp current = DateUtil.getCurrentTimestamp();
        String remark = userWithdrawFlow.getRemark();
        if (StringUtils.isBlank(remark)) {
            return null;
        }
        Map<String, Object> remarkMap = JSONObject.parseObject(remark, HashMap.class);
        if (!remarkMap.containsKey("jdBankCardEn")) {
            log.error("jd 提现记录异常，不包含银行卡编码,withdrawId:" + userWithdrawFlow.getWithdrawId());
            return null;
        }
        String jdBankCardEn = (String) remarkMap.get("jdBankCardEn");

        Map<String, Object> sellerInfo = new HashMap<>();
        sellerInfo.put("customer_code", customerNo);
        sellerInfo.put("customer_type", "CUSTOMER_NO");

        map.put("payee_bank_code", jdBankCardEn);
        map.put("customer_no", customerNo);
        map.put("payee_account_type", "P");
        map.put("trade_currency", "CNY");
        map.put("pay_tool", "TRAN");
        map.put("payee_account_no", userWithdrawFlow.getBankCard());
        map.put("payee_account_name", userWithdrawFlow.getUserName());
        map.put("notify_url", callBackUrl);//商户处理数据的异步通知地址
//        map.put("biz_trade_no", "2015003456");
        map.put("out_trade_no", userWithdrawFlow.getWithdrawId());//外部交易号
        map.put("seller_info", JSONObject.toJSONString(sellerInfo));
        map.put("out_trade_date", WithdrawDefrayUtil.getJDFormatDateStr(userWithdrawFlow.getCreateTime()));
        map.put("trade_amount", userWithdrawFlow.getWithdrawAmount() + "");

        map.put("sign_type", "SHA-256");
        map.put("request_datetime", WithdrawDefrayUtil.getJDFormatDateStr(current));
        map.put("trade_subject", "智慧代付");
        map.put("payee_card_type", "DE");
//        map.put("payee_mobile", "1333333333");
        return map;
    }

    public Map<String, String> getQueryJDDefrayParam(String customerNo) {
        Map<String, Object> buyerInfo = new HashMap<>();
        Timestamp current = DateUtil.getCurrentTimestamp();

        buyerInfo.put("customer_code", customerNo);
        buyerInfo.put("customer_type", "CUSTOMER_NO");

        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("customer_no", customerNo);//提交者会员号（商户会员号）
        paramMap.put("request_datetime", WithdrawDefrayUtil.getJDFormatDateStr(current));
        paramMap.put("out_trade_no", System.currentTimeMillis() + "");
        paramMap.put("out_trade_date", WithdrawDefrayUtil.getJDFormatDateStr(current));
        paramMap.put("buyer_info", JSONObject.toJSONString(buyerInfo));//customer_code必须和上面的会员号一致
        paramMap.put("query_type", "BUSINESS_BASIC");
        paramMap.put("ledger_type", "00");

        return paramMap;
    }

    private String name;
    private Integer code;

    WithdrawEnum(String name, Integer code) {
    }

    public String getName() {
        return name;
    }

    public Integer getCode() {
        return code;
    }

    private static final Logger log = CronEnum.PERIOD.getLogger();

    public abstract WithdrawMerchantBalanceResponse queryBalance();

    public abstract Map<String, Object> createWithdrawOrder(UserWithdrawFlow userWithdrawFlow);

    public abstract Map<String, Object> queryOrder(UserWithdrawFlow userWithdrawFlow);
}
