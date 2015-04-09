
package com.tc.session.servlet;

import static com.tc.session.RequestCache.PLACEHOLDER;
import static com.tc.session.RequestCache.SESSION;
import static com.tc.session.SessionManager.SESSION_NAME;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tc.session.RequestCache;
import com.tc.session.SessionManager;
import com.tc.session.helper.CookieHelper;

/**
 * 
 * 通过实现{@link javax.servlet.http.HttpServletRequestWrapper}完成其它调用的转发。<br />
 * 拦截getSession的两个方法
 * 
 * @author gaofeng
 * @date Sep 12, 2013 3:11:37 PM
 * @id $Id$
 */
public class RemotableRequestWrapper extends HttpServletRequestWrapper {
    
    protected Logger logger = LoggerFactory.getLogger(RemotableRequestWrapper.class);
    
    private SessionManager sessionManager;
    
    private HttpServletRequest request;
    
    // hold an HttpServletResponse instance in case a new Cookie would be generated & added to user-agent.
    private HttpServletResponse response;
    
    private RequestCache cache;
    
    /**
     * 构造方法
     * 
     * @param request
     */
    public RemotableRequestWrapper(HttpServletRequest request, HttpServletResponse response, SessionManager sessionManager) {
    
        super(request);
        this.request = request;
        this.response = response;
        this.sessionManager = sessionManager;
        cache = RequestCache.getFromThread();
    }
    
    @Override
    public String getRequestedSessionId() {
    
        String sessionId = cache.get(SESSION_NAME);
        if (sessionId == null) {
            sessionId = CookieHelper.findSessionId(this.request);
            if (StringUtils.isNotBlank(sessionId)) {
                cache.put(SESSION_NAME, sessionId);
            }
        }
        return sessionId;
    }
    
    @Override
    public HttpSession getSession(boolean create) {
    
        if (sessionManager == null) {
            throw new IllegalStateException("SessionManager not initialized");
        }
        
        Object s = cache.get(SESSION);
        if (!create && s != null) {
            return s == PLACEHOLDER ? null : (HttpSession) s;
        }
        String sessionid = getRequestedSessionId();
        HttpSession session = null;
        
        if (sessionid != null) {
            session = sessionManager.getHttpSession(sessionid);
            if (session == null && !create) {
                cache.put(SESSION, PLACEHOLDER);
                return null;
            }
        }
        if (session == null && create) {
            session = sessionManager.newHttpSession(this.request, this.response);
        }
        cache.put(SESSION, session == null ? PLACEHOLDER : session);
        return session;
    }
    
    @Override
    public HttpSession getSession() {
    
        return getSession(true);
    }
    
    @Override
    public boolean isRequestedSessionIdValid() {
    
        return getSession(false) != null;
    }
    
    @Override
    public boolean isRequestedSessionIdFromCookie() {
    
        return true;
    }
    
    @Override
    public boolean isRequestedSessionIdFromURL() {
    
        return false;
    }
    
    @Override
    public boolean isRequestedSessionIdFromUrl() {
    
        return isRequestedSessionIdFromURL();
    }
}
