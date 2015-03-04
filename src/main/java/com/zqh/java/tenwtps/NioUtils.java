/**
 * @version 2007-2-6
 * @author xalinx at gmail dot com
 */
package com.zqh.java.tenwtps;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * @version 2007-2-6
 * @author xalinx at gmail dot com
 * 
 */
public class NioUtils {
	// Returns an output stream for a ByteBuffer.
	// The write() methods use the relative ByteBuffer put() methods.
	public static OutputStream newOutputStream(final ByteBuffer buf) {
		return new OutputStream() {
			public synchronized void write(int b) throws IOException {
				buf.put((byte) b);
			}

			public synchronized void write(byte[] bytes, int off, int len)
					throws IOException {
				buf.put(bytes, off, len);
			}
		};
	}

	// Returns an input stream for a ByteBuffer.
	// The read() methods use the relative ByteBuffer get() methods.
	public static InputStream newInputStream(final ByteBuffer buf) {
		return new InputStream() {
			public synchronized int read() throws IOException {
				if (!buf.hasRemaining()) {
					return -1;
				}
				return buf.get();
			}

			public synchronized int read(byte[] bytes, int off, int len)
					throws IOException {
				// Read only what's left
				len = Math.min(len, buf.remaining());
				buf.get(bytes, off, len);
				return len;
			}
		};
	}
}
