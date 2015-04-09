
package com.tc.trinity.utils;

import java.lang.reflect.Method;

import org.springframework.aop.TargetSource;

/**
 *
 * @author kozz.gaof
 * @date Jan 20, 2015 8:53:04 PM
 * @id $Id$
 */
public class CGLIBHelper {
    
    public static Object getTargetObject(Object proxied) {
    
        try {
            return findSpringTargetSource(proxied).getTarget();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private static TargetSource findSpringTargetSource(Object proxied) {
    
        Method[] methods = proxied.getClass().getDeclaredMethods();
        Method targetSourceMethod = findTargetSourceMethod(methods);
        targetSourceMethod.setAccessible(true);
        try {
            return (TargetSource) targetSourceMethod.invoke(proxied);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private static Method findTargetSourceMethod(Method[] methods) {
    
        for (Method method : methods) {
            if (method.getName().endsWith("getTargetSource")) {
                return method;
            }
        }
        throw new IllegalStateException("Could not find target source method on proxied object.");
    }
}
