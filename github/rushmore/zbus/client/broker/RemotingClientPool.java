package rushmore.zbus.client.broker;

import java.io.IOException;

import rushmore.zbus.common.pool.BasePooledObjectFactory;
import rushmore.zbus.common.pool.PooledObject;
import rushmore.zbus.common.pool.impl.DefaultPooledObject;
import rushmore.zbus.common.pool.impl.GenericObjectPool;
import rushmore.zbus.common.pool.impl.GenericObjectPoolConfig;
import rushmore.zbus.remoting.ClientDispatcherManager;
import rushmore.zbus.remoting.RemotingClient;
import rushmore.zbus.common.pool.BasePooledObjectFactory;
import rushmore.zbus.remoting.ClientDispatcherManager;

public class RemotingClientPool extends GenericObjectPool<RemotingClient>{
	public RemotingClientPool(ClientDispatcherManager clientMgr, String broker, GenericObjectPoolConfig config) throws IOException{
		super(new RemotingClientFactory(clientMgr, broker), config);
	}  
}

class RemotingClientFactory extends BasePooledObjectFactory<RemotingClient> {
	private final ClientDispatcherManager cliengMgr;
	private final String broker; 
	
	public RemotingClientFactory(final ClientDispatcherManager clientMgr, final String broker){
		this.cliengMgr = clientMgr;
		this.broker = broker;
	}
	
	@Override
	public RemotingClient create() throws Exception { 
		return new RemotingClient(broker, cliengMgr);
	}

	@Override
	public PooledObject<RemotingClient> wrap(RemotingClient obj) { 
		return new DefaultPooledObject<RemotingClient>(obj);
	} 
	
	@Override
	public void destroyObject(PooledObject<RemotingClient> p) throws Exception {
		RemotingClient client = p.getObject();
		client.close();
	}
	
	@Override
	public boolean validateObject(PooledObject<RemotingClient> p) {
		return p.getObject().hasConnected();
	}
}