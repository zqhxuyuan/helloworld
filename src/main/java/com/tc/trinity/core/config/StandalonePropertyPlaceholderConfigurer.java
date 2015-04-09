
package com.tc.trinity.core.config;

import com.tc.trinity.core.ConfigContext;
import com.tc.trinity.core.standalone.StandaloneConfigContext;

/**
 * TODO 类的功能描述。
 *
 * @author kozz.gaof
 * @date Jul 17, 2014 4:14:28 PM
 * @id $Id$
 */
public class StandalonePropertyPlaceholderConfigurer extends EnhancedPropertyPlaceholderConfigurer {
    
    @Override
    public ConfigContext getConfigContext() {
    
        return StandaloneConfigContext.getConfigContext();
    }
    
}
