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
public abstract class AdminRequestBodyParser<E extends AdminRequest> extends
		SingletonParser<E> implements AdminRequestParser<E> {

	public void write(E value, DataOutput out) throws IOException {
		out.writeInt(value.getCommand());
		writeOther(value, out);
	}

	protected abstract void writeOther(E value, DataOutput out)
			throws IOException;

}
