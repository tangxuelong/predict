/**   
 * @Title: StringEscape.java 
 * @Package com.jd.jr.pay.demo.util 
 * @Description: TODO(用一句话描述该文件做什么) 
 * @author mythling   
 * @date 2016年8月6日 下午3:43:32 
 * @version V1.0   
 */
package com.mojieai.predict.util.JDPay;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: StringEscape
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author mythling
 * @date 2016年8月6日 下午3:43:32
 * @version V1.0
 */
public class StringEscape {
	private static StringEscape instance = new StringEscape();

	public static StringEscape getInstance() {
		return instance;
	}

	public static List<String> htmlSecurityEscape(List<String> strList) {
		ArrayList contentList = new ArrayList();
		for (String str : strList) {
			contentList.add(htmlSecurityEscape(str));
		}
		return contentList;
	}

	public static String htmlSecurityEscape(String str) {
		if (str == null) {
			return null;
		}
		StringBuilder sb = null;
		int len = str.length();
		try {
			for (int i = 0; i < len; ++i) {
				char ch = str.charAt(i);
				switch (ch) {
//				case '\'':
//					if (sb == null) {
//						sb = new StringBuilder(str.length() << 1);
//						sb.append(str, 0, i);
//					}
//					sb.append("&#39;");
//					break;
//				case '"':
//					if (sb == null) {
//						sb = new StringBuilder(str.length() << 1);
//						sb.append(str, 0, i);
//					}
//					sb.append("&quot;");
//					break;
//				case '\\':
//					if (sb == null) {
//						sb = new StringBuilder(str.length() << 1);
//						sb.append(str, 0, i);
//					}
//					sb.append("\\&shy;");
//					break;
				case '>':
					if (sb == null) {
						sb = new StringBuilder(str.length() << 1);
						sb.append(str, 0, i);
					}
					sb.append("&gt;");
					break;
				case '<':
					if (sb == null) {
						sb = new StringBuilder(str.length() << 1);
						sb.append(str, 0, i);
					}
					sb.append("&lt;");
					break;
				case '&':
					int in = str.indexOf(59, i + 1);
					if ((in != -1) && (in - i < 9) && (str.substring(i + 1, in).indexOf(38) == -1)) {
						if (sb != null)
							sb.append(ch);
					} else {
						if (sb == null) {
							sb = new StringBuilder(str.length() << 1);
							sb.append(str, 0, i);
						}
						sb.append("&amp;");
					}
					break;
//				case '/':
//					if ((i + 1 < str.length()) && (str.charAt(i + 1) == '*')) {
//						if (sb == null) {
//							sb = new StringBuilder(str.length() << 1);
//							sb.append(str, 0, i);
//						}
//						sb.append("&#47;&#42;");
//						++i;
//					} else if (sb != null) {
//						sb.append(ch);
//					}
//					break;
				case '*':
					if ((i + 1 < str.length()) && (str.charAt(i + 1) == '/')) {
						if (sb == null) {
							sb = new StringBuilder(str.length() << 1);
							sb.append(str, 0, i);
						}
						sb.append("&#42;&#47;");
						++i;
					} else if (sb != null) {
						sb.append(ch);
					}
					break;
				default:
					if ((Character.getDirectionality(ch) == 2) || (Character.getDirectionality(ch) == 16)
							|| (Character.getDirectionality(ch) == 17)) {
						if (sb == null) {
							sb = new StringBuilder(str.length() << 1);
							sb.append(str, 0, i);
						}
					} else if (sb != null)
						sb.append(ch);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(str);
		}
		if (null != sb) {
			return sb.toString();
		}
		return str;
	}

	public static String htmlSecurityUnescape(String str) {
		if (str == null) {
			return null;
		}
		int firstAmp = str.indexOf(38);
		if (firstAmp == -1) {
			return str;
		}
		StringBuilder sb = new StringBuilder(str.length() << 1);
		sb.append(str, 0, firstAmp);

		for (int i = firstAmp; i < str.length(); ++i) {
			char c = str.charAt(i);
			if (c == '&') {
				int nextIdx = i + 1;
				int semiColonIdx = str.indexOf(59, nextIdx);
				if (semiColonIdx == -1) {
					sb.append(c);
				} else {
					int amphersandIdx = str.indexOf(38, nextIdx);

					if ((amphersandIdx != -1) && (amphersandIdx < semiColonIdx)) {
						sb.append(c);
					} else {
						String entityContent = str.substring(nextIdx, semiColonIdx);

						if (entityContent.equalsIgnoreCase("#39"))
							sb.append('\'');
						else if (entityContent.equalsIgnoreCase("#40"))
							sb.append('(');
						else if (entityContent.equalsIgnoreCase("#41"))
							sb.append(')');
						else if (entityContent.equalsIgnoreCase("lt"))
							sb.append('<');
						else if (entityContent.equalsIgnoreCase("gt"))
							sb.append('>');
						else if (entityContent.equalsIgnoreCase("amp"))
							sb.append('&');
						else if (entityContent.equalsIgnoreCase("quot"))
							sb.append('"');
						else {
							sb.append(c);
						}
						i = semiColonIdx;
					}
				}
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

}
