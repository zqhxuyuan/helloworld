
package com.tc.session;

import javax.servlet.ServletContext;

/**
 * TODO 类的功能描述。
 *
 * @author kozz.gaof
 * @date Jan 12, 2015 4:40:30 PM
 * @id $Id$
 */
public abstract class AbstractSessionManager implements SessionManager {
    
    private ServletContext servletContext;
    
    public ServletContext getServletContext() {
    
        return servletContext;
    }
    
    public void setServletContext(ServletContext servletContext) {
    
        this.servletContext = servletContext;
    }
}
