
package com.tc.trinity.core;

import java.util.Properties;

import com.tc.trinity.core.spi.Configurable;
import com.tc.trinity.core.spi.RemoteConfigClient;

/**
 * 抽象实现。对异常进行处理 <br />
 * 提供模板方法：{@link #doInit(ConfigContext, Properties)}和{@link #doOnChange(String, String, String)}
 * 
 *
 * @author gaofeng
 * @date Jun 13, 2014 2:31:20 PM
 * @id $Id$
 */
public abstract class AbstractConfigurable implements Configurable {
    
    protected ConfigContext configContext;
    
    private boolean isProduct = false;
    
    public void setConfigContext(ConfigContext context) {
    
        this.configContext = context;
    }
    
    public ConfigContext getConfigContext() {
    
        return this.configContext;
    }
    
    protected boolean isProductEnv() {
    
        return this.isProduct;
    }
    
    @Override
    public void init(ConfigContext context, Properties properties) {
    
        this.configContext = context;
        this.isProduct = "product".equalsIgnoreCase(properties.getProperty(RemoteConfigClient.ENVIRONMENT));
        try {
            if (!doInit(context, properties)) {
                System.err.println("Error in initializing " + getName());
            }
        } catch (Exception ex) {
            System.err.println("Error in initializing " + getName());
        }
    }
    
    @Override
    public void onChange(String key, String originalValue, String value) {
    
        try {
            if (!doOnChange(key, originalValue, value)) {
                System.err.println("Error in doOnChange " + getName());
            }
        } catch (Exception ex) {
            System.err.println("Error in doOnChange " + getName());
        }
    }
    
    protected abstract boolean doInit(ConfigContext context, Properties properties);
    
    protected abstract boolean doOnChange(String key, String originalValue, String value);
    
    @Override
    public String toString() {
    
        return "ConfigModule : " + this.getName();
    }
    
    @Override
    public boolean checkValidity() {
    
        return true;
    }
    
    @Override
    public void fallbackSetting(Properties properties) {
    
    }
}
