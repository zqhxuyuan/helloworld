
package com.tc.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An cache works in request lifecycle.
 *
 * @author kozz.gaof
 * @date Jan 10, 2015 5:06:42 PM
 * @id $Id$
 */
public class RequestCache {
    
    public static final Object PLACEHOLDER = new Object();
    
    public static final String SESSION = "tcsession.current.session";
    
    private static final ThreadLocal<RequestCache> cache = new ThreadLocal<RequestCache>();
    
    private Map<String, Object> m = new ConcurrentHashMap<String, Object>();
    
    // empty constructor to avoid instantiating this class unintentionally outside.
    private RequestCache() {
    
    }
    
    // this cache is intend to store actually everything..
    @SuppressWarnings("unchecked")
    public <T> T get(String k) {
    
        return (T) m.get(k);
    }
    
    public void put(String k, Object v) {
    
        m.put(k, v);
    }
    
    public static void begin() {
    
        cache.set(new RequestCache());
    }
    
    public static RequestCache getFromThread() {
    
        RequestCache requestCache = cache.get();
        if (requestCache == null) {
            throw new IllegalStateException("A RequestCache can ONLY be get after calling its begin() method!");
        }
        return requestCache;
    }
    
    public static void remove() {
    
        cache.remove();
    }
}
