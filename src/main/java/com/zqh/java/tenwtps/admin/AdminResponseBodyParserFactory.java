/**
 * @version 2007-3-8
 * @author xalinx at gmail dot com
 */
package com.zqh.java.tenwtps.admin;


/**
 * @version 2007-3-8
 * @author xalinx at gmail dot com
 * 
 */
public class AdminResponseBodyParserFactory {

	private static final AdminResponseBodyParser<ConnectionStatusResponse> connectionStatusParser = new ConnectionStatusResponseParser();

	@SuppressWarnings("unchecked")
	public static AdminResponseBodyParser<AdminResponse> getParser(int cmd) {
		AdminResponseBodyParser response = null;
		if (cmd == AdminCommandConstants.CONN_STATUS) {
			response = connectionStatusParser;
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	public static <E extends AdminResponse> AdminResponseBodyParser<AdminResponse> getParser(
			Class<E> clz) {
		AdminResponseBodyParser response = null;
		if (clz == ConnectionStatusResponse.class) {
			response = connectionStatusParser;
		}
		return response;
	}

}
