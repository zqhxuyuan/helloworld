/**
 * @version 2007-1-31
 * @author xalinx at gmail dot com
 */
package com.zqh.java.tenwtps;

import java.nio.channels.SelectionKey;


/**
 * 任务
 * 
 * @version 2007-2-16
 * @author xalinx at gmail dot com
 * 
 * @param <E>
 *            request object type
 * @param <M>
 *            response object type
 */
public interface Task<E, M> extends Runnable {

	/**
	 * get task id
	 * 
	 * @return
	 */
	int getId();

	/**
	 * set task id
	 * 
	 * @param id
	 */
	void setId(int id);

	/**
	 * get task create time
	 * 
	 * @return task create time, millisecond
	 */
	long getCreateTime();

	/**
	 * set create time
	 * 
	 * @param time task create time, millisecond
	 */
	void setCreateTime(long time);

	/**
	 * binds this task to specified selection key
	 * 
	 * @param key
	 */
	void bind(SelectionKey key);


	/**
	 * 得到编码后的字节数组
	 * 
	 * @return
	 */
	byte[] getEncodeData();


	/**
	 * 设置需要解码的数据
	 * 
	 * @param decode
	 */
	void setDecodeData(byte[] decode);

	/**
	 * 设置请求解析器
	 * 
	 * @param requestParser
	 */
	void setRequestParser(Parser<E> requestParser);

	/**
	 * 设置响应解析器
	 * 
	 * @param responseParser
	 */
	void setResponseParser(Parser<M> responseParser);

}
