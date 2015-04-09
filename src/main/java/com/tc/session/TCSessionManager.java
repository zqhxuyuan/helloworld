
package com.tc.session;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tc.session.helper.CookieHelper;
import com.tc.session.helper.SessionIdGenerator;
import com.tc.session.zookeeper.TimeoutCheckTask;
import com.tc.session.zookeeper.ZookeeperSessionClient;

/**
 * 
 * 基于Zooker实现的SessionManager
 * 
 * @author gaofeng
 * @date Sep 12, 2013 3:47:38 PM
 * @id $Id$
 */
public class TCSessionManager extends AbstractSessionManager {
    
    private static final Logger log = LoggerFactory.getLogger(TCSessionManager.class);
    
    /** ZK客户端操作 */
    protected SessionClient client;
    
    /** 定时任务执行器 */
    protected ExecutorService executor;
    
    private Lock sessionLock = new ReentrantLock();
    
    /**
     * 构造方法
     * N.B. 访问权限设置为public是为了让core包通过反射后初始化
     * 
     * @param config
     */
    public TCSessionManager() {
    
        executor = Executors.newSingleThreadExecutor();
        executor.submit(new TimeoutCheckTask());
        
        client = ZookeeperSessionClient.getInstance();
        // 建立Zookeeper的根节点
        client.createSession(null);
        
        if (log.isInfoEnabled()) {
            log.info("创建SESSIONS组节点完成");
        }
        
    }
    
    /**
     * 这个方法存在线程安全性问题，必须加上同步机制
     */
    @Override
    public HttpSession getHttpSession(String id) {
    
        TCSession session = client.getSession(id);
        
        if (session == null || !session.isValid()) {
            try {
                sessionLock.lock();
                if (session != null) {
                    if (log.isDebugEnabled()) {
                        log.debug(">>>>>>>>>>> Try removing expired session: " + session);
                    }
                    session.invalidate();
                }
                return null;
            } finally {
                sessionLock.unlock();
            }
        }
        
        client.updateSession(session);
        return session;
    }
    
    @Override
    public HttpSession newHttpSession(HttpServletRequest request, HttpServletResponse response) {
    
        String id = SessionIdGenerator.newSessionId(request); // 获取新的Session ID
        Cookie cookie = CookieHelper.writeSessionIdToCookie(id, request, response, COOKIE_EXPIRY);
        if (cookie != null) {
            log.info(">>>>>>>>>>> Write tsid to Cookie,name:[" + cookie.getName() + "],value:[" + cookie.getValue() + "]");
        }
        TCSession session = new TCSession(getSessionClient(), id);
        
        int sessionTimeout = NumberUtils.toInt(Configuration.SESSION_TIMEOUT);
        session.setMaxInactiveInterval(sessionTimeout * 60 * 1000); // 转换成毫秒
        // 在ZooKeeper服务器上创建session节点，节点名称为Session ID
        client.createSession(session);
        
        return session;
    }
    
    @Override
    public void close() throws Exception {
    
        ZookeeperPoolManager.getInstance().close();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public SessionClient getSessionClient() {
    
        return this.client;
    }
    
}
