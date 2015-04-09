
package com.tc.trinity.core.web;

import javax.servlet.ServletContext;

import com.tc.trinity.core.SimpleConfigContext;

/**
 * Web环境下，配置上下文环境实现
 * 
 * @author gaofeng
 * @date Jun 11, 2014 5:06:42 PM
 * @id $Id$
 */
public class DefaultWebConfigContext extends SimpleConfigContext implements WebConfigContext{

    private ServletContext servletContext;
    
    @Override
    public ServletContext getServletContext() {
    
        return this.servletContext;
    }
    
    @Override
    public void setServletContext(ServletContext servletContext) {
    
        this.servletContext = servletContext;
    }
    
    @Override
    public Object getAttribute(String str) {
        
        ServletContext sc = getServletContext();
        if (sc == null) {
            throw new IllegalStateException();
        }
        Object o = sc.getAttribute(str);
        if (o != null) {
            return o;
        }
        return super.getAttribute(str);
    }
}
