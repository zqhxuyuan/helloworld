
package com.tc.session.helper;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SessionId生成器
 * 
 * @author gaofeng
 * @date Sep 18, 2013 2:04:12 PM
 * @id $Id$
 */
public class SessionIdGenerator {
    
    protected final static String SESSION_ID_RANDOM_ALGORITHM = "SHA1PRNG";
    
    protected final static String SESSION_ID_RANDOM_ALGORITHM_ALT = "IBMSecureRandom";
    
    private static Logger log = LoggerFactory.getLogger(SessionIdGenerator.class);
    
    private static Random random;
    
    private static boolean weakRandom;
    
    private static final Object SEEDOBJECT = new Object();
    
    private SessionIdGenerator() {
    
    }
    
    static {
        
        if (random == null) {
            try {
                random = SecureRandom.getInstance(SESSION_ID_RANDOM_ALGORITHM);
                weakRandom = false;
            } catch (NoSuchAlgorithmException e) {
                try {
                    random = SecureRandom.getInstance(SESSION_ID_RANDOM_ALGORITHM_ALT);
                    weakRandom = false;
                } catch (NoSuchAlgorithmException e_alt) {
                    log.warn(String.format("==========> Failed in using %s and %s algorithm as random sessionid generator! As an alternative, degrading to java.util.Random.",
                            SESSION_ID_RANDOM_ALGORITHM, SESSION_ID_RANDOM_ALGORITHM_ALT));
                    random = new Random();
                    weakRandom = true;
                }
            }
        }
        random.setSeed(random.nextLong() ^ System.currentTimeMillis()
                ^ SEEDOBJECT.hashCode() ^ Runtime.getRuntime().freeMemory());
    }
    
    public static synchronized String newSessionId(HttpServletRequest request) {
    
        // pick a new unique ID!
        String id = null;
        while (id == null || id.length() == 0) {
            long r = weakRandom ? (SEEDOBJECT.hashCode()
                    ^ Runtime.getRuntime().freeMemory() ^ random.nextInt() ^ (((long) request
                    .hashCode()) << 32)) : random.nextLong();
            r ^= System.currentTimeMillis();
            if (request != null && request.getRemoteAddr() != null)
                r ^= request.getRemoteAddr().hashCode();
            if (r < 0)
                r = -r;
            id = Long.toString(r, 36);
        }
        
        return id;
    }
}
