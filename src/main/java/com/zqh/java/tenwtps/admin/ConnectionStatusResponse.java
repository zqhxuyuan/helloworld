/**
 * @version 2007-3-7
 * @author xalinx at gmail dot com
 */
package com.zqh.java.tenwtps.admin;

import com.zqh.java.tenwtps.ParserUtils;


/**
 * @version 2007-3-7
 * @author xalinx at gmail dot com
 * 
 */
public class ConnectionStatusResponse extends AdminResponse {

	private ConnectionStatus[] clients;

	public ConnectionStatus[] getClients() {
		return clients;
	}

	public void setClients(ConnectionStatus[] connections) {
		this.clients = connections;
	}

	private ConnectionStatus[] admins;

	public ConnectionStatus[] getAdmins() {
		return admins;
	}

	public void setAdmins(ConnectionStatus[] admins) {
		this.admins = admins;
	}

	public ConnectionStatusResponse() {
		this.command = AdminCommandConstants.CONN_STATUS;
	}

	@Override
	public int length() {
		int length = super.length();
		length += getConnsLength(clients);
		length += getConnsLength(admins);
		return length;
	}

	private int getConnsLength(ConnectionStatus[] conns) {
		int length = 0;
		// 锟斤拷锟介长锟斤拷4锟街斤拷
		length += 4;
		if (conns != null) {
			for (int i = 0; i < conns.length; i++) {
				// 锟街凤拷锟斤拷4锟街斤拷
				length += 4 + ParserUtils.stringByteLength(conns[i]
						.getAddress());
			}
		}
		return length;
	}

}
