//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.mojieai.predict.util;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreeDes1Util {
    private static final Logger log = LoggerFactory.getLogger(ThreeDes1Util.class);
    private static final int MAX_MSG_LENGTH = 16384;
    private static final String Algorithm = "DESede";
    private static final String PADDING = "DESede/ECB/NoPadding";
    public static final byte[] DEFAULT_KEY = new byte[]{49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 49, 50, 51, 52};

    public ThreeDes1Util() {
    }

    public static byte[] encrypt(byte[] keybyte, byte[] src) {
        try {
            SecretKey deskey = new SecretKeySpec(keybyte, "DESede");
            Cipher c1 = Cipher.getInstance("DESede/ECB/NoPadding");
            c1.init(1, deskey);
            return c1.doFinal(src);
        } catch (NoSuchAlgorithmException var4) {
            var4.printStackTrace();
        } catch (NoSuchPaddingException var5) {
            var5.printStackTrace();
        } catch (Exception var6) {
            var6.printStackTrace();
        }

        return null;
    }

    private static byte[] decrypt(byte[] keybyte, byte[] src) {
        try {
            SecretKey deskey = new SecretKeySpec(keybyte, "DESede");
            Cipher c1 = Cipher.getInstance("DESede/ECB/NoPadding");
            c1.init(2, deskey);
            return c1.doFinal(src);
        } catch (NoSuchAlgorithmException var4) {
            var4.printStackTrace();
        } catch (NoSuchPaddingException var5) {
            var5.printStackTrace();
        } catch (Exception var6) {
            var6.printStackTrace();
        }

        return null;
    }

    private static String byte2Hex(byte[] b) {
        String hs = "";
        String stmp = "";

        for (int n = 0; n < b.length; ++n) {
            stmp = Integer.toHexString(b[n] & 255);
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }

            if (n < b.length - 1) {
                hs = hs + ":";
            }
        }

        return hs.toUpperCase();
    }

    public static String encrypt2HexStr(byte[] keys, String sourceData) {
        byte[] source = new byte[0];

        try {
            source = sourceData.getBytes("UTF-8");
            int merchantData = source.length;
            log.info("原数据据:" + sourceData);
            log.info("原数据byte长度:" + merchantData);
            log.info("原数据HEX表示:" + bytes2Hex(source));
            int x = (merchantData + 4) % 8;
            int y = x == 0 ? 0 : 8 - x;
            log.info("需要补位 :" + y);
            byte[] sizeByte = intToByteArray(merchantData);
            byte[] resultByte = new byte[merchantData + 4 + y];
            resultByte[0] = sizeByte[0];
            resultByte[1] = sizeByte[1];
            resultByte[2] = sizeByte[2];
            resultByte[3] = sizeByte[3];

            int i;
            for (i = 0; i < merchantData; ++i) {
                resultByte[4 + i] = source[i];
            }

            for (i = 0; i < y; ++i) {
                resultByte[merchantData + 4 + i] = 0;
            }

            log.info("补位后的byte数组长度:" + resultByte.length);
            log.info("补位后数据HEX表示:" + bytes2Hex(resultByte));
            log.info("秘钥HEX表示:" + bytes2Hex(keys));
            log.info("秘钥长度:" + keys.length);
            byte[] desdata = encrypt(keys, resultByte);
            log.info("加密后的长度:" + desdata.length);
            return bytes2Hex(desdata);
        } catch (UnsupportedEncodingException var9) {
            var9.printStackTrace();
            return null;
        }
    }

    public static String decrypt4HexStr(byte[] keys, String data) {
        byte[] hexSourceData = new byte[0];

        try {
            hexSourceData = hex2byte(data.getBytes("UTF-8"));
            byte[] unDesResult = decrypt(keys, hexSourceData);
            byte[] dataSizeByte = new byte[]{unDesResult[0], unDesResult[1], unDesResult[2], unDesResult[3]};
            int dsb = byteArrayToInt(dataSizeByte, 0);
            if (dsb > 16384) {
                throw new RuntimeException("msg over MAX_MSG_LENGTH or msg error");
            } else {
                byte[] tempData = new byte[dsb];

                for (int i = 0; i < dsb; ++i) {
                    tempData[i] = unDesResult[4 + i];
                }

                return hex2bin(toHexString(tempData));
            }
        } catch (UnsupportedEncodingException var8) {
            var8.printStackTrace();
            return null;
        }
    }

    private static String hex2bin(String hex) throws UnsupportedEncodingException {
        String digital = "0123456789abcdef";
        char[] hex2char = hex.toCharArray();
        byte[] bytes = new byte[hex.length() / 2];

        for (int i = 0; i < bytes.length; ++i) {
            int temp = digital.indexOf(hex2char[2 * i]) * 16;
            temp += digital.indexOf(hex2char[2 * i + 1]);
            bytes[i] = (byte) (temp & 255);
        }

        return new String(bytes, "UTF-8");
    }

    private static String toHexString(byte[] ba) {
        StringBuilder str = new StringBuilder();

        for (int i = 0; i < ba.length; ++i) {
            str.append(String.format("%x", ba[i]));
        }

        return str.toString();
    }

    private static String bytes2Hex(byte[] bts) {
        String des = "";
        String tmp = null;

        for (int i = 0; i < bts.length; ++i) {
            tmp = Integer.toHexString(bts[i] & 255);
            if (tmp.length() == 1) {
                des = des + "0";
            }

            des = des + tmp;
        }

        return des;
    }

    public static byte[] hex2byte(byte[] b) {
        if (b.length % 2 != 0) {
            throw new IllegalArgumentException("长度不是偶数");
        } else {
            byte[] b2 = new byte[b.length / 2];

            for (int n = 0; n < b.length; n += 2) {
                String item = new String(b, n, 2);
                b2[n / 2] = (byte) Integer.parseInt(item, 16);
            }
            return b2;
        }
    }

    private static byte[] intToByteArray(int i) {
        byte[] result = new byte[]{(byte) (i >> 24 & 255), (byte) (i >> 16 & 255), (byte) (i >> 8 & 255), (byte) (i & 255)};
        return result;
    }

    private static int byteArrayToInt(byte[] b, int offset) {
        int value = 0;

        for (int i = 0; i < 4; ++i) {
            int shift = (3 - i) * 8;
            value += (b[i + offset] & 255) << shift;
        }

        return value;
    }

    public static void main(String[] args) {
        String szSrc = "This is a 3DES test. 测试abcdf";
        System.out.println("加密前的字符串:" + szSrc);
        byte[] encoded = new byte[0];

        try {
            System.out.println("加密前长度:" + szSrc.getBytes("UTF-8").length);
            System.out.println("加密前HEX:" + bytes2Hex(szSrc.getBytes("UTF-8")));
            encoded = encrypt(DEFAULT_KEY, szSrc.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException var5) {
            var5.printStackTrace();
        }

        try {
            System.out.println("加密后长度:" + encoded.length);
            System.out.println("加密后的字符串:" + new String(encoded, "UTF-8"));
        } catch (UnsupportedEncodingException var4) {
            var4.printStackTrace();
        }

        byte[] srcBytes = decrypt(DEFAULT_KEY, encoded);
        System.out.println("解密后HEX:" + bytes2Hex(srcBytes));
        System.out.println("解密后的字符串:" + new String(srcBytes));
    }
}
