package com.mojieai.predict.util;


import com.mojieai.predict.constant.LogConstant;
import org.apache.logging.log4j.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/*
 * AES加解密算法
 *
 * @author singal
 */

public class AESUtil {
    private static final Logger log = LogConstant.commonLog;

    // 加密
    public static String encrypt(String sSrc, String sKey) {
        try {
            if (sSrc == null)
                return null;
            if (sKey == null) {
                return null;
            }
            // 判断Key是否为16位
            if (sKey.length() != 16) {
                return null;
            }
            byte[] raw = sKey.getBytes();
            SecretKeySpec sKeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");// "算法/模式/补码方式"
            IvParameterSpec iv = new IvParameterSpec("0102030405060708".getBytes());// 使用CBC模式，需要一个向量iv，可增加加密算法的强度
            cipher.init(Cipher.ENCRYPT_MODE, sKeySpec, iv);
            byte[] encrypted = cipher.doFinal(sSrc.getBytes());

            return Base64.encodeS(encrypted);// 此处使用BASE64做转码功能，同时能起到2次加密的作用。
        } catch (Throwable e) {
            log.warn("AESUtil encrypt error" + sSrc + "|" + sKey, e);
            return null;
        }
    }

    // 解密
    public static String decrypt(String sSrc, String sKey) {
        try {
            // 判断Key是否正确
            if (sKey == null) {
                return null;
            }
            // 判断Key是否为16位
            if (sKey.length() != 16) {
                return null;
            }
            byte[] raw = sKey.getBytes("ASCII");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec("0102030405060708".getBytes());
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] encrypted1 = Base64.decode(sSrc);// 先用base64解密
            byte[] original = cipher.doFinal(encrypted1);
            String originalString = new String(original);
            return originalString;
        } catch (Throwable th) {
            log.warn("AESUtil encrypt error" + sSrc + "|" + sKey, th);
            return null;
        }
    }

    public static void main(String[] args) {
        // * 加密用的Key 可以用26个字母和数字组成，最好不要用保留字符
        // * 此处使用AES-128-CBC加密模式，key需要为16位。
        // */
        String cKey = "a6993a8b56f5d376";
        // 需要加密的字串
        String cSrc = "s12345478979";
        System.out.println(cSrc);
        // 加密
        long lStart = System.currentTimeMillis();
        String enString = AESUtil.encrypt(cSrc, cKey);
        System.out.println("加密后的字串是：" + enString);

        long lUseTime = System.currentTimeMillis() - lStart;
        System.out.println("加密耗时：" + lUseTime + "毫秒");
        // 解密
        enString = "htgQkBZSmTjY+b/annoPxQ==";
        cKey = "iog37g76cb5nhg9f";

        lStart = System.currentTimeMillis();
        String DeString = AESUtil.decrypt(enString, cKey);
        System.out.println("解密后的字串是：" + DeString);
        lUseTime = System.currentTimeMillis() - lStart;
        System.out.println("解密耗时：" + lUseTime + "毫秒");
    }
}
