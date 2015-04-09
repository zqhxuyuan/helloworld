
package com.tc.trinity.configclient.zk;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tc.trinity.configclient.AbstractRemoteConfigClient;
import com.tc.trinity.configclient.Reactable;
import com.tc.trinity.core.Constants;
import com.tc.trinity.core.TrinityException;

/**
 * 基于zookeeper实现的配置客户端
 * 
 * @author gaofeng
 * @date Jun 3, 2014 9:13:52 PM
 * @id $Id$
 */
public class ZookeeperClient extends AbstractRemoteConfigClient implements Watcher {
    
    private Logger logger = LoggerFactory.getLogger(ZookeeperClient.class);
    
    public static final String SERVERS_PROP_KEY = "trinity.remote.zookeeper.servers";
    
    public static final String SESSION_TIMEOUT_PROP_KEY = "trinity.remote.zookeeper.session_timeout";
    
    public static final String ROOT_NODE_PATH = "/trinity/config/";
    
    private static final String SEPARATOR = "_=|=_";
    
    private static final ConcurrentHashMap<String, NodeDataWatcher> watcherMap = new ConcurrentHashMap<String, NodeDataWatcher>();
    
    private String servers;
    
    private int sessionTimeout;
    
    private String contextPath;
    
    private ZooKeeper zk = null;
    
    private ReentrantLock connectionLock = new ReentrantLock();
    
    @Override
    public Properties getConfig() throws TrinityException {
    
        Properties props = new Properties();
        try {
            List<String> keys = zk.getChildren(ROOT_NODE_PATH + this.contextPath, this);
            for (String key : keys) {
                String value = getValue(key, reactable);
                props.put(key, value);
            }
        } catch (KeeperException e) {
            if (e.code() != Code.NONODE) {
                throw new TrinityException("Error in retrieving key list from Zookeeper.", e);
            }
        } catch (InterruptedException e) {
            throw new TrinityException("Error in retrieving key list from Zookeeper.", e);
        }
        return props;
    }
    
    @Override
    public String getValue(String propKey, Reactable _reactable) throws TrinityException {
    
        try {
            connectionLock.lock();
            NodeDataWatcher watcher = watcherMap.get(propKey);
            if (watcher == null) {
                watcher = new NodeDataWatcher(propKey, _reactable == null ? this.reactable : _reactable);
                watcherMap.put(propKey, watcher);
            }
            byte[] value_byte = zk.getData(ROOT_NODE_PATH + this.contextPath + "/" + propKey, watcher, null);
            String value = value_byte != null ? new String(value_byte) : "";
            String v = value.split(SEPARATOR)[0]; // remove comments, only value is needed
            watcher.setOriginalValue(v);
            return v;
        } catch (KeeperException e) {
            if (e.code() != Code.NONODE) {
                logger.error("Error in getting config data from Zookeeper. Key: " + propKey, e);
                throw new TrinityException(e);
            }
            return null;
        } catch (InterruptedException e) {
            logger.error("Error in getting config data from Zookeeper. Key: " + propKey, e);
            throw new TrinityException(e);
        } finally {
            connectionLock.unlock();
        }
    }
    
    public String getServers() {
    
        return servers;
    }
    
    public void setServers(String servers) {
    
        this.servers = servers;
    }
    
    public int getSessionTimeout() {
    
        return sessionTimeout;
    }
    
    public void setSessionTimeout(int sessionTimeout) {
    
        this.sessionTimeout = sessionTimeout;
    }
    
