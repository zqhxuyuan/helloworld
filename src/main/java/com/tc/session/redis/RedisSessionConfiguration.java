
package com.tc.session.redis;

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.tc.trinity.core.AbstractConfigurable;
import com.tc.trinity.core.ConfigContext;

/**
 * trinity redis properties
 *
 * @author liming
 * @date Sep 16, 2014 5:19:23 PM
 * @id $Id$
 */
public class RedisSessionConfiguration extends AbstractConfigurable {
    
    @Override
    public String getName() {
    
        return "tcsession";
    }
    
    @Override
    public String getExtensionName() {
    
        return this.getClass().getName();
    }
    
    @Override
    protected boolean doInit(ConfigContext context, Properties properties) {
    
        try {
            Class.forName("com.tc.session.redis.RedisConfiguration", false, this.getClass().getClassLoader());
            if (StringUtils.isBlank(RedisConfiguration.SERVERS) || isProductEnv()) {
                RedisConfiguration.SERVERS = properties.getProperty("tc.session.redis.servers");
            }
            if (StringUtils.isBlank(RedisConfiguration.MAX_IDLE) || isProductEnv()) {
                RedisConfiguration.MAX_IDLE = properties.getProperty("tc.session.redis.max_idle");
            }
            if (StringUtils.isBlank(RedisConfiguration.MIN_IDLE) || isProductEnv()) {
                RedisConfiguration.MIN_IDLE = properties.getProperty("tc.session.redis.min_idle");
            }
            if (StringUtils.isBlank(RedisConfiguration.MAX_ACTIVE) || isProductEnv()) {
                RedisConfiguration.MAX_ACTIVE = properties.getProperty("tc.session.redis.max_active");
            }
            if (StringUtils.isBlank(RedisConfiguration.MAX_WAIT) || isProductEnv()) {
                RedisConfiguration.MAX_WAIT = properties.getProperty("tc.session.redis.max_wait");
            }
            if (StringUtils.isBlank(RedisConfiguration.TEST_ON_BORROW) || isProductEnv()) {
                RedisConfiguration.TEST_ON_BORROW = properties.getProperty("tc.session.redis.test_on_borrow");
            }
            if (StringUtils.isBlank(RedisConfiguration.TEST_ON_RETURN) || isProductEnv()) {
                RedisConfiguration.TEST_ON_RETURN = properties.getProperty("tc.session.redis.test_on_return");
            }
            if (RedisConfiguration.SESSION_TIMEOUT == null || RedisConfiguration.SESSION_TIMEOUT <= 0 || isProductEnv()) {
                RedisConfiguration.SESSION_TIMEOUT = NumberUtils.toInt(properties.getProperty("tc.session.redis.session_timeout"));
            }
            if (RedisConfiguration.DATABASE == null || RedisConfiguration.DATABASE <= 0 || isProductEnv()) {
                RedisConfiguration.DATABASE = NumberUtils.toInt(properties.getProperty("tc.session.redis.database"));
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        
        return true;
    }
    
    @Override
    protected boolean doOnChange(String key, String originalValue, String value) {
    
        return false;
    }
    
}
