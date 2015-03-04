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
 * admin response parser proxy and factory
 * 
 * @version 2007-2-26
 * @author xalinx at gmail dot com
 * 
 */
public class AdminResponseParserProxy extends ParserProxy<AdminResponse>
		implements AdminResponseParser<AdminResponse> {
	/**
	 * singleton
	 */
	private static final AdminResponseParserProxy single = new AdminResponseParserProxy();

	/**
	 * get instance
	 * 
	 * @return
	 */
	public static AdminResponseParserProxy getInstance() {
		return single;
	}
	

	@Override
	protected AdminResponse readDefault(DataInput in, int cmd)
			throws IOException {
		AdminResponse response = null;
		Parser<AdminResponse> parser = AdminResponseBodyParserFactory
				.getParser(cmd);
		if (parser == null) {
			response = new AdminResponse();
			response.setCommand(cmd);
		} else {
			response = parser.read(in);
		}
		return response;
	}

	@Override
	protected void writeDefault(AdminResponse value, DataOutput out)
			throws IOException {
		Parser<AdminResponse> parser = AdminResponseBodyParserFactory
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
	protected int getWriteLength(AdminResponse value) throws IOException {
		return value.length();
	}

	@Override
	protected AdminResponse readExtend(DataInput in, int cmd)
			throws IOException {
		return null;
	}

	@Override
	protected boolean writeExtend(AdminResponse value, DataOutput out)
			throws IOException {
		return false;
	}

}
