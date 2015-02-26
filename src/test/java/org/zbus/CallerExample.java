package org.zbus;

import java.io.IOException;

import rushmore.zbus.client.Broker;
import rushmore.zbus.client.broker.SingleBrokerConfig;
import rushmore.zbus.client.broker.SingleBroker;
import rushmore.zbus.client.service.Caller;
import rushmore.zbus.remoting.Message;
import rushmore.zbus.remoting.ticket.ResultCallback;

public class CallerExample {
	public static void main(String[] args) throws IOException{  
		//1）创建Broker代表
		SingleBrokerConfig config = new SingleBrokerConfig();
		config.setBrokerAddress("127.0.0.1:15555");
		Broker broker = new SingleBroker(config);
		
		//2) 创建生产者
		Caller caller = new Caller(broker, "MyMQ");
		
		Message msg = new Message();
		msg.setBody("hello world");
		
		caller.invokeAsync(msg, new ResultCallback() {
			@Override
			public void onCompleted(Message result) {
				System.out.println(result);
			}
		});
		
	} 
}
