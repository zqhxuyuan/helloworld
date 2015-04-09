
package com.tc.trinity.core;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

import com.tc.trinity.core.spi.Configurable;
import com.tc.trinity.core.spi.RemoteConfigClient;

/**
 * 一个简单的ConfigContext实现
 *
 * @author kozz.gaof
 * @date Jul 17, 2014 2:42:31 PM
 * @id $Id$
 */
public class SimpleConfigContext implements ConfigContext {
    
    private RemoteConfigClient configClient;
    
    private ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<String, Object>();
    
    private ConcurrentHashMap<String, Configurable> configMap = new ConcurrentHashMap<String, Configurable>();;
    
    public RemoteConfigClient getConfigClient() {
    
        return configClient;
    }
    
    public void setConfigClient(RemoteConfigClient configClient) {
    
        this.configClient = configClient;
    }
    
    public void register(Configurable config) {
    
        config.setConfigContext(this);
        if (StringUtils.isNotEmpty(config.getName())) {
            configMap.put(config.getName(), config);
        }
        
    }
    
    public Configurable getConfigModule(String name) {
    
        return configMap.get(name);
    }
    
    public Collection<Configurable> getConfigModules() {
    
        return configMap.values();
    }
    
    public void setAttribute(String str, Object obj) {
    
        map.put(str, obj);
        
    }
    
    @Override
    public Object getAttribute(String str) {
    
        return map.get(str);
    }
}
