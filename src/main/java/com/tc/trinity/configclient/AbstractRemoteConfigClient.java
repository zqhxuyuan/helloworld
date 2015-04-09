
package com.tc.trinity.configclient;

import org.apache.commons.lang3.StringUtils;

import com.tc.trinity.core.TrinityException;
import com.tc.trinity.core.spi.RemoteConfigClient;

/**
 * 抽象配置客户端实现
 *
 * @author gaofeng
 * @date Jun 9, 2014 1:50:39 PM
 * @id $Id$
 */
public abstract class AbstractRemoteConfigClient implements RemoteConfigClient {
    
    protected Reactable reactable;
    
    public String getValue(String propKey) throws TrinityException {
    
        return getValue(propKey, this.reactable);
    }
    
    public void register(Reactable reactable) {
    
        if (reactable != null) {
            this.reactable = reactable;
        }
    }
    
    protected String normalize(String str) {
    
        if (StringUtils.isBlank(str)) {
            throw new AssertionError("str cannot be null");
        }
        return str.toLowerCase().trim();
    }
    
}
