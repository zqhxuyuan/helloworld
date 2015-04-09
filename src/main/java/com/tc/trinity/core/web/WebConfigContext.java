
package com.tc.trinity.core.web;

import javax.servlet.ServletContext;

import com.tc.trinity.core.ConfigContext;

/**
 * Web应用环境下的配置上下文环境
 *
 * @author gaofeng
 * @date Jun 13, 2014 2:40:30 PM
 * @id $Id$
 */
public interface WebConfigContext extends ConfigContext {
    
    /**
     * 获取<{@link ServletContext}
     *
     * @return
     */
    public abstract ServletContext getServletContext();
    
    /**
     * 设置<{@link ServletContext}
     *
     * @return
     */
    public abstract void setServletContext(ServletContext servletContext);
    
}
