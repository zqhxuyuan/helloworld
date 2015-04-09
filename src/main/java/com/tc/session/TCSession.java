
package com.tc.session;

import static com.tc.session.RequestCache.PLACEHOLDER;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionContext;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * HttpSession的代理类。实际的操作通过远端访问完成
 * 
 * @author gaofeng
 * @date Sep 12, 2013 4:12:46 PM
 * @id $Id$
 */
@SuppressWarnings("deprecation")
public class TCSession implements HttpSession {
    
    private static final Logger logger = LoggerFactory.getLogger(TCSession.class);
    
    private SessionClient sessionClient;
    
    private ServletContext servletContext;
    
    private SessionMetaData metadata;
    
    private boolean isNew;
    
    private RequestCache cache;
    
    /**
     * 构造方法,指定ID
     * 
     * @param sessionClient
     * @param id
     */
    public TCSession(SessionClient sessionClient, SessionMetaData metadata, boolean isNew) {
    
        this.sessionClient = sessionClient;
        this.metadata = metadata;
        this.isNew = isNew;
        this.cache = RequestCache.getFromThread();
    }
    
    public TCSession(SessionClient sessionClient, String id) {
    
        this.sessionClient = sessionClient;
        this.metadata = new SessionMetaData();
        this.metadata.setId(id);
        this.cache = RequestCache.getFromThread();
    }
    
    public SessionMetaData getSessionMetadata() {
    
        return this.metadata;
    }
    
    @Override
    public long getCreationTime() {
    
        return metadata.getCreationTime();
    }
    
    @Override
    public String getId() {
    
        return metadata.getId();
    }
    
    @Override
    public long getLastAccessedTime() {
    
        return metadata.getLastAccessedTime();
    }
    
    public void setServletContext(ServletContext servletContext) {
    
        this.servletContext = servletContext;
    }
    
    @Override
    public ServletContext getServletContext() {
    
        return this.servletContext;
    }
    
    @Override
    public void setMaxInactiveInterval(int interval) {
    
        this.metadata.setMaxInactiveInterval(interval);
    }
    
    @Override
    public int getMaxInactiveInterval() {
    
        return this.metadata.getMaxInactiveInterval();
    }
    
    @Override
    public HttpSessionContext getSessionContext() {
    
        // I don't have a fucking idea with this fucking method
        // so, just copy the following snippet from Jetty's implementation.
        return new HttpSessionContext() {
            
            public HttpSession getSession(String sessionId) {
            
                return null;
            }
            
            @SuppressWarnings({ "rawtypes", "unchecked" })
            public Enumeration getIds() {
            
                return Collections.enumeration(Collections.EMPTY_LIST);
            }
        };
    }
    
    @Override
    public Object getValue(String name) {
    
        return getAttribute(name);
    }
    
    @Override
    public void putValue(String name, Object value) {
    
        setAttribute(name, value);
    }
    
