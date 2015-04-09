package com.tc.session;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tc.session.helper.CookieHelper;
import com.tc.session.helper.SessionIdGenerator;
import com.tc.session.redis.JedisPoolManager;
import com.tc.session.redis.RedisConfiguration;
import com.tc.session.redis.RedisSessionClient;

/**
 * @author liming
 * @version 1.5.4
 * @date 15-1-6 下午5:48
 */
public class TCRedisSessionManager extends AbstractSessionManager {

    private static final Logger log = LoggerFactory.getLogger(TCSessionManager.class);

    /** Redis客户端操作 */
    protected SessionClient client;

    private Lock sessionLock = new ReentrantLock();

    /**
     * 构造方法
     */
    public TCRedisSessionManager() {

        client = RedisSessionClient.getInstance();

        if (log.isInfoEnabled()) {
            log.info("创建SESSIONS组节点完成");
        }

    }

    /**
     * 这个方法存在线程安全性问题，必须加上同步机制 {@inheritDoc}
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
        session.setServletContext(getServletContext());
        session.setMaxInactiveInterval(RedisConfiguration.SESSION_TIMEOUT * 60 * 1000); // 转换成毫秒

        // 在Redis服务器上创建session，key为Session ID
        if(client.createSession(session)){

            return session;
        }else{
            throw new IllegalStateException("Failed to create new session");
        }
    }

    @Override
    public void close() throws Exception {

        JedisPoolManager.getInstance().destory();
    }

    @Override
    public SessionClient getSessionClient() {

        return this.client;
    }
}

