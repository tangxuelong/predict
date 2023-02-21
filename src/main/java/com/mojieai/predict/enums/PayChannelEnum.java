package com.mojieai.predict.enums;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.domain.AlipayTradeCloseModel;
import com.alipay.api.request.AlipayDataDataserviceBillDownloadurlQueryRequest;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayDataDataserviceBillDownloadurlQueryResponse;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.jd.jr.pay.gate.signature.util.SignUtil;
import com.jd.jr.pay.gate.signature.util.ThreeDesUtil;
import com.mojieai.predict.cache.PayChannelInfoCache;
import com.mojieai.predict.cache.PayClientChannelCache;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.IniConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.PayConstant;
import com.mojieai.predict.entity.dto.BasePayOrderInfo;
import com.mojieai.predict.entity.po.PayChannelInfo;
import com.mojieai.predict.entity.po.PayClientChannel;
import com.mojieai.predict.entity.po.UserAccountFlow;
import com.mojieai.predict.util.*;
import com.mojieai.predict.util.Base64;
import com.mojieai.predict.util.JDPay.StringEscape;
import com.yeepay.g3.sdk.yop.client.YopClient3;
import com.yeepay.g3.sdk.yop.client.YopRequest;
import com.yeepay.g3.sdk.yop.client.YopResponse;
import com.yeepay.g3.sdk.yop.encrypt.CertTypeEnum;
import com.yeepay.g3.sdk.yop.encrypt.DigestAlgEnum;
import com.yeepay.g3.sdk.yop.encrypt.DigitalSignatureDTO;
import com.yeepay.g3.sdk.yop.utils.DigitalEnvelopeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by tangxuelong on 2017/12/11.
 */
