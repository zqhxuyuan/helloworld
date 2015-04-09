
package com.tc.trinity.core.standalone;

import com.tc.trinity.core.ConfigContext;
import com.tc.trinity.core.SimpleConfigContext;

/**
 * TODO 类的功能描述。
 *
 * @author kozz.gaof
 * @date Jul 17, 2014 11:04:32 AM
 * @id $Id$
 */
public class StandaloneConfigContext extends SimpleConfigContext {
    
    private static StandaloneConfigContext configContext;
    
    public static synchronized ConfigContext getConfigContext() {
    
        if (configContext == null) {
            configContext = new StandaloneConfigContext();
        }
        return configContext;
    }
    
}
