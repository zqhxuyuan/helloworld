/**
 * @version 2007-2-2
 * @author xalinx at gmail dot com
 */
package com.zqh.java.tenwtps;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池实现的任务搬运工
 * 
 * @version 2007-2-2
 * @author xalinx at gmail dot com
 * 
 */
public abstract class ThreadPoolTaskPorter<E, M> extends
		AbstractTaskPorter<E, M> {
	/**
	 * 搬运标志
	 * 
	 * 初始不监听
	 */
	private volatile boolean workTag = false;

	/**
	 * 活动状态标志
	 * 
	 * 初始不活动
	 */
	private volatile boolean openTag = false;

	/**
	 * 线程池
	 */
	private ThreadPoolExecutor taskPool;

	/**
	 * 任务队列
	 */
	private BlockingQueue<Runnable> taskQueue;

	/**
	 * 队列里的任务最大数
	 */
	private int maxTaskSize;

	public int getMaxTaskSize() {
		return maxTaskSize;
	}

	public void setMaxTaskSize(int maxTaskSize) {
		this.maxTaskSize = maxTaskSize;
	}

	/**
	 * 池里的最小线程数
	 */
	private int minThreadSize;

	public int getMinThreadSize() {
		return minThreadSize;
	}

	public void setMinThreadSize(int minThreadSize) {
		this.minThreadSize = minThreadSize;
	}

	/**
	 * 当池里的线程超过minThreadSize时候,idle线程的存活期
	 */
	private long threadLiveTime;

	public long getThreadLiveTime() {
		return threadLiveTime;
	}

	public void setThreadLiveTime(long threadLiveTime) {
		this.threadLiveTime = threadLiveTime;
	}

	/**
	 * 池里的最大线程数
	 */
	private int maxThreadSize;

	public int getMaxThreadSize() {
		return maxThreadSize;
	}

	public void setMaxThreadSize(int maxThreadSize) {
		this.maxThreadSize = maxThreadSize;
	}

	/**
	 * 设置默认参数
	 */
	public ThreadPoolTaskPorter() {
		setName("Thread Pool Task Porter");
		this.maxTaskSize = 10240;
		this.minThreadSize = Runtime.getRuntime().availableProcessors() + 1;
		this.maxThreadSize = minThreadSize * 2;

	}

	public boolean isOpen() {
		return openTag;
	}

	public void add(Task<E, M> task) {
		this.taskPool.execute(task);

	}

	public int getTaskSize() {
		return this.taskQueue.size();
	}

	public void run() {
		initResource();

		workTag = true;
		openTag = true;

		log.info(getName() + " open");

		while (workTag) {
			try {
				//TODO :(
				sleep(2000);
			} catch (InterruptedException e) {
				log.warn(getName() + " run: ", e);
			}
		}

		clearResource();

		openTag = false;
	}

	public void doStop() {
		log.info(getName() + " stopping ...");

		// 关闭搬运标志
		workTag = false;

		// 等待此线程退出
		try {
			this.join();
		} catch (InterruptedException e) {
			log.warn(getName() + " do stop:", e);
		}

		log.info(getName() + " stopped");
	}

	protected void initResource() {
		this.taskQueue = new LinkedBlockingQueue<Runnable>(maxTaskSize);
		this.taskPool = new ThreadPoolExecutor(minThreadSize, maxThreadSize,
				threadLiveTime, TimeUnit.MICROSECONDS, taskQueue);
	}

	protected void clearResource() {
		this.taskPool.shutdown();
		this.taskPool = null;
		this.taskQueue = null;
	}

}
