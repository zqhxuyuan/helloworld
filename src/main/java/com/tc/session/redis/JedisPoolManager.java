package com.tc.session.redis;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

/**
 * jedis pool 单机
 *
 * @author liming
 * @version 1.5.4
 * @date 15 -1-13 下午5:01
 */
public class JedisPoolManager {

	private static final Logger log = LoggerFactory.getLogger(JedisPoolManager.class);
	/**
	 * ip,端口间隔符
	 */
	public static final String IP_PORT_SEPARATOR = ":";
	/**
	 * 连接池
	 */
	private JedisPool pool;
	/**
	 * 单例
	 */
	protected static JedisPoolManager instance;

	/**
	 * 构造函数
	 */
	private JedisPoolManager(){
		init();
		if(pool != null){
			if (log.isInfoEnabled()) {
				log.info("jedis 连接池初始化完成");
			}
		}
		else{
			if (log.isWarnEnabled()) {
				log.warn("jedis 连接池初始化出错");
			}
		}
	}

	/**
	 * 获取连接池manager
	 *
	 * @return jedis pool manager
	 * @author liming
	 * @version 1.5.4
	 */
	public static synchronized JedisPoolManager getInstance() {

		if (instance == null) {
			instance = new JedisPoolManager();
		}
		return instance;
	}

	/**
	 * 从连接池中获取Jedis对象
	 * @return
	 */
	public Jedis borrowObject(){
		if(pool != null){
			Jedis jedis = pool.getResource();
			log.info("连接池中获取jedis对象");
			return jedis;
		}
		else{
			return null;
		}
	}

	/**
	 * 归还jedis对象
	 * @param jedis
	 */
	public void returnObject(Jedis jedis){
		if(jedis != null && pool != null){
			pool.returnResource(jedis);
		}
	}

	/**
	 * 销毁连接池
	 */
	public void destory(){
		if(pool != null){
			pool.destroy();
		}
	}

	/**
	 * 初始化连接池
	 */
	private void init() {
		//redis 连接池配置
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxIdle(NumberUtils.toInt(RedisConfiguration.MAX_IDLE));
		config.setMinIdle(NumberUtils.toInt(RedisConfiguration.MIN_IDLE));
		//config.setMaxActive(NumberUtils.toInt(RedisConfiguration.MAX_ACTIVE));
		//config.setMaxWait(NumberUtils.toLong(RedisConfiguration.MAX_WAIT) * 1000);
		config.setTestOnBorrow(StringUtils.equalsIgnoreCase("true", RedisConfiguration.TEST_ON_BORROW));
		config.setTestOnReturn(StringUtils.equalsIgnoreCase("true", RedisConfiguration.TEST_ON_RETURN));
		//config.setTestWhileIdle(true);

		//redis服务器群
		String redisServer = RedisConfiguration.SERVERS;

		String[] ipPort = StringUtils.splitByWholeSeparator(redisServer, IP_PORT_SEPARATOR);
			if (ipPort.length != 2) {
				log.error("tc.session.redis.servers 服务器配置错误,格式为(ip1:port1)");
				throw new IllegalArgumentException("redis 服务器配置错误,格式为(ip1:port1)");
			}
			//ip
			String ip = ipPort[0];
			//端口
			Integer port = null;
			try {
				port = Integer.parseInt(ipPort[1]);
			} catch (NumberFormatException nfe) {
				log.error("tc.session.redis.servers 服务器配置错误,端口必须为数字");
				throw nfe;
			}
		//连接池
		pool = new JedisPool(config,ip, port, Protocol.DEFAULT_TIMEOUT, null, RedisConfiguration.DATABASE);
	}

}
