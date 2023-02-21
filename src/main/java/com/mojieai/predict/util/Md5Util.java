package com.mojieai.predict.util;

import com.mojieai.predict.constant.LogConstant;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.security.MessageDigest;

/**
 * Desc: MD5 Util 
 * <p/>Date: 2014/8/28
 * <br/>Time: 10:56
 * <br/>User: ylzhu
 */
public final class Md5Util
{
	private static final Logger log = LogConstant.commonLog;

	private static final char HEX_DIGITS[] =
	{ '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	/**
	 * 生成字符串的md5校验值 
	 *
	 * @param s
	 * @return
	 */
	public static String getMD5String(String s)
	{
		return getMD5String(s.getBytes());
	}

	/**
	 * 生成字符串的md5校验值，指定字符编码方式
	 *
	 * @param s
	 * @return
	 */
	public static String getMD5String(String s, String charset)
	{
		try
		{
			return getMD5String(s.getBytes(charset));
		}
		catch (UnsupportedEncodingException e)
		{
            log.error("md5摘要发生异常，getMD5String有问题，s is " + s + " charset is" + charset, e);
        }
		return null;
	}

	public static String getMD5String(byte[] bytes)
	{
		MessageDigest messagedigest = null;
		try
		{
			messagedigest = MessageDigest.getInstance("MD5");
		}
		catch (Exception e)
		{
            log.warn(e);
        }
		messagedigest.update(bytes);
		return bufferToHex(messagedigest.digest());
	}

	/**
	 * 生成文件的md5校验值 
	 *
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static String getFileMD5String(File file)
	{
		if (file == null || !file.exists())
		{
            log.warn("File not found!" + (file == null ? "null" : file.getAbsolutePath()));
        }
		MessageDigest messagedigest = null;
		try
		{
			messagedigest = MessageDigest.getInstance("MD5");
		}
		catch (Exception e)
		{
            log.warn(e);
        }
		InputStream fis = null;
		try
		{
			fis = new FileInputStream(file);
			byte[] buffer = new byte[1024];
			int numRead = 0;
			while ((numRead = fis.read(buffer)) > 0)
			{
				messagedigest.update(buffer, 0, numRead);
			}
		}
		catch (Exception e)
		{
            log.warn("Get MD5 Error!");
        }
		finally
		{
			if (fis != null)
			{
				try
				{
					fis.close();
				}
				catch (Exception e)
				{
                    log.warn(e);
                }
			}
		}

		return bufferToHex(messagedigest.digest());
	}

	/**
	 * 判断字符串的md5校验码是否与一个已知的md5码相匹配 
	 *
	 * @param sourceStr 要校验的字符串 
	 * @param checkStr 已知的md5校验码 
	 * @return
	 */
	public static boolean check(String sourceStr, String checkStr)
	{
		if (sourceStr == null && checkStr == null)
		{
			return true;
		}
		if (sourceStr == null || checkStr == null)
		{
			return false;
		}
		String s = getMD5String(sourceStr);
		return s.equals(checkStr);
	}

	public static String bufferToHex(byte bytes[])
	{
		return bufferToHex(bytes, 0, bytes.length);
	}

	private static String bufferToHex(byte bytes[], int m, int n)
	{
		StringBuffer stringbuffer = new StringBuffer(2 * n);
		int k = m + n;
		for (int l = m; l < k; l++)
		{
			appendHexPair(bytes[l], stringbuffer);
		}
		return stringbuffer.toString();
	}

	private static void appendHexPair(byte bt, StringBuffer stringbuffer)
	{
		char c0 = HEX_DIGITS[(bt & 0xf0) >>> 4];// 取字节中高 4 位的数字转换
		char c1 = HEX_DIGITS[bt & 0xf];// 取字节中低 4 位的数字转换
		stringbuffer.append(c0);
		stringbuffer.append(c1);
	}

	public static void main(String[] args)
	{
		System.out.println(Md5Util.getMD5String("123"));
	}
}
