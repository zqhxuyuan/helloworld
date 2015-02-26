package rushmore.zbus.remoting;

import java.io.IOException;

import rushmore.zbus.common.logging.Logger;
import rushmore.zbus.common.logging.LoggerFactory;
import rushmore.zbus.remoting.callback.ConnectedCallback;
import rushmore.zbus.remoting.callback.ErrorCallback;
import rushmore.zbus.remoting.callback.MessageCallback;
import rushmore.zbus.remoting.nio.EventAdaptor;
import rushmore.zbus.remoting.nio.Session;
import rushmore.zbus.remoting.ticket.Ticket;
import rushmore.zbus.remoting.ticket.TicketManager;
import rushmore.zbus.remoting.nio.EventAdaptor;
import rushmore.zbus.remoting.ticket.Ticket;

public class ClientEventAdaptor extends EventAdaptor {
	private static final Logger log = LoggerFactory.getLogger(ClientEventAdaptor.class); 
    private MessageCallback messageCallback;
    private ErrorCallback errorCallback;
    private ConnectedCallback connectedCallback;
    
	@Override
    public void onMessage(Object obj, Session sess) throws IOException {  
    	Message msg = (Message)obj; 
    	 
    	//先验证是否有Ticket处理
    	Ticket ticket = TicketManager.getTicket(msg.getMsgId());
    	if(ticket != null){
    		ticket.notifyResponse(msg);
    		TicketManager.removeTicket(msg.getMsgId());
    		return;
    	}  
    	
    	if(messageCallback != null){
    		this.messageCallback.onMessage(msg, sess);
    		return;
    	}
    	
    	log.warn("!!!!!!!!!!!!!!!!!!!!!!!!!!Drop,%s", msg);
    }
	
	@Override
	public void onException(Throwable e, Session sess) throws IOException {
		if(e instanceof IOException && this.errorCallback != null){
			this.errorCallback.onError((IOException)e, sess);
		} else {
			super.onException(e, sess);
		}
	}
	
	@Override
	public void onSessionConnected(Session sess) throws IOException {
		super.onSessionConnected(sess);
		log.info("Connected: "+sess);
		if(this.connectedCallback != null){
			this.connectedCallback.onConnected(sess);
		}
	}
	
	
	public void setMessageCallback(MessageCallback messageCallback) {
		this.messageCallback = messageCallback;
	}


	public void setErrorCallback(ErrorCallback errorCallback) {
		this.errorCallback = errorCallback;
	}

	public void setConnectedCallback(ConnectedCallback connectedCallback) {
		this.connectedCallback = connectedCallback;
	}   
}

