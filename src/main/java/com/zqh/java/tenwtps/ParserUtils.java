package com.zqh.java.tenwtps;

import com.zqh.java.tenwtps.utils.StringKit;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;


/**
 * 解析器工具类
 * 
 * @author lin.wangl
 * 
 */
public class ParserUtils {

	public static void writeArrayLength(Object[] array, DataOutput out)
			throws IOException {
		if (array == null) {
			out.writeInt(-1);
			return;
		}
		out.writeInt(array.length);
	}

	/**
	 * 字符串占的字节数
	 * 
	 * @param s
	 * @return
	 */
	public static int stringByteLength(String s) {
		return s == null ? 0 : s.getBytes().length;
	}

	/**
	 * 写入字符串,包括长度和字符字节
	 * 
	 * @param out
	 * @param s
	 * @throws IOException
	 */
	public static void writeString(DataOutput out, String s) throws IOException {
		if (s == null) {
			out.writeInt(-1);
			return;
		}
		if (s.length() == 0) {
			out.writeInt(0);
			return;
		}
		byte[] buffer = s.getBytes();
		int len = buffer.length;
		out.writeInt(len);
		out.write(buffer, 0, len);
	}

	/**
	 * 读出字符串
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static String readString(DataInput in) throws IOException {
		int length = in.readInt();
		if (length < 0) {
			return null;
		}
		if (length == 0) {
			return StringKit.EMPTY;
		}
		byte[] buffer = new byte[length];
		in.readFully(buffer);
		return new String(buffer);
	}

}
