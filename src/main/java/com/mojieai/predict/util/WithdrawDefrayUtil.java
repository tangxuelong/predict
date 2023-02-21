package com.mojieai.predict.util;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.IniCache;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.IniConstant;
import com.mojieai.predict.entity.bo.JDDefrayConfig;
import com.mojieai.predict.util.JDDefray.JDDefrayCodeConst;
import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class WithdrawDefrayUtil {
    public static JDDefrayConfig getJDDefrayConfig() {
        String jdDefrayConfig = IniCache.getIniValue(IniConstant.JD_DEFRAY_CONFIG);
        if (StringUtils.isBlank(jdDefrayConfig)) {
            return null;
        }
        Map<String, Object> jdDefrayConfigMap = JSONObject.parseObject(jdDefrayConfig, HashMap.class);
        if (jdDefrayConfigMap == null) {
            return null;
        }
        JDDefrayConfig jdDefray = new JDDefrayConfig();
        jdDefray.setCustomerNo(jdDefrayConfigMap.get("customerNo").toString());
        jdDefray.setQueryBalanceUrl(jdDefrayConfigMap.get("balanceUrl").toString());
        jdDefray.setDefrayPayUrl(jdDefrayConfigMap.get("defrayPayUrl").toString());
        jdDefray.setTradeQueryUrl(jdDefrayConfigMap.get("tradeQueryUrl").toString());
        jdDefray.setCallBackUrl(jdDefrayConfigMap.get("callBackUrl").toString());
        jdDefray.setFilePwd(jdDefrayConfigMap.get("filePwd").toString());
        return jdDefray;
    }

    public static Integer tradeJDCode(Map<String, String> map) {
        Integer result = CommonConstant.THREE_PARTY_WITHDRAW_ORDER_UNKNOWN;
        String trade_status = map.get("trade_status");
        if (JDDefrayCodeConst.TRADE_FINI.equals(trade_status)) {
            //成功后业务逻辑
            result = CommonConstant.THREE_PARTY_WITHDRAW_ORDER_SUCCESS;
        } else if (JDDefrayCodeConst.TRADE_CLOS.equals(trade_status)) {
            // 失败后业务逻辑
            result = CommonConstant.THREE_PARTY_WITHDRAW_ORDER_FAIL;
        } else if (JDDefrayCodeConst.TRADE_WPAR.equals(trade_status) || JDDefrayCodeConst.TRADE_BUID.equals
                (trade_status)) {
            // 处理中业务逻辑
            result = CommonConstant.THREE_PARTY_WITHDRAW_ORDER_PROCESSING;
        } else if (JDDefrayCodeConst.TRADE_ACSU.equals(trade_status)) {
            //已受理
            result = CommonConstant.THREE_PARTY_WITHDRAW_ORDER_ACCEPT;
        }
        return result;
    }

    public static String getJDFormatDateStr(Timestamp dateTime) {
        return DateUtil.formatTime(dateTime, "yyyyMMdd") + "T" + DateUtil.formatTime(dateTime, "HHmmss");
    }

    public static String getBankCardImg(String bankCn) {
        if (bankCn.equals("招商银行")) {
            return "http://7vzspj.com2.z0.glb.qiniucdn.com/cardbin%2F%E6%8B%9B%E5%95%86.png";
        } else if (bankCn.equals("农业银行")) {
            return "http://7vzspj.com2.z0.glb.qiniucdn.com/cardbin%2F%E5%86%9C%E8%A1%8C.png";
        } else if (bankCn.equals("工商银行")) {
            return "http://7vzspj.com2.z0.glb.qiniucdn.com/cardbin%2F%E5%B7%A5%E5%95%86.png";
        } else if (bankCn.equals("邮储银行")) {
            return "http://7vzspj.com2.z0.glb.qiniucdn.com/cardbin%2F%E9%82%AE%E6%94%BF.png";
        } else if (bankCn.equals("建设银行")) {
            return "http://7vzspj.com2.z0.glb.qiniucdn.com/cardbin%2F%E5%BB%BA%E8%AE%BE.png";
        } else if (bankCn.equals("交通银行")) {
            return "http://7vzspj.com2.z0.glb.qiniucdn.com/cardbin%2F%E4%BA%A4%E9%80%9A.png";
        } else if (bankCn.equals("中国银行")) {
            return "http://7xoiug.com1.z0.glb.clouddn.com/cardbin%2F%E4%B8%AD%E5%9B%BD%E9%93%B6%E8%A1%8C.png";
        } else if (bankCn.equals("民生银行")) {
            return "http://7vzspj.com2.z0.glb.qiniucdn.com/cardbin%2F%E6%B0%91%E7%94%9F.png";
        } else if (bankCn.equals("中信银行")) {
            return "http://7vzspj.com2.z0.glb.qiniucdn.com/cardbin%2F%E4%B8%AD%E4%BF%A1%E9%93%B6%E8%A1%8C.png";
        } else if (bankCn.equals("浦发银行")) {
            return "http://7vzspj.com2.z0.glb.qiniucdn.com/cardbin%2F%E6%B5%A6%E5%8F%91.png";
        } else if (bankCn.equals("广发银行")) {
            return "http://7vzspj.com2.z0.glb.qiniucdn.com/cardbin%2F%E5%B9%BF%E5%8F%91.png";
        } else if (bankCn.equals("光大银行")) {
            return "http://7vzspj.com2.z0.glb.qiniucdn.com/cardbin%2F%E5%85%89%E5%A4%A7.png";
        } else if (bankCn.equals("北京银行")) {
            return "http://7xoiug.com1.z0.glb.clouddn.com/cardbin%2F%E5%8C%97%E4%BA%AC%E9%93%B6%E8%A1%8C.png";
        } else {
            return CommonUtil.getImgUrlWithDomain("cp_user_withdraw_bank_card_default.png");
        }
    }
}
