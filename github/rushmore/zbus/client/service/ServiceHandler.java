package rushmore.zbus.client.service;

import rushmore.zbus.remoting.Message;
import rushmore.zbus.remoting.Message;

public interface ServiceHandler { 
	public Message handleRequest(Message request);
}
