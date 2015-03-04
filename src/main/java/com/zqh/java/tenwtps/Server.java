package com.zqh.java.tenwtps;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 10wtps server
 * 
 * @version 2011-7-4
 * @author xalinx at gmail dot com
 * 
 */
public class Server {
	private final Log log = LogFactory.getLog(this.getClass());

	/**
	 * server starting flag
	 */
	private volatile boolean starting = false;

	/**
	 * socket listener
	 */
	private SocketListener socketListener;

	public void setSocketListener(SocketListener socketListener) {
		this.socketListener = socketListener;
	}

	public boolean isOpen() {
		return this.starting;
	}

	/**
	 * server name
	 */
	private String name;

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public Server() {
	}

	/**
	 * Construct init by socket listener
	 * 
	 * @param socketListener
	 */
	public Server(SocketListener socketListener) {
		this.socketListener = socketListener;
	}

	/**
	 * Starting server
	 * 
	 * @throws IOException
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public synchronized void doStart() {
		// check if server already starting
		if (true == starting) {
			log.info(getName() + " already starting.");
			return;
		}

		// starting server
		log.info(getName() + " starting ...");

		// start socket listener
		Thread listener = new Thread(socketListener);
		listener.setDaemon(true);
		listener.start();
		while (!socketListener.isStarted()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				log.error("", e);
			}
		}

		// flag starting finished
		starting = true;

		log.info(getName() + " started.");
	}

	/**
	 * stop server
	 * 
	 * @throws InterruptedException
	 * 
	 */
	public synchronized void doStop() {
		log.info(getName() + " stopping ...");

		// stop socket listener
		socketListener.doStop();

		// flag stop finished
		starting = false;

		log.info(getName() + " stopped.");
	}

	// =========== error process ==================

}
