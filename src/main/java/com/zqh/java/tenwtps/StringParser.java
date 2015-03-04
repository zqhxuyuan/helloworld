package com.zqh.java.tenwtps;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @version 2007-2-15
 * @author zjf2308
 * @author xalinx at gmail dot com
 * 
 */
public class StringParser extends SingletonParser<String> {

	public String read(DataInput in) throws IOException {
		int length = in.readInt();
		byte[] content = new byte[length];
		in.readFully(content);
		return new String(content);
	}

	public void write(String value, DataOutput out) throws IOException {
		out.writeInt(value.getBytes().length);
		out.write(value.getBytes());
	}

	public byte[] encode(String value) throws IOException {
		return value.getBytes();
	}

}
