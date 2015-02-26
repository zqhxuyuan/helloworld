package org.zbus.ha;

import java.io.IOException;

import rushmore.zbus.client.Broker;
import rushmore.zbus.client.Producer;
import rushmore.zbus.client.broker.HaBroker;
import rushmore.zbus.client.broker.HaBrokerConfig;
import rushmore.zbus.remoting.Message;
import rushmore.zbus.remoting.ticket.ResultCallback;

public class PubExample {
	public static void main(String[] args) throws IOException{  
		//1）创建Broker代表
		HaBrokerConfig config = new HaBrokerConfig();
		config.setTrackAddrList("127.0.0.1:16666:127.0.0.1:16667");
		Broker broker = new HaBroker(config);
		
		//2) 创建生产者
		Producer producer = new Producer(broker, "MyPubSub");
		
		Message msg = new Message();
		msg.setTopic("hong");
		msg.setBody("hello world");
		
		producer.send(msg, new ResultCallback() {
			@Override
			public void onCompleted(Message result) {
				System.out.println(result);
			}
		});
	} 
}
