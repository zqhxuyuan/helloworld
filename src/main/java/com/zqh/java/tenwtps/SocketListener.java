/**
 * @version 2007-1-31
 * @author xalinx at gmail dot com
 */
package com.zqh.java.tenwtps;

/**
 * socket listener
 * 
 * @version 2007-1-31
 * @author xalinx at gmail dot com
 * 
 */
public interface SocketListener extends Runnable {

	public boolean isStarted();

	public void doStop();

}
