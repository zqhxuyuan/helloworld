package rushmore.zbus.client;

import java.io.IOException;

import rushmore.zbus.remoting.Message;
import rushmore.zbus.remoting.RemotingClient;
import rushmore.zbus.remoting.ticket.ResultCallback;
import rushmore.zbus.remoting.Message;
import rushmore.zbus.remoting.ticket.ResultCallback;


public interface Broker {
	RemotingClient getClient(ClientHint hint) throws IOException;
	void closeClient(RemotingClient client) throws IOException;

	void invokeAsync(Message msg, final ResultCallback callback) throws IOException;
	Message invokeSync(Message req, int timeout) throws IOException;  
	void destroy();
}
