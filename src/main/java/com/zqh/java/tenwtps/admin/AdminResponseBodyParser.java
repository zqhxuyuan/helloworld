/**
 * @version 2007-3-9
 * @author xalinx at gmail dot com
 */
package com.zqh.java.tenwtps.admin;

import com.zqh.java.tenwtps.SingletonParser;

import java.io.DataOutput;
import java.io.IOException;



/**
 * @version 2007-3-9
 * @author xalinx at gmail dot com
 * 
 */
public abstract class AdminResponseBodyParser<E extends AdminResponse> extends
		SingletonParser<E> implements AdminResponseParser<E> {

	public void write(E value, DataOutput out) throws IOException {
		out.writeInt(value.getCommand());
		writeOther(value, out);
	}

	/**
	 * 写锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷牟锟斤拷锟?
	 * 
	 * @param value
	 * @param out
	 * @throws IOException
	 */
	protected abstract void writeOther(E value, DataOutput out)
			throws IOException;

}
