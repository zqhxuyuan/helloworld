
package com.tc.trinity.core.spi;

import java.util.Properties;

import com.tc.trinity.core.ConfigContext;

/**
 * <p>
 * 可配置模块 - SPI组件
 * <p>
 * 一个可配值模块有两个生命周期绑定的两个接口{@link #init(Properties properties)} 和
 * {@link #onChange(String key, String originalValue, String value)}
 * <p>
 * 在对Configurable组件调用任何方法前，都必须确保{@link #checkValidity()}方法的返回值，只有校验通过才能保证方法能顺利执行。
 * 
 * @author gaofeng
 * @date Jun 10, 2014 7:30:47 PM
 * @id $Id$
 */
public interface Configurable extends SPI {
    
    /**
     * 缺省设置
     *
     */
    void fallbackSetting(Properties properties);
    
    /**
     * 初始化阶段的回调接口
     * 
     * @param properties
     */
    void init(ConfigContext context, Properties properties);
    
    /**
     * 属性变更时的回调接口
     * 
     * @param key
     *            变更的属性名称
     * @param originalValue
     *            变更前的初始值
     * @param value
     *            属性对应的新值
     */
    void onChange(String key, String originalValue, String value);
    
    /**
     * 检查该组件的有效性
     * 
     * @return
     */
    boolean checkValidity();
    
    /**
     * 获取实现名称
     * 
     * @return
     */
    String getName();
    
    /**
     * 设置{@link ConfigContext}
     * 
     */
    void setConfigContext(ConfigContext configContext);
    
    /**
     * 访问{@link ConfigContext}
     * 
     */
    ConfigContext getConfigContext();
}