    @Override
    public void removeValue(String name) {
    
        removeAttribute(name);
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public String[] getValueNames() {
    
        List<String> names = new ArrayList<String>();
        Enumeration n = getAttributeNames();
        while (n.hasMoreElements()) {
            names.add((String) n.nextElement());
        }
        return names.toArray(new String[] {});
    }
    
    @Override
    public boolean isNew() {
    
        return isNew;
    }
    
    /**
     * 被访问
     */
    public void access() {
    
        this.isNew = false;
        this.metadata.setLastAccessedTime(System.currentTimeMillis());
    }
    
    /**
     * 触发Session的事件
     * 
     * @param value
     */
    protected void fireHttpSessionBindEvent(String name, Object value) {
    
        // 处理Session的监听器
        if (value != null && value instanceof HttpSessionBindingListener) {
            HttpSessionBindingEvent event = new HttpSessionBindingEvent(this, name, value);
            ((HttpSessionBindingListener) value).valueBound(event);
        }
    }
    
    /**
     * 触发Session的事件
     * 
     * @param value
     */
    protected void fireHttpSessionUnbindEvent(String name, Object value) {
    
        // 处理Session的监听器
        if (value != null && value instanceof HttpSessionBindingListener) {
            HttpSessionBindingEvent event = new HttpSessionBindingEvent(this, name, value);
            ((HttpSessionBindingListener) value).valueUnbound(event);
        }
    }
    
    public boolean isValid() {
    
        return getLastAccessedTime() + getMaxInactiveInterval() > System.currentTimeMillis();
    }
    
    @Override
    public Object getAttribute(String name) {
    
        if (name == null) {
            throw new IllegalArgumentException("attribute name cannot be null");
        }
        
        access();
        Object v = cache.get(getId() + "_" + name);
        if (v != null) {
            return v == PLACEHOLDER ? null : v;
        } else {
            try {
                Object o = sessionClient.getAttribute(getId(), name);
                cache.put(this.metadata.getId() + "_" + name, o == null ? PLACEHOLDER : o);
                return o;
            } catch (Exception ex) {
                logger.error(String.format("==========> Error occurs when getting attribute: [%s] from session: [%s]", name, this), ex);
                return null;
            }
        }
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Enumeration getAttributeNames() {
    
        access();
        String id = getId();
        if (StringUtils.isNotBlank(id)) {
            // 返回Session节点下的数据名字
            try {
                List<String> names = sessionClient.getAttributeNames(id);
                if (names != null) {
                    return Collections.enumeration(names);
                }
            } catch (Exception ex) {
                logger.error("==========> Error occurs when calling getAttributeNames from session: " + this, ex);
            }
        }
        return null;
    }
    
    @Override
    public void setAttribute(String name, Object value) {
    
        if (name == null) {
            throw new IllegalArgumentException("attribute name cannot be null");
        }
        
        if (value == null) {
            removeAttribute(name);
        } else if (!(value instanceof Serializable)) {
            logger.warn(String.format("==========> Error in setting attribute [key: %s] - [value: %s] ", name, value));
            throw new IllegalArgumentException("The attribute of TCSession MUST implement Serializable!");
        }
        
        access();
        try {
            sessionClient.setAttribute(getId(), name, (Serializable) value);
            cache.put(getId() + "_" + name, value);
        } catch (Exception ex) {
            logger.error(String.format("==========> Error occurs when setting attribute: [%s] of session: [%s]", name, this), ex);
        }
        fireHttpSessionBindEvent(name, value);
    }
    
    @Override
    public void removeAttribute(String name) {
    
        if (name == null) {
            throw new IllegalArgumentException("attribute name cannot be null");
        }
        
        access();
        Object value = null;
        String id = getId();
        if (StringUtils.isNotBlank(id)) {
            try {
                sessionClient.removeAttribute(id, name);
                cache.put(getId() + "_" + name, PLACEHOLDER);
            } catch (Exception ex) {
                logger.error(String.format("==========> Error occurs when removing attribute: [%s] from session: [%s]", name, this), ex);
            }
        }
        fireHttpSessionUnbindEvent(name, value);
    }
    
    @Override
    public void invalidate() {
    
        String id = getId();
        if (StringUtils.isNotBlank(id)) {
            try {
                Map<String, Object> sessionMap = sessionClient.removeSession(id);
                if (sessionMap != null && sessionMap.size() > 0) {
                    for (Map.Entry<String, Object> entry : sessionMap.entrySet()) {
                        fireHttpSessionUnbindEvent(entry.getKey(), entry.getValue());
                    }
                }
            } catch (Exception ex) {
                logger.error("==========> Error occurs when invalidating session: " + this, ex);
            }
        }
    }
    
    @Override
    public String toString() {
    
        return "TCSession [id=" + getId() + ", createTime=" + getCreationTime() + ", lastAccessTime=" + getLastAccessedTime() + ", maxInactiveInterval=" + getMaxInactiveInterval() + ", isNew=" + isNew + "]";
    }
    
    public static class SessionMetaData implements Serializable {
        
        private static final long serialVersionUID = -6446174402446690125L;
        
        private String id;
        
        /** session的创建时间 */
        private Long creationTime;
        
        /** session的最大空闲时间 */
        private int maxInactiveInterval;
        
        /** session的最后一次访问时间 */
        private Long lastAccessedTime;
        /** 当前版本 */
        private int version = 0;
        
        public SessionMetaData() {
        
            this.creationTime = System.currentTimeMillis();
            this.lastAccessedTime = this.creationTime;
        }
        
        public Long getCreationTime() {
        
            return creationTime;
        }
        
        public void setCreationTime(Long creationTime) {
        
            this.creationTime = creationTime;
        }
        
        public int getMaxInactiveInterval() {
        
            return maxInactiveInterval;
        }
        
        public void setMaxInactiveInterval(int maxInactiveInterval) {
        
            this.maxInactiveInterval = maxInactiveInterval;
        }
        
        public Long getLastAccessedTime() {
        
            return lastAccessedTime;
        }
        
        public void setLastAccessedTime(Long lastAccessedTime) {
        
            this.lastAccessedTime = lastAccessedTime;
        }
        
        public Boolean isValid() {
        
            return (getLastAccessedTime() + getMaxInactiveInterval()) > System.currentTimeMillis();
        }
        
        public String getId() {
        
            return id;
        }
        
        public void setId(String id) {
        
            this.id = id;
        }
        
        public int getVersion() {
        
            return version;
        }
        
        public void setVersion(int version) {
        
            this.version = version;
        }
        
        @Override
        public String toString() {
        
            return "SessionMetaData [id=" + id + ", createTime=" + new Date(creationTime) + ", maxIdle=" + maxInactiveInterval + ", lastAccessTime=" + new Date(lastAccessedTime) + ", version=" + version + "]";
        }
    }
    
}
