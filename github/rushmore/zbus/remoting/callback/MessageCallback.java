package rushmore.zbus.remoting.callback;

import java.io.IOException;

import rushmore.zbus.remoting.Message;
import rushmore.zbus.remoting.nio.Session;
import rushmore.zbus.remoting.Message;
import rushmore.zbus.remoting.nio.Session;


public interface MessageCallback { 
	public void onMessage(Message msg, Session sess) throws IOException;
}
