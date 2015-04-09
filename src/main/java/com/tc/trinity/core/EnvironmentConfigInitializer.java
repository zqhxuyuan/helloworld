
package com.tc.trinity.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.tc.trinity.configclient.zk.ZookeeperClient;
import com.tc.trinity.core.spi.RemoteConfigClient;

/**
 * 此处处理一些环境信息
 *
 * @author kozz.gaof
 * @date Aug 7, 2014 8:18:31 PM
 * @id $Id$
 */
public abstract class EnvironmentConfigInitializer extends ConfigInitializer {
    
    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    
    private static final String ENVIRON_CONFIG = "/eetop/conf/global.properties";
    
    /**
     * 追加对环境配置文件(/eetop/conf/global.properties)的加载<br />
     * <ul>
     * <li>windows环境或无此文件时，不作处理。</li>
     * <li>目前只从此环境配置文件中加载trinity.remote.zookeeper.servers和trinity.config.environment这两个配置项</li>
     * </ul>
     * 
     */
    @Override
    public Properties mergeProperties() throws TrinityException {
    
        Properties properties = super.mergeProperties();
        File f = new File(ENVIRON_CONFIG);
        if (OS_NAME.contains("win") || !f.exists()) {
            return properties;
        }
        
        Properties p = new Properties();
        InputStream is = null;
        try {
            is = new LineTrimInputStream(new FileInputStream(f));
            p.load(is);
        } catch (Exception e) {
            System.err.println("Cannot load file: " + ENVIRON_CONFIG);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        if (p.containsKey(ZookeeperClient.SERVERS_PROP_KEY)) {
            properties.put(ZookeeperClient.SERVERS_PROP_KEY, p.get(ZookeeperClient.SERVERS_PROP_KEY));
        }
        if (p.containsKey(RemoteConfigClient.ENVIRONMENT)) {
            properties.put(RemoteConfigClient.ENVIRONMENT, p.get(RemoteConfigClient.ENVIRONMENT));
        }
        return properties;
    }
}
