package com.tc.trinity.core;


/**
 * 生命周期管理接口
 *
 * @author gaofeng
 * @date Jun 10, 2014 5:01:19 PM
 * @id $Id$
 */
public interface LifeCycle {
    
    void start(ConfigContext context);
    
    void stop();
    
    boolean isStarted();
    
}
