/**
 * @version 2007-1-31
 * @author xalinx at gmail dot com
 */
package com.zqh.java.tenwtps;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * NTS
 * 
 * blocking queueu搬运工
 * 
 * @author zjf2308
 * @author xalinx
 * 
 */
public abstract class BlockingTaskPorter<E, M> extends AbstractTaskPorter<E, M> {
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
	 * 队列里的任务最大数
	 */
	private int maxTaskSize;

	public void setMaxTaskSize(int maxTaskSize) {
		this.maxTaskSize = maxTaskSize;
	}

	public boolean isOpen() {
		return openTag;
	}

	/**
	 * Task queue
	 */
	protected final BlockingQueue<Task<E, M>> taskQueue = new LinkedBlockingQueue<Task<E, M>>();

	public void run() {
		initResource();

		workTag = true;
		openTag = true;

		log.info(getName() + " open");

		while (workTag) {
			try {
				Task<E, M> task;
				try {
					task = this.retrieveTask();
				} catch (InterruptedException e) {
					continue;
				}
				if (!workTag) {
					break;
				}
				if (null == task) {
					continue;
				}
				runTask(task);
			} catch (Exception e) {
				log.warn(getName() + " caught: " + e, e);
			}
		}

		clearResource();

		openTag = false;
	}

	public void doStop() {
		log.info(getName() + " stopping ...");

		// 关闭搬运标志
		workTag = false;

		while (isOpen()) {
			try {
				sleep(100);
			} catch (InterruptedException e) {
				log.warn(e);
			}
		}

		log.info(getName() + " stopped");
	}

	/**
	 * For the use of server to add task into task queue
	 * 
	 * @param task
	 */
	public void add(Task<E, M> task) {
		if (getTaskSize() >= this.maxTaskSize) {
			log.warn("Task queue overflow");
			return;
		}
		try {
			this.taskQueue.put(task);
		} catch (InterruptedException e) {
			log.warn("addTask:", e);
		}
	}

	public int getTaskSize() {
		return this.taskQueue.size();
	}

	protected abstract void runTask(Task<E, M> task) throws Exception;

	/**
	 * retrieve task
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	protected Task<E, M> retrieveTask() throws InterruptedException {
		// TODO 这里用poll不用take是为了能通过doStop终止porter
		Task<E, M> task = taskQueue.poll(1000, TimeUnit.MILLISECONDS);
		return task;
	}

}
