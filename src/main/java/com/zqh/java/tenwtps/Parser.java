package com.zqh.java.tenwtps;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * 传输对象解析器
 * 
 * TS
 * 
 * 一种任务解析需要2个parser(request parser, response parser)
 * 
 * @author zjf2308
 * @author lin.wangl
 * 
 */
public interface Parser<E> {

	/**
	 * 写入对象
	 * 
	 * @param out
	 * @throws IOException
	 */
	void write(E value, DataOutput out) throws IOException;



	/**
	 * 读入对象
	 * 
	 * @param in
	 * @throws IOException
	 */
	E read(DataInput in) throws IOException;
}
