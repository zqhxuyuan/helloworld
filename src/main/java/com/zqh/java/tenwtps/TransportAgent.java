/**
 * @version 2007-3-23
 * @author xalinx at gmail dot com
 */
package com.zqh.java.tenwtps;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


/**
 * Transport agent, used for client to transport request and response.
 * 
 * @version 2007-3-23
 * @author xalinx at gmail dot com
 * 
 */
public interface TransportAgent<REQP extends Parser<REQ>, RESP extends Parser<RES>, REQ extends Request, RES extends Response> {

	/**
	 * send request and receive response
	 * 
	 * @param in
	 * @param out
	 * @param request
	 * @param requestParser
	 * @param responseParser
	 * @return
	 * @throws IOException
	 * @throws RemoteException
	 */
	public RES transact(DataInputStream in, DataOutputStream out, REQ request,
			REQP requestParser, RESP responseParser) throws IOException,
			RemoteException;

	public void keepAlive(DataOutputStream out) throws IOException,
			RemoteException;

	public void exit(DataOutputStream out) throws IOException, RemoteException;

}
