
package com.tc.trinity.test;

import java.util.Properties;

import junit.framework.TestCase;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;

import com.tc.trinity.core.ExtensionLoader;
import com.tc.trinity.core.spi.Configurable;

/**
 * TODO 类的功能描述。
 *
 * @author kozz.gaof
 * @date Jul 8, 2014 1:37:14 PM
 * @id $Id$
 */
public class LogbackFallbackSettingTest extends TestCase {
    
    LoggerContext loggerContext;
    
    @Override
    public void setUp() {
    
        loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
    }
    
    public void testLogbackFallbackSetting() {
    
        if (loggerContext == null) {
            loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        }
        
        assertTrue(loggerContext.getProperty("logback.appender.sqllog.file_dynamic") == null);
        assertTrue(loggerContext.getProperty("logback.appender.sqllog.rolling_file_pattern_dynamic") == null);
        assertTrue(loggerContext.getProperty("logback.appender.stdout.pattern_dynamic") == null);
        assertTrue(loggerContext.getProperty("logback.logger.com.ibatis.common.jdbc.SimpleDataSource.level_dynamic") == null);
        assertTrue(loggerContext.getProperty("logback.logger.root.level_dynamic") == null);
        
        try {
            for (Configurable configurable : ExtensionLoader.loadConfigurable()) {
                if ("logback".equals(configurable.getName())) {
                    configurable.fallbackSetting(new Properties());
                }
                
            }
            assertTrue(loggerContext.getProperty("logback.appender.sqllog.file_dynamic") != null);
            assertTrue(loggerContext.getProperty("logback.appender.sqllog.rolling_file_pattern_dynamic") != null);
            assertTrue(loggerContext.getProperty("logback.appender.stdout.pattern_dynamic") != null);
            assertTrue(loggerContext.getProperty("logback.appender.logfile.file_dynamic").equalsIgnoreCase("app.log"));
            assertTrue(loggerContext.getProperty("logback.logger.com.ibatis.common.jdbc.SimpleDataSource.level_dynamic").equalsIgnoreCase("debug"));
            assertTrue(loggerContext.getProperty("logback.logger.root.level_dynamic").equalsIgnoreCase("debug"));
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
        
    }
    
}
