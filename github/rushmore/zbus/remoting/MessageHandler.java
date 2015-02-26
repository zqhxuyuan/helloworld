package rushmore.zbus.remoting;

import java.io.IOException;

import rushmore.zbus.remoting.Message;
import rushmore.zbus.remoting.nio.Session;

 
public interface MessageHandler { 
	public void handleMessage(Message msg, Session sess) throws IOException;   
}
