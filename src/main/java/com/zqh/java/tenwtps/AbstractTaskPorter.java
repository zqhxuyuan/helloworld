/**
 * @version 2007-1-31
 * @author xalinx at gmail dot com
 */
package com.zqh.java.tenwtps;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * NTS
 * 
 * @version 2007-1-31
 * @author xalinx at gmail dot com
 * 
 */
public abstract class AbstractTaskPorter<E, M> extends Thread implements TaskPorter<E, M> {
	protected final Log log = LogFactory.getLog(this.getClass());

	protected abstract void initResource();

	/**
	 * Clear resource
	 */
	protected abstract void clearResource();

}
