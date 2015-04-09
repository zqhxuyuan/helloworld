
package com.tc.trinity.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.ServiceLoader;

import com.tc.trinity.core.spi.Configurable;
import com.tc.trinity.core.spi.RemoteConfigClient;

/**
 * Java SPI 扩展点装载器类
 * 
 * @see <a href="http://java.sun.com/j2se/1.5.0/docs/guide/jar/jar.html#Service%20Provider">JDK5.0的自动发现机制实现</a>
 *
 * @author kozz.gaof
 * @date Jun 29, 2014 4:01:44 PM
 * @id $Id$
 */
public class ExtensionLoader {
    
    public static RemoteConfigClient loadConfigClient() throws IOException {
    
        // load RemoteConfigClient instance from /META-INF/trinity-inner.properties
        Properties extensionProperties = new Properties();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = new LineTrimInputStream(cl.getResourceAsStream(Constants.EXTENSION_INTERNAL_FILE));
        extensionProperties.load(inputStream);
        inputStream.close();
        
        // load RemoteConfigClient instance from external file when exists. Intance loaded from external file will
        // override the one loaded from internal.
        inputStream = cl.getResourceAsStream(Constants.EXTENSION_FILE);
        if (inputStream != null) {
            extensionProperties.load(inputStream);
        }
        
        ServiceLoader<RemoteConfigClient> configClientLoader = ServiceLoader.load(RemoteConfigClient.class);
        for (RemoteConfigClient client : configClientLoader) {
            if (client.getExtensionName().equals(extensionProperties.get(RemoteConfigClient.class.getName()))) {
                return client;
            }
        }
        return null;
    }
    
    public static Iterable<Configurable> loadConfigurable() {
    
        ServiceLoader<Configurable> configurableLoader = ServiceLoader.load(Configurable.class);
        return configurableLoader;
    }
}
