package com.tc.session.redis;

import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author liming
 * @version 1.5.4
 * @date 15-1-4 下午4:48
 */
public class RedisConfiguration {

	private static final Logger log = LoggerFactory.getLogger(RedisConfiguration.class);

	private static final String config_file = "session.properties";

	/** redis服务器 */
	public static String SERVERS;

	/** 初始化连接数
	 * the number of "sleeping" instances in the pool
	 * */
	public static String MAX_IDLE;

	/** 连接池中，最小连接数 */
	public static String MIN_IDLE;

	/**
	 * 最大连接数,不能超过初始化连接数
	 */
	public static String MAX_ACTIVE;

	/**
	 * 和服务器建立的连接超时时间,单位秒
	 */
	public static String MAX_WAIT;
	/**
	 * test on borrow
	 */
	public static String TEST_ON_BORROW;
	/**
	 * TEST ON RETURN
	 */
	public static String TEST_ON_RETURN;

	/** session的生命周期 单位分钟 */
	public static Integer SESSION_TIMEOUT;

	/** 数据库号 **/
	public static Integer DATABASE;

	static {
		InputStream in = RedisConfiguration.class.getClassLoader().getResourceAsStream(config_file);
		Properties props = new Properties();
		try {
			if (in != null) {
				props.load(in);
			}
			SERVERS = props.getProperty("tc.session.redis.servers");
			MAX_IDLE = props.getProperty("tc.session.redis.max_idle");
			MIN_IDLE = props.getProperty("tc.session.redis.min_idle");
			MAX_ACTIVE = props.getProperty("tc.session.redis.max_active");
			MAX_WAIT = props.getProperty("tc.session.redis.max_wait");
			TEST_ON_BORROW = props.getProperty("tc.session.redis.test_on_borrow");
			TEST_ON_RETURN = props.getProperty("tc.session.redis.test_on_return");
			SESSION_TIMEOUT = NumberUtils.toInt(props.getProperty("tc.session.redis.session_timeout"));
			DATABASE = NumberUtils.toInt(props.getProperty("tc.session.redis.database"));
		} catch (Exception e) {
			log.error("读取session配置文件时出错", e);
		}
	}
}
