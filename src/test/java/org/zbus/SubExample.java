package org.zbus;

import java.io.IOException;

import rushmore.zbus.client.Broker;
import rushmore.zbus.client.Consumer;
import rushmore.zbus.client.broker.SingleBroker;
import rushmore.zbus.client.broker.SingleBrokerConfig;
import rushmore.zbus.common.MessageMode;
import rushmore.zbus.remoting.Message;

public class SubExample {
	public static void main(String[] args) throws IOException{  
		//1）创建Broker代表
		SingleBrokerConfig config = new SingleBrokerConfig();
		config.setBrokerAddress("127.0.0.1:15555");
		
		Broker broker = new SingleBroker(config);
		
		//2) 创建消费者
		Consumer c = new Consumer(broker, "MyPubSub", MessageMode.PubSub); 
		c.setTopic("hong");
		while(true){
			Message msg = c.recv(10000);
			if(msg == null) continue;
			
			System.out.println(msg);
		}
	} 
}
