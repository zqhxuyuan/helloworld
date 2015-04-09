package com.tc.trinity.core;

import java.util.Collection;

import com.tc.trinity.core.spi.Configurable;
import com.tc.trinity.core.spi.RemoteConfigClient;


/**
 * 全局的配置上下文环境接口
 *
 * @author gaofeng
 * @date Jun 11, 2014 4:25:54 PM
 * @id $Id$
 */
public interface ConfigContext {
    
    /**
     * 设置全局配置客户端
     *
     * @param configClient
     */
    void setConfigClient(RemoteConfigClient configClient);
    
    /**
     * 获取全局配置客户端
     *
     * @return
     */
    RemoteConfigClient getConfigClient();
    
    /**
     * 注册<{@link Configurable}组件
     *
     * @param config
     */
    public void register(Configurable config);
    
    /**
     * 根据名称查询<{@link Configurable}组件
     *
     * @param name
     * @return
     */
    public Configurable getConfigModule(String name);
    
    /**
     * 获取所有的<{@link Configurable}组件
     *
     * @return
     */
    public Collection<Configurable> getConfigModules();
    
    /**
     * 设置属性
     *
     * @param str
     * @param obj
     */
    void setAttribute(String str, Object obj);
    
    /**
     * 获取属性
     *
     * @param str
     * @return
     */
    Object getAttribute(String str);
    
}
