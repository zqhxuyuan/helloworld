/**
 * @version 2007-2-26
 * @author xalinx at gmail dot com
 */
package com.zqh.java.tenwtps;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;


/**
 * 管理解析器代理
 * 
 * @version 2007-2-26
 * @author xalinx at gmail dot com
 * 
 */
public abstract class ParserProxy<E> extends LengthBodyParser<E> {

	public E readBody(DataInput in) throws IOException {
		int cmd = in.readInt();
		E value = readExtend(in, cmd);
		if (value == null) {
			value = readDefault(in, cmd);
		}
		return value;
	}

	public void writeBody(E value, DataOutput out) throws IOException {
		if (!writeExtend(value, out)) {
			writeDefault(value, out);
		}
	}

	/**
	 * 读默认的对象
	 * 
	 * @param in
	 * @param cmd
	 * @return
	 * @throws IOException
	 */
	protected abstract E readDefault(DataInput in, int cmd) throws IOException;

	/**
	 * 写默认的对象
	 * 
	 * @param value
	 * @param out
	 * @throws IOException
	 */
	protected abstract void writeDefault(E value, DataOutput out)
			throws IOException;

	/**
	 * 读扩展的
	 * 
	 * @param in
	 * @param cmd
	 * @return
	 * @throws IOException
	 */
	protected abstract E readExtend(DataInput in, int cmd) throws IOException;

	/**
	 * 写扩展的
	 * 
	 * @param value
	 * @param out
	 * @return
	 * @throws IOException
	 */
	protected abstract boolean writeExtend(E value, DataOutput out)
			throws IOException;
}
