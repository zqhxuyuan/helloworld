
package com.tc.session.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tc.session.RequestCache;
import com.tc.session.SessionManager;
import com.tc.session.servlet.RemotableRequestWrapper;

/**
 * 
 * session拦截器。<br />
 * 1. 加载SessionManager具体实现类 2. 替换ServletRequest的实现，完成代理模式
 * 
 * @author gaofeng
 * @date Sep 18, 2013 1:27:10 PM
 * @id $Id$
 */
public class TCSessionFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(TCSessionFilter.class);
    private SessionManager sessionManager;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    
        ServletContext sc = filterConfig.getServletContext();
	    //暂时注释掉
	    //sc.getSessionCookieConfig().setName(SESSION_NAME);
        try {
            this.sessionManager = (SessionManager) Class.forName("com.tc.session.TCSessionManager").newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            logger.error("==========> Error occurs when initializing TCSessionManager. ", e);
            throw new ServletException(e);
        }
        logger.info(">>>>>>>>>>> TCSessionFilter.init completed.");
        this.sessionManager.setServletContext(sc);
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
    
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletResponse resp = (HttpServletResponse) response;
            
            RequestCache.begin();
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug(">>>>>>>>>>> Delegating HttpServletRequest with com.tc.session.servlet.RemotableRequestWrapper.");
                }
                chain.doFilter(new RemotableRequestWrapper(req, resp, sessionManager), response);
            } finally {
                RequestCache.remove();
            }
        } else {
            chain.doFilter(request, response);
        }
        
    }
    
    @Override
    public void destroy() {
    
        if (sessionManager != null) {
            try {
                sessionManager.close();
                logger.info(">>>>>>>>>>> TCSessionFilter.destroy completed.");
            } catch (Exception ex) {
                logger.error("==========> Error occurs when closing TCSessionManager. ", ex);
            }
        }
    }
}
