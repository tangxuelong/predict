package com.mojieai.predict.util;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


public class GzipUtil
{

	public static void main(String[] args)
	{
		//		gzipCompressByFile("d:/test.txt", "d:/test2.gzip");
		//		unGzipByFile("d:/test2.gzip", "d:/test2.txt");

	}

	public static void gzipCompressByFile(String srcFilePath, String desFilePath)
	{
		try
		{
			//打开需压缩文件作为文件输入流 
			FileInputStream fin = new FileInputStream(srcFilePath);
			//建立压缩文件输出流 
			FileOutputStream fout = new FileOutputStream(desFilePath);
			//建立gzip压缩输出流  
			GZIPOutputStream gzout = new GZIPOutputStream(fout);
			//设定读入缓冲区尺寸 
			byte[] buf = new byte[1024];
			int num;
			while ((num = fin.read(buf)) != -1)
			{
				gzout.write(buf, 0, num);
			}

			gzout.close();
			fout.close();
			fin.close();
		}
		catch (Exception ex)
		{
			System.err.println(ex.toString());
		}
	}

	public static byte[] gZip(byte[] data)
	{
		byte[] b = null;
		try
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			GZIPOutputStream gzip = new GZIPOutputStream(bos);
			gzip.write(data);
			gzip.finish();
			gzip.close();
			b = bos.toByteArray();
			bos.close();
		}
		catch (Exception ex)
		{
			System.err.println(ex.toString());
		}
		return b;
	}

	public static byte[] gZip(File file)
	{
		byte[] b = null;
		try
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			//打开需压缩文件作为文件输入流 
			FileInputStream fin = new FileInputStream(file);
			//建立gzip压缩输出流  
			GZIPOutputStream gzip = new GZIPOutputStream(bos);
			//设定读入缓冲区尺寸 
			byte[] buf = new byte[1024];
			int num;
			while ((num = fin.read(buf)) != -1)
			{
				gzip.write(buf, 0, num);
			}

			gzip.finish();
			gzip.close();
			fin.close();

			b = bos.toByteArray();
			bos.close();
		}
		catch (Exception ex)
		{
			System.err.println(ex.toString());
		}
		return b;
	}

	/**
	 * 
	 * @param srcFilePath
	 * @param desFilePath
	 */
	public static void gzipCompressByStream(InputStream inputStream)
	{
		//		try
		//		{
		//			//打开需压缩文件作为文件输入流 
		//			//FileInputStream fin = new FileInputStream(srcFilePath); 
		//			//建立压缩文件输出流 
		//			FileOutputStream fout = new FileOutputStream(desFilePath);
		//			//建立gzip压缩输出流  
		//			GZIPOutputStream gzout = new GZIPOutputStream(fout);
		//			//设定读入缓冲区尺寸 
		//			byte[] buf = new byte[1024];
		//			int num;
		//			while ((num = fin.read(buf)) != -1)
		//			{
		//				gzout.write(buf, 0, num);
		//			}
		//
		//			gzout.close();
		//			fout.close();
		//			fin.close();
		//		}
		//		catch (Exception ex)
		//		{
		//			System.err.println(ex.toString());
		//		}
	}

	public static void unGzipByFile(String inputFilePath, String outPutFilePath)
	{

		try
		{
			//建立grip压缩文件输入流 
			FileInputStream fin = new FileInputStream(inputFilePath);
			//建立gzip解压工作流 
			GZIPInputStream gzin = new GZIPInputStream(fin);
			//建立解压文件输出流 
			FileOutputStream fout = new FileOutputStream(outPutFilePath);
			byte[] buf = new byte[1024];
			int num;
			while ((num = gzin.read(buf, 0, buf.length)) != -1)
			{
				fout.write(buf, 0, num);
			}

			gzin.close();
			fout.close();
			fin.close();
		}
		catch (Exception ex)
		{
			System.err.println(ex.toString());
		}
	}

	/**
	 * 压缩
	 * 
	 * @param str
	 * @return
	 * @throws IOException
	 */
	public static byte[] compress(String str)
	{
		try
		{
			if (str == null || str.length() == 0)
			{
				return null;
			}
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			GZIPOutputStream gzip = new GZIPOutputStream(out);
			gzip.write(str.getBytes());
			gzip.close();
			return out.toByteArray();
		}
		catch (Exception e)
		{
			System.err.println(e.toString());
			return null;
		}
		// return out.toString("ISO-8859-1");
	}

	/**
	 * 解压缩
	 * 
	 * @param b
	 * @return
	 * @throws IOException
	 */
	public static String uncompress(byte[] b)
	{
		try
		{
			if (b == null || b.length == 0)
			{
				return null;
			}
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ByteArrayInputStream in = new ByteArrayInputStream(b);
			GZIPInputStream gunzip = new GZIPInputStream(in);
			byte[] buffer = new byte[256];
			int n;
			while ((n = gunzip.read(buffer)) >= 0)
			{
				out.write(buffer, 0, n);
			}
			// toString()使用平台默认编码，也可以显式的指定如toString("GBK")
			return out.toString();
		}
		catch (Exception e)
		{
			System.err.println(e.toString());
			return null;
		}
	}
}
