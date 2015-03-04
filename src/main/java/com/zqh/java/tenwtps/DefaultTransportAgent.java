/**
 * @version 2007-2-26
 * @author xalinx at gmail dot com
 */
package com.zqh.java.tenwtps;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Default transport agent, used by admin client.
 * 
 * @version 2007-2-26
 * @author xalinx at gmail dot com
 * 
 */
public class DefaultTransportAgent<REQP extends Parser<REQ>, RESP extends Parser<RES>, REQ extends Request, RES extends Response>
		extends AbstractTransportAgent<REQP, RESP, REQ, RES> {
	/**
	 * task id creator
	 */
	private final AtomicInteger taskId = new AtomicInteger();

	/**
	 * get currrent task id
	 * 
	 * @return
	 */
	public int getCurrentTaskId() {
		return taskId.get();
	}

	/**
	 * @throws RemoteException
	 *             when send and receve id not equals
	 * @see AbstractTransportAgent#transact(java.io.DataInputStream,
	 *      java.io.DataOutputStream, com.taobao.remote.AdminRequest,
	 *      com.taobao.remote.AdminRequestParserProxy,
	 *      com.taobao.remote.AdminResponseParserProxy)
	 */
	public RES transact(DataInputStream in, DataOutputStream out, REQ request,
			REQP requestParser, RESP responseParser) throws IOException,
			RemoteException {
		final int sendId = taskId.incrementAndGet();
		write(sendId, request, requestParser, out);
		final IntWrap codeWrap = new IntWrap();
		final IntWrap receiveTaskIdWrap = new IntWrap();
		RES response = read(in, responseParser, receiveTaskIdWrap, codeWrap);
		// send and receve id must equals
		if (sendId != receiveTaskIdWrap.getValue()) {
			throw new RemoteException("receve id = "
					+ receiveTaskIdWrap.getValue() + " but send id = " + sendId);
		}
		if (response == null) {
			throw new RemoteException("response code:" + codeWrap.getValue());
		}
		return response;
	}

}
