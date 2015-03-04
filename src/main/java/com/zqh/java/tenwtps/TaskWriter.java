/**
 * @version 2007-3-5
 * @author xalinx at gmail dot com
 */
package com.zqh.java.tenwtps;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 绑定一个socketChannel和该channel上的SelectionKey
 * 
 * @version 2007-3-5
 * @author xalinx at gmail dot com
 * 
 */
public class TaskWriter {
	// private static final Log log = LogFactory.getLog(TaskWriter.class);

	private SelectionKey key;

	private SocketChannel channel;

	private List<ByteBuffer> buffers;

	private ByteBuffer writeBuffer;

	private final Lock writeLock = new ReentrantLock();

	public TaskWriter(SelectionKey key) {
		this.key = key;
		this.channel = (SocketChannel) key.channel();
	}

	// { 供nioSocketListener 和 abstractTask 多线程调用
	public void write(byte[] encode) {
		try {
			writeLock.lock();
			if (buffers == null) {
				buffers = new LinkedList<ByteBuffer>();
			}
			if (encode != null && encode.length > 0) {
				buffers.add(ByteBuffer.wrap(encode));
			}
			if (key.isValid()) {
				key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
				key.selector().wakeup();
			}
		} finally {
			writeLock.unlock();
		}
	}

	// }

	// { 供NioSocketListener单线程调用

	/**
	 * 写入
	 * 
	 * @return 返回此次写入的字节数
	 * @throws IOException
	 */
	public long flush() throws IOException {
		long count;

		try {
			writeLock.lock();
			if (writeBuffer == null) {
				// int size = buffers.size();
				// writeBuffers = new ByteBuffer[size];
				// buffers.toArray(writeBuffers);
				// buffers.clear();
				writeBuffer = buffers.remove(buffers.size() - 1);
			}

			count = channel.write(writeBuffer);

			// if (!writeBuffers[writeBuffers.length - 1].hasRemaining()) {
			if (!writeBuffer.hasRemaining()) {
				writeBuffer = null;
				if (buffers.isEmpty()) {
					key.interestOps(SelectionKey.OP_READ);
				}
			}
		} finally {
			writeLock.unlock();
		}

		return count;
	}

	/**
	 * 
	 */
	public void close() {
		try {
			writeLock.lock();
			writeBuffer = null;
			buffers = null;
		} finally {
			writeLock.unlock();
		}
	}
	// }
}
