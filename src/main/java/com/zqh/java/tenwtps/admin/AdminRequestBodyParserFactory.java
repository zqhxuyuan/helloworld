/**
 * @version 2007-3-9
 * @author xalinx at gmail dot com
 */
package com.zqh.java.tenwtps.admin;


/**
 * @version 2007-3-9
 * @author xalinx at gmail dot com
 * 
 */
public class AdminRequestBodyParserFactory {
	public static AdminRequestBodyParser<AdminRequest> getParser(int cmd) {
		return null;
	}

	public static <E extends AdminRequest> AdminRequestBodyParser<AdminRequest> getParser(
			Class<E> clz) {
		return null;
	}
}
