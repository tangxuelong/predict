package com.mojieai.predict.constant;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.TimeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.filter.ThresholdFilter;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;
import java.util.Map;

public class LogConstant {
    public static final Logger commonLog = LogManager.getLogger("predict");

    public static void dynamicLog4j2(Map<String, Logger> loggerFactory, String strEnum) {
        if (!loggerFactory.containsKey(strEnum)) {
            loggerFactory.put(strEnum, getLogger(strEnum));
        }
    }

    public static void dynamicLog4j2(Map<String, Logger> loggerFactory, String strEnum, String path) {
        if (!loggerFactory.containsKey(strEnum)) {
            loggerFactory.put(strEnum, getLogger(path, strEnum));
        }
    }

    public static Logger getLogger(String strEnum) {
        return getLogger("", strEnum);
    }

    public static Logger getLogger(String path, String strEnum) {
        LoggerContext ctx = (LoggerContext) LogManager.getContext();
        Configuration config = ctx.getConfiguration();
        String filePath = System.getProperty("catalina.home") + CommonConstant.SEPARATOR_FILE + "logs" +
                CommonConstant.SEPARATOR_FILE + "predict" + path;
        Layout<? extends Serializable> layout = PatternLayout.createLayout("%d{yyyy.MM.dd HH:mm:ss,SSS} " +
                "%-5level %l %C{36} %L %M - %m%xEx%n", null, config, null, null, true, false, null, null);
        ThresholdFilter filter = ThresholdFilter.createFilter(Level.INFO, Filter.Result.ACCEPT, Filter.Result
                .DENY);
        TimeBasedTriggeringPolicy policy = TimeBasedTriggeringPolicy.createPolicy("1", "true");
        String fileName = strEnum + ".log";
        RollingFileAppender appender = RollingFileAppender.createAppender(filePath + CommonConstant
                        .SEPARATOR_FILE + fileName, filePath + "/${date:yyyy-MM}/%d{yyyy-MM-dd}." +
                        fileName + ".gz", null, strEnum, null, null, null, policy, null, layout, filter,
                null, null, null, config);
        config.addAppender(appender);
        LoggerConfig loggerConfig = LoggerConfig.createLogger(false, Level.INFO, strEnum, null, new
                AppenderRef[]{}, null, config, null);
        loggerConfig.addAppender(appender, null, null);
        loggerConfig.addAppender(config.getAppender("predict-error"), null, null);
        loggerConfig.addAppender(config.getAppender("predict-warn"), null, null);
        config.addLogger(strEnum, loggerConfig);
        ctx.updateLoggers(config);
        return ctx.getLogger(strEnum);
    }
}