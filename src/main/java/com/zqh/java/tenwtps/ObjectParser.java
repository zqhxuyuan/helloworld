package com.zqh.java.tenwtps;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 
 * TS
 * 
 * @version 2007-2-16
 * @author xalinx at gmail dot com
 * 
 */
public class ObjectParser<E extends Serializable> extends SingletonParser<E> {

	@SuppressWarnings("unchecked")
	public E read(DataInput in) throws IOException {
		int length = in.readInt();

		// 防止流坏了，超长
		if (length > 3000000) {
			throw new IOException("流超长。");
		}

		// 生成对象
		byte[] content = new byte[length];
		in.readFully(content);
		ByteArrayInputStream byteIn = new ByteArrayInputStream(content);
		GZIPInputStream gzip = new GZIPInputStream(byteIn);
		ObjectInputStream objIn = new ObjectInputStream(gzip);

		E value = null;
		try {
			value = (E) objIn.readObject();
		} catch (ClassNotFoundException e) {
			throw new IOException("类不存在");
		}
		return value;

	}

	/**
	 * 将对象序列化之后返回给对方
	 */
	public void write(E value, DataOutput out) throws IOException {
		byte[] valueEncode = encode(value);
		// 先输出cache的长度
		out.writeInt(valueEncode.length);
		// 再输出cache字节
		out.write(valueEncode);
	}

	public byte[] encode(E value) throws IOException {
		// 将对象先序列化
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		GZIPOutputStream gzip = new GZIPOutputStream(bOut);
		ObjectOutputStream outStream = new ObjectOutputStream(gzip);
		outStream.writeObject(value);
		// 将压缩完的值压入数组
		gzip.finish();
		byte[] valueEncode = bOut.toByteArray();
		return valueEncode;
	}

}
