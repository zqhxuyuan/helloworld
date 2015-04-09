package com.tc.session.test;

import com.tc.session.RequestCache;
import com.tc.session.SessionClient;
import com.tc.session.TCSession;
import com.tc.session.redis.JedisPoolManager;
import com.tc.session.redis.RedisConfiguration;
import com.tc.session.redis.RedisSessionClient;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 去除注释,在resources中增加session.properties,内容如下
 *
 *   tc.session.redis.servers=127.0.0.1:6379
	 tc.session.redis.max_idle=8
	 tc.session.redis.min_idle=1
	 tc.session.redis.max_active=8
	 tc.session.redis.max_wait=2
	 tc.session.redis.test_on_borrow=true
	 tc.session.redis.test_on_return=true
	 tc.session.redis.session_timeout=10
        tc.session.redis.database=1
 *
 * @author liming
 * @version 1.5.4
 * @date 15-1-7 下午2:18
 */
public class RedisSessionClientTest {


	private static SessionClient sessionClient;

	private String sessionId = "sessionTest1";

	@BeforeClass
	public static void init() {
		sessionClient = RedisSessionClient.getInstance();
		RequestCache.begin();
	}

	@AfterClass
	public static void destory(){
		if(sessionClient != null){
			JedisPoolManager.getInstance().destory();
			sessionClient = null;
		}
	}

	@Test
	public void testCreateSession(){
		TCSession session = new TCSession(sessionClient, sessionId);
		int sessionTimeout = RedisConfiguration.SESSION_TIMEOUT.intValue();
		session.setMaxInactiveInterval(sessionTimeout * 60 * 1000);
		Assert.assertTrue(sessionClient.createSession(session));
	}

	@Test
	public void testGetSessionAndUpdateSession(){
		TCSession session = sessionClient.getSession(sessionId);
		Assert.assertNotNull(session);
		Assert.assertEquals(session.getId(), sessionId);
		Assert.assertTrue(session.getCreationTime() < System.currentTimeMillis());
		Assert.assertEquals(session.getSessionMetadata().getVersion(),1);
		System.out.println(session);

		sessionClient.updateSession(session);

		TCSession sessionAfter = sessionClient.getSession(sessionId);
		Assert.assertNotNull(sessionAfter);
		Assert.assertEquals(sessionAfter.getId(), sessionId);
		Assert.assertTrue(sessionAfter.getCreationTime() < System.currentTimeMillis());
		Assert.assertEquals(sessionAfter.getSessionMetadata().getVersion(),3);
		System.out.println(sessionAfter);

	}

	@Test
	public void testGetSetAndRemoveAttribute(){
		boolean rst = sessionClient.setAttribute(sessionId,"id",132);
		Assert.assertTrue(rst);

		Assert.assertEquals(132, sessionClient.getAttribute(sessionId, "id"));

		Assert.assertNull(sessionClient.getAttribute(sessionId + "2", "id"));

		Assert.assertTrue(sessionClient.removeAttribute(sessionId,"id"));
		Assert.assertTrue(!sessionClient.removeAttribute(sessionId, "id"));
		Assert.assertTrue(!sessionClient.removeAttribute(sessionId+"2","id"));
	}

	@Test
	public void testGetAttibutes(){
		Assert.assertTrue(sessionClient.setAttribute(sessionId, "id", 1));
		Assert.assertTrue(sessionClient.setAttribute(sessionId, "name", "liming"));

		List<String> keys = new ArrayList<String>();
		keys.add("id");
		keys.add("name");

		List<String> sessionkeys  = sessionClient.getAttributeNames(sessionId);
		System.out.println(sessionkeys);
		Assert.assertEquals(keys,sessionkeys);
	}

	@Test
	public void testRemove(){

		Map<String,Object> rst = sessionClient.removeSession(sessionId);
		System.out.println(rst);
		Assert.assertNotNull(rst);
		Assert.assertEquals(rst.get("id"),1);
		Assert.assertEquals(rst.get("name"),"liming");

		Assert.assertNull(sessionClient.getSession(sessionId));
	}
}
