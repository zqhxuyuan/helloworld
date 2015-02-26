package rushmore.zbus.remoting.callback;

import java.io.IOException;

import rushmore.zbus.remoting.nio.Session;

 
public interface ErrorCallback { 
	public void onError(IOException e, Session sess) throws IOException;   
}
