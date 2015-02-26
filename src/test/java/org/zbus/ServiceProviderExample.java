package org.zbus;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import rushmore.zbus.client.Broker;
import rushmore.zbus.client.broker.SingleBroker;
import rushmore.zbus.client.broker.SingleBrokerConfig;
import rushmore.zbus.client.service.ServiceConfig;
import rushmore.zbus.client.service.ServiceHandler;
import rushmore.zbus.client.service.ServiceLoader;
import rushmore.zbus.client.service.ServiceProvider;
import rushmore.zbus.common.logging.Logger;
import rushmore.zbus.common.logging.LoggerFactory;
import rushmore.zbus.remoting.Message;

public class ServiceProviderExample implements ServiceProvider, ServiceHandler {
	private static final Logger log = LoggerFactory.getLogger(ServiceProviderExample.class);
	private ExecutorService executor = new ThreadPoolExecutor(4, 16, 120, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
	
	public ServiceProviderExample(){
		
	}
	
	@Override
	public ServiceConfig getConfig() { 
		ServiceConfig config = new ServiceConfig();
		config.setMq("HttpCallback"); 
		config.setServiceHandler(this);
		return config;
	}
	
	
	private void handleMessage(Message req){
		//填写业务逻辑
		
	}
	
	
	@Override
	public Message handleRequest(final Message msg) { 
		try{
			log.info(msg.toString());
			executor.submit(new Runnable() {
				@Override
				public void run() { 
					handleMessage(msg);
				}
			});
			
		} catch (Throwable e){
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
	
	/**
	 * 单独测试注册到指定的zbus，插件模式下只需要把本class所在的jar放到zbus插件可扫描的任何目录下即可
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		SingleBrokerConfig config = new SingleBrokerConfig();
		config.setBrokerAddress("127.0.0.1:15555");
		Broker broker = new SingleBroker(config);
		final ServiceLoader serviceLoader = new ServiceLoader(broker);
		
		
		ServiceProvider sp = new ServiceProviderExample(); 
		serviceLoader.loadService(sp);
	}

}
