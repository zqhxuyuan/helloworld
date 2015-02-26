package rushmore.zbus.remoting.callback;

import java.io.IOException;

import rushmore.zbus.remoting.nio.Session;

 
public interface ConnectedCallback { 
	public void onConnected(Session sess) throws IOException;   
}
