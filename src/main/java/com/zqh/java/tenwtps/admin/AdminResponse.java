/**
 * @version 2007-2-25
 * @author xalinx at gmail dot com
 */
package com.zqh.java.tenwtps.admin;

import com.zqh.java.tenwtps.Response;


/**
 * 锟斤拷锟斤拷锟斤拷应锟斤拷锟斤拷
 * 
 * @version 2007-2-25
 * @author xalinx at gmail dot com
 * 
 */
public class AdminResponse implements Response {

	/**
	 * 锟斤拷应指锟斤拷
	 */
	protected int command;

	public int getCommand() {
		return command;
	}

	public void setCommand(int command) {
		this.command = command;
	}

	/**
	 * 锟斤拷锟斤拷枚锟斤拷锟斤拷锟揭拷锟斤拷纸诔锟斤拷锟?
	 * 
	 * @see com.zqh.java.tenwtps.Request#length()
	 */
	public int length() {
		// 4为command占锟斤拷锟街斤拷锟斤拷
		return 4;
	}
}
