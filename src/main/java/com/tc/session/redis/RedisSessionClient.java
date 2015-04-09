package com.tc.session.redis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tc.session.SessionClient;
import com.tc.session.TCSession;
import com.tc.session.TCSession.SessionMetaData;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

/**
 * 默认选择2号数据库
 *  select 2
 * 设入redis中的数据格式如下
 *    sessionid
 *          [sessionid,value]
 *          [key1,value]
 *          [key2,value]
 *   设置或者删除属性时,key值不能和sessionid相同
 *
 * @author liming
 * @version 1.5.4
 * @date 15-1-6 下午2:29
 */
public class RedisSessionClient implements SessionClient {

	private static final Logger log = LoggerFactory.getLogger(RedisSessionClient.class);

	/**
	 * 连接池manager *
	 */
	private JedisPoolManager manager;

	/**
	 * 单例对象
	 */
	private static SessionClient instance;

	private RedisSessionClient() {

		manager = JedisPoolManager.getInstance();
	}

	/**
	 * 获取单例
	 *
	 * @return
	 */
	public synchronized static SessionClient getInstance() {

		if (instance == null) {
			instance = new RedisSessionClient();
		}
		return instance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TCSession getSession(String sessionid) {

		if (StringUtils.isBlank(sessionid)) {
			log.error("==========> sessionid is empty");
			throw new IllegalArgumentException();
		}
		log.debug(">>>>>>>>>>> Try to get sessionid: " + sessionid);
		Jedis jedis = manager.borrowObject();
		if (jedis == null) {
			log.error("从连接池中获取连接时，发生错误");
			throw new IllegalStateException();
		}
		try {
			if (jedis.exists(sessionid)) {
				byte[] bytes = jedis.hget(sessionid.getBytes(),sessionid.getBytes());
				Pipeline pipeline = jedis.pipelined();
				Object obj = SerializationUtils.deserialize(bytes);
				if(obj instanceof SessionMetaData){
					SessionMetaData metaData = (SessionMetaData)obj;
					doUpdateMetaDataExpiredTime(pipeline, metaData);
					return new TCSession(this, metaData, false);
				}
				return null;
			} else {
				log.error("sessionid不存在或者已经过期:" + sessionid);
				return null;
			}
		} finally {
			manager.returnObject(jedis);
		}
	}
	/**
	 * 获取metaData 更新metaData 和session 过期时间
	 * @param sessionid
	 * @param jedis
	 */
	private boolean updateMetaDataExpiredTime(String sessionid, Jedis jedis) {

		byte[] bytes = jedis.hget(sessionid.getBytes(),sessionid.getBytes());
		Pipeline pipeline = jedis.pipelined();
		Object obj = SerializationUtils.deserialize(bytes);
		if(obj instanceof SessionMetaData) {
			SessionMetaData metaData = (SessionMetaData) obj;
			doUpdateMetaDataExpiredTime(pipeline, metaData);
		}
		else{
			log.error("session中metaData不存在:"+sessionid);
			return false;
		}
		return true;
	}
	/**
	 * 更新metaData 和session 过期时间
	 * @param pipeline
	 * @param metaData
	 */
	private void doUpdateMetaDataExpiredTime(Pipeline pipeline, SessionMetaData metaData) {

		metaData.setLastAccessedTime(System.currentTimeMillis());
		metaData.setVersion(metaData.getVersion() + 1);
		byte[] val = SerializationUtils.serialize(metaData);
		//更新metaData
		pipeline.hset(metaData.getId().getBytes(),metaData.getId().getBytes(),val);
		//设置session过期时间(分钟)
		pipeline.expire(metaData.getId().getBytes(), RedisConfiguration.SESSION_TIMEOUT * 60);
		pipeline.sync();
	}

	/**
	 * {@inheritDoc}
	 * 版本号加1,更新最新访问时间,更新jedis失效时间
	 */
	@Override
	public boolean updateSession(TCSession session) {

	    SessionMetaData metadata = session.getSessionMetadata();
		Jedis jedis = manager.borrowObject();
		if (jedis == null) {
			log.error("从连接池中获取连接时，发生错误");
			return false;
		}
		try {
			String sessionid = metadata.getId();
			if (jedis.exists(sessionid)) {
				doUpdateMetaDataExpiredTime(jedis.pipelined(), metadata);
			} else {
				log.error("sessionid不存在或者已经过期:" + sessionid);
				return false;
			}
		} finally {
			manager.returnObject(jedis);
		}
		return false;
	}

	/**
	 * 如果metadata不为空，建立对应的session节点。如果为空，则建立根节点
	 * hset (sessionid,sessionid,metadata)
	 */
	@Override
	public boolean createSession(TCSession session) {

	    SessionMetaData metadata = session.getSessionMetadata();
		Jedis jedis = manager.borrowObject();
		if (jedis == null) {
			log.error("从连接池中获取连接时，发生错误");
			return false;
		}
		try {
			long currentTime = System.currentTimeMillis();
			metadata.setCreationTime(currentTime);
			metadata.setLastAccessedTime(currentTime);
			metadata.setVersion(0);
			byte[] val = SerializationUtils.serialize(metadata);
			jedis.hset(metadata.getId().getBytes(),metadata.getId().getBytes(),val);
			//设置session过期时间(分钟)
			jedis.expire(metadata.getId(), RedisConfiguration.SESSION_TIMEOUT * 60);
		} finally {
			manager.returnObject(jedis);
		}
		if (log.isDebugEnabled()) {
			log.debug("创建redis session hset完成:[" + metadata.getId() + "]" );
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 jedis.expire(metadata.getId(), RedisConfiguration.SESSION_TIMEOUT * 60);
	 */
	@Override
	public boolean setAttribute(String sessionid, String key, Serializable value) {

		if(value == null){
			String s = "sessionId:"+sessionid+",key:"+key+",value is[null]";
			log.error(s);
			throw new IllegalArgumentException(s);
		}

		//不能设置和session id同样的属性,因为已经被用SessionMetaData
		if(key.equals(sessionid)){
			log.warn("不能设置和session id同样的属性,因为已经被用SessionMetaData!");
			return false;
		}

		Jedis jedis = manager.borrowObject();
		if (jedis == null) {
			log.error("从连接池中获取连接时，发生错误");
			return false;
		}
		try {
			//已经存在的情况下设值
			if (jedis.exists(sessionid)) {
				byte[] bytes = jedis.hget(sessionid.getBytes(),sessionid.getBytes());
				Pipeline pipeline = jedis.pipelined();
				Object obj = SerializationUtils.deserialize(bytes);
				if(obj instanceof SessionMetaData) {
					SessionMetaData metaData = (SessionMetaData) obj;
					//设置属性
					pipeline.hset(sessionid.getBytes(), key.getBytes(), SerializationUtils.serialize(value));
					//更新过期时间
					doUpdateMetaDataExpiredTime(pipeline, metaData);
				}
				else{
					log.error("session中metaData不存在:"+sessionid);
					return false;
				}
			}
			//不存在
			else {
				log.error("sessionid不存在或者已经过期:" + sessionid);
				return false;
			}
		} finally {
			manager.returnObject(jedis);
		}
		if (log.isDebugEnabled()) {
			log.debug("更新redis hset 数据完成[" + sessionid + ":" + key + "][" + value + "]");
		}
		return true;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getAttribute(String sessionid, String key) {
		//不能获取和session id同样的属性,因为已经被用SessionMetaData
		if(sessionid.equals(key)){
			return null;
		}
		Jedis jedis = manager.borrowObject();
		if (jedis == null) {
			log.error("从连接池中获取连接时，发生错误");
			return null;
		}
		try {
			byte[] vals = jedis.hget(sessionid.getBytes(),key.getBytes());
			if(vals == null || vals.length == 0){
				log.warn("session过期或者不存在sessionid="+sessionid+",或者session中没有该key="+key);
				return null;
			}
			//设置session过期时间(分钟)
			if (!updateMetaDataExpiredTime(sessionid, jedis)) {
				return false;
			}
			return SerializationUtils.deserialize(vals);
		} finally {
			manager.returnObject(jedis);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean removeAttribute(String sessionid, String key) {
		//不能删除和session id同样的属性,因为已经被用SessionMetaData
		if(sessionid.equals(key)){
			return false;
		}
		Jedis jedis = manager.borrowObject();
		if (jedis == null) {
			log.error("从连接池中获取连接时，发生错误");
			return false;
		}
		try {
			//没有session或没有key返回失败
			if (jedis.exists(sessionid)) {
				Long rst = jedis.hdel(sessionid.getBytes(), key.getBytes());
				if (rst > 0) {
					log.info("删除[" + sessionid + ":" + key + "]" + "属性成功");
					//设置session过期时间(分钟)
					jedis.expire(sessionid, RedisConfiguration.SESSION_TIMEOUT * 60);
					return true;
				} else {
					log.warn("删除失败,session:[" + sessionid + "]没有属性:[" + key + "]");
					//设置session过期时间(分钟)
					jedis.expire(sessionid, RedisConfiguration.SESSION_TIMEOUT * 60);
					return false;
				}
			} else {
				log.error("sessionid不存在或者已经过期:" + sessionid);
				return false;
			}
		} finally {
			manager.returnObject(jedis);
		}
	}

	/**
	 * {@inheritDoc}
	 * 不返回和session id同样的属性,因为已经被用SessionMetaData
	 */
	@Override
	public List<String> getAttributeNames(String sessionid) {
		Jedis jedis = manager.borrowObject();
		if (jedis == null) {
			log.error("从连接池中获取连接时，发生错误");
			return null;
		}
		try {
			//没有session返回null
			if (jedis.exists(sessionid)) {
				Set<String> keys = jedis.hkeys(sessionid);
				//删除和和session id同样的属性
				keys.remove(sessionid);
				//设置session过期时间(分钟)
				if (!updateMetaDataExpiredTime(sessionid, jedis)) {
					return null;
				}

				return new ArrayList<String>(keys);
			} else {
				log.error("sessionid不存在或者已经过期:" + sessionid);
				return null;
			}
		} finally {
			manager.returnObject(jedis);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> removeSession(String sessionid) {
		Jedis jedis = manager.borrowObject();
		if (jedis == null) {
			log.error("从连接池中获取连接时，发生错误");
			return null;
		}
		try {
			//没有session返回null
			if (jedis.exists(sessionid)) {
				Map<byte[], byte[]> val = jedis.hgetAll(sessionid.getBytes());
				//删除和session id同样的属性
				val.remove(sessionid.getBytes());
				Map<String, Object> rst = new HashMap<String, Object>();
				for (Map.Entry<byte[], byte[]> entry : val.entrySet()) {
					rst.put(new String(entry.getKey()), SerializationUtils.deserialize(entry.getValue()));
				}
				//删除redis中key
				jedis.del(sessionid);
				return rst;
			} else {
				log.error("sessionid不存在或者已经过期:" + sessionid);
				return null;
			}
		} finally {
			manager.returnObject(jedis);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getSessions() {

		return null;
	}
}
