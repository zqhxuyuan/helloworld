/**
 * @version 2007-1-31
 * @author xalinx at gmail dot com
 */
package com.zqh.java.tenwtps;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * socket listener, implements by nio
 * 
 * @version 2007-1-31
 * @author xalinx at gmail dot com
 * 
 */
public abstract class NioSocketListener<E, M> implements SocketListener {
	private final Log log = LogFactory.getLog(this.getClass());

	/**
	 * Socket listening flag, default is closed
	 */
	private volatile boolean listening = false;

	/**
	 * Listener start flag, default is stoped
	 */
	private volatile boolean starting = false;

	/**
	 * Socket listening port
	 */
	private int port;

	/**
	 * backlog length
	 */
	private int backlog;

	/**
	 * task porter
	 */
	protected AbstractTaskPorter<E, M> taskPorter;

	// { getter / setter

	public void setTaskPorter(AbstractTaskPorter<E, M> taskPorter) {
		this.taskPorter = taskPorter;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setBacklog(int backlog) {
		this.backlog = backlog;
	}

	public int getPort() {
		return port;
	}

	// }

	/**
	 * Server socket channel
	 */
	private ServerSocketChannel serverSocketChannel;

	/**
	 * Channel selector
	 */
	private Selector selector;

	/**
	 * 
	 */
	private final ReentrantLock lock;
	private final Condition notRuning;

	// { constructor
	protected NioSocketListener() {
		this.lock = new ReentrantLock();
		this.notRuning = lock.newCondition();
	}

	// }

	public void run() {
		// start porter
		taskPorter.setDaemon(true);
		taskPorter.start();
		while (!taskPorter.isOpen()) { // waiting until porter start
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				log.error("", e);
			}
		}

		InetSocketAddress address = new InetSocketAddress(port);
		try {
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);

			// bind address and backlog
			serverSocketChannel.socket().bind(address, backlog);
			selector = Selector.open();

			// register accept event
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		} catch (IOException e) {
			log.error("", e);
		}

		listening = true;
		log.info(" listening on port:" + port);

		while (listening) {
			try {
				selector.select();
			} catch (IOException e) {
				log.warn("run() selector.select():", e);
				continue;
			}
			starting = true;
			for (Iterator<SelectionKey> itt = selector.selectedKeys().iterator(); itt.hasNext();) {
				SelectionKey key = itt.next();
				if (key.isValid()) {
					try {
						if (key.isReadable()) {
							doRead(key);
						} else if (key.isWritable()) {
							doWrite(key);
						} else if (key.isAcceptable()) {
							doAccept(key);
						}
					} catch (Throwable e) {
						destroyKeyAndChannel(key); // destroy key and channel
						log.error("do key:", e);
					}
				}
				itt.remove();
			}

		}

		try {
			serverSocketChannel.close();
			selector.close();
		} catch (IOException e) {
			log.error("stop listener: ", e);
		}

		// 销毁任务搬运工
		this.taskPorter.doStop();

