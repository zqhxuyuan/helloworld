
package com.tc.trinity.core.spi;

import java.util.Properties;

import com.tc.trinity.configclient.Reactable;
import com.tc.trinity.core.TrinityException;

/**
 * 配置客户端 - SPI组件
 *
 * @author gaofeng
 * @date Jun 3, 2014 9:04:56 PM
 * @id $Id$
 */
public interface RemoteConfigClient extends SPI {
    
    static final String PROJECT_NAME = "trinity.config.project_name";
    
    static final String ENVIRONMENT = "trinity.config.environment";
    
    static final String PROJECT_VERSION = "trinity.config.project_version";
    
    /**
     * 客户端初始化
     *
     * @param prop
     * @throws TrinityException
     */
    void init(Properties prop) throws TrinityException;
    
    /**
     * 获取所有的配置信息
     *
     * @return
     * @throws TrinityException
     */
    Properties getConfig() throws TrinityException;
    
    /**
     * 注册全局回调接口
     *
     * @param reactable
     */
    void register(Reactable reactable);
    
    /**
     * 获取指定键值的对应配置项
     * 
     * @param propKey
     * @return
     * @throws TrinityException
     */
    String getValue(String propKey) throws TrinityException;
    
    /**
     * 获取指定键值的对应配置项，并为此键值注册指定回调接口
     * 
     * @param propKey
     * @param reactable
     * @return
     * @throws TrinityException
     */
    String getValue(String propKey, Reactable reactable) throws TrinityException;
    
    /**
     * 关闭客户端
     *
     * @throws TrinityException
     */
    void close() throws TrinityException;
    
}
