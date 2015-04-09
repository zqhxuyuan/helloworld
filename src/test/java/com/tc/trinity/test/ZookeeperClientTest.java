
package com.tc.trinity.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

import com.tc.trinity.configclient.AbstractRemoteConfigClient;
import com.tc.trinity.configclient.Reactable;
import com.tc.trinity.configclient.zk.ZookeeperClient;
import com.tc.trinity.core.ExtensionLoader;
import com.tc.trinity.core.TrinityException;
import com.tc.trinity.core.spi.Configurable;

/**
 * TODO 类的功能描述。
 *
 * @author kozz.gaof
 * @date Jul 23, 2014 10:31:26 AM
 * @id $Id$
 */
public class ZookeeperClientTest extends TestCase {
    
    private Properties props;
    
    private ZookeeperClient client;
    
    private ZooKeeper zk;
    
    @Override
    public void setUp() throws IOException, TrinityException {
    
        for (Configurable configurable : ExtensionLoader.loadConfigurable()) {
            if ("logback".equals(configurable.getName())) {
                configurable.fallbackSetting(new Properties());
            }
            
        }
        
        props = new Properties();
        props.load(this.getClass().getResourceAsStream("/test-config.properties"));
        client = new ZookeeperClient();
        zk = new ZooKeeper(props.getProperty(ZookeeperClient.SERVERS_PROP_KEY), Integer.parseInt(props.getProperty(ZookeeperClient.SESSION_TIMEOUT_PROP_KEY)), new Watcher() {
            
            @Override
            public void process(WatchedEvent event) {
            
            }
        });
        client.init(props);
        
    }
    
    private String getPath() {
    
        return ZookeeperClient.ROOT_NODE_PATH +
                props.getProperty(AbstractRemoteConfigClient.PROJECT_NAME) + "/" +
                props.getProperty(AbstractRemoteConfigClient.ENVIRONMENT) + "/" +
                props.getProperty(AbstractRemoteConfigClient.PROJECT_VERSION) + "/";
    }
    
    public void testGetConfig() throws TrinityException, KeeperException, InterruptedException {
    
        if (zk.exists(getPath() + "testKey", false) != null) {
            zk.delete(getPath() + "testKey", -1);
        }
        zk.create(getPath() + "testKey", "testValue".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        assertTrue(client.getConfig().size() > 0);
        assertTrue(client.getValue("testKey").equals("testValue"));
        zk.delete(getPath() + "testKey", -1);
    }
    
    public void testNodeChangeCallback() throws KeeperException, InterruptedException, TrinityException {
    
        if (zk.exists(getPath() + "testKey2", false) != null) {
            zk.delete(getPath() + "testKey2", -1);
        }
        zk.create(getPath() + "testKey2", "testValue".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        final HashMap<String, String> map = new HashMap<String, String>();
        assertTrue(client.getValue("testKey2", new Reactable() {
            
            @Override
            public void onChange(String key, String originalValue, String value) {
            
                map.put("key", key);
                map.put("originalValue", originalValue);
                map.put("value", value);
            }
            
        }).equals("testValue"));
        zk.setData(getPath() + "testKey2", "testValueChanged".getBytes(), -1);
        Thread.sleep(500);
        assertTrue("testKey2".equals(map.get("key")));
        assertTrue("testValue".equals(map.get("originalValue")));
        assertTrue("testValueChanged".equals(map.get("value")));
        
        zk.setData(getPath() + "testKey2", "testValueChanged2".getBytes(), -1);
        Thread.sleep(500);
        assertTrue("testKey2".equals(map.get("key")));
        assertTrue("testValueChanged".equals(map.get("originalValue")));
        assertTrue("testValueChanged2".equals(map.get("value")));
        zk.delete(getPath() + "testKey2", -1);
    }
    
    /**
     * 增删节点的动态回调 @TODO 目前不工作
     *
     * @throws KeeperException
     * @throws InterruptedException
     */
    public void testNodeAddDeleteCallback() throws KeeperException, InterruptedException {
//    
//        if (zk.exists(getPath() + "testKey3", false) != null) {
//            zk.delete(getPath() + "testKey3", -1);
//        }
//        
//        final HashMap<String, String> map = new HashMap<String, String>();
//        client.register(new Reactable() {
//            
//            @Override
//            public void onChange(String key, String originalValue, String value) {
//            
//                map.put("key", key);
//                map.put("originalValue", originalValue);
//                map.put("value", value);
//            }
//            
//        });
//        zk.create(getPath() + "testKey3", "testValue".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
//        zk.delete(getPath() + "testKey3", -1);
//        Thread.sleep(500);
//        assertTrue("testKey3".equals(map.get("key")));
//        assertTrue("testValue".equals(map.get("originalValue")));
//        assertTrue("testValueChanged".equals(map.get("value")));
//        
//        zk.delete(getPath() + "testKey3", -1);
    }
    
    @Override
    public void tearDown() throws TrinityException, InterruptedException {
    
        zk.close();
        client.close();
    }
}