		this.starting = false;
		this.lock.lock();
		try {
			this.notRuning.signalAll();
		} finally {
			this.lock.unlock();
		}
	}

	/**
	 * 判断是否监听状态
	 * 
	 * @return
	 */
	public boolean isStarted() {
		return starting;
	}

	/**
	 * stop listener
	 */
	public void doStop() {
		log.info(" stopping ...");

		// set listening flag false to close listening in method run()
		listening = false;

		if (selector != null) {
			selector.wakeup();
		}

		// waiting run() ended
		this.lock.lock();
		try {
			try {
				if (starting == true) {
					this.notRuning.await();
				}
			} catch (InterruptedException e) {
				log.error("", e);
			}
		} finally {
			this.lock.unlock();
		}

		log.info(" stopped on:" + port);
	}

	/**
	 * 创建空任务模版方法
	 * 
	 * @return
	 */
	protected abstract Task<E, M> createEmpetyTask();

	/**
	 * 创建请求解析器模版方法
	 * 
	 * @return
	 */
	protected abstract Parser<E> getRequestParser();

	/**
	 * 创建响应解析器模版方法
	 * 
	 * @return
	 */
	protected abstract Parser<M> getResponseParser();

	/**
	 * 有客户端连上来，则处理
	 * 
	 * @param acceptKey
	 * @throws IOException
	 */
	private void doAccept(SelectionKey acceptKey) throws IOException {
		if (log.isDebugEnabled()) {
			log.debug("do accept");
		}
		ServerSocketChannel ssc = (ServerSocketChannel) acceptKey.channel();
		SocketChannel channel = ssc.accept();
		if (channel == null) {
			throw new IOException("accept nothing when do accept key");
		}
		channel.configureBlocking(false);

		log.info("do accept: " + channel);

		// 注册读key
		SelectionKey taskKey = channel.register(selector, SelectionKey.OP_READ);

		// 创建任务阅读器
		TaskReader<E, M> reader = new TaskReader<E, M>(createEmpetyTask(), taskKey, getRequestParser(), getResponseParser());
		// 创建任务写入器
		TaskWriter writer = new TaskWriter(taskKey);
		TaskAttach<E, M> attach = new TaskAttach<E, M>(reader, writer);

		taskKey.attach(attach);
	}

	/**
	 * 读取的数据，组装成要处理的task,放入task porter
	 * 
	 * @param readKey
	 * @throws IOException
	 * @throws IOException
	 * @throws IOException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@SuppressWarnings("unchecked")
	private void doRead(SelectionKey readKey) throws IOException {
		if (log.isDebugEnabled()) {
			log.debug("do read");
		}
		TaskAttach<E, M> attach = (TaskAttach<E, M>) readKey.attachment();
		TaskReader<E, M> reader = attach.getTaskReader();
		// 尝试读取任务
		int count = reader.read();
		// 客户端关闭产生的读key有可能读到-1个长度的字节
		if (count < 0) {
			log.warn("client abnormity close: " + readKey.channel());
			destroyKeyAndChannel(readKey);
			return;
		}

		// finish reading task
		if (reader.isReadFinish()) {
			int taskLength = reader.getTaskLength();
			if (taskLength == -1) {
				log.info("client exist: " + readKey.channel());
				destroyKeyAndChannel(readKey);
			} else if (taskLength > 0) {// taskLength > 0 denote common task
				// package
				Task<E, M> task = reader.getTask();
				if (task == null) {
					throw new IllegalStateException("valid task shouldn't be null");
				}
				if (task != null) {
					// transfer task to task porter
					taskPorter.add(task);
				}
			} else if (taskLength == 0) {
				if (log.isDebugEnabled()) {
					log.debug("keep alive: " + readKey.channel());
				}
			}
			if (taskLength != -1) {
				// reset status and ready for next reading
				reader.reset(createEmpetyTask());
			}
		}
	}

	/**
	 * 写入数据
	 * 
	 * @param writeKey
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private void doWrite(SelectionKey writeKey) throws IOException {
		if (log.isDebugEnabled()) {
			log.debug("do write");
		}
		TaskAttach<E, M> attach = (TaskAttach<E, M>) writeKey.attachment();
		TaskWriter writer = attach.getTaskWriter();

		// 输出写缓存
		long count = writer.flush();

		if (log.isDebugEnabled()) {
			log.debug("write count:" + count + " to " + writeKey.channel());
		}
	}

	private void destroyKeyAndChannel(SelectionKey key) {
		SocketChannel channel = (SocketChannel) key.channel();
		try {
			if (key.attachment() != null) {
				@SuppressWarnings("rawtypes")
				TaskAttach attach = (TaskAttach) key.attachment();
				try {
					attach.close();
				} catch (Throwable e) {
					log.error("destroy key attach:", e);
				}
				attach = null;
			}

		} finally {
			try {
				key.cancel();
			} catch (Throwable e) {
				log.error("destroy key:" + key, e);
			}
			try {
				channel.close();
			} catch (Throwable e) {
				log.error("destroy channel:" + channel, e);
			}
		}

	}
}
