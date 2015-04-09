
package com.tc.session;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 
 * Session管理器
 *
 * @author gaofeng
 * @date Sep 13, 2013 9:33:27 AM
 * @id $Id$
 */
public interface SessionManager {
    
    /** Cookie的过期时间，默认30天 */
    public static final int COOKIE_EXPIRY = 30 * 24 * 60 * 60;
    
    public static final String SESSION_NAME = "tsid";
    
    /**
     * 返回指定ID的HttpSession对象
     * 
     * @param id
     *            Session ID
     * @param request
     *            HTTP请求
     * @return
     */
    public HttpSession getHttpSession(String id);
    
    /**
     * 创建一个新的HttpSession对象
     * 
     * @param request
     *            HTTP请求
     * @return
     */
    public HttpSession newHttpSession(HttpServletRequest request, HttpServletResponse response);
    
    public ServletContext getServletContext();
    
    public void setServletContext(ServletContext sc);
    
    public SessionClient getSessionClient();
    
    /**
     * close the SessionManager instance
     * implementation can do some necessary clean work here, such as closing connection, etc.
     *
     * @author gaofeng
     * @throws Exception
     * @date Sep 17, 2013 8:05:04 PM
     *
     */
    public void close() throws Exception;
    
}
