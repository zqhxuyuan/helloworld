
package com.tc.trinity.core;

/**
 * 应用异常
 * 
 * @author gaofeng
 * @date Jun 3, 2014 9:49:42 PM
 * @id $Id$
 */
public class TrinityException extends Exception {
    
    private static final long serialVersionUID = -7398720697259771243L;
    
    public TrinityException(String msg) {
    
        super(msg);
    }
    
    public TrinityException(String msg, Throwable cause) {
    
        super(msg, cause);
    }
    
    public TrinityException(Throwable cause) {
    
        super(cause);
    }
}
