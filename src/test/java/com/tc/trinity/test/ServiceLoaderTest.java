
package com.tc.trinity.test;

import java.io.IOException;
import java.util.Properties;
import java.util.ServiceLoader;

import junit.framework.TestCase;

import com.tc.trinity.core.spi.RemoteConfigClient;

/**
 * TODO 类的功能描述。
 *
 * @author gaofeng
 * @date Jun 10, 2014 8:19:29 PM
 * @id $Id$
 */
public class ServiceLoaderTest extends TestCase {
    
    public void testServiceLoad() throws IOException {
    
        Properties p = new Properties();
        p.load(this.getClass().getResourceAsStream("/META-INF/trinity-inner.properties"));
        // p.load(this.getClass().getClassLoader().getResourceAsStream("/META-INF/trinity-inner.properties"));
        ServiceLoader<RemoteConfigClient> o = ServiceLoader.load(com.tc.trinity.core.spi.RemoteConfigClient.class);
        for (RemoteConfigClient client : o) {
            assertTrue(client.getExtensionName().equals(p.get(com.tc.trinity.core.spi.RemoteConfigClient.class.getName())));
        }
    }
    
}
