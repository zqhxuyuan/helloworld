/**
 * @version 2007-2-26
 * @author xalinx at gmail dot com
 */
package com.zqh.java.tenwtps.admin;

import com.zqh.java.tenwtps.Parser;
import com.zqh.java.tenwtps.ParserProxy;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;


/**
 * admin request parser proxy and factory
 * 
 * @version 2007-2-26
 * @author xalinx at gmail dot com
 * 
 */
public class AdminRequestParserProxy extends ParserProxy<AdminRequest>
		implements AdminRequestParser<AdminRequest> {
	/**
	 * singleton
	 */
	private static final AdminRequestParserProxy single = new AdminRequestParserProxy();

	/**
	 * get instance
	 * 
	 * @return
	 */
	public static AdminRequestParserProxy getInstance() {
		return single;
	}

	protected AdminRequest readDefault(DataInput in, int cmd)
			throws IOException {
		AdminRequest request = null;
		AdminRequestParser<AdminRequest> parser = AdminRequestBodyParserFactory
				.getParser(cmd);
		if (parser == null) {
			request = new AdminRequest();
			request.setCommand(cmd);
		} else {
			request = parser.read(in);
		}
		return request;
	}

	protected void writeDefault(AdminRequest value, DataOutput out)
			throws IOException {
		Parser<AdminRequest> parser = AdminRequestBodyParserFactory
				.getParser(value.getClass());
		if (parser == null) {
			out.writeInt(value.getCommand());
		} else {
			parser.write(value, out);
		}
	}

	@Override
	protected void checkLength(int length) throws IOException {
		if (length < -1 || length > 3000) {
			throw new IOException("request length overflow");
		}

	}

	@Override
	protected int getWriteLength(AdminRequest value) throws IOException {
		return value.length();
	}

	/**
	 * @see com.zqh.java.tenwtps.ParserProxy#readExtend(java.io.DataInput,
	 *      int)
	 */
	@Override
	protected AdminRequest readExtend(DataInput in, int cmd) throws IOException {
		return null;
	}

	/**
	 * @see com.zqh.java.tenwtps.ParserProxy#writeExtend(java.lang.Object,
	 *      java.io.DataOutput)
	 */
	@Override
	protected boolean writeExtend(AdminRequest value, DataOutput out)
			throws IOException {
		return false;
	}

}
