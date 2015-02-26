package rushmore.zbus.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import rushmore.zbus.common.MessageMode;
import rushmore.zbus.common.Proto;
import rushmore.zbus.common.logging.Logger;
import rushmore.zbus.common.logging.LoggerFactory;
import rushmore.zbus.remoting.Message;
import rushmore.zbus.remoting.RemotingClient;
import rushmore.zbus.common.MessageMode;
import rushmore.zbus.common.Proto;
import rushmore.zbus.common.logging.Logger;
import rushmore.zbus.remoting.Message;


public class Consumer{    
	private static final Logger log = LoggerFactory.getLogger(Consumer.class);
	private final Broker broker;    
	private RemotingClient client;      //消费者拥有一个物理链接
	
	private final String mq;            //队列唯一性标识
	private String accessToken = "";    //访问控制码
	private String registerToken = "";  //注册认证码 
	private final int mode; 
	//为发布订阅者的主题，当Consumer的模式为发布订阅时候起作用
	private String topic = null;
	
	public Consumer(Broker broker, String mq, MessageMode... mode){
		this.broker = broker;
		this.mq = mq;  
		if(mode.length == 0){
			this.mode = MessageMode.intValue(MessageMode.MQ); 
		} else {
			this.mode = MessageMode.intValue(mode);
		} 
	} 
	
	public Consumer(MqConfig config){
		this.broker = config.getBroker();
		this.mq = config.getMq();
		this.accessToken = config.getAccessToken();
		this.registerToken = config.getRegisterToken(); 
		this.mode = config.getMode();
		this.topic = config.getTopic();
	}
	
	private ClientHint myClientHint(){
		ClientHint hint = new ClientHint();
		hint.setMq(this.mq);  
		return hint;
	}
	
	
    public Message recv(int timeout) throws IOException{
    	if(this.client == null){
	    	this.client = broker.getClient(myClientHint());
    	}
    	Message req = new Message();
    	req.setCommand(Proto.Consume);
    	req.setMq(mq);
    	req.setToken(accessToken); 
    	if(MessageMode.isEnabled(this.mode, MessageMode.PubSub)){
    		if(this.topic != null){
    			req.setTopic(this.topic);
    		}
    	}
    	
    	Message res = null;
    	try{
	    	res = client.invokeSync(req, timeout);
			if(res != null && res.isStatus404()){
				if(!this.createMQ()){
					throw new IllegalStateException("register error");
				}
				return recv(timeout);
			}
    	} catch(IOException e){
    		log.error(e.getMessage(), e);
    		try{
    			broker.closeClient(client);
    			client = broker.getClient(myClientHint());
    		} catch(IOException ex){
    			log.error(e.getMessage(), e);
    		}
    	}
    	return res;
    }
    
    
    public void reply(Message msg) throws IOException{ 
    	if(msg.getStatus() != null){
    		msg.setReplyCode(msg.getStatus());
    	}
    	msg.setCommand(Proto.Produce); 
    	msg.setAck(false);
    	client.getSession().write(msg); 
    }
    
    public boolean createMQ() throws IOException{
    	Map<String, String> params = new HashMap<String, String>();
    	params.put("mqName", mq);
    	params.put("accessToken", accessToken);
    	params.put("mqMode", "" + this.mode);
    	
    	Message req = Proto.buildSubCommandMessage(Proto.Admin, Proto.AdminCreateMQ, params);
    	req.setToken(this.registerToken);
    	
    	Message res = client.invokeSync(req);
    	if(res == null) return false;
    	return res.isStatus200();
    } 

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getRegisterToken() {
		return registerToken;
	}

	public void setRegisterToken(String registerToken) {
		this.registerToken = registerToken;
	} 	   

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		if(!MessageMode.isEnabled(this.mode, MessageMode.PubSub)){
			throw new IllegalStateException("topic support for none-PubSub mode");
		}
		this.topic = topic;
	}
}