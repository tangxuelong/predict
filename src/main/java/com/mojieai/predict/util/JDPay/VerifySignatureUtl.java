package com.mojieai.predict.util.JDPay;

import com.mojieai.predict.util.SHAUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;

public class VerifySignatureUtl {
    private static final Logger logger = Logger.getLogger(VerifySignatureUtl.class);

    public VerifySignatureUtl() {
    }

    public static String encryptMerchant(String sourceSignString, String rsaPriKey) {
        logger.debug("encrypt merchant: sourceSignString=[" + sourceSignString + "]");
        String result = "";

        try {
            String sha256SourceSignString = SHAUtil.Encrypt(sourceSignString, (String)null);
            byte[] newsks = RSACoder.encryptByPrivateKey(sha256SourceSignString.getBytes("UTF-8"), rsaPriKey);
            result = Base64.encodeBase64String(newsks);
            return result;
        } catch (Exception var5) {
            logger.error("encrypt merchant error.", var5);
            throw new RuntimeException("verify signature failed.", var5);
        }
    }

    public static boolean decryptMerchant(String strSourceData, String signData, String rsaPubKey) {
        boolean flag = false;
        logger.debug("verify signature: strSourceData=[" + strSourceData + "]");
        if (signData != null && !signData.isEmpty()) {
            if (rsaPubKey != null && !rsaPubKey.isEmpty()) {
                try {
                    String sha256SourceSignString = SHAUtil.Encrypt(strSourceData, (String)null);
                    logger.debug("verify signature: sha256SourceSignString=[" + sha256SourceSignString + "]");
                    byte[] signByte = Base64.decodeBase64(signData);
                    logger.debug("verify signature: pubKey=[" + rsaPubKey + "]");
                    byte[] decryptArr = RSACoder.decryptByPublicKey(signByte, rsaPubKey);
                    String decryptStr = RSACoder.bytesToString(decryptArr);
                    logger.debug("verify signature: decryptStr=[" + decryptStr + "]");
                    if (sha256SourceSignString.equals(decryptStr)) {
                        flag = true;
                        logger.debug("verify signature: result=[verify successfully.]");
                        return flag;
                    } else {
                        logger.debug("verify signature: result=[verify failed]");
                        throw new RuntimeException("Signature verification failed.");
                    }
                } catch (UnsupportedEncodingException var8) {
                    logger.error("验证商户签名失败" + var8.getMessage());
                    throw new RuntimeException("verify signature failed.", var8);
                } catch (RuntimeException var9) {
                    logger.error("验证商户签名失败" + var9.getMessage());
                    throw var9;
                } catch (Exception var10) {
                    logger.error("验证商户签名失败" + var10.getMessage());
                    throw new RuntimeException("verify signature failed.", var10);
                }
            } else {
                throw new IllegalArgumentException("Argument 'key' is null or empty");
            }
        } else {
            throw new IllegalArgumentException("Argument 'signData' is null or empty");
        }
    }
}
