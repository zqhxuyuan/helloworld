package com.zqh.paas.cache;

import java.text.SimpleDateFormat;
import java.util.*;

import com.zqh.paas.PaasContextHolder;
import com.zqh.paas.cache.ICache;

public class CacheUtil {

	private static ICache cacheSv = null;

	private CacheUtil() {}

	private static ICache getIntance() {
		if (null != cacheSv)
			return cacheSv;
		return  (ICache) PaasContextHolder.getContext().getBean("cacheSv");
	}

	public static void addItem(String key, Object object) {
		getIntance().addItem(key, object);
	}
	public static void addItem(String key, Object object, int seconds) {
		getIntance().addItem(key, object, seconds);
	}
	public static Object getItem(String key) {
		return getIntance().getItem(key);
	}
	public static void delItem(String key) {
		getIntance().delItem(key);
	}
	public static long getIncrement(String key) {
		return getIntance().getIncrement(key);
	}
	public static void addMap(String key, Map<String, String> map) {
		getIntance().addMap(key, map);
	}
	public static Map<String, String> getMap(String key) {
		return getIntance().getMap(key);
	}
	public static String getMapItem(String key,String field) {
		return getIntance().getMapItem(key, field);
	}
	public static void addMapItem(String key,String field,String value) {
		getIntance().addMapItem(key, field,value);
	}
	public static void delMapItem(String key,String field) {
		getIntance().delMapItem(key, field);
	}
	public static void addSet(String key, Set<String> set) {
		getIntance().addSet(key, set);
	}
	public static Set<String> getSet(String key) {
		return getIntance().getSet(key);
	}
	public static void addList(String key, List<String> list) {
		getIntance().addList(key, list);
	}
	public static List<String> getList(String key) {
		return getIntance().getList(key);
	}
	public static void addItemFile(String key, byte[] file) {
		getIntance().addItemFile(key, file);
	}


	public static void main(String[] args) throws Exception{
		// add then get value
        addItem("country", "weme");
		addItem("sex", "8");
		addItem("country2", "weme2",4);
		System.out.println(getItem("country"));
		System.out.println(getItem("sex"));
		System.out.println(getItem("country2"));

        // expire
        Thread.sleep(1000);
        System.out.println(getItem("country"));
        System.out.println(getItem("sex"));
        System.out.println(getItem("countr2"));

        // delete then get empty
        delItem("country");
		delItem("sex");
		System.out.println(getItem("country"));
		System.out.println(getItem("sex"));

		Map<String,String> map1 = new HashMap<String,String>();
		map1.put("1", "a");
		map1.put("2", "b");
		map1.put("3", "c");
		map1.put("4", "d");
		Map<String,String> map2 = new LinkedHashMap<String,String>();
		map2.put("1", "a");
		map2.put("2", "b");
		map2.put("3", "c");
		map2.put("4", "d");
		for(String key:map2.keySet()){
			System.out.println(key+"----"+map2.get(key));
		}
		addMap("map1-liwx", map1);
		addMap("map2-liwx", map2);
		System.out.println("map1:"+getMap("map1-liwx"));
		System.out.println("map2:"+getMap("map2-liwx"));
		System.out.println("map2 item3:"+getMapItem("map2-liwx","3"));
		
		addItem("map2-liwx-s", map2);
		System.out.println(getItem("map2-liwx-s"));
		//System.out.println("map2-s item3"+getMapItem("map2-liwx-s","3"));
		
		addMapItem("map1-liwx", "5","e");
		System.out.println("map1:"+getMap("map1-liwx"));
		delMapItem("map1-liwx", "1");
		System.out.println("map1:"+getMap("map1-liwx"));

		
		Set<String> set = new HashSet<String>();
		set.add("aa");
		set.add("bb");
		set.add("cc");
		set.add("dd");
		addSet("set-liwx", set);
		System.out.println("set:"+getSet("set-liwx"));
		Set<String> tree = new TreeSet<String>();
		tree.add("aa");
		tree.add("bb");
		tree.add("cc");
		tree.add("dd");
		addSet("tree-liwx", tree);
		System.out.println("treeset:"+getSet("tree-liwx"));
		
		List<String> list = new ArrayList<String>();
		list.add("aaaa");
		list.add("bbbb");
		list.add("cccc");
		list.add("dddd");
		delItem("list-liwx");
		addList("list-liwx", list);
		System.out.println("list:"+getList("list-liwx"));
	}

}
