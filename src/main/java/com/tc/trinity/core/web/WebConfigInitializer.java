
package com.tc.trinity.core.web;

import java.util.Set;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import com.tc.trinity.core.Constants;
import com.tc.trinity.core.EnvironmentConfigInitializer;

/**
 * <p>
 * Web环境中的初始化类。实现{@link ServletContainerInitializer}接口。 由支持Servlet 3.0规范的应用容器，于启动时加载。
 * 
 * @author gaofeng
 * @date Jun 10, 2014 5:12:29 PM
 * @id $Id$
 */
public class WebConfigInitializer extends EnvironmentConfigInitializer implements ServletContainerInitializer {
    
    @Override
    public void stop() {
    
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public boolean isStarted() {
    
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
    
        WebConfigContext configContext = new DefaultWebConfigContext();
        this.start(configContext);
        configContext.setServletContext(ctx);
        ctx.setAttribute(Constants.CONFIG_CONTEXT, configContext);
        
    }
    
}
