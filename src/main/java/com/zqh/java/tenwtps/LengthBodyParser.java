/**
 * @version 2007-3-8
 * @author xalinx at gmail dot com
 */
package com.zqh.java.tenwtps;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * 正文长度和正文内容分开的传输对象解析器
 * 
 * @version 2007-3-8
 * @author xalinx at gmail dot com
 * 
 */
public abstract class LengthBodyParser<E> extends SingletonParser<E> {
	private final Log log = LogFactory.getLog(this.getClass());

	/**
	 * @see Parser#read(java.io.DataInput)
	 */
	public E read(DataInput in) throws IOException {
		int length = readLength(in);
		log.debug("read length: " + length);
		checkLength(length);
		E value = readBody(in);
		return value;
	}

	/**
	 * @see Parser#write(java.lang.Object,
	 *      java.io.DataOutput)
	 */
	public void write(E value, DataOutput out) throws IOException {
		int length = getWriteLength(value);
		log.debug("write length: " + length);
		checkLength(length);
		writeLength(out, length);
		writeBody(value, out);
	}

	/**
	 * 读正文
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	protected abstract E readBody(DataInput in) throws IOException;

	/**
	 * 得到要写入的正文长度
	 * 
	 * @param out
	 * @param length
	 * @throws IOException
	 */
	protected abstract int getWriteLength(E value) throws IOException;

	/**
	 * 写入请求
	 * 
	 * @param out
	 * @param request
	 * @throws IOException
	 */
	protected abstract void writeBody(E value, DataOutput out)
			throws IOException;

	/**
	 * 校验正文长度
	 * 
	 * @param length
	 * @throws IOException
	 */
	protected abstract void checkLength(int length) throws IOException;

	/**
	 * 读正文长度
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	private int readLength(DataInput in) throws IOException {
		int length = in.readInt();
		return length;
	}

	private void writeLength(DataOutput out, int length) throws IOException {
		out.writeInt(length);
	}

}
