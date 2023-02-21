package com.mojieai.predict.util;

import com.mojieai.predict.constant.LogConstant;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


/**
 * properties帮助类 默认加载config.properties
 * 
 * @author Singal
 */
public class PropertyUtils {
	private static Logger log = LogConstant.commonLog;

	private static final String CONFIG_PROPERTIES = "config.properties";

	private static Map<String, String> configMap = new HashMap<String, String>();

	static {
		load(CONFIG_PROPERTIES);
	}

	public static String getProperty(String key) {
		if (StringUtils.isBlank(key)) {
			return null;
		}
		return configMap.get(key);
	}

	public static String getProperty(String key, String defaultValue) {
		if (StringUtils.isEmpty(key)) {
			return (StringUtils.isEmpty(defaultValue) ? null : defaultValue);
		}
		return (StringUtils.isEmpty(configMap.get(key)) ? defaultValue
				: configMap.get(key));
	}

	@SuppressWarnings("rawtypes")
	private static void load(String name) {
		InputStream is = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(name);
		Properties p = new Properties();
		try {
			p.load(is);
			if (CONFIG_PROPERTIES.equals(name)) {
				for (Map.Entry e : p.entrySet()) {
					configMap.put((String) e.getKey(), (String) e.getValue());
				}
			}

		} catch (IOException e) {
            log.warn("load property file failed. file name: "
                    + name, e);
		}
	}

	public static int getPropertyIntValue(String key, int defaultValue) {
		if (StringUtils.isEmpty(key)) {
			return defaultValue;
		}
		return (StringUtils.isEmpty(configMap.get(key)) || !isInt(configMap
				.get(key))) ? defaultValue : Integer.parseInt(configMap
				.get(key));
	}

	private static boolean isInt(String str) {
		if (isEmpty(str))
			return false;
		try {
			Integer.parseInt(str);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private static boolean isEmpty(String str) {
		return str == null || str.trim().isEmpty();
	}

	public static void main(String[] args) {
		System.out.println(getProperty("shop.netease.prikey"));
	}
}
