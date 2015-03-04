/**
 * @version 2007-3-15
 * @author xalinx at gmail dot com
 */
package com.zqh.java.tenwtps.admin;

/**
 * admin request factory.
 * 
 * @version 2007-3-15
 * @author xalinx at gmail dot com
 * 
 */
public class AdminRequestFactory {
	private static final AdminRequest CONN_STATUS_REQUEST = new AdminRequest() {

		{
			this.command = AdminCommandConstants.CONN_STATUS;
		}

		public void setCommand(int command) {
			throw new UnsupportedOperationException();
		}
	};

	public static final AdminRequest getConnStatusRequest() {
		return CONN_STATUS_REQUEST;
	}
}
