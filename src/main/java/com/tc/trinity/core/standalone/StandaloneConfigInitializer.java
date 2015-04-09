
package com.tc.trinity.core.standalone;

import com.tc.trinity.core.EnvironmentConfigInitializer;

/**
 * main函数直接启动容器时的配置初始化入口
 * 
 * @see <a
 *      href="http%3A%2F%2Fdocs.oracle.com%2Fjavase%2F6%2Fdocs%2Fapi%2Fjava%2Flang%2Finstrument%2Fpackage-summary.html">Java
 *      Agent</a>
 *
 * @author kozz.gaof
 * @date Jul 13, 2014 1:49:47 PM
 * @id $Id$
 */
public class StandaloneConfigInitializer extends EnvironmentConfigInitializer {

    @Override
    public void stop() {
    
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean isStarted() {
    
        // TODO Auto-generated method stub
        return false;
    }
    
    /**
     * Java Agent入口
     *
     * @param args
     */
    public static void premain(String args) {
    
        new StandaloneConfigInitializer().start(StandaloneConfigContext.getConfigContext());
    }
}
