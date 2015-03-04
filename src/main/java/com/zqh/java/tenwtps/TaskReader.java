package com.zqh.java.tenwtps;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;


/**
 * 
 * NTS
 * 
 * 任务读取对象,从一个channel里读取数据组成各个task
 * 
 * @author zjf2308
 * 
 */
public class TaskReader<E, M> {
	// private static final Log log = LogFactory.getLog(TaskReader.class);

	/**
	 * taskbuffer
	 */
	private ByteBuffer taskBuffer;

	/**
	 * task长度buffer
	 */
	private ByteBuffer taskLengthBuffer;

	/**
	 * 最后读取数据的时间
	 */
	private long lastMakeTime;

	/**
	 * 正文长度
	 */
	private int taskLength;

	/**
	 * 填充用的任务
	 */
	private Task<E, M> task;

	/**
	 * 绑定的key
	 */
	private SelectionKey key;

	/**
	 * 跟key对应的频道
	 */
	private SocketChannel channel;

	/**
	 * 任务是否读取完标志
	 */
	private boolean readFinish;

	/**
	 * 请求解析器
	 */
	private Parser<E> requestParser;

	/**
	 * 响应解析器
	 */
	private Parser<M> responseParser;

	/**
	 * 判断任务是否读取完整
	 * 
	 * @return
	 */
	public boolean isReadFinish() {
		return readFinish;
	}

	public int getTaskLength() {
		return taskLength;
	}

	/**
	 * 初始化
	 */
	private TaskReader() {
		// 分配任务长度缓冲区
		this.taskLengthBuffer = ByteBuffer.allocate(4);
	}

	/**
	 * @param task
	 *            供填充用的空白任务
	 * @param key
	 *            nio selector key
	 * @param requestParser
	 *            请求解析器
	 * @param responseParser
	 *            响应解析器
	 */
	public TaskReader(Task<E, M> task, SelectionKey key,
			Parser<E> requestParser, Parser<M> responseParser) {
		this();
		this.task = task;
		this.key = key;
		this.channel = (SocketChannel) key.channel();
		this.requestParser = requestParser;
		this.responseParser = responseParser;
	}

	/**
	 * 得到最后读取数据的时间
	 * 
	 * @return
	 */
	public long getLastReadTime() {
		return lastMakeTime;
	}

	public Task<E, M> getTask() {
		return task;
	}

	/**
	 * 读取报文并生成task
	 * 
	 * 外部程序应该不停调用该方法后尝试获取task
	 * 
	 * @return 此次读取的字节数
	 * 
	 * @throws IOException
	 * @throws IOException
	 */
	public int read() throws IOException {
		int count = 0;
		// 未读满正文长度
		if (taskLengthBuffer.remaining() > 0) {
			count = channel.read(taskLengthBuffer);
			// 已读满正文长度
			if (taskLengthBuffer.remaining() == 0) {
				taskLengthBuffer.flip();
				taskLength = taskLengthBuffer.getInt();// 得到task长度
				if (taskLength <= 0) {// client keep-alive or exist
					readFinish = true; // 
				} else if (taskLength > 3000000) {
					throw new IOException("task input data overflow: "
							+ taskLength);
				} else {
					taskBuffer = ByteBuffer.allocate(taskLength);// 分配正文buffer
				}
			}
		}

		if (taskBuffer != null && taskBuffer.remaining() > 0) {// 未读满正文
			count += channel.read(taskBuffer);
			if (taskBuffer.remaining() == 0) {// 已读满正文
				taskBuffer.flip();
				fillTask();// 构建任务
				readFinish = true;// 读完
			}
		}

		lastMakeTime = System.currentTimeMillis();

		return count;
	}

	/**
	 * 创建task
	 * 
	 * @return
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void fillTask() {
		int taskId = taskBuffer.getInt();
		task.setId(taskId);
		byte[] decodeData = new byte[taskLength - 4];
		taskBuffer.get(decodeData);
		task.setDecodeData(decodeData);
		task.bind(key);
		task.setRequestParser(requestParser);
		task.setResponseParser(responseParser);
		task.setCreateTime(System.currentTimeMillis());
	}

	/**
	 * 关闭task maker
	 * 
	 * 清除buffers
	 * 
	 * @throws IOException
	 */
	public void close() {
		taskBuffer = taskLengthBuffer = null;
		task = null;
		readFinish = false;
	}

	/**
	 * 重置task maker状态,等待新报文读取
	 * 
	 * 重置报文长度buffer,取消报文buffer,设置待填充空任务
	 * 
	 * @param emptyTask
	 */
	public void reset(Task<E, M> emptyTask) {
		taskLengthBuffer.clear();
		taskBuffer = null;
		task = emptyTask;
		readFinish = false;
	}
}
