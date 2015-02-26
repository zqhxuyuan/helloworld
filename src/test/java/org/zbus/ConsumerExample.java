package org.zbus;

import java.io.IOException;

import rushmore.zbus.client.Broker;
import rushmore.zbus.client.Consumer;
import rushmore.zbus.client.MqConfig;
import rushmore.zbus.client.broker.SingleBrokerConfig;
import rushmore.zbus.client.broker.SingleBroker;
import rushmore.zbus.remoting.Message;

public class ConsumerExample {
	public static void main(String[] args) throws IOException{  
		//1）创建Broker代表
		SingleBrokerConfig brokerConfig = new SingleBrokerConfig();
		brokerConfig.setBrokerAddress("127.0.0.1:15555");
		Broker broker = new SingleBroker(brokerConfig);
		
		MqConfig config = new MqConfig(); 
		config.setBroker(broker);
		config.setMq("MyMQ");
		
		//2) 创建消费者
		Consumer c = new Consumer(config);
		while(true){
			Message msg = c.recv(10000);
			if(msg == null) continue;
			
			System.out.println(msg);
		}
	} 
}
