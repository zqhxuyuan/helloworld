/**
 * @version 2007-3-23
 * @author xalinx at gmail dot com
 */
package com.zqh.java.tenwtps;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


/**
 * Abstract transport agent
 * 
 * @version 2007-3-23
 * @author xalinx at gmail dot com
 * 
 */
public abstract class AbstractTransportAgent<REQP extends Parser<REQ>, RESP extends Parser<RES>, REQ extends Request, RES extends Response>
		implements TransportAgent<REQP, RESP, REQ, RES> {

	public abstract RES transact(DataInputStream in, DataOutputStream out,
			REQ request, REQP requestParser, RESP responseParser)
			throws IOException, RemoteException;

	public void exit(DataOutputStream out) throws IOException, RemoteException {
		out.writeInt(-1);
	}

	public void keepAlive(DataOutputStream out) throws IOException,
			RemoteException {
		out.writeInt(0);
	}

	/**
	 * Read task.
	 * 
	 * @param in
	 * @param responseParser
	 * @param taskIdWrap
	 * @param codeWrap
	 * @return return admin response if response code is ok, else return null
	 * @throws IOException
	 */
	protected RES read(DataInputStream in, RESP responseParser,
			IntWrap taskIdWrap, IntWrap codeWrap) throws IOException {
		int taskId = in.readInt();
		if (taskIdWrap != null) {
			taskIdWrap.setValue(taskId);
		}
		int code = in.readInt();
		if (codeWrap != null) {
			codeWrap.setValue(code);
		}

		RES resp = null;
		if (code == CodeConstants.CODE_OK) { // code ok
			resp = responseParser.read(in);
		}
		return resp;
	}

	/**
	 * Write task.
	 * 
	 * @param taskId
	 * @param request
	 * @param requestParser
	 * @param out
	 * @throws IOException
	 */
	protected void write(int taskId, REQ request, REQP requestParser,
			DataOutputStream out) throws IOException {
		out.writeInt(8 + request.length());
		out.writeInt(taskId);
		requestParser.write(request, out);
		out.flush();
	}

}
