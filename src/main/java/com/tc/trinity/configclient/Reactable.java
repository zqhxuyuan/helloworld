package com.tc.trinity.configclient;


/**
 * 可回调接口
 * 
 * @author gaofeng
 * @date Jun 9, 2014 1:48:09 PM
 * @id $Id$
 */
public interface Reactable {
    
    /**
     * 回调入口
     *
     * @param key 键值
     * @param originalValue 初始值
     * @param value 现在值
     */
    public void onChange(String key, String originalValue, String value);
    
}
