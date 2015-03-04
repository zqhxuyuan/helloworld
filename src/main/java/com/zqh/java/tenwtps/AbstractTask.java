package com.zqh.java.tenwtps;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * NTS
 * 
 * 任务抽象类,构建后注入需要解码的字节
 * 
 * @version 2007-4-27
 * @author xingdian
 * @author xalinx at gmail dot com
 * 
 * @param <E>
 *            Request
 * @param <M>
 *            Response
 */
public abstract class AbstractTask<E, M> implements Task<E, M> {
	private static final Log log = LogFactory.getLog(AbstractTask.class);

	/**
	 * 客户端调用ID号, -1表示未解码
	 */
	private int id;

	private int code = CodeConstants.CODE_OK;

	private SelectionKey key;

	/**
	 * 外部注入的供解码成输入数据处理类的字节数组
	 */
	private byte[] decodeData = TaskHelper.emptyBytes;

	/**
	 * 任务处理完以后编码好的结果字节，供外部使用
	 */
	private byte[] encodeData = TaskHelper.emptyBytes;

	/**
	 * 任务创建时间
	 */
	private long createTime;

	/**
	 * 请求解析器
	 */
	private Parser<E> requestParser;

	/**
	 * 响应解析器
	 */
	private Parser<M> responseParser;

	/**
	 * 解码后的对象
	 */
	private E request;

	/**
	 * 供编码的对象
	 */
	private M response;

	public void setRequestParser(Parser<E> requestParser) {
		this.requestParser = requestParser;
	}

	public void setResponseParser(Parser<M> responseParser) {
		this.responseParser = responseParser;
	}

	public Parser<E> getRequestParser() {
		return requestParser;
	}

	public Parser<M> getResponseParser() {
		return responseParser;
	}

	public byte[] getEncodeData() {
		return encodeData;
	}

	public void setDecodeData(byte[] decode) {
		this.decodeData = decode;
	}

	public void setEncodeData(byte[] encode) {
		this.encodeData = encode;
	}

	public void bind(SelectionKey key) {
		this.key = key;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public AbstractTask() {
	}

	public String toString() {
		return "task #" + id + " createTime:" + createTime + " encode size:"
				+ encodeData.length;
	}

	/**
	 * Run task
	 */
	public void run() {
		try {
			decode();
		} catch (Throwable e) {
			code = CodeConstants.CODE_DECODE_ERROR;
			log.error("task decode: ", e);
		}

		try {
			response = compute();
		} catch (Throwable e) {
			code = CodeConstants.CODE_COMPUTE_ERROR;
			log.error("task compute: ", e);
		}

		try {
			encode();
		} catch (Throwable e) {
			log.warn("task encode: ", e);
			close();
			return;
		}

		try {
			register();
		} catch (CancelledKeyException e) { // register()时可能抛出
			log.info("task register but key already cancelled: ", e);
			close();
			return;
		} catch (Throwable e) {
			log.warn("task register: ", e);
			close();
			return;
		}
	}

	/**
	 * 根据输入的writable对象做处理，生成要输出的writable对象
	 * 
	 * @throws Exception
	 */
	protected abstract M compute() throws Exception;

	protected E getRequest() {
		return request;
	}

	protected void setCode(int code) {
		this.code = code;
	}

	protected byte[] getDecodeData() {
		return decodeData;
	}

	/**
	 * 把输入的字节数组解码成输入writable
	 * 
	 * @throws IOException
	 */
	protected void decode() throws IOException {
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(
				decodeData));
		request = getRequestParser().read(dis);
	}

	/**
	 * 
	 * encode ok or error response
	 * 
	 * @throws IOException
	 */
	private void encode() throws IOException {
		if (code == CodeConstants.CODE_OK) {
			encodeOKResponse();
		} else {
			encodeErrorResponse();
		}
	}

	/**
	 * encode error response
	 * 
	 * @param dos
	 * @throws IOException
	 */
	private void encodeErrorResponse() throws IOException {
		encodeData = new byte[8];
		TaskHelper.addInt(encodeData, id, code);
	}

	/**
	 * encode ok response
	 * 
	 * @param dos
	 * @throws IOException
	 */
	protected void encodeOKResponse() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		dos.writeInt(id);
		dos.writeInt(code);
		if (response != null) {
			getResponseParser().write(response, dos);
		}
		encodeData = baos.toByteArray();
	}

	/**
	 * 添加写入数据，重置key信号
	 * 
	 */
	private void register() {
		TaskHelper.writeTask(key, this);
	}

	private void close() {
		encodeData = TaskHelper.emptyBytes;
		decodeData = TaskHelper.emptyBytes;
	}

}