    @Override
    public void register(Reactable reactable) {
    
        super.register(reactable);
        NodeDataWatcher rootWatcher = new NodeDataWatcher("/", reactable);
        try {
            zk.getData(ROOT_NODE_PATH + this.contextPath, rootWatcher, null);
        } catch (KeeperException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        zk.register(rootWatcher);
    }
    
    @Override
    public void init(Properties prop) throws TrinityException {
    
        try {
            connectionLock.lock();
            if (zk != null) {
                try {
                    zk.close();
                } catch (InterruptedException e) {
                    throw new TrinityException("Error in closing connection with Zookeeper", e);
                }
            }
            try {
                this.servers = prop.getProperty(SERVERS_PROP_KEY);
                this.sessionTimeout = Integer.parseInt(prop.getProperty(SESSION_TIMEOUT_PROP_KEY, "5000"));
                String pn = prop.getProperty(PROJECT_NAME);
                String env = prop.getProperty(ENVIRONMENT);
                if (StringUtils.isEmpty(servers) || StringUtils.isEmpty(pn) || StringUtils.isEmpty(env)) {
                    throw new TrinityException("Wrong Configuration! - check trinity.config.project_name, trinity.config.environment and trinity.remote.zookeeper.servers settings.");
                }
                this.contextPath = normalize(pn) + "/" + normalize(env);
                if (StringUtils.isNotEmpty(prop.getProperty(PROJECT_VERSION))) {
                    this.contextPath += "/" + normalize(prop.getProperty(PROJECT_VERSION));
                }
                zk = new ZooKeeper(this.servers, this.sessionTimeout, this);
            } catch (NumberFormatException e) {
                throw new TrinityException("Wrong Configuration! - check trinity.remote.zookeeper.session_timeout settings", e);
            } catch (IOException e) {
                throw new TrinityException("Error in connecting to Zookeeper", e);
            }
            
            checkRootExistence();
        } finally {
            connectionLock.unlock();
        }
        
    }
    
    protected void checkRootExistence() throws TrinityException {
    
        try {
            String rootPath = ROOT_NODE_PATH + this.contextPath;
            recursivelyCreate(rootPath);
        } catch (KeeperException | InterruptedException e) {
            String errMsg = "Error in initilizing zk node";
            logger.error(errMsg);
            throw new TrinityException(errMsg, e);
        }
    }
    
    private void recursivelyCreate(String path) throws KeeperException, InterruptedException {
    
        Stat stat = this.zk.exists(path, false);
        if (stat != null) {
            return;
        } else if (stat == null && path.lastIndexOf("/") > 0) {
            recursivelyCreate(path.substring(0, path.lastIndexOf("/")));
        }
        zk.create(path, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        if (logger.isInfoEnabled()) {
            logger.info(Constants.LOG_PREFIX + " creates node '" + path + "' successfully!");
        }
    }
    
    private class NodeDataWatcher implements Watcher {
        
        private Reactable reactable;
        
        private String originalValue;
        
        private String key;
        
        NodeDataWatcher(String key, Reactable reactable) {
        
            this.key = key;
            this.reactable = reactable;
        }
        
        void setOriginalValue(String originalValue) {
        
            this.originalValue = originalValue;
        }
        
        /**
         * 处理节点数据变化事件。对于配置属性节点，进入普通处理流程EventType.NodeDataChanged。对于增、删节点，需要/节点特殊处理。
         * 
         */
        @Override
        public void process(WatchedEvent event) {
        
            if (reactable != null) {
                try {
                    connectionLock.lock();
                    if (event.getType() == EventType.NodeDataChanged) {
                        
                        byte[] value_byte = zk.getData(event.getPath(), this, null);
                        String value = value_byte != null ? new String(value_byte) : "";
                        if(StringUtils.isNotBlank(value)){
                            value = value.split(SEPARATOR)[0]; // remove comments, only value is needed
                        }
                        String original = this.originalValue;
                        if(!value.equals(originalValue)){
                            this.setOriginalValue(value);
                            reactable.onChange(key, original, value);
                        }
                    } else if (event.getType() == EventType.NodeChildrenChanged) {
                        byte[] value_byte = zk.getData(event.getPath(), this, null);
                        String value = value_byte != null ? new String(value_byte) : "";
                        reactable.onChange(key, "NodeCreated", value);
                    } else if (event.getType() == EventType.NodeDeleted) {
                        
                    }
                } catch (KeeperException | InterruptedException e) {
                    logger.error("Error in getting config data when responding to EventType.NodeDataChanged. Key: " + key, e);
                } finally {
                    connectionLock.unlock();
                }
            }
        }
    }
    
    /**
     * 
     * 处理和链路状态相关的事件；目前在链接过期或是意外断开的时候，采取重连的方式保持连接
     *
     * @param event
     */
    @Override
    public void process(WatchedEvent event) {
    
        if (event.getState() == KeeperState.Expired || event.getState() == KeeperState.Disconnected) {
            if (logger.isInfoEnabled()) {
                logger.info(Constants.LOG_PREFIX + " Connection losts, Try to reconnect...");
            }
            try {
                connectionLock.lock();
                if (zk != null) {
                    zk.close();
                }
                zk = new ZooKeeper(this.servers, this.sessionTimeout, this);
            } catch (IOException | InterruptedException e) {
                logger.error("Error in re-connecting to Zookeeper", e);
            } finally {
                connectionLock.unlock();
            }
        }
    }
    
    @Override
    public String getExtensionName() {
    
        return this.getClass().getName();
    }
    
    @Override
    public void close() throws TrinityException {
    
        try {
            connectionLock.lock();
            if (zk != null) {
                this.zk.close();
            }
        } catch (InterruptedException e) {
            logger.error("Error in closing connection with Zookeeper");
            throw new TrinityException(e);
        } finally {
            connectionLock.unlock();
        }
    }
}
