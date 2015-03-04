/**
 * @version 2007-2-2
 * @author xalinx at gmail dot com
 */
package com.zqh.java.tenwtps;

/**
 * NTS
 * 
 * 任务搬运工
 * 
 * 负责接收任务,处理任务
 * 
 * @version 2007-2-2
 * @author xalinx at gmail dot com
 * 
 */
public interface TaskPorter<E, M> {
	String getName();

	int getTaskSize();

	void add(Task<E, M> task);

	boolean isOpen();

	void doStop();

}
