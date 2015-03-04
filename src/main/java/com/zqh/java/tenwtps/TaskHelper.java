/**
 * @version 2007-2-9
 * @author xalinx at gmail dot com
 */
package com.zqh.java.tenwtps;

import java.nio.channels.SelectionKey;

/**
 * @version 2007-2-9
 * @author xalinx at gmail dot com
 * 
 */
public class TaskHelper {

	public static final int notifyTaskId = -1;

	public static final byte[] emptyBytes = new byte[0];

	private static final byte trueByte = (byte) 1;

	private static final byte falseByte = (byte) 0;

	private static final char[] byte2Hex = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	public static byte[] encodeTask(int id, boolean tag, byte[] content) {
		byte[] out = new byte[content.length + 1 + 4 + 4];
		byte[] ids = intToBytes(id);
		byte[] lens = intToBytes(content.length);
		System.arraycopy(ids, 0, out, 0, 4);
		out[4] = tag ? trueByte : falseByte;
		System.arraycopy(lens, 0, out, 5, 4);
		System.arraycopy(content, 0, out, 9, content.length);
		return out;
	}

	public static byte[] intToBytes(int i) {
		final byte tmp[] = new byte[4];
		addInt(tmp, i);
		return tmp;
	}

	public static void addInt(byte[] tmp, int... ids) {
		int pos = 0;
		for (int id : ids) {
			tmp[pos++] = (byte) ((0xff000000 & id) >> 24);
			tmp[pos++] = (byte) ((0xff0000 & id) >> 16);
			tmp[pos++] = (byte) ((0xff00 & id) >> 8);
			tmp[pos++] = (byte) (0xff & id);
		}
	}

	public static String bytesToHex(byte[] bs) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < bs.length; i++) {
			byte b = bs[i];
			builder.append(byte2Hex[(0xf0 & b) >> 4]);
			builder.append(byte2Hex[0xf & b]);
		}
		return builder.toString();
	}

	public static void writeTask(SelectionKey key, Task task) {
		if (key.isValid()) {
			((TaskAttach) key.attachment()).getTaskWriter().write(
					task.getEncodeData());
		}
	}

}