public enum PayChannelEnum {
    WX(1001, "wxPay") {
        @Override
        public Map<String, String> getChannelPrePay(UserAccountFlow userAccountFlow, Integer clientId) {
            // 支付渠道配置
            PayChannelInfo payChannelInfo = PayChannelInfoCache.getChannelInfo(userAccountFlow.getChannel());

            // 根据客户端ID获取支付配置
            String payClientChannelKey = String.valueOf(clientId) + CommonConstant.COMMON_COLON_STR + String.valueOf
                    (userAccountFlow.getChannel());
            PayClientChannel payClientChannel = PayClientChannelCache.getClientChannel(payClientChannelKey);
            Map<String, String> payKeyStr = (Map<String, String>) JSONObject.parse(payClientChannel.getPayKeyStr());

            String appid = payKeyStr.get(IniConstant.WX_PAY_APP_ID); //微信开放平台审核通过的应用APPID
            String mch_id = payKeyStr.get(IniConstant.WX_PAY_MCH_ID); //微信支付分配的商户号
            String key = payKeyStr.get(IniConstant.WX_PAY_KEY);

            // 拼接支付串
            String device_info = "WEB"; //终端设备号(门店号或收银设备ID)，默认请传"WEB"
            String nonce_str = PayUtil.wxNonceStr(); //随机字符串，不长于32位。推荐随机数生成算法
            SortedMap<String, String> parameters = new TreeMap<>();
            parameters.put("appid", appid);
            parameters.put("mch_id", mch_id);
            parameters.put("nonce_str", nonce_str);
            parameters.put("fee_type", "CNY");
            parameters.put("notify_url", payChannelInfo.getNotifyUrl());// // TODO: 2017/12/13 通知地址
            parameters.put("trade_type", "APP");
            parameters.put("body", CommonConstant.OUT_TRADE_GOODS_NAME);
            parameters.put("spbill_create_ip", userAccountFlow.getClientIp());// 客户端IP
            parameters.put("out_trade_no", userAccountFlow.getFlowId()); // 订单id这里我的订单id生成规则是订单id+时间
            parameters.put("total_fee", userAccountFlow.getPayAmount().toString()); //
            // 测试时，每次支付一分钱，微信支付所传的金额是以分为单位的，因此实际开发中需要x100

            String sign = PayUtil.wxSignStr("UTF-8", parameters, key);
            parameters.put("sign", sign);
            // 封装请求参数结束
            String requestXML = PayUtil.getRequestXml(parameters); // 获取xml结果
            // 调用统一下单接口
            String result = HttpServiceUtils.sendHttpsPostRequest(payChannelInfo.getPayUrl(), requestXML, "UTF-8");

            // 把prepayId返回调用方
            SortedMap<String, String> parameterMap = new TreeMap<>();
            try {
                Document document = DocumentHelper.parseText(result);
                Map<String, Object> resultXmlMap = PayUtil.convertNodesFromXml(document);
                //Element prepayid = document.getRootElement().element("prepay_id");
                parameterMap.put("appid", appid);
                parameterMap.put("partnerid", mch_id);
                parameterMap.put("prepayid", resultXmlMap.get("prepay_id").toString());
                parameterMap.put("package", "Sign=WXPay");
                parameterMap.put("noncestr", PayUtil.wxNonceStr());
                // 本来生成的时间戳是13位，但是ios必须是10位，所以截取了一下
                parameterMap.put("timestamp", String.valueOf(System.currentTimeMillis()).toString().substring(0, 10));
                parameterMap.put("sign", PayUtil.wxSignStr("UTF-8", parameterMap, key));

            } catch (Exception e) {
                e.printStackTrace();
            }
            // 返回客户端拉起支付需要的信息
            // appid partnerid prepayid package noncestr timestamp sign
            return parameterMap;
        }

        @Override
        public Map<String, Object> payQuery(UserAccountFlow userAccountFlow) {
            Map<String, Object> res = new HashMap<>();
            Map<String, Object> returnRes = wxPayQuery(userAccountFlow, CommonConstant.WX_QUERY_TYPE_QUERY);
            if (returnRes == null) {
                log.info("Wx payQuery return null" + userAccountFlow.getFlowId() + " payId:" + userAccountFlow
                        .getPayId());
                Integer orderStatus = PayConstant.OUT_TRADE_ORDER_STATUS_NO_PAY;
                res.put("orderStatus", orderStatus);
                return res;
            }
            Integer orderStatus = PayConstant.OUT_TRADE_ORDER_STATUS_NO_PAY;
            String tradeState = returnRes.get("trade_state") == null ? "" : returnRes.get("trade_state").toString();
            if (StringUtils.isNotBlank(tradeState)) {
                if ("SUCCESS".equals(tradeState)) {
                    orderStatus = PayConstant.OUT_TRADE_ORDER_STATUS_PAY_SUCCESS;
                } else if ("USERPAYING".equals(tradeState)) {
                    orderStatus = PayConstant.OUT_TRADE_ORDER_STATUS_TRADING;
                }
            } else {
                if ("ORDERNOTEXIST".equals(returnRes.get("err_code"))) {
                    orderStatus = PayConstant.OUT_TRADE_ORDER_STATUS_NO_PAY;
                }
            }

            res.put("orderStatus", orderStatus);
            return res;
        }

        @Override
        public Map<String, Object> payClose(UserAccountFlow userAccountFlow) {
            return wxPayQuery(userAccountFlow, CommonConstant.WX_QUERY_TYPE_CLOSE);
        }

        @Override
        public Map<String, Object> getsignkey() {
            String url = "https://apitest.mch.weixin.qq.com/sandboxnew/pay/getsignkey";
            String payClientChannelKey = String.valueOf(1000) + CommonConstant.COMMON_COLON_STR + String.valueOf(1001);
            PayClientChannel payClientChannel = PayClientChannelCache.getClientChannel(payClientChannelKey);
            Map<String, String> payKeyStr = (Map<String, String>) JSONObject.parse(payClientChannel.getPayKeyStr());
            String mch_id = payKeyStr.get(IniConstant.WX_PAY_MCH_ID); //微信支付分配的商户号
            String key = payKeyStr.get(IniConstant.WX_PAY_KEY);

            // 拼接支付串
            String nonce_str = PayUtil.wxNonceStr(); //随机字符串，不长于32位。推荐随机数生成算法
            SortedMap<String, String> parameters = new TreeMap<>();
            parameters.put("mch_id", mch_id);
            parameters.put("nonce_str", nonce_str);

            String sign = PayUtil.wxSignStr("UTF-8", parameters, key);
            parameters.put("sign", sign);
            // 封装请求参数结束
            String requestXML = PayUtil.getRequestXml(parameters); // 获取xml结果
            // 调用统一下单接口
            String result = HttpServiceUtils.sendHttpsPostRequest(url, requestXML, "UTF-8");

            // 解析
            try {
                Document document = DocumentHelper.parseText(result);
                Map<String, Object> params = PayUtil.convertNodesFromXml(document);
                if ("SUCCESS".equals(params.get("return_code"))) {
                    return params;
                }
            } catch (Exception e) {
                log.error("wx query order parse xml error" + result);
            }
            return null;
        }
    }, APPLY_APY(1002, CommonConstant.APPLY_PAY_NAME) {
        @Override
        public Map<String, String> getChannelPrePay(UserAccountFlow userAccountFlow, Integer clientId) {
            Map payForToken = new HashMap();
            payForToken.put("prepayid", null);
            return payForToken;
        }

        @Override
        public Map<String, Object> payQuery(UserAccountFlow userAccountFlow) {
            Map<String, Object> res = new HashMap<>();
            res.put("orderStatus", PayConstant.OUT_TRADE_ORDER_STATUS_NO_PAY);
            return res;
        }

    }, WISDOM_COIN(CommonConstant.WISDOM_COIN_CHANNEL_ID, CommonConstant.WISDOM_COIN_PAY_NAME) {
        @Override
        public Map<String, String> getChannelPrePay(UserAccountFlow userAccountFlow, Integer clientId) {
            Map payForToken = new HashMap();
            payForToken.put("prepayid", null);
            return payForToken;
        }

        @Override
        public Map<String, Object> payQuery(UserAccountFlow userAccountFlow) {
            Map<String, Object> res = new HashMap<>();
            res.put("orderStatus", PayConstant.OUT_TRADE_ORDER_STATUS_NO_PAY);
            return res;
        }

        @Override
        public Integer getAccountType() {
            return CommonConstant.ACCOUNT_TYPE_WISDOM_COIN;
        }
    }, ALI_PAY(CommonConstant.ALI_PAY_CHANNEL_ID, CommonConstant.ALI_PAY_ENUM_NAME) {
        @Override
        public Map<String, String> getChannelPrePay(UserAccountFlow userAccountFlow, Integer clientId) {
            // 支付渠道配置
            PayChannelInfo payChannelInfo = PayChannelInfoCache.getChannelInfo(userAccountFlow.getChannel());

            Map payForToken = new HashMap();
            //实例化客户端
            AlipayClient client = getAliPayClient(userAccountFlow.getClientId(), userAccountFlow.getChannel(),
                    PayConstant.SAND_BOX_SWITCH);
            //实例化具体API对应的request类,类名称和接口名称对应,当前调用接口名称：alipay.trade.app.pay
            AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
            //SDK已经封装掉了公共参数，这里只需要传入业务参数。以下方法为sdk的model入参方式(model和biz_content同时存在的情况下取biz_content)。
            AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
            model.setBody(CommonConstant.OUT_TRADE_GOODS_NAME);
            model.setSubject(CommonConstant.OUT_TRADE_GOODS_NAME);
            model.setOutTradeNo(userAccountFlow.getFlowId());
            model.setTimeoutExpress("30m");
            model.setTotalAmount(CommonUtil.convertFen2Yuan(userAccountFlow.getPayAmount()).toString());
            model.setProductCode("QUICK_MSECURITY_PAY");//销售产品码，商家和支付宝签约的产品
            request.setBizModel(model);
            request.setNotifyUrl(payChannelInfo.getNotifyUrl());
            try {
                //这里和普通的接口调用不同，使用的是sdkExecute
                AlipayTradeAppPayResponse response = client.sdkExecute(request);
                if (!response.isSuccess()) {
                    log.error("支付宝下单异常getChannelPrePay:" + userAccountFlow.getFlowId() + response.getSubCode());
                    return null;
                }
                payForToken.put("prepayid", null);
                payForToken.put("orderString", response.getBody());
            } catch (AlipayApiException e) {
                log.error("支付宝支付支付异常", e);
            }
            return payForToken;
        }

        @Override
        public Map<String, Object> payQuery(UserAccountFlow userAccountFlow) {
            //实例化客户端
            AlipayClient client = getAliPayClient(userAccountFlow.getClientId(), userAccountFlow.getChannel(),
                    PayConstant.SAND_BOX_SWITCH);
            //实例化具体API对应的request类,类名称和接口名称对应,当前调用接口名称：alipay.trade.query
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            request.setBizContent("{\"out_trade_no\":\"" + userAccountFlow.getFlowId() + "\"}");
            try {
                Map<String, Object> res = new HashMap<>();
                Integer orderStatus = PayConstant.OUT_TRADE_ORDER_STATUS_NO_PAY;
                AlipayTradeQueryResponse response = client.execute(request);
                if (!response.isSuccess()) {
                    res.put("orderStatus", orderStatus);
                    return res;
                }
                if (response.getTradeStatus().equals("TRADE_SUCCESS")) {
                    orderStatus = PayConstant.OUT_TRADE_ORDER_STATUS_PAY_SUCCESS;
                }

                res.put("orderStatus", orderStatus);
                return res;
            } catch (AlipayApiException e) {
                log.error("支付宝payQuery异常", e);
            }
            return null;
        }

        @Override
        public Map<String, Object> payClose(UserAccountFlow userAccountFlow) {
            AlipayClient client = getAliPayClient(userAccountFlow.getClientId(), userAccountFlow.getChannel(),
                    PayConstant.SAND_BOX_SWITCH);

            AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
            AlipayTradeCloseModel model = new AlipayTradeCloseModel();
            model.setOutTradeNo(userAccountFlow.getFlowId());
            request.setBizModel(model);
            AlipayTradeCloseResponse response = null;
            try {
                response = client.sdkExecute(request);
                //调用成功，则处理业务逻辑
                if (!response.isSuccess()) {
                    log.error("关闭订单异常" + response.getSubCode());
                }

                Map<String, Object> res = null;
                return res;
            } catch (AlipayApiException e) {
                log.error("支付宝关闭订单异常", e);
            }
            return null;
        }

        @Override
        public Map<String, Object> billQuery(UserAccountFlow userAccountFlow, Timestamp timestamp) {
            String date = DateUtil.formatTime(timestamp, "yyyy-MM-dd");
            AlipayClient alipayClient = getAliPayClient(userAccountFlow.getClientId(), userAccountFlow.getChannel(),
                    PayConstant.SAND_BOX_SWITCH);
            AlipayDataDataserviceBillDownloadurlQueryRequest request = new
                    AlipayDataDataserviceBillDownloadurlQueryRequest();
            request.setBizContent("{" +
                    "\"bill_type\":\"trade\"," +
                    "\"bill_date\":\"" + date + "\"" +
                    "  }");
            AlipayDataDataserviceBillDownloadurlQueryResponse response = null;
            try {
                response = alipayClient.execute(request);
            } catch (AlipayApiException e) {
                e.printStackTrace();
            }
            if (response.isSuccess()) {
                System.out.println("调用成功");
            } else {
                System.out.println("调用失败");
            }
            return null;
        }

        private AlipayClient getAliPayClient(Integer clientId, Integer channelId, boolean sandBox) {
            AlipayClient alipayClient = null;
            if (sandBox) {
                alipayClient = new DefaultAlipayClient(PayConstant.ALI_SX_ADDRESS, PayConstant.ALI_SX_APPID,
                        PayConstant.ALI_SX_PRIVATE_KEY, "json", CommonConstant.CHARSET_UTF_8, PayConstant
                        .ALI_SX_PUBLIC_KEY, CommonConstant.SIGN_TYPE_RSA2);
            } else {
                // 根据客户端ID获取支付配置
                String payClientChannelKey = String.valueOf(clientId) + CommonConstant.COMMON_COLON_STR + String.valueOf
                        (channelId);
                PayClientChannel payClientChannel = PayClientChannelCache.getClientChannel(payClientChannelKey);
                Map<String, String> payKeyStr = (Map<String, String>) JSONObject.parse(payClientChannel.getPayKeyStr());
                String appid = payKeyStr.get(IniConstant.ALI_PAY_APP_ID); //支付宝开放平台审核通过的应用APPID
                String appPrivateKey = payKeyStr.get(IniConstant.ALI_PRIVATE_KEY);
//                String appPublicKey = payKeyStr.get(IniConstant.ALI_PUBLIC_KEY);
                String aliPayPublicKey = PayConstant.ALI_APP_PUBLIC_KEY;
                alipayClient = new DefaultAlipayClient(PayConstant.ALI_ONLINE_ADDRESS, appid, appPrivateKey, "json",
                        CommonConstant.CHARSET_UTF_8, aliPayPublicKey, CommonConstant.SIGN_TYPE_RSA2);
            }
            return alipayClient;
        }
    }, YOP_PAY(CommonConstant.YOP_PAY_CHANNEL_ID, CommonConstant.YOP_PAY_ENUM_NAME) {
        // 易宝支付
        @Override
        public Map<String, String> getChannelPrePay(UserAccountFlow userAccountFlow, Integer clientId) {
            // 支付渠道配置
            PayChannelInfo payChannelInfo = PayChannelInfoCache.getChannelInfo(userAccountFlow.getChannel());

            // 根据客户端ID获取支付配置
            String payClientChannelKey = String.valueOf(clientId) + CommonConstant.COMMON_COLON_STR + String.valueOf
                    (userAccountFlow.getChannel());
            PayClientChannel payClientChannel = PayClientChannelCache.getClientChannel(payClientChannelKey);
            Map<String, String> payKeyStr = (Map<String, String>) JSONObject.parse(payClientChannel.getPayKeyStr());
            String parentMerchantNo = payKeyStr.get("parentMerchantNo");
            String merchantNo = payKeyStr.get("merchantNo");
            String privateKey = payKeyStr.get("privateKey");
            String redirectUrl = payKeyStr.get("redirectUrl") + "?flowId=" + userAccountFlow.getFlowId();// h5页面回调地址
            String notifyUrl = payKeyStr.get("notifyUrl");// 服务器回调地址

            // 1.拼接支付所需要的必要参数
            String orderId = userAccountFlow.getFlowId(); // 订单ID
            String orderAmount = String.valueOf(CommonUtil.convertFen2Yuan(userAccountFlow.getPayAmount())); // 订单金额
            String goodsName = userAccountFlow.getPayDesc();
            String goodsDesc = userAccountFlow.getPayDesc();
            String goodsParamExt = "{\"goodsName\":\"" + goodsName + "\",\"goodsDesc\":\"" + goodsDesc + "\"}";

            Map<String, String> params = new HashMap<>();
            params.put("orderId", orderId);
            params.put("orderAmount", orderAmount);
            params.put("timeoutExpress", String.valueOf(60 * 24 * 30));
            params.put("redirectUrl", redirectUrl);
            params.put("notifyUrl", notifyUrl);
            params.put("goodsParamExt", goodsParamExt);

            Map<String, String> result = new HashMap<>();
            String uri = "/rest/v1.0/std/trade/order"; //创建订单URI
            String[] TRADEORDER = {"parentMerchantNo", "merchantNo", "orderId", "orderAmount", "timeoutExpress",
                    "requestDate", "redirectUrl", "notifyUrl", "goodsParamExt", "paymentParamExt",
                    "industryParamExt", "memo", "riskParamExt", "csUrl", "fundProcessType", "divideDetail",
                    "divideNotifyUrl"};
            result = requestYOP(params, uri, TRADEORDER, merchantNo, parentMerchantNo, privateKey);

            String token = result.get("token");
            String codeRe = result.get("code");
            if (!"OPR00000".equals(codeRe)) {
                String message = result.get("message");
                log.info("YOP RESULT:::>>>" + message);
            }

            params.put("parentMerchantNo", parentMerchantNo);
            params.put("merchantNo", merchantNo);
            params.put("orderId", orderId);
            params.put("token", token);
            params.put("userNo", String.valueOf(userAccountFlow.getUserId()));
            params.put("userType", "USER_ID");
            params.put("timestamp", DateUtil.getCurrentTimeMillis());


            String url = getUrl(params, merchantNo, privateKey);
            log.info("YOP URL:::>>>" + url.toString());
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("prepayid", url.toString());
            return resultMap;
        }

        @Override
        public Map<String, Object> payQuery(UserAccountFlow userAccountFlow) {
            return null;
        }
    }, JING_DONG_PAY(CommonConstant.JD_PAY_CHANNEL_ID, CommonConstant.JD_PAY_ENUM_NAME) {
        @Override
        public Map<String, String> getChannelPrePay(UserAccountFlow userAccountFlow, Integer clientId) {
            // 支付渠道配置
            PayChannelInfo payChannelInfo = PayChannelInfoCache.getChannelInfo(userAccountFlow.getChannel());

            // 根据客户端ID获取支付配置
            String payClientChannelKey = String.valueOf(clientId) + CommonConstant.COMMON_COLON_STR + String.valueOf
                    (userAccountFlow.getChannel());
            PayClientChannel payClientChannel = PayClientChannelCache.getClientChannel(payClientChannelKey);
            Map<String, String> payKeyStr = (Map<String, String>) JSONObject.parse(payClientChannel.getPayKeyStr());
            String merchant = payKeyStr.get(IniConstant.JD_PAY_MCH_ID); //支付分配的商户号
            String privateKey = payKeyStr.get(IniConstant.JD_PRIVATE_KEY);
            String desKey = payKeyStr.get(IniConstant.JD_PRIVATE_DES_KEY);
            String callBackUrl = payKeyStr.get(IniConstant.JD_NOTIFY_URL) + "?flowId=" + userAccountFlow.getFlowId();
            byte[] key = Base64.decode(desKey);

            Timestamp tradeTime = DateUtil.getCurrentTimestamp();
            // 拼接支付串
//            SortedMap<String, String> parameters = new TreeMap<>();
//            parameters.put("version", "V2.0");
//            parameters.put("merchant", merchant);
//            parameters.put("tradeNum", userAccountFlow.getFlowId());
//            parameters.put("tradeName", CommonConstant.OUT_TRADE_GOODS_NAME);
//            parameters.put("tradeTime", DateUtil.formatTime(tradeTime, "yyyyMMddHHmmss"));
//            parameters.put("amount", userAccountFlow.getPayAmount().toString());
//            parameters.put("orderType", "1");
//            parameters.put("currency", "CNY");
//            parameters.put("callBackUrl", callBackUrl);
//            parameters.put("notifyUrl", payChannelInfo.getNotifyUrl());
//            parameters.put("ip", userAccountFlow.getClientIp());
//            parameters.put("specCardNo", PayUtil.getUserBankCardFromRemark(userAccountFlow.getRemark()));
//            parameters.put("userId", userAccountFlow.getUserId().toString());
//            parameters.put("sign", PayUtil.jdSignStr(parameters, privateKey));
//
//            // 上面除merchant和version、sign字段外，各字段均需要进行3DES加密
//            parameters.put("tradeNum", ThreeDesUtil.encrypt2HexStr(key, userAccountFlow.getFlowId()));
//            parameters.put("tradeName", ThreeDesUtil.encrypt2HexStr(key, CommonConstant.OUT_TRADE_GOODS_NAME));
//            parameters.put("tradeTime", ThreeDesUtil.encrypt2HexStr(key, DateUtil.formatTime(tradeTime,
//                    "yyyyMMddHHmmss")));
//            parameters.put("amount", ThreeDesUtil.encrypt2HexStr(key, userAccountFlow.getPayAmount().toString()));
//            parameters.put("orderType", ThreeDesUtil.encrypt2HexStr(key, "1"));
//            parameters.put("currency", ThreeDesUtil.encrypt2HexStr(key, "CNY"));
//            parameters.put("callBackUrl", ThreeDesUtil.encrypt2HexStr(key, callBackUrl));
//            parameters.put("notifyUrl", ThreeDesUtil.encrypt2HexStr(key, payChannelInfo.getNotifyUrl()));
//            parameters.put("ip", ThreeDesUtil.encrypt2HexStr(key, userAccountFlow.getClientIp()));
//            parameters.put("specCardNo", ThreeDesUtil.encrypt2HexStr(key, PayUtil.getUserBankCardFromRemark
// (userAccountFlow.getRemark())));
//            parameters.put("userId", ThreeDesUtil.encrypt2HexStr(key, userAccountFlow.getUserId().toString()));
            BasePayOrderInfo basePayOrderInfo = new BasePayOrderInfo();
            basePayOrderInfo.setVersion("V2.0");
            basePayOrderInfo.setMerchant(merchant);
            basePayOrderInfo.setDevice(null);
            basePayOrderInfo.setTradeNum(userAccountFlow.getFlowId());
            basePayOrderInfo.setTradeName(CommonConstant.OUT_TRADE_GOODS_NAME);
            basePayOrderInfo.setTradeDesc(null);
            basePayOrderInfo.setTradeTime(DateUtil.formatTime(tradeTime, "yyyyMMddHHmmss"));
            basePayOrderInfo.setAmount(userAccountFlow.getPayAmount() + "");
            basePayOrderInfo.setCurrency("CNY");
            basePayOrderInfo.setNote(null);
            basePayOrderInfo.setCallbackUrl(callBackUrl);
            basePayOrderInfo.setNotifyUrl(payChannelInfo.getNotifyUrl());
            basePayOrderInfo.setIp(userAccountFlow.getClientIp());
            basePayOrderInfo.setUserType(null);
            basePayOrderInfo.setUserId(userAccountFlow.getUserId() + "");
            basePayOrderInfo.setExpireTime(null);
            basePayOrderInfo.setOrderType("1");
            basePayOrderInfo.setIndustryCategoryCode(null);
            basePayOrderInfo.setSpecCardNo(PayUtil.getUserBankCardFromRemark(userAccountFlow.getRemark()));
            basePayOrderInfo.setSpecId(null);
            basePayOrderInfo.setSpecName(null);
            basePayOrderInfo.setPayChannel(null);

            basePayOrderInfo.setVendorId(null);
            basePayOrderInfo.setGoodsInfo(null);
            basePayOrderInfo.setOrderGoodsNum(null);
            basePayOrderInfo.setTermInfo(null);
            basePayOrderInfo.setReceiverInfo(null);
            basePayOrderInfo.setRiskInfo(null);
            filterCharProcess(basePayOrderInfo);

            List<String> unSignedKeyList = new ArrayList<String>();
            unSignedKeyList.add("sign");

            String sign = SignUtil.signRemoveSelectedKeys(basePayOrderInfo, privateKey, unSignedKeyList);
            sign = sign.replaceAll("\\n", "");
            basePayOrderInfo.setSign(sign);

            if (StringUtils.isNotBlank(basePayOrderInfo.getDevice())) {
                basePayOrderInfo.setDevice(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getDevice()));
            }
            basePayOrderInfo.setTradeNum(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getTradeNum()));
            if (StringUtils.isNotBlank(basePayOrderInfo.getTradeName())) {
                basePayOrderInfo.setTradeName(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getTradeName()));
            }
            if (StringUtils.isNotBlank(basePayOrderInfo.getTradeDesc())) {
                basePayOrderInfo.setTradeDesc(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getTradeDesc()));
            }
            basePayOrderInfo.setTradeTime(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getTradeTime()));
            basePayOrderInfo.setAmount(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getAmount()));
            basePayOrderInfo.setCurrency(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getCurrency()));
            if (StringUtils.isNotBlank(basePayOrderInfo.getNote())) {
                basePayOrderInfo.setNote(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getNote()));
            }
            basePayOrderInfo.setCallbackUrl(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getCallbackUrl()));
            basePayOrderInfo.setNotifyUrl(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getNotifyUrl()));
            basePayOrderInfo.setIp(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getIp()));
            if (StringUtils.isNotBlank(basePayOrderInfo.getUserType())) {
                basePayOrderInfo.setUserType(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getUserType()));
            }
            if (StringUtils.isNotBlank(basePayOrderInfo.getUserId())) {
                basePayOrderInfo.setUserId(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getUserId()));
            }
            if (StringUtils.isNotBlank(basePayOrderInfo.getExpireTime())) {
                basePayOrderInfo.setExpireTime(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getExpireTime()));
            }
            if (StringUtils.isNotBlank(basePayOrderInfo.getOrderType())) {
                basePayOrderInfo.setOrderType(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getOrderType()));
            }
            if (StringUtils.isNotBlank(basePayOrderInfo.getIndustryCategoryCode())) {
                basePayOrderInfo
                        .setIndustryCategoryCode(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo
                                .getIndustryCategoryCode()));
            }

            if (StringUtils.isNotBlank(basePayOrderInfo.getSpecCardNo())) {
                basePayOrderInfo.setSpecCardNo(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getSpecCardNo()));
            }
            if (StringUtils.isNotBlank(basePayOrderInfo.getSpecId())) {
                basePayOrderInfo.setSpecId(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getSpecId()));
            }
            if (StringUtils.isNotBlank(basePayOrderInfo.getSpecName())) {
                basePayOrderInfo.setSpecName(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getSpecName()));
            }
            if (StringUtils.isNotBlank(basePayOrderInfo.getPayChannel())) {
                basePayOrderInfo.setPayChannel(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getPayChannel()));
            }
            if (StringUtils.isNotBlank(basePayOrderInfo.getVendorId())) {
                basePayOrderInfo.setVendorId(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getVendorId()));
            }
            if (StringUtils.isNotBlank(basePayOrderInfo.getGoodsInfo())) {
                basePayOrderInfo.setGoodsInfo(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getGoodsInfo()));
            }
            if (StringUtils.isNotBlank(basePayOrderInfo.getOrderGoodsNum())) {
                basePayOrderInfo.setOrderGoodsNum(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getOrderGoodsNum
                        ()));
            }
            if (StringUtils.isNotBlank(basePayOrderInfo.getTermInfo())) {
                basePayOrderInfo.setTermInfo(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getTermInfo()));
            }
            if (StringUtils.isNotBlank(basePayOrderInfo.getReceiverInfo())) {
                basePayOrderInfo.setReceiverInfo(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getReceiverInfo()));
            }
            if (StringUtils.isNotBlank(basePayOrderInfo.getRiskInfo())) {
                basePayOrderInfo.setRiskInfo(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getRiskInfo()));
            }

            if (StringUtils.isNotBlank(basePayOrderInfo.getCert())) {
                basePayOrderInfo.setCert(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getCert()));
            }


            try {
                basePayOrderInfo.setSign(URLEncoder.encode(basePayOrderInfo.getSign(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            // 把prepayId返回调用方
            SortedMap<String, String> parameterMap = new TreeMap<>();
            parameterMap.put("appid", "");
            parameterMap.put("partnerid", merchant);
            parameterMap.put("prepayid", PayUtil.getJDPayUrl(BeanMapUtil.beanToMap(basePayOrderInfo)));
            return parameterMap;
        }

        @Override
        public Map<String, Object> payQuery(UserAccountFlow userAccountFlow) {
            return null;
        }
    }, WX_JSAPI(CommonConstant.WX_PAY_CHANNEL_WX_JSAPI, CommonConstant.WX_JSAPI_PAY_ENUM_NAME) {
        @Override
        public Map<String, String> getChannelPrePay(UserAccountFlow userAccountFlow, Integer clientId) {

            Map<String, Object> remarkMap = JSONObject.parseObject(userAccountFlow.getRemark(), HashMap.class);
            if (!remarkMap.containsKey("openId") || remarkMap.get("openId") == null) {
                return null;
            }
            String openId = remarkMap.get("openId").toString();

            // 支付渠道配置
            PayChannelInfo payChannelInfo = PayChannelInfoCache.getChannelInfo(userAccountFlow.getChannel());

            // 根据客户端ID获取支付配置
            String payClientChannelKey = String.valueOf(clientId) + CommonConstant.COMMON_COLON_STR + String.valueOf
                    (userAccountFlow.getChannel());
            PayClientChannel payClientChannel = PayClientChannelCache.getClientChannel(payClientChannelKey);
            Map<String, String> payKeyStr = (Map<String, String>) JSONObject.parse(payClientChannel.getPayKeyStr());

            String appid = payKeyStr.get(IniConstant.WX_JSAPI_PAY_APP_ID); //微信开放平台审核通过的应用APPID
            String mch_id = payKeyStr.get(IniConstant.WX_JSAPI_PAY_MCH_ID); //微信支付分配的商户号
            String key = payKeyStr.get(IniConstant.WX_JSAPI_PAY_KEY);


            // 拼接支付串
            String nonce_str = PayUtil.wxNonceStr(); //随机字符串，不长于32位。推荐随机数生成算法
            SortedMap<String, String> parameters = new TreeMap<>();
            parameters.put("appid", appid);
            parameters.put("mch_id", mch_id);
            parameters.put("nonce_str", nonce_str);
            parameters.put("fee_type", "CNY");
            parameters.put("notify_url", payChannelInfo.getNotifyUrl());//
            parameters.put("trade_type", "JSAPI");
            parameters.put("body", CommonConstant.OUT_TRADE_GOODS_NAME);
            parameters.put("spbill_create_ip", userAccountFlow.getClientIp());// 客户端IP
            parameters.put("out_trade_no", userAccountFlow.getFlowId()); // 订单id这里我的订单id生成规则是订单id+时间
            parameters.put("total_fee", userAccountFlow.getPayAmount().toString()); //
            parameters.put("openid", openId); //
            // 测试时，每次支付一分钱，微信支付所传的金额是以分为单位的，因此实际开发中需要x100

            String sign = PayUtil.wxSignStr("UTF-8", parameters, key);
            parameters.put("sign", sign);
            // 封装请求参数结束
            String requestXML = PayUtil.getRequestXml(parameters); // 获取xml结果
            // 调用统一下单接口
            String result = HttpServiceUtils.sendHttpsPostRequest(payChannelInfo.getPayUrl(), requestXML, "UTF-8");

            // 把prepayId返回调用方
            SortedMap<String, String> parameterMap = new TreeMap<>();
            try {
                Document document = DocumentHelper.parseText(result);
                Map<String, Object> resultXmlMap = PayUtil.convertNodesFromXml(document);
                //Element prepayid = document.getRootElement().element("prepay_id");
                parameterMap.put("appId", appid);
                parameterMap.put("package", "prepay_id=" + resultXmlMap.get("prepay_id").toString());
                parameterMap.put("nonceStr", PayUtil.wxNonceStr());
                // 本来生成的时间戳是13位，但是ios必须是10位，所以截取了一下
                parameterMap.put("timeStamp", String.valueOf(System.currentTimeMillis()).substring(0, 10));
                parameterMap.put("signType", "MD5");
                parameterMap.put("paySign", PayUtil.wxSignStr("UTF-8", parameterMap, key));


            } catch (Exception e) {
                e.printStackTrace();
            }
            // 返回客户端拉起支付需要的信息
            // appid partnerid prepayid package noncestr timestamp sign
            return parameterMap;
        }

        @Override
        public Map<String, Object> payQuery(UserAccountFlow userAccountFlow) {
            return null;
        }

    }, HAODIAN_ALI(CommonConstant.HAO_DIAN_PAY_CHANNEL_ID, CommonConstant.HAO_DIAN_PAY_ENUM_NAME) {
        @Override
        public Map<String, String> getChannelPrePay(UserAccountFlow userAccountFlow, Integer clientId) {
            Map<String, String> result = new HashMap<>();
            // 支付渠道配置
            PayChannelInfo payChannelInfo = PayChannelInfoCache.getChannelInfo(userAccountFlow.getChannel());

            // 根据客户端ID获取支付配置
            String payClientChannelKey = String.valueOf(clientId) + CommonConstant.COMMON_COLON_STR + String.valueOf
                    (userAccountFlow.getChannel());
            PayClientChannel payClientChannel = PayClientChannelCache.getClientChannel(payClientChannelKey);
            Map<String, String> payKeyStr = (Map<String, String>) JSONObject.parse(payClientChannel.getPayKeyStr());

            String dsId = payKeyStr.get(IniConstant.HAO_DIAN_PAY_DS_ID);
            String mchId = payKeyStr.get(IniConstant.HAO_DIAN_PAY_MCH_ID);
            String md5Key = payKeyStr.get(IniConstant.HAO_DIAN_PAY_MD5_KEY);
            String callBackUrl = payKeyStr.get(IniConstant.HAO_DIAN_PAY_NOTIFY_URL) + "?flowId=" + userAccountFlow.getFlowId();

            Map<String, Object> webPayParam = new HashMap<>();
            webPayParam.put("mch_id", mchId);
            webPayParam.put("ds_trade_no", PayUtil.packageHaoDianFlowId(userAccountFlow.getFlowId()));
            webPayParam.put("pay_fee", CommonUtil.convertFen2Yuan(userAccountFlow.getPayAmount()).toString());
            webPayParam.put("trade_type", "AP");
            webPayParam.put("user_ip", userAccountFlow.getClientIp());
            webPayParam.put("notify_url", payChannelInfo.getNotifyUrl());
            webPayParam.put("callback_url", callBackUrl);
            webPayParam.put("trade_subject", "智慧预测");

            SortedMap<String, String> httpParam = new TreeMap<>();
            httpParam.put("ds_id", dsId);
            httpParam.put("timestamp", DateUtil.getCurrentTimeMillis());
            httpParam.put("version", "1.0");
            httpParam.put("sign_type", "MD5");
            httpParam.put("biz_content", JSONObject.toJSONString(webPayParam));

            String httpParamStr = PayUtil.getSortedKeyString(httpParam) + md5Key;
            httpParam.put("sign", Md5Util.getMD5String(httpParamStr).toUpperCase());
            try {
                httpParam.put("biz_content", URLEncoder.encode(JSONObject.toJSONString(webPayParam), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String payUrl = payChannelInfo.getPayUrl() + "?" + PayUtil.getSortedKeyString(httpParam) + "&sign=" +
                    httpParam.get("sign");
            String res = HttpServiceUtils.sendRequest(payUrl);
            String prepayUrl = "";
            if (StringUtils.isNotBlank(res)) {
                Map<String, String> resMap = JSONObject.parseObject(res, HashMap.class);
                if (resMap.containsKey("status") && String.valueOf(resMap.get("status")).equals("0")) {
                    prepayUrl = resMap.get("prepay_url");
                }
            }
            result.put("prepayid", prepayUrl);
            result.put("flowId", userAccountFlow.getFlowId());
            return result;
        }

        @Override
        public Map<String, Object> billQuery(UserAccountFlow userAccountFlow, Timestamp timestamp) {
            Map<String, Object> result = new HashMap<>();

            // 根据客户端ID获取支付配置
            String payClientChannelKey = String.valueOf(CommonConstant.CLIENT_TYPE_ANDRIOD) + CommonConstant
                    .COMMON_COLON_STR + String.valueOf(userAccountFlow.getChannel());
            PayClientChannel payClientChannel = PayClientChannelCache.getClientChannel(payClientChannelKey);
            Map<String, String> payKeyStr = (Map<String, String>) JSONObject.parse(payClientChannel.getPayKeyStr());

            String dsId = payKeyStr.get(IniConstant.HAO_DIAN_PAY_DS_ID);
            String mchId = payKeyStr.get(IniConstant.HAO_DIAN_PAY_MCH_ID);
            String md5Key = payKeyStr.get(IniConstant.HAO_DIAN_PAY_MD5_KEY);
            String url = "http://openapi.zhifu6688.com:30010/sett/paybillquery";

            Map<String, Object> webPayParam = new HashMap<>();
            webPayParam.put("mch_id", mchId);
            webPayParam.put("bill_date", DateUtil.formatTime(timestamp, "yyyy-MM-dd"));
            webPayParam.put("bill_type", "PAY");
            webPayParam.put("page_no", 1);

            SortedMap<String, String> httpParam = new TreeMap<>();
            httpParam.put("ds_id", dsId);
            httpParam.put("timestamp", DateUtil.getCurrentTimeMillis());
            httpParam.put("version", "1.0");
            httpParam.put("sign_type", "MD5");
            httpParam.put("biz_content", JSONObject.toJSONString(webPayParam));

            String httpParamStr = PayUtil.getSortedKeyString(httpParam) + md5Key;
            httpParam.put("sign", Md5Util.getMD5String(httpParamStr).toUpperCase());
            try {
                httpParam.put("biz_content", URLEncoder.encode(JSONObject.toJSONString(webPayParam), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String payUrl = url + "?" + PayUtil.getSortedKeyString(httpParam) + "&sign=" + httpParam.get("sign");
            String res = HttpServiceUtils.sendRequest(payUrl);
            String prepayUrl = "";
            if (StringUtils.isNotBlank(res)) {
                Map<String, String> resMap = JSONObject.parseObject(res, HashMap.class);
                if (resMap.containsKey("status") && String.valueOf(resMap.get("status")).equals("0")) {
                    prepayUrl = resMap.get("prepay_url");
                }
            }

            return null;
        }

        @Override
        public Map<String, Object> payQuery(UserAccountFlow userAccountFlow) {
            return null;
        }
    };

    //标准收银台——拼接支付链接
    public static String getUrl(Map<String, String> paramValues, String merchantNo, String privateKey) {
        String[] CASHIER = {"merchantNo", "token", "timestamp", "directPayType", "cardType", "userNo", "userType"};

        StringBuffer url = new StringBuffer();
        url.append("https://cash.yeepay.com/cashier/std");
        StringBuilder stringBuilder = new StringBuilder();
        System.out.println(paramValues);
        for (int i = 0; i < CASHIER.length; i++) {
            String name = CASHIER[i];
            String value = paramValues.get(name);
            if (i != 0) {
                stringBuilder.append("&");
            }
            if (StringUtils.isBlank(value)) {
                value = "";
            }
            stringBuilder.append(name + "=").append(value);
        }
        System.out.println("stringbuilder:" + stringBuilder);
        String sign = getSign(stringBuilder.toString(), merchantNo, privateKey);
        url.append("?sign=" + sign + "&" + stringBuilder);
        return url.toString();
    }

    //获取sign
    public static String getSign(String stringBuilder, String merchantNo, String privateKey) {
        try {
            String appKey = "OPR:" + merchantNo;
            PrivateKey isvPrivateKey = CommonUtil.getPrivateKey(privateKey);
            DigitalSignatureDTO digitalSignatureDTO = new DigitalSignatureDTO();
            digitalSignatureDTO.setAppKey(appKey);
            digitalSignatureDTO.setCertType(CertTypeEnum.RSA2048);
            digitalSignatureDTO.setDigestAlg(DigestAlgEnum.SHA256);
            digitalSignatureDTO.setPlainText(stringBuilder.toString());
            String sign = DigitalEnvelopeUtils.sign0(digitalSignatureDTO, isvPrivateKey);
            return sign;
        } catch (Exception e) {
            return null;
        }
    }

    public static Map<String, String> requestYOP(Map<String, String> params, String uri, String[] paramSign,
                                                 String merchantNo, String parentMerchantNo, String privateKey) {
        Map<String, String> result = new HashMap<String, String>();
        String BASE_URL = "https://open.yeepay.com/yop-center";
        params.put("parentMerchantNo", parentMerchantNo);
        params.put("merchantNo", merchantNo);
        /**
         * 第二种方式：只传appkey
         */
        YopRequest request = new YopRequest("OPR:" + merchantNo, privateKey, BASE_URL);

        for (int i = 0; i < paramSign.length; i++) {
            String key = paramSign[i];
            request.addParam(key, params.get(key));
        }
        System.out.println(request.getParams());
        try {
            YopResponse response = YopClient3.postRsa(uri, request);
            System.out.println(response.toString());
            if ("FAILURE".equals(response.getState())) {
                if (response.getError() != null)
                    result.put("code", response.getError().getCode());
                result.put("message", response.getError().getMessage());
                return result;
            }
            if (response.getStringResult() != null) {
                result = parseResponse(response.getStringResult());
            }
        } catch (Exception e) {

        }
        return result;
    }

    //将获取到的response解密完成的json转换成map
    public static Map<String, String> parseResponse(String response) {

        Map<String, String> jsonMap = new HashMap<>();
        jsonMap = JSON.parseObject(response, new TypeReference<TreeMap<String, String>>() {
        });
        return jsonMap;
    }

    protected Map<String, Object> wxPayQuery(UserAccountFlow userAccountFlow, String wxQueryTypeClose) {
        // 渠道配置信息
        String payClientChannelKey = String.valueOf(userAccountFlow.getClientId()) + CommonConstant
                .COMMON_COLON_STR + String.valueOf(userAccountFlow.getChannel());
        PayClientChannel payClientChannel = PayClientChannelCache.getClientChannel(payClientChannelKey);
        Map<String, String> payKeyStr = (Map<String, String>) JSONObject.parse(payClientChannel.getPayKeyStr());
        String appid = payKeyStr.get(IniConstant.WX_PAY_APP_ID); //微信开放平台审核通过的应用APPID
        String mch_id = payKeyStr.get(IniConstant.WX_PAY_MCH_ID); //微信支付分配的商户号
        String key = payKeyStr.get(IniConstant.WX_PAY_KEY);
        String queryUrl = "https://api.mch.weixin.qq.com/pay/orderquery";
        if (wxQueryTypeClose.equals(CommonConstant.WX_QUERY_TYPE_CLOSE)) {
            queryUrl = "https://api.mch.weixin.qq.com/pay/closeorder";
        }

        // 拼接支付串
        String nonce_str = PayUtil.wxNonceStr(); //随机字符串，不长于32位。推荐随机数生成算法
        SortedMap<String, String> parameters = new TreeMap<>();
        parameters.put("appid", appid);
        parameters.put("mch_id", mch_id);
        parameters.put("nonce_str", nonce_str);
        parameters.put("out_trade_no", userAccountFlow.getFlowId());

        String sign = PayUtil.wxSignStr("UTF-8", parameters, key);
        parameters.put("sign", sign);
        // 封装请求参数结束
        String requestXML = PayUtil.getRequestXml(parameters); // 获取xml结果
        // 调用统一下单接口
        String result = HttpServiceUtils.sendHttpsPostRequest(queryUrl, requestXML, "UTF-8");

        // 解析
        try {
            Document document = DocumentHelper.parseText(result);
            Map<String, Object> params = PayUtil.convertNodesFromXml(document);
            if ("SUCCESS".equals(params.get("return_code"))) {
                return params;
            }
        } catch (Exception e) {
            log.error("wx query order parse xml error" + result);
        }
        return null;
    }

    protected Logger log = LogConstant.commonLog;

    private Integer channel;
    private String channelName;

    PayChannelEnum(Integer channel, String channelName) {
        this.channel = channel;
        this.channelName = channelName;
    }

    public Integer getChannel() {
        return this.channel;
    }

    public String getChannelName() {
        return this.channelName;
    }

    public static PayChannelEnum getPayChannelEnum(Integer channel) {
        for (PayChannelEnum channelEnum : values()) {
            if (channelEnum.getChannel().equals(channel)) {
                return channelEnum;
            }
        }
        return null;
    }

    public static PayChannelEnum getPayChannelEnum(String channelName) {
        for (PayChannelEnum channelEnum : values()) {
            if (channelEnum.getChannelName().equals(channelName)) {
                return channelEnum;
            }
        }
        return null;
    }

    /*
     * 预支付ID
     * */
    public Map<String, String> getChannelPrePay(UserAccountFlow userAccountFlow, Integer clientId) {
        throw new AbstractMethodError("getChannelPrePay error");
    }

    /*
     * 支付通知解析
     * */
    public Map<String, String> payNotify(UserAccountFlow userAccountFlow) {
        throw new AbstractMethodError("getChannelPrePay error");
    }

    /*
     * 支付查询
     * */
    public Map<String, Object> payQuery(UserAccountFlow userAccountFlow) {
        throw new AbstractMethodError("getChannelPrePay error");
    }

    /**
     * 关闭交易
     */
    public Map<String, Object> payClose(UserAccountFlow userAccountFlow) {
        throw new AbstractMethodError("getPayClose error");
    }

    /**
     * 对账查询
     *
     * @return
     */
    public Map<String, Object> billQuery(UserAccountFlow userAccountFlow, Timestamp date) {
        throw new AbstractMethodError("billQuery");
    }

    public Map<String, Object> getsignkey() {
        throw new AbstractMethodError("getsignkey error");
    }

    public Integer getAccountType() {
        return CommonConstant.ACCOUNT_TYPE_CASH;
    }

    private static void filterCharProcess(BasePayOrderInfo basePayOrderInfo) {
        basePayOrderInfo.setVersion(doFilterCharProcess(basePayOrderInfo.getVersion()));
        basePayOrderInfo.setMerchant(doFilterCharProcess(basePayOrderInfo.getMerchant()));
        basePayOrderInfo.setDevice(doFilterCharProcess(basePayOrderInfo.getDevice()));
        basePayOrderInfo.setTradeNum(doFilterCharProcess(basePayOrderInfo.getTradeNum()));
        basePayOrderInfo.setTradeName(doFilterCharProcess(basePayOrderInfo.getTradeName()));
        basePayOrderInfo.setTradeDesc(doFilterCharProcess(basePayOrderInfo.getTradeDesc()));
        basePayOrderInfo.setTradeTime(doFilterCharProcess(basePayOrderInfo.getTradeTime()));
        basePayOrderInfo.setAmount(doFilterCharProcess(basePayOrderInfo.getAmount()));
        basePayOrderInfo.setCurrency(doFilterCharProcess(basePayOrderInfo.getCurrency()));
        basePayOrderInfo.setNote(doFilterCharProcess(basePayOrderInfo.getNote()));
        basePayOrderInfo.setCallbackUrl(doFilterCharProcess(basePayOrderInfo.getCallbackUrl()));
        basePayOrderInfo.setNotifyUrl(doFilterCharProcess(basePayOrderInfo.getNotifyUrl()));
        basePayOrderInfo.setIp(doFilterCharProcess(basePayOrderInfo.getIp()));
        basePayOrderInfo.setUserType(doFilterCharProcess(basePayOrderInfo.getUserType()));
        basePayOrderInfo.setUserId(doFilterCharProcess(basePayOrderInfo.getUserId()));
        basePayOrderInfo.setExpireTime(doFilterCharProcess(basePayOrderInfo.getExpireTime()));
        basePayOrderInfo.setOrderType(doFilterCharProcess(basePayOrderInfo.getOrderType()));
        basePayOrderInfo.setIndustryCategoryCode(doFilterCharProcess(basePayOrderInfo.getIndustryCategoryCode()));
        basePayOrderInfo.setSpecCardNo(doFilterCharProcess(basePayOrderInfo.getSpecCardNo()));
        basePayOrderInfo.setSpecId(doFilterCharProcess(basePayOrderInfo.getSpecId()));
        basePayOrderInfo.setSpecName(doFilterCharProcess(basePayOrderInfo.getSpecName()));

        basePayOrderInfo.setVendorId(doFilterCharProcess(basePayOrderInfo.getVendorId()));
        basePayOrderInfo.setGoodsInfo(doFilterCharProcess(basePayOrderInfo.getGoodsInfo()));
        basePayOrderInfo.setOrderGoodsNum(doFilterCharProcess(basePayOrderInfo.getOrderGoodsNum()));
        basePayOrderInfo.setTermInfo(doFilterCharProcess(basePayOrderInfo.getTermInfo()));
        basePayOrderInfo.setReceiverInfo(doFilterCharProcess(basePayOrderInfo.getReceiverInfo()));

    }

    /**
     * @throws
     * @Title: doFilterCharProcess
     * @Description: 执行特殊字符处理
     * @param: @param param
     * @param: @return
     * @return: String
     * @author mythling
     * @Date 2016年8月6日 下午3:54:58
     */
    private static String doFilterCharProcess(String param) {
        if (param == null || param.equals("")) {
            return param;
        } else {
            return StringEscape.htmlSecurityEscape(param);
        }
    }
}
