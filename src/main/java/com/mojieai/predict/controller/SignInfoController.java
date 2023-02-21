package com.mojieai.predict.controller;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.util.AESUtil;
import com.mojieai.predict.util.Base64;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.PropertyUtils;
import com.mojieai.predict.util.qiniu.Auth;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequestMapping("/signInfo")
@Controller
public class SignInfoController extends BaseController {

    @RequestMapping("/init")
    @ResponseBody
    public Object initSign() {
        String encrypt = null;
        String serviceSignKey = null;
        String plainTextSignKey = null;
        Map<String, String> signInfo = new HashMap<>();

        try {
            serviceSignKey = PropertyUtils.getProperty("signPublicKey");
            if (StringUtils.isNotBlank(serviceSignKey)) {
                Map tempMap = new HashMap();

                plainTextSignKey = UUID.randomUUID().toString().replace(CommonConstant.COMMON_DASH_STR, "")
                        .substring(0, 16);
                tempMap.put("plainTextSignKey", plainTextSignKey);
                tempMap.put("disturbNum", System.currentTimeMillis());
                encrypt = AESUtil.encrypt(JSONObject.toJSONString(tempMap), serviceSignKey);
            }
        } catch (Exception e) {
            log.error("init signInfo error" + e);
            return buildErrJson("请重试");
        }
        signInfo.put("plainTextSignKey", plainTextSignKey);
        signInfo.put("signKey", encrypt);
        return buildSuccJson(signInfo);
    }

    @RequestMapping("/genUpToken1")
    @ResponseBody
    public Object genUpToken(@RequestParam String imgName) {
        Map<String, Object> upTokenMap = new HashMap<>();

        String uploadToken = "";
        String myBucket = PropertyUtils.getProperty("headImgBucket");
        String accessKey = PropertyUtils.getProperty("accessKey");
        String secretKey = PropertyUtils.getProperty("secretKey");
        Timestamp deadline = DateUtil.getIntervalSeconds(DateUtil.getCurrentTimestamp(), 3600);

        try {
            //1.生成encode putPolicy
            Map<String, Object> putPolicyMap = new HashMap<>();

            putPolicyMap.put("scope", myBucket + CommonConstant.COMMON_COLON_STR + imgName);
            putPolicyMap.put("deadline", deadline.getTime());
            String putPolicy = JSONObject.toJSONString(putPolicyMap);
            //URL 安全的 Base64 编码，
            String encodedPutPolicy = Base64.encodeS(putPolicy.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET));

            //2.生成encodedSign
            //对上一步生成的待签名字符串计算HMAC-SHA1签名 并对签名进行URL安全的Base64编码
            Mac mac = createMac(secretKey);
            String encodedSign = Base64.encodeS(mac.doFinal(encodedPutPolicy.getBytes(RedisConstant
                    .REDIS_DEFAULT_CHARSET)));

            //3.拼接uploadKey
            uploadToken = accessKey + CommonConstant.COMMON_COLON_STR + encodedSign + CommonConstant
                    .COMMON_COLON_STR + encodedPutPolicy;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        upTokenMap.put("uploadToken", uploadToken);
        return buildSuccJson(upTokenMap);
    }

    private Mac createMac(String secretKey) {
        Mac mac = null;
        try {
            SecretKeySpec secretKeyS = new SecretKeySpec(secretKey.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET),
                    "HmacSHA1");

            mac = Mac.getInstance("HmacSHA1");
            mac.init(secretKeyS);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return mac;
    }

    @RequestMapping("/genUpToken")
    @ResponseBody
    public Object genUpToken() {
        Map<String, Object> upTokenMap = new HashMap<>();
        String myBucket = PropertyUtils.getProperty("headImgBucket");
        String accessKey = PropertyUtils.getProperty("accessKey");
        String secretKey = PropertyUtils.getProperty("secretKey");
        String domainUrl = PropertyUtils.getProperty("domainUrl");

        String uploadToken = Auth.create(accessKey, secretKey).uploadToken(myBucket);

        upTokenMap.put("uploadToken", uploadToken);
        upTokenMap.put("domain", domainUrl);
        return buildSuccJson(upTokenMap);
    }
}
