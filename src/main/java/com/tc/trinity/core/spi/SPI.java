package com.tc.trinity.core.spi;


/**
 * 服务扩展点接口
 *
 * @author gaofeng
 * @date Jun 10, 2014 8:37:49 PM
 * @id $Id$
 */
public interface SPI {
    
    /**
     * 返回扩展点名称
     * 
     * @return
     */
    String getExtensionName();
    
}
