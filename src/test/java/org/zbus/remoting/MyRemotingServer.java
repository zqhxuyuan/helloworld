package org.zbus.remoting;

import java.io.IOException;

import rushmore.zbus.common.json.JSON;
import rushmore.zbus.remoting.Message;
import rushmore.zbus.remoting.MessageHandler;
import rushmore.zbus.remoting.RemotingServer;
import rushmore.zbus.remoting.nio.Session;

public class MyRemotingServer extends RemotingServer {
	
	@Override
	public String findHandlerKey(Message msg) {
		String cmd = msg.getCommand();
		if(cmd == null){
			cmd = msg.getPath();
		}
		return cmd;
	}
	
	
	public MyRemotingServer(int serverPort) {
		super(serverPort); 
		
		this.registerHandler("hello", new MessageHandler() {
			@Override
			public void handleMessage(Message msg, Session sess) throws IOException {
				System.out.println(msg);
				msg.setStatus("200"); 
				msg.setJsonBody(JSON.toJSONString(msg));
				sess.write(msg);
			}
		});
	}

	public static void main(String[] args) throws Exception { 
		
		MyRemotingServer server = new MyRemotingServer(15555);
		server.start();
	}

}
