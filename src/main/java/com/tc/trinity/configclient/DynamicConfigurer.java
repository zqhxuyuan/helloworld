
package com.tc.trinity.configclient;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tc.trinity.core.ConfigContext;
import com.tc.trinity.core.Constants;
import com.tc.trinity.core.spi.Configurable;

/**
 * <p>
 * 动态配置器。实现了{@link Reactable}接口
 * 
 * <p>
 * 对于以{@link #DYNAMIC_SUFFIX}结尾的键值，会调用对应的{@link Configurable}模块
 * 
 * @author gaofeng
 * @date Jun 13, 2014 10:46:35 AM
 * @id $Id$
 */
public class DynamicConfigurer implements Reactable {
    
    public static final String DYNAMIC_SUFFIX = "_dynamic";
    
    private Logger logger = LoggerFactory.getLogger(DynamicConfigurer.class);
    
    private ConfigContext configContext;
    
    public DynamicConfigurer(ConfigContext configContext) {
    
        this.configContext = configContext;
    }
    
    @Override
    public void onChange(String key, String originalValue, String value) {
    
        logger.info(key + " is changeded from " + originalValue + " to " + value);
        if (!isDynamicKey(key)) {
            logger.info(key + " is not a dynamic key");
            return;
        }
        
        for (Configurable c : getConfigurable(key)) {
            c.onChange(key, originalValue, value);
        }
    }
    
    protected boolean isDynamicKey(String key) {
    
        if (StringUtils.isEmpty(key)) {
            throw new AssertionError("key cannot be null");
        }
        return key.endsWith(DYNAMIC_SUFFIX);
    }
    
    protected Configurable[] getConfigurable(String key) {
    
        if (StringUtils.isEmpty(key) || key.indexOf(".") < 0) {
            logger.warn(Constants.LOG_PREFIX + " Invalid key: " + key);
            return new Configurable[] {};
        }
        Configurable c = configContext.getConfigModule(key.substring(0, key.indexOf(".")));
        if (c == null) {
            logger.warn(Constants.LOG_PREFIX + " Failed to load corresponding configurable module! key: " + key);
        }
        
        return new Configurable[] { c };
    }
    
}
